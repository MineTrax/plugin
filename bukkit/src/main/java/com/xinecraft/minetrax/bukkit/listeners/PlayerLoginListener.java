package com.xinecraft.minetrax.bukkit.listeners;

import com.xinecraft.minetrax.bukkit.MinetraxBukkit;
import com.xinecraft.minetrax.common.utils.LoggingUtil;
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

                LoggingUtil.debug("Player " + event.getPlayer().getName() + " joined from " + address);
                MinetraxBukkit.getPlugin().joinAddressCache.put(playerUUID.toString(), address);
            }
        } catch (Exception e) {
            LoggingUtil.warntrace(e);
        }
    }
}
