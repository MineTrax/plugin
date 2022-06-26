package com.xinecraft.listeners;

import com.xinecraft.Minetrax;
import com.xinecraft.data.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandSendEvent;

public class PlayerCommandListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerCommand(PlayerCommandSendEvent event) {
        Player player = event.getPlayer();

        PlayerData playerData = Minetrax.getPlugin().playersDataMap.get(player.getUniqueId().toString());
        if (playerData != null) {
            playerData.last_active_timestamp = System.currentTimeMillis();
        }
    }
}
