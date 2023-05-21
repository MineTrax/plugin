package com.xinecraft.hooks.chat;

import com.xinecraft.Minetrax;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import ru.mrbrikster.chatty.api.chats.Chat;
import ru.mrbrikster.chatty.api.events.ChattyMessageEvent;

public class ChattyHook implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChattyChat(ChattyMessageEvent event) {
        Minetrax.getPlugin().getLogger().info("Chatty chat event fired");
        Player player = event.getPlayer();
        String message = event.getMessage();

        Minetrax.getPlugin().getLogger().info(player.getDisplayName() + " said: " + message);

        Chat chat = event.getChat();
        String name = chat.getName();
        String format = chat.getFormat();
        boolean isPermissionRequired = chat.isPermissionRequired();
        int range = chat.getRange();

        Minetrax.getPlugin().getLogger().info("Chat name: " + name);
        Minetrax.getPlugin().getLogger().info("Chat format: " + format);
        Minetrax.getPlugin().getLogger().info("Chat isPermissionRequired: " + isPermissionRequired);
        Minetrax.getPlugin().getLogger().info("Chat range: " + range);
    }
}
