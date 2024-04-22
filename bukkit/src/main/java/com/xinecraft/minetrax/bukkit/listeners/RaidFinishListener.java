package com.xinecraft.minetrax.bukkit.listeners;

import com.xinecraft.minetrax.bukkit.MinetraxBukkit;
import com.xinecraft.minetrax.bukkit.data.PlayerData;
import com.xinecraft.minetrax.bukkit.data.PlayerSessionIntelData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.raid.RaidFinishEvent;

public class RaidFinishListener implements Listener {
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onRaidFinish(RaidFinishEvent event) {
        if (event.getWinners().isEmpty()) {
            // No winners in the raid, ignore the event
            return;
        }

        for (Player winner : event.getWinners()) {
            PlayerData playerData = MinetraxBukkit.getPlugin().playersDataMap.get(winner.getUniqueId().toString());
            if (playerData == null) {
                continue;
            }

            PlayerSessionIntelData playerSessionIntelData = MinetraxBukkit.getPlugin().playerSessionIntelDataMap.get(playerData.session_uuid);
            playerSessionIntelData.raids_won_xmin = playerSessionIntelData.raids_won_xmin + 1;
        }
    }
}
