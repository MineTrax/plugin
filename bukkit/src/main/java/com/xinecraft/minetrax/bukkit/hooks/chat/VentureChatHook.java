package com.xinecraft.minetrax.bukkit.hooks.chat;

import com.xinecraft.minetrax.common.actions.ReportServerChat;
import mineverse.Aust1n46.chat.api.MineverseChatPlayer;
import mineverse.Aust1n46.chat.api.events.VentureChatEvent;
import mineverse.Aust1n46.chat.channel.ChatChannel;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

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

                ReportServerChat.reportAsync(
                        "player-chat",
                        message,
                        player.getName(),
                        player.getUniqueId().toString()
                );
            }
        }
    }
}
