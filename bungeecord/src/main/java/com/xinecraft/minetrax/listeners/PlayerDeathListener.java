package com.xinecraft.minetrax.listeners;

import com.xinecraft.minetrax.Minetrax;
import com.xinecraft.minetrax.utils.HttpUtil;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.HashMap;
import java.util.Map;

public class PlayerDeathListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event)
    {
        if (!Minetrax.getPlugin().getIsChatLogEnabled()) {
            return;
        }

        // If death is of a valid human player then only proceed
        if (event.getEntity().getPlayer() != null) {
            Map<String, String> params = new HashMap<String, String>();
            params.put("api_key", Minetrax.getPlugin().getApiKey());
            params.put("api_secret", Minetrax.getPlugin().getApiSecret());
            params.put("type", "player-death");
            params.put("chat", event.getDeathMessage());
            params.put("causer_username", event.getEntity().getPlayer().getName());
            params.put("causer_uuid", event.getEntity().getPlayer().getUniqueId().toString());
            params.put("server_id", Minetrax.getPlugin().getApiServerId());
            // Run this async to not block the main thread
            Bukkit.getScheduler().runTaskAsynchronously(Minetrax.getPlugin(), new Runnable() {
                @Override
                public void run() {
                    try {
                        HttpUtil.postForm(Minetrax.getPlugin().getApiHost() + "/api/v1/server/chat", params);
                    } catch (Exception e) {
                        Minetrax.getPlugin().getLogger().warning(e.getMessage());
                    }
                }
            });
        }
    }
}
