package com.xinecraft.minetrax.utils;

import com.google.gson.Gson;
import com.xinecraft.minetrax.Minetrax;
import com.xinecraft.minetrax.data.PlayerSessionIntelData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Date;
import java.util.UUID;

public class PlayerIntelUtil {
    public static Gson gson = Minetrax.getPlugin().getGson();

    public static void reportPlayerIntel(PlayerSessionIntelData playerSessionData, boolean forceSessionToEnd) {
        Player onlinePlayer = Bukkit.getPlayer(UUID.fromString(playerSessionData.uuid));

        // If online then report and reset the xmin keys
        if (onlinePlayer != null && onlinePlayer.isOnline()) {
            int playerPing;
            try {
                playerPing = onlinePlayer.getPing();
            } catch (NoSuchMethodError e) {
                playerPing = 0;
            }
            playerSessionData.player_ping = playerPing;
            playerSessionData.world_location = gson.toJson(onlinePlayer.getLocation().serialize());
            playerSessionData.world_name = onlinePlayer.getWorld().getName();

            // Player Inventory
            if (Minetrax.getPlugin().getIsSendInventoryDataToPlayerIntel()) {
                playerSessionData.inventory = gson.toJson(onlinePlayer.getInventory().getContents());
                playerSessionData.ender_chest = gson.toJson(onlinePlayer.getEnderChest().getContents());
            }

            // Get Vault Plugin Data.
            playerSessionData.vault_balance = Minetrax.getVaultEconomy() != null ? Minetrax.getVaultEconomy().getBalance(onlinePlayer) : 0;
            if (Minetrax.getVaultPermission() != null && Minetrax.getVaultPermission().hasGroupSupport()) {
                playerSessionData.vault_groups = Minetrax.getVaultPermission().getPlayerGroups(onlinePlayer);
            }

            if (forceSessionToEnd) {
                reportAndRemoveSessionFromDataMap(playerSessionData);
            } else {
                reportAndResetXminData(playerSessionData);
            }
        }
        // If not online then report and delete key from session variable ending the session.
        else {
            // This should happen rarely as we already remove when player quit.
            reportAndRemoveSessionFromDataMap(playerSessionData);
        }
    }

    private static void reportAndResetXminData(PlayerSessionIntelData playerSession) {
        String playerSessionDataJson = gson.toJson(playerSession);
        try {
            LoggingUtil.info("Reporting Periodic Session Data: " + playerSessionDataJson);
            HttpUtil.postJsonWithAuth(Minetrax.getPlugin().getApiHost() + "/api/v1/intel/player/report/event", playerSessionDataJson);
        } catch (Exception e) {
            Minetrax.getPlugin().getLogger().warning(e.getMessage());
        }
        playerSession.resetXminKeys();
    }

    private static void reportAndRemoveSessionFromDataMap(PlayerSessionIntelData playerSession) {
        LoggingUtil.info("REPORT FINAL SESSION END FOR RARE CASE OF SESSION STILL IN DATA WHEN PLAYER ALREADY OFF");
        playerSession.session_ended_at = new Date().getTime();
        String leftPlayerSessionDataJson = gson.toJson(playerSession);
        // REMOVE SESSION TO MAP
        Minetrax.getPlugin().playerSessionIntelDataMap.remove(playerSession.session_uuid);
        try {
            LoggingUtil.info("Final Session Data: " + leftPlayerSessionDataJson);
            HttpUtil.postJsonWithAuth(Minetrax.getPlugin().getApiHost() + "/api/v1/intel/player/report/event", leftPlayerSessionDataJson);
        } catch (Exception e) {
            Minetrax.getPlugin().getLogger().warning(e.getMessage());
        }
    }
}
