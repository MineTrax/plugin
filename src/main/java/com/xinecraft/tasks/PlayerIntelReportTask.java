package com.xinecraft.tasks;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.xinecraft.Minetrax;
import com.xinecraft.data.PlayerSessionIntelData;
import com.xinecraft.utils.HttpUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

public class PlayerIntelReportTask implements Runnable {
    public final Gson gson;

    public PlayerIntelReportTask() {
        this.gson = new GsonBuilder().serializeNulls().create();
    }

    @Override
    public void run() {
        // Get list of all session
        HashMap<String, PlayerSessionIntelData> playerSessionIntelDataMap = Minetrax.getPlugin().getPlayerSessionIntelDataMap();

        // Loop thru each
        for (PlayerSessionIntelData playerSessionData : playerSessionIntelDataMap.values()) {
            // Check if session player is online

            Player onlinePlayer = Bukkit.getPlayer(UUID.fromString(playerSessionData.uuid));

            // If online then report and reset the xmin keys
            if (onlinePlayer != null && onlinePlayer.isOnline()) {
                playerSessionData.player_ping = onlinePlayer.getPing();
                playerSessionData.world_location = gson.toJson(onlinePlayer.getLocation().serialize());
                reportAndResetXminData(playerSessionData);
            }
            // If not online then report and delete key from session variable ending the session.
            else {
                reportAndRemoveSessionFromDataMap(playerSessionData);
            }
        }
    }

    public void reportAndResetXminData(PlayerSessionIntelData playerSession) {
        String playerSessionDataJson = gson.toJson(playerSession);
        try {
            Bukkit.getLogger().info("Reporting Periodic Session Data: " + playerSessionDataJson);
            HttpUtil.postJsonWithAuth(Minetrax.getPlugin().getApiHost() + "/api/v1/intel/player/report/event", playerSessionDataJson);
        } catch (Exception e) {
            Bukkit.getLogger().warning(e.getMessage());
        }
        playerSession.resetXminKeys();
    }

    private void reportAndRemoveSessionFromDataMap(PlayerSessionIntelData playerSession) {
        Bukkit.getLogger().info("REPORT FINAL SESSION END FOR RARE CASE OF SESSION STILL IN DATA WHEN PLAYER ALREADY OFF");
        playerSession.session_ended_at = new Date().getTime();
        String leftPlayerSessionDataJson = gson.toJson(playerSession);
        // REMOVE SESSION TO MAP
        Minetrax.getPlugin().playerSessionIntelDataMap.remove(playerSession.session_uuid);
        try {
            Bukkit.getLogger().info("Final Session Data: " + leftPlayerSessionDataJson);
            HttpUtil.postJsonWithAuth(Minetrax.getPlugin().getApiHost() + "/api/v1/intel/player/report/event", leftPlayerSessionDataJson);
        } catch (Exception e) {
            Bukkit.getLogger().warning(e.getMessage());
        }
    }
}