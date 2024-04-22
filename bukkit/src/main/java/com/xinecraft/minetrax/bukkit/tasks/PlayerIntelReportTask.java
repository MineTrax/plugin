package com.xinecraft.minetrax.bukkit.tasks;

import com.google.gson.Gson;
import com.xinecraft.minetrax.bukkit.MinetraxBukkit;
import com.xinecraft.minetrax.bukkit.data.PlayerSessionIntelData;
import com.xinecraft.minetrax.bukkit.utils.PlayerIntelUtil;

import java.util.HashMap;

public class PlayerIntelReportTask implements Runnable {
    public final Gson gson;

    public PlayerIntelReportTask() {
        this.gson = MinetraxBukkit.getPlugin().getGson();
    }

    @Override
    public void run() {
        // Get list of all session
        HashMap<String, PlayerSessionIntelData> playerSessionIntelDataMap = MinetraxBukkit.getPlugin().getPlayerSessionIntelDataMap();

        // Loop thru each
        for (PlayerSessionIntelData playerSessionData : playerSessionIntelDataMap.values()) {
            // Check if session player is online
            PlayerIntelUtil.reportPlayerIntel(playerSessionData, false);
        }
    }
}
