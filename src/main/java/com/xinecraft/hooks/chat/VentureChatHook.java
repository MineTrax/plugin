package com.xinecraft.hooks.chat;

import com.xinecraft.Minetrax;
import com.xinecraft.utils.HttpUtil;
import mineverse.Aust1n46.chat.api.MineverseChatPlayer;
import mineverse.Aust1n46.chat.api.events.VentureChatEvent;
import mineverse.Aust1n46.chat.channel.ChatChannel;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Map;

public class VentureChatHook implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onVentureChat(VentureChatEvent event) {

        ChatChannel chatChannel = event.getChannel();

        if (chatChannel == null) return;

        // If channel is not default then return
        if (!chatChannel.isDefaultchannel()) {
            return;
        }

        String chatMessage = event.getChat();

        MineverseChatPlayer chatPlayer = event.getMineverseChatPlayer();
        if (chatPlayer != null) {
            Player player = chatPlayer.getPlayer();
            if (player != null) {

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
}
