package com.xinecraft.tasks;

import com.xinecraft.Minetrax;
import com.xinecraft.data.PlayerData;
import com.xinecraft.data.PlayerSessionIntelData;
import com.xinecraft.data.PlayerWorldStatsIntelData;
import org.bukkit.entity.Player;

public class PlayerAfkAndWorldIntelTrackerTask implements Runnable {
    @Override
    public void run() {
        // Get list of all online players and loop thru them
        for(Player player : Minetrax.getPlugin().getServer().getOnlinePlayers())
        {
            PlayerData playerData = Minetrax.getPlugin().playersDataMap.get(player.getUniqueId().toString());
            if (playerData == null) continue;

            PlayerSessionIntelData playerSessionIntelData = Minetrax.getPlugin().playerSessionIntelDataMap.get(playerData.session_uuid);
            if (playerSessionIntelData == null) continue;

            // Track Play Time
            playerSessionIntelData.play_time = playerSessionIntelData.play_time + 1;

            // Track AFK Time
            long lastMoveInMs = System.currentTimeMillis() - playerData.last_active_timestamp;
            if (lastMoveInMs > Minetrax.getPlugin().getAfkThresholdInMs()) {
                playerSessionIntelData.afk_time = playerSessionIntelData.afk_time + 1;
                playerSessionIntelData.afk_time_xmin = playerSessionIntelData.afk_time_xmin + 1;
            }

            // Track World Statistics
            String playerWorldName = player.getWorld().getName();
            PlayerWorldStatsIntelData playerWorldStatsIntelData = playerSessionIntelData.players_world_stat_intel.get(playerWorldName);
            String gameMode = player.getGameMode().toString();
            switch (gameMode) {
                case "SURVIVAL":
                    playerWorldStatsIntelData.survival_time = playerWorldStatsIntelData.survival_time + 1;
                    break;
                case "CREATIVE":
                    playerWorldStatsIntelData.creative_time = playerWorldStatsIntelData.creative_time + 1;
                    break;
                case "SPECTATOR":
                    playerWorldStatsIntelData.spectator_time = playerWorldStatsIntelData.spectator_time + 1;
                    break;
                case "ADVENTURE":
                    playerWorldStatsIntelData.adventure_time = playerWorldStatsIntelData.adventure_time + 1;
                    break;
                default:

            }
        }
    }
}
