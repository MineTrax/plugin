package com.xinecraft.minetrax.bukkit.hooks.chat;

import com.xinecraft.minetrax.common.actions.ReportServerChat;
import io.signality.Modules.Chat.EpicCoreChatEvent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

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

        ReportServerChat.reportAsync(
                "player-chat",
                message,
                player.getName(),
                player.getUniqueId().toString()
        );
    }
}
