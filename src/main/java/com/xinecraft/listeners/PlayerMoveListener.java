package com.xinecraft.listeners;

import com.xinecraft.Minetrax;
import com.xinecraft.data.PlayerData;
import com.xinecraft.data.PlayerSessionIntelData;
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
        Player player = event.getPlayer();

        PlayerData playerData = Minetrax.getPlugin().playersDataMap.get(player.getUniqueId().toString());
        if (playerData != null) {
            playerData.last_active_timestamp = System.currentTimeMillis();

            trackDistanceTravelled(event, player, playerData);
        }
    }

    private void trackDistanceTravelled(PlayerMoveEvent event, Player player, PlayerData playerData) {
        Location from = event.getFrom();
        Location to = event.getTo();
        if (to == null || !Objects.equals(from.getWorld(), to.getWorld())) {
            // Ignore movement between different worlds or when the locations are not available
            return;
        }

        double distance = from.distance(to);

        PlayerSessionIntelData playerSessionIntelData = Minetrax.getPlugin().playerSessionIntelDataMap.get(playerData.session_uuid);
        // Track total distance travelled
        playerSessionIntelData.distance_traveled_xmin += distance;

        // Categorize and track distance based on movement type
        if (player.isFlying() || player.isGliding()) {
            playerSessionIntelData.distance_traveled_on_air_xmin += distance;
        } else if (player.isSwimming()) {
            playerSessionIntelData.distance_traveled_on_water_xmin += distance;
        } else {
            playerSessionIntelData.distance_traveled_on_land_xmin += distance;
        }
    }
}
