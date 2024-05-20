package com.xinecraft.minetrax.velocity.webquery;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.xinecraft.minetrax.common.interfaces.webquery.CommonWebQuery;
import com.xinecraft.minetrax.common.utils.LoggingUtil;
import com.xinecraft.minetrax.velocity.MinetraxVelocity;
import com.xinecraft.minetrax.velocity.utils.SkinUtil;
import net.kyori.adventure.text.Component;
import net.skinsrestorer.api.PropertyUtils;
import net.skinsrestorer.api.SkinsRestorer;
import net.skinsrestorer.api.property.SkinProperty;
import net.skinsrestorer.api.storage.PlayerStorage;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class VelocityWebQuery implements CommonWebQuery {
    private final MinetraxVelocity plugin;

    public VelocityWebQuery(MinetraxVelocity plugin) {
        this.plugin = plugin;
    }

    @Override
    public String handleStatus() throws Exception {
        JsonArray jsonArray = new JsonArray();
        ProxyServer proxyServer = this.plugin.getProxyServer();

        Collection<Player> playerList = proxyServer.getAllPlayers();
        for (Player player : playerList) {
            JsonObject playerJsonObject = new JsonObject();
            playerJsonObject.addProperty("username", player.getUsername());
            playerJsonObject.addProperty("display_name", player.getUsername());
            playerJsonObject.addProperty("id", player.getUniqueId().toString());
            playerJsonObject.addProperty("is_op", false);
            int playerPing;
            try {
                playerPing = (int) player.getPing();
            } catch (NoSuchMethodError e) {
                playerPing = 0;
            }
            playerJsonObject.addProperty("ping", playerPing);
            playerJsonObject.addProperty("ip_address", Objects.requireNonNull(player.getRemoteAddress()).getHostString());

            if (this.plugin.getHasSkinsRestorer()) {
                SkinsRestorer skinsRestorerApi = this.plugin.getSkinsRestorerApi();
                PlayerStorage playerStorage = skinsRestorerApi.getPlayerStorage();
                try {
                    Optional<SkinProperty> skin = playerStorage.getSkinForPlayer(player.getUniqueId(), player.getUsername());
                    skin.ifPresent(skinProperty -> playerJsonObject.addProperty("skin_texture_id", PropertyUtils.getSkinTextureUrlStripped(skinProperty)));
                } catch (Exception e) {
                    LoggingUtil.info("[WebQuery -> status] Error getting skin for player: " + player.getUsername());
                }
            }

            jsonArray.add(playerJsonObject);
        }

        // Add total online players count
        JsonObject response = new JsonObject();
        response.addProperty("online_players", playerList.size());
        response.addProperty("max_players", proxyServer.getConfiguration().getShowMaxPlayers());
        response.add("players", jsonArray);

        return this.plugin.getGson().toJson(response);
    }

    @Override
    public String handleUserSay(String user, String message) throws Exception {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public String handleBroadcast(String message) throws Exception {
        Component messageComponent = Component.text(message);
        this.plugin.getProxyServer().getAllPlayers().forEach(player -> player.sendMessage(messageComponent));
        return "ok";
    }

    @Override
    public String handleCommand(String command) throws Exception {
        boolean isAllowOnlyWhitelistedCommandsFromWeb = this.plugin.getIsAllowOnlyWhitelistedCommandsFromWeb();
        List<String> whitelistedCommands = this.plugin.getWhitelistedCommandsFromWeb();
        boolean isWhitelisted = true;
        if (isAllowOnlyWhitelistedCommandsFromWeb) {
            // Check if params start with any of the whitelisted commands
            isWhitelisted = false;
            for (String whitelistedCmd : whitelistedCommands) {
                if (command.startsWith(whitelistedCmd)) {
                    isWhitelisted = true;
                    break;
                }
            }
        }

        if (!isWhitelisted) {
            throw new Exception("Command: " + command + " is not whitelisted.");
        }

        this.plugin.getProxyServer().getCommandManager().executeAsync(this.plugin.getProxyServer().getConsoleCommandSource(), command);
        return "ok";
    }

    @Override
    public String handleSetPlayerSkin(String playerUuid, String commandType, String value) throws Exception {
        // Ignore if not has skins restorer
        if (!this.plugin.getHasSkinsRestorer()) {
            return "ok";
        }

        switch (commandType) {
            case "url":
            case "username":
                SkinUtil.setPlayerSkinUsingUrlOrName(playerUuid, value);
                break;
            case "upload":
                SkinUtil.setPlayerSkinUsingCustom(playerUuid, value);
                break;
            case "clear":
                SkinUtil.clearPlayerSkin(playerUuid);
                break;
            default:
                break;
        }

        return "ok";
    }

    @Override
    public String handleAccountLinkSuccess(String playerUuid, String userId) throws Exception {
        throw new UnsupportedOperationException("Not supported.");
    }
}
