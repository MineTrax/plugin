package com.xinecraft.tasks;

import com.google.gson.Gson;
import com.xinecraft.Minetrax;
import com.xinecraft.data.PlayerSessionIntelData;
import com.xinecraft.utils.PlayerIntelUtil;

import java.util.HashMap;

public class PlayerIntelReportTask implements Runnable {
    public final Gson gson;

    public PlayerIntelReportTask() {
        this.gson = Minetrax.getPlugin().getGson();
    }

    @Override
    public void run() {
        // Get list of all session
        HashMap<String, PlayerSessionIntelData> playerSessionIntelDataMap = Minetrax.getPlugin().getPlayerSessionIntelDataMap();

        // Loop thru each
        for (PlayerSessionIntelData playerSessionData : playerSessionIntelDataMap.values()) {
            // Check if session player is online
            PlayerIntelUtil.reportPlayerIntel(playerSessionData, false);
        }
    }
}
