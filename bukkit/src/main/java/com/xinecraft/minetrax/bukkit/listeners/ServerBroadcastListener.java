package com.xinecraft.minetrax.bukkit.listeners;

import com.xinecraft.minetrax.bukkit.MinetraxBukkit;
import com.xinecraft.minetrax.bukkit.utils.LoggingUtil;
import com.xinecraft.minetrax.common.actions.ReportServerChat;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.BroadcastMessageEvent;

public class ServerBroadcastListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onServerBroadcastMessage(BroadcastMessageEvent event) {
        if (!MinetraxBukkit.getPlugin().getIsChatLogEnabled()) {
            return;
        }

        LoggingUtil.info("Server Broadcasting: " + event.getMessage());

        ReportServerChat.reportAsync(
                "server-broadcast",
                event.getMessage(),
                null,
                null
        );
    }
}
