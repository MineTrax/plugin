package com.xinecraft.minetrax.bukkit.webquery;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.xinecraft.minetrax.bukkit.MinetraxBukkit;
import com.xinecraft.minetrax.bukkit.utils.SkinUtil;
import com.xinecraft.minetrax.common.data.PlayerData;
import com.xinecraft.minetrax.common.interfaces.webquery.CommonWebQuery;
import com.xinecraft.minetrax.common.utils.LoggingUtil;
import net.skinsrestorer.api.PropertyUtils;
import net.skinsrestorer.api.SkinsRestorer;
import net.skinsrestorer.api.exception.DataRequestException;
import net.skinsrestorer.api.exception.MineSkinException;
import net.skinsrestorer.api.property.SkinProperty;
import net.skinsrestorer.api.storage.PlayerStorage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class BukkitWebQuery implements CommonWebQuery {
    private final MinetraxBukkit plugin;

    public BukkitWebQuery(MinetraxBukkit plugin) {
        this.plugin = plugin;
    }

    @Override
    public String handleStatus() {
        JsonArray jsonArray = new JsonArray();
        // Get list of online players
        List<Player> playerList = new ArrayList<>(Bukkit.getOnlinePlayers());
        for (Player player : playerList) {
            JsonObject playerJsonObject = new JsonObject();
            playerJsonObject.addProperty("username", player.getName());
            playerJsonObject.addProperty("display_name", player.getDisplayName());
            playerJsonObject.addProperty("id", player.getUniqueId().toString());
            playerJsonObject.addProperty("is_op", player.isOp());
            int playerPing;
            try {
                playerPing = player.getPing();
            } catch (NoSuchMethodError e) {
                playerPing = 0;
            }
            playerJsonObject.addProperty("ping", playerPing);
            playerJsonObject.addProperty("ip_address", Objects.requireNonNull(player.getAddress()).getHostString());

            // If SkinRestorer is enabled, then add skin data
            if (this.plugin.getHasSkinRestorer()) {
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
        response.addProperty("max_players", Bukkit.getMaxPlayers());
        response.add("players", jsonArray);

        return this.plugin.gson.toJson(response);
    }

    @Override
    public String handleUserSay(String user, String message) {
        String messageFormat = this.plugin.getWebMessageFormat();
        messageFormat = messageFormat.replace("{USERNAME}", user);
        messageFormat = ChatColor.translateAlternateColorCodes('&', messageFormat);
        messageFormat = messageFormat.replace("{MESSAGE}", message);
        Bukkit.getServer().broadcastMessage(messageFormat);
        return "ok";
    }

    @Override
    public String handleBroadcast(String message) {
        this.plugin.getServer().broadcastMessage(ChatColor.translateAlternateColorCodes('&', message));
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

        Bukkit.getScheduler().callSyncMethod(this.plugin, () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command)).get();
        return "ok";
    }

    @Override
    public String handleSetPlayerSkin(String playerUuid, String commandType, String value) throws MineSkinException, DataRequestException {
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
    public String handleAccountLinkSuccess(String playerUuid, String userId) {
        PlayerData playerData = this.plugin.playersDataMap.get(playerUuid);
        if (playerData != null) {
            playerData.is_verified = true;
        }
        return "ok";
    }
}
