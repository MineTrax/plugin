package com.xinecraft.minetrax.bungee.webquery;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.xinecraft.minetrax.bungee.MinetraxBungee;
import com.xinecraft.minetrax.bungee.utils.SkinUtil;
import com.xinecraft.minetrax.common.interfaces.webquery.CommonWebQuery;
import com.xinecraft.minetrax.common.utils.LoggingUtil;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.skinsrestorer.api.PropertyUtils;
import net.skinsrestorer.api.SkinsRestorer;
import net.skinsrestorer.api.property.SkinProperty;
import net.skinsrestorer.api.storage.PlayerStorage;

import java.util.*;

public class BungeeWebQuery implements CommonWebQuery {

    private final MinetraxBungee plugin;

    public BungeeWebQuery(MinetraxBungee plugin) {
        this.plugin = plugin;
    }

    @Override
    public String handleStatus() throws Exception {
        JsonArray jsonArray = new JsonArray();
        ProxyServer proxyServer = this.plugin.getProxy();

        Collection<ProxiedPlayer> playerList = proxyServer.getPlayers();
        for (ProxiedPlayer player : playerList) {
            JsonObject playerJsonObject = new JsonObject();
            playerJsonObject.addProperty("username", player.getName());
            playerJsonObject.addProperty("display_name", player.getDisplayName());
            playerJsonObject.addProperty("id", player.getUniqueId().toString());
            playerJsonObject.addProperty("is_op", false);
            int playerPing;
            try {
                playerPing = player.getPing();
            } catch (NoSuchMethodError e) {
                playerPing = 0;
            }
            playerJsonObject.addProperty("ping", playerPing);
            playerJsonObject.addProperty("ip_address", Objects.requireNonNull(player.getAddress()).getHostString());

            if (this.plugin.getHasSkinsRestorer()) {
                SkinsRestorer skinsRestorerApi = this.plugin.getSkinsRestorerApi();
                PlayerStorage playerStorage = skinsRestorerApi.getPlayerStorage();
                try {
                    Optional<SkinProperty> skin = playerStorage.getSkinForPlayer(player.getUniqueId(), player.getName());
                    skin.ifPresent(skinProperty -> playerJsonObject.addProperty("skin_texture_id", PropertyUtils.getSkinTextureUrlStripped(skinProperty)));
                } catch (Exception e) {
                    LoggingUtil.info("[WebQuery -> status] Error getting skin for player: " + player.getName());
                }
            }

            jsonArray.add(playerJsonObject);
        }

        // Add total online players count
        JsonObject response = new JsonObject();
        response.addProperty("online_players", playerList.size());
        response.addProperty("max_players", proxyServer.getConfig().getPlayerLimit());
        response.add("players", jsonArray);

        return this.plugin.gson.toJson(response);
    }

    @Override
    public String handleUserSay(String user, String message) throws Exception {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public String handleBroadcast(String message) throws Exception {
        BaseComponent[] messageComponent = TextComponent.fromLegacyText(message);
        this.plugin.getProxy().broadcast(messageComponent);
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

        this.plugin.getProxy().getPluginManager().dispatchCommand(this.plugin.getProxy().getConsole(), command);
        return "ok";
    }

    @Override
    public String handleSetPlayerSkin(String playerUuid, String commandType, String value) throws Exception {
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
