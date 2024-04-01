package com.xinecraft.minetrax.hooks.chat;

import com.xinecraft.minetrax.Minetrax;
import com.xinecraft.minetrax.utils.HttpUtil;
import io.signality.Modules.Chat.EpicCoreChatEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Map;

public class EpicCoreChatHook implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEpicCoreChatEvent(EpicCoreChatEvent event) {
        String channel = event.getChannel();

        if (!channel.equals("global")) {
            return;
        }

        Player player = event.getPlayer();
        if (player == null) return;

        String chatMessage = event.getMessage();
        String message = String.format(event.getFormat(), player.getDisplayName(), chatMessage) + ' ' + chatMessage;
        message = ChatColor.translateAlternateColorCodes('&', message);

        Map<String, String> params = new HashMap<String, String>();
        params.put("api_key", Minetrax.getPlugin().getApiKey());
        params.put("api_secret", Minetrax.getPlugin().getApiSecret());
        params.put("type", "player-chat");
        params.put("chat", message);
        params.put("causer_username", player.getName());
        params.put("causer_uuid", player.getUniqueId().toString());
        params.put("server_id", Minetrax.getPlugin().getApiServerId());

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
