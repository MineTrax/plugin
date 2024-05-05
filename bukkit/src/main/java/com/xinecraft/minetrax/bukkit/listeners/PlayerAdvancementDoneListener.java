package com.xinecraft.minetrax.bukkit.listeners;

import com.xinecraft.minetrax.common.utils.LoggingUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;

public class PlayerAdvancementDoneListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerAdvancementDone(PlayerAdvancementDoneEvent event)
    {
        LoggingUtil.info("Advancement Made:" + event.getAdvancement().getKey().toString());
    }
}
