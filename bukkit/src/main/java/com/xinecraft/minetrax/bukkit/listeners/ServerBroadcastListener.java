package com.xinecraft.minetrax.bukkit.listeners;

import com.xinecraft.minetrax.bukkit.MinetraxBukkit;
import com.xinecraft.minetrax.bukkit.utils.HttpUtil;
import com.xinecraft.minetrax.bukkit.utils.LoggingUtil;
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
        if (!MinetraxBukkit.getPlugin().getIsChatLogEnabled()) {
            return;
        }

        LoggingUtil.info("Server Broadcasting: "+ event.getMessage());

        Map<String, String> params = new HashMap<String, String>();
        params.put("api_key", MinetraxBukkit.getPlugin().getApiKey());
        params.put("api_secret", MinetraxBukkit.getPlugin().getApiSecret());
        params.put("type", "server-broadcast");
        params.put("chat", event.getMessage());
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
