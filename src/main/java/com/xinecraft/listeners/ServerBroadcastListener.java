package com.xinecraft.listeners;

import com.xinecraft.Minetrax;
import com.xinecraft.utils.HttpUtil;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.BroadcastMessageEvent;

import java.util.HashMap;
import java.util.Map;

public class ServerBroadcastListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onServerBroadcastMessage(BroadcastMessageEvent event)
    {
        if (!Minetrax.getPlugin().getIsChatLogEnabled()) {
            return;
        }

        Minetrax.getPlugin().getLogger().info("Server Broadcasting: "+ event.getMessage());

        Map<String, String> params = new HashMap<String, String>();
        params.put("api_key", Minetrax.getPlugin().getApiKey());
        params.put("api_secret", Minetrax.getPlugin().getApiSecret());
        params.put("type", "server-broadcast");
        params.put("chat", event.getMessage());
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
