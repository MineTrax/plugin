package com.xinecraft.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class WebSayCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings)
    {
        if (commandSender instanceof Player) {
            Bukkit.getLogger().info("Player cannot run this command");
            return false;
        }

        String messageFormat = "<%1$s> %2$s";
        String senderName = strings[0];
        String message = String.join(" ", Arrays.copyOfRange(strings, 1, strings.length));
        String messageFormatted = String.format(messageFormat, senderName, message);

        Bukkit.getServer().broadcastMessage(messageFormatted);
        return false;
    }
}
