package com.xinecraft.minetrax.bukkit.listeners;

import com.xinecraft.minetrax.bukkit.MinetraxBukkit;
import com.xinecraft.minetrax.common.actions.ReportServerChat;
import com.xinecraft.minetrax.common.data.PlayerData;
import com.xinecraft.minetrax.common.data.PlayerSessionIntelData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerKickBanListener implements Listener {
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerKicked(PlayerKickEvent event) {
        // Update Session Data.
        updateSessionKickBanData(event);

        // Send ChatLog
        if (!MinetraxBukkit.getPlugin().getIsChatLogEnabled()) {
            return;
        }

        ReportServerChat.reportAsync(
                "player-kick",
                event.getLeaveMessage(),
                event.getPlayer().getName(),
                event.getPlayer().getUniqueId().toString()
        );
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerBanned(PlayerQuitEvent event) {
        if (!event.getPlayer().isBanned()) {
            return;
        }

        // Send ChatLog
        if (!MinetraxBukkit.getPlugin().getIsChatLogEnabled()) {
            return;
        }

        ReportServerChat.reportAsync(
                "player-ban",
                event.getQuitMessage(),
                event.getPlayer().getName(),
                event.getPlayer().getUniqueId().toString()
        );
    }

    private void updateSessionKickBanData(PlayerKickEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = MinetraxBukkit.getPlugin().playersDataMap.get(player.getUniqueId().toString());
        if (playerData == null) {
            return;
        }

        PlayerSessionIntelData playerSessionIntelData = MinetraxBukkit.getPlugin().playerSessionIntelDataMap.get(playerData.session_uuid);
        playerSessionIntelData.is_kicked = true;
        if (player.isBanned()) {
            playerSessionIntelData.is_banned = true;
        }
    }
}
