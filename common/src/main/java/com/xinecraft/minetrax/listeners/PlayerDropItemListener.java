package com.xinecraft.minetrax.listeners;

import com.xinecraft.minetrax.Minetrax;
import com.xinecraft.minetrax.data.PlayerData;
import com.xinecraft.minetrax.data.PlayerSessionIntelData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;

public class PlayerDropItemListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();

        PlayerData playerData = Minetrax.getPlugin().playersDataMap.get(player.getUniqueId().toString());
        if (playerData == null)
        {
            return;
        }

        PlayerSessionIntelData playerSessionIntelData = Minetrax.getPlugin().playerSessionIntelDataMap.get(playerData.session_uuid);
        playerSessionIntelData.items_dropped_xmin = playerSessionIntelData.items_dropped_xmin + 1;
    }
}
