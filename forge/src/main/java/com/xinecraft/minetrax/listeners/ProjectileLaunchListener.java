package com.xinecraft.minetrax.listeners;

import com.xinecraft.minetrax.Minetrax;
import com.xinecraft.minetrax.data.PlayerData;
import com.xinecraft.minetrax.data.PlayerSessionIntelData;
import org.bukkit.entity.Player;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;

public class ProjectileLaunchListener implements Listener {
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        // Add for Egg, EnderPearl, Snowball, ThrownExpBottle
        if (event.getEntity() instanceof Trident) {
            return;
        }

        if (!(event.getEntity().getShooter() instanceof Player)) {
            // The shooter must be a player, ignore other entities shooting eggs
            return;
        }

        Player player = (Player) event.getEntity().getShooter();

        PlayerData playerData = Minetrax.getPlugin().playersDataMap.get(player.getUniqueId().toString());
        if (playerData == null) {
            return;
        }

        PlayerSessionIntelData playerSessionIntelData = Minetrax.getPlugin().playerSessionIntelDataMap.get(playerData.session_uuid);
        playerSessionIntelData.items_used_xmin = playerSessionIntelData.items_used_xmin + 1;
    }
}
