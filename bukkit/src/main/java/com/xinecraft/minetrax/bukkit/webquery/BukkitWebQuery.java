package com.xinecraft.minetrax.bukkit.webquery;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.xinecraft.minetrax.bukkit.MinetraxBukkit;
import com.xinecraft.minetrax.bukkit.utils.PlayerUtil;
import com.xinecraft.minetrax.bukkit.utils.SkinUtil;
import com.xinecraft.minetrax.common.data.PlayerData;
import com.xinecraft.minetrax.common.enums.BanWardenPunishmentType;
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

import java.util.*;

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
        int playerCount = 0;
        for (Player player : playerList) {
            // ignore if player is vanished
            if (PlayerUtil.isVanished(player)) {
                continue;
            }

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
            if (this.plugin.getHasSkinsRestorer()) {
                String skinTextureId = SkinUtil.getSkinTextureId(player.getUniqueId(), player.getName());
                if (skinTextureId != null) {
                    playerJsonObject.addProperty("skin_texture_id", skinTextureId);
                }
            }

            jsonArray.add(playerJsonObject);
            playerCount++;
        }

        // Add total online players count
        JsonObject response = new JsonObject();
        response.addProperty("online_players", playerCount);
        response.addProperty("max_players", Bukkit.getMaxPlayers());
        response.add("players", jsonArray);

        return this.plugin.gson.toJson(response);
    }

    @Override
    public String handlePing() throws Exception {
        JsonObject response = new JsonObject();
        response.addProperty("online_players", Bukkit.getOnlinePlayers().size());
        response.addProperty("max_players", Bukkit.getMaxPlayers());

        return this.plugin.gson.toJson(response);
    }

    @Override
    public String handleUserSay(String user, String message) {
        String messageFormat = this.plugin.getWebMessageFormat();
        messageFormat = messageFormat.replace("{USERNAME}", user);
        messageFormat = ChatColor.translateAlternateColorCodes('&', messageFormat);
        messageFormat = messageFormat.replace("{MESSAGE}", message);
        Bukkit.getServer().broadcastMessage(messageFormat);
        return "true";
    }

    @Override
    public String handleBroadcast(String message) {
        this.plugin.getServer().broadcastMessage(ChatColor.translateAlternateColorCodes('&', message));
        return "true";
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
        return "true";
    }

    @Override
    public String handleSetPlayerSkin(String playerUuid, String commandType, String value) throws MineSkinException, DataRequestException {
        // Ignore if not has skins restorer or skins restorer in proxy mode
        if (!this.plugin.getHasSkinsRestorer() || this.plugin.getHasSkinsRestorerInProxyMode()) {
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

        return "true";
    }

    @Override
    public String handleAccountLinkSuccess(String playerUuid, String userId) {
        PlayerData playerData = this.plugin.playersDataMap.get(playerUuid);
        if (playerData != null) {
            playerData.is_verified = true;
        }
        return "true";
    }

    @Override
    public String handleCheckPlayerOnline(String playerUuid) throws Exception {
        Player player = Bukkit.getPlayer(UUID.fromString(playerUuid));
        if (player != null && player.isOnline()) {
            return "true";
        } else {
            return "false";
        }
    }

    @Override
    public String handleBanwardenPardon(BanWardenPunishmentType type, String victim, String reason, String admin) throws Exception {
        boolean status = this.plugin.getCommon().getBanWarden().pardon(type, victim, reason, admin);
        return status ? "true" : "false";
    }
}
