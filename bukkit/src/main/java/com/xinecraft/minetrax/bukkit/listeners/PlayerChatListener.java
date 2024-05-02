package com.xinecraft.minetrax.bukkit.listeners;

import com.xinecraft.minetrax.bukkit.MinetraxBukkit;
import com.xinecraft.minetrax.common.actions.ReportServerChat;
import com.xinecraft.minetrax.common.data.PlayerData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class PlayerChatListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (!MinetraxBukkit.getPlugin().getIsChatLogEnabled()) {
            return;
        }

        // Report Chat
        String message = String.format(event.getFormat(), event.getPlayer().getDisplayName(), event.getMessage());
        ReportServerChat.reportAsync(
                "player-chat",
                message,
                event.getPlayer().getName(),
                event.getPlayer().getUniqueId().toString()
        );

        // Update player last active timestamp since chat is activity
        PlayerData playerData = MinetraxBukkit.getPlugin().playersDataMap.get(event.getPlayer().getUniqueId().toString());
        if (playerData != null) {
            playerData.last_active_timestamp = System.currentTimeMillis();
        }
    }
}
