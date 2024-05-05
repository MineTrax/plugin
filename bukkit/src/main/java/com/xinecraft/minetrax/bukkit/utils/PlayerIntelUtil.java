package com.xinecraft.minetrax.bukkit.utils;

import com.google.gson.Gson;
import com.xinecraft.minetrax.bukkit.MinetraxBukkit;
import com.xinecraft.minetrax.common.actions.ReportPlayerIntel;
import com.xinecraft.minetrax.common.data.PlayerSessionIntelData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Date;
import java.util.UUID;

public class PlayerIntelUtil {
    public static Gson gson = MinetraxBukkit.getPlugin().getGson();

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
            if (MinetraxBukkit.getPlugin().getIsSendInventoryDataToPlayerIntel()) {
                playerSessionData.inventory = gson.toJson(onlinePlayer.getInventory().getContents());
                playerSessionData.ender_chest = gson.toJson(onlinePlayer.getEnderChest().getContents());
            }

            // Get Vault Plugin Data.
            playerSessionData.vault_balance = MinetraxBukkit.getVaultEconomy() != null ? MinetraxBukkit.getVaultEconomy().getBalance(onlinePlayer) : 0;
            if (MinetraxBukkit.getVaultPermission() != null && MinetraxBukkit.getVaultPermission().hasGroupSupport()) {
                playerSessionData.vault_groups = MinetraxBukkit.getVaultPermission().getPlayerGroups(onlinePlayer);
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
        try {
            ReportPlayerIntel.reportEventSync(playerSession);
        } catch (Exception e) {
            MinetraxBukkit.getPlugin().getLogger().warning(e.getMessage());
        }
        playerSession.resetXminKeys();
    }

    private static void reportAndRemoveSessionFromDataMap(PlayerSessionIntelData playerSession) {
        playerSession.session_ended_at = new Date().getTime();
        MinetraxBukkit.getPlugin().playerSessionIntelDataMap.remove(playerSession.session_uuid);
        try {
            ReportPlayerIntel.reportEventSync(playerSession);
        } catch (Exception e) {
            MinetraxBukkit.getPlugin().getLogger().warning(e.getMessage());
        }
    }
}
