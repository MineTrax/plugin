package com.xinecraft.minetrax.bukkit.listeners;

import com.xinecraft.minetrax.bukkit.MinetraxBukkit;
import com.xinecraft.minetrax.bukkit.utils.HttpUtil;
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
        if (!MinetraxBukkit.getPlugin().getIsChatLogEnabled()) {
            return;
        }

        // If death is of a valid human player then only proceed
        if (event.getEntity().getPlayer() != null) {
            Map<String, String> params = new HashMap<String, String>();
            params.put("api_key", MinetraxBukkit.getPlugin().getApiKey());
            params.put("api_secret", MinetraxBukkit.getPlugin().getApiSecret());
            params.put("type", "player-death");
            params.put("chat", event.getDeathMessage());
            params.put("causer_username", event.getEntity().getPlayer().getName());
            params.put("causer_uuid", event.getEntity().getPlayer().getUniqueId().toString());
            params.put("server_id", MinetraxBukkit.getPlugin().getApiServerId());
            // Run this async to not block the main thread
            Bukkit.getScheduler().runTaskAsynchronously(MinetraxBukkit.getPlugin(), new Runnable() {
                @Override
                public void run() {
                    try {
                        HttpUtil.postForm(MinetraxBukkit.getPlugin().getApiHost() + "/api/v1/server/chat", params);
                    } catch (Exception e) {
                        MinetraxBukkit.getPlugin().getLogger().warning(e.getMessage());
                    }
                }
            });
        }
    }
}
