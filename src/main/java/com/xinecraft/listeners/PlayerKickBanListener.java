package com.xinecraft.listeners;

import com.xinecraft.Minetrax;
import com.xinecraft.utils.HttpUtil;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;

public class PlayerKickBanListener implements Listener
{
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerKicked(PlayerKickEvent event)
    {
        if (!Minetrax.getPlugin().getIsChatLogEnabled()) {
            return;
        }

        Map<String, String> params = new HashMap<String, String>();
        params.put("api_key", Minetrax.getPlugin().getApiKey());
        params.put("api_secret", Minetrax.getPlugin().getApiSecret());
        params.put("type", "player-kick");
        params.put("chat", event.getLeaveMessage());
        params.put("causer_username", event.getPlayer().getName());
        params.put("causer_uuid", event.getPlayer().getUniqueId().toString());
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

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerBanned(PlayerQuitEvent event)
    {
        if (!Minetrax.getPlugin().getIsChatLogEnabled()) {
            return;
        }

        if (event.getPlayer().isBanned())
        {
            Map<String, String> params = new HashMap<String, String>();
            params.put("api_key", Minetrax.getPlugin().getApiKey());
            params.put("api_secret", Minetrax.getPlugin().getApiSecret());
            params.put("type", "player-ban");
            params.put("chat", event.getQuitMessage());
            params.put("causer_username", event.getPlayer().getName());
            params.put("causer_uuid", event.getPlayer().getUniqueId().toString());
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
