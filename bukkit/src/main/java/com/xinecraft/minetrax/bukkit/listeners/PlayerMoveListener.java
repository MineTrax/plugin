package com.xinecraft.minetrax.bukkit.listeners;

import com.xinecraft.minetrax.bukkit.MinetraxBukkit;
import com.xinecraft.minetrax.common.data.PlayerData;
import com.xinecraft.minetrax.common.data.PlayerSessionIntelData;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.Objects;

public class PlayerMoveListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.getFrom().getBlockX() == event.getTo().getBlockY() && event.getFrom().getBlockZ() == event.getTo().getBlockZ() && event.getFrom().getBlockY() == event.getTo().getBlockY()) {
            return;
        }

        Player player = event.getPlayer();
        PlayerData playerData = MinetraxBukkit.getPlugin().playersDataMap.get(player.getUniqueId().toString());
        if (playerData != null) {
            playerData.last_active_timestamp = System.currentTimeMillis();

            // Track distance travelled if not disabled.
            if (! MinetraxBukkit.getPlugin().isDisablePlayerMovementTracking) {
                trackDistanceTravelled(event, player, playerData);
            }
        }
    }

    private void trackDistanceTravelled(PlayerMoveEvent event, Player player, PlayerData playerData) {
        Location from = event.getFrom();
        Location to = event.getTo();
        if (to == null || !Objects.equals(from.getWorld(), to.getWorld())) {
            // Ignore movement between different worlds or when the locations are not available
            return;
        }

        PlayerSessionIntelData playerSessionIntelData = MinetraxBukkit.getPlugin().playerSessionIntelDataMap.get(playerData.session_uuid);
        if (playerSessionIntelData == null) {
            return;
        }

        // Track total distance travelled
        double distance = from.distance(to);
        playerSessionIntelData.distance_traveled_xmin += distance;

        boolean isSwimming = false;
        try {
            isSwimming = (boolean) player.getClass().getMethod("isSwimming").invoke(player);
        } catch (Exception e) {
            // log
        }
        // Categorize and track distance based on movement type
        if (player.isFlying() || player.isGliding()) {
            playerSessionIntelData.distance_traveled_on_air_xmin += distance;
        } else if (isSwimming) {
            playerSessionIntelData.distance_traveled_on_water_xmin += distance;
        } else {
            playerSessionIntelData.distance_traveled_on_land_xmin += distance;
        }
    }
}
