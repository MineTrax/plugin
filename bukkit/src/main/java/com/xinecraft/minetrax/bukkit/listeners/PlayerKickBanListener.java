package com.xinecraft.minetrax.bukkit.listeners;

import com.xinecraft.minetrax.bukkit.MinetraxBukkit;
import com.xinecraft.minetrax.bukkit.data.PlayerData;
import com.xinecraft.minetrax.bukkit.data.PlayerSessionIntelData;
import com.xinecraft.minetrax.bukkit.utils.HttpUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;

public class PlayerKickBanListener implements Listener {
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerKicked(PlayerKickEvent event) {
        // Update Session Data.
        updateSessionKickBanData(event);

        // Send ChatLog
        if (!MinetraxBukkit.getPlugin().getIsChatLogEnabled()) {
            return;
        }
        Map<String, String> params = new HashMap<String, String>();
        params.put("api_key", MinetraxBukkit.getPlugin().getApiKey());
        params.put("api_secret", MinetraxBukkit.getPlugin().getApiSecret());
        params.put("type", "player-kick");
        params.put("chat", event.getLeaveMessage());
        params.put("causer_username", event.getPlayer().getName());
        params.put("causer_uuid", event.getPlayer().getUniqueId().toString());
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

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerBanned(PlayerQuitEvent event) {
        if (!event.getPlayer().isBanned()) {
            return;
        }

        // Send ChatLog
        if (!MinetraxBukkit.getPlugin().getIsChatLogEnabled()) {
            return;
        }
        Map<String, String> params = new HashMap<String, String>();
        params.put("api_key", MinetraxBukkit.getPlugin().getApiKey());
        params.put("api_secret", MinetraxBukkit.getPlugin().getApiSecret());
        params.put("type", "player-ban");
        params.put("chat", event.getQuitMessage());
        params.put("causer_username", event.getPlayer().getName());
        params.put("causer_uuid", event.getPlayer().getUniqueId().toString());
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

    private void updateSessionKickBanData(PlayerKickEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = MinetraxBukkit.getPlugin().playersDataMap.get(player.getUniqueId().toString());
        if (playerData == null) {
            return;
        }

        PlayerSessionIntelData playerSessionIntelData = MinetraxBukkit.getPlugin().playerSessionIntelDataMap.get(playerData.session_uuid);
        playerSessionIntelData.is_kicked = true;
        if(player.isBanned()) {
            playerSessionIntelData.is_banned = true;
        }
    }
}
