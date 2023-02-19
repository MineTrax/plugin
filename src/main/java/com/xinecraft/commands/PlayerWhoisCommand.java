package com.xinecraft.commands;

import com.xinecraft.Minetrax;
import com.xinecraft.utils.LoggingUtil;
import com.xinecraft.utils.WhoisUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Objects;

public class PlayerWhoisCommand implements CommandExecutor
{
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings)
    {
        String username = null;
        if (strings.length > 0) {
            username = strings[0];
        }
        boolean shouldBroadcast = false;
        Player senderPlayer = null;
        if ((commandSender instanceof Player)) {
            senderPlayer = (Player) commandSender;
        } else {
            shouldBroadcast = true;
        }

        if (username == null) {
            if (senderPlayer != null) {
                username = senderPlayer.getName();
            }
            else {
                Minetrax.getPlugin().getLogger().info("Username is required! Eg: ww notch");
                return false;
            }
        }

        // Check if Player is online
        Player player = Bukkit.getPlayerExact(username);
        String uuid = null;
        String ipAddress = null;
        if (player != null && player.isOnline()) {
            uuid = player.getUniqueId().toString();
            LoggingUtil.info("UUID " + uuid);
            ipAddress = Objects.requireNonNull(player.getAddress()).getHostString();
        }

        WhoisUtil.forPlayer(username, uuid, ipAddress, shouldBroadcast, false, senderPlayer);
        return false;
    }
}
