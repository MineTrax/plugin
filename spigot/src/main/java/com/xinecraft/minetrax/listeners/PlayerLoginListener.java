package com.xinecraft.minetrax.listeners;

import com.xinecraft.minetrax.Minetrax;
import com.xinecraft.minetrax.utils.LoggingUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

import java.util.UUID;

public class PlayerLoginListener implements Listener {
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerLogin(PlayerLoginEvent event) {
        addJoinAddressToCache(event);
    }

    private void addJoinAddressToCache(PlayerLoginEvent event) {
        try {
            UUID playerUUID = event.getPlayer().getUniqueId();

            String address = event.getHostname();
            if (!address.isEmpty()) {
                int endIndex = address.lastIndexOf(':');
                if (endIndex == -1) {
                    endIndex = address.length();
                }
                address = address.substring(0, endIndex);
                if (address.contains("\u0000")) {
                    address = address.substring(0, address.indexOf('\u0000'));
                }

                LoggingUtil.info("Player " + event.getPlayer().getName() + " joined from " + address);
                Minetrax.getPlugin().joinAddressCache.put(playerUUID.toString(), address);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
