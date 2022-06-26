package com.xinecraft.tasks;

import com.xinecraft.Minetrax;
import com.xinecraft.data.PlayerData;
import com.xinecraft.data.PlayerSessionIntelData;
import org.bukkit.entity.Player;

public class PlayerAfkTrackerTask implements Runnable {
    @Override
    public void run() {
        // Get list of all online players and loop thru them
        for(Player player : Minetrax.getPlugin().getServer().getOnlinePlayers())
        {
            PlayerData playerData = Minetrax.getPlugin().playersDataMap.get(player.getUniqueId().toString());

            if (playerData == null) continue;

            long lastMoveInMs = System.currentTimeMillis() - playerData.last_active_timestamp;
            if (lastMoveInMs > Minetrax.getPlugin().getAfkThresholdInMs()) {
                PlayerSessionIntelData playerSessionIntelData = Minetrax.getPlugin().playerSessionIntelDataMap.get(playerData.session_uuid);
                if (playerSessionIntelData == null) continue;
                playerSessionIntelData.afk_time = playerSessionIntelData.afk_time + 1;
                playerSessionIntelData.afk_time_xmin = playerSessionIntelData.afk_time_xmin + 1;
            }
        }
    }
}
