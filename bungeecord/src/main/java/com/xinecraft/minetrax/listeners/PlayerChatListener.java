package com.xinecraft.minetrax.listeners;

import com.xinecraft.minetrax.Minetrax;
import com.xinecraft.minetrax.data.PlayerData;
import com.xinecraft.minetrax.utils.HttpUtil;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.HashMap;
import java.util.Map;

public class PlayerChatListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerChat(AsyncPlayerChatEvent event)
    {
        if (!Minetrax.getPlugin().getIsChatLogEnabled()) {
            return;
        }

        String message = String.format(event.getFormat(), event.getPlayer().getDisplayName(), event.getMessage());

        Map<String, String> params = new HashMap<String, String>();
        params.put("api_key", Minetrax.getPlugin().getApiKey());
        params.put("api_secret", Minetrax.getPlugin().getApiSecret());
        params.put("type", "player-chat");
        params.put("chat", message);
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

        // Update player last active timestamp since chat is activity
        PlayerData playerData = Minetrax.getPlugin().playersDataMap.get(event.getPlayer().getUniqueId().toString());
        if (playerData != null) {
            playerData.last_active_timestamp = System.currentTimeMillis();
        }
    }
}
