package com.xinecraft.minetrax.tasks;

import com.xinecraft.minetrax.Minetrax;
import com.xinecraft.minetrax.data.PlayerData;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class AccountLinkReminderTask implements Runnable {
    @Override
    public void run() {
        // Get list of all online players and loop thru them
        for(Player player : Minetrax.getPlugin().getServer().getOnlinePlayers())
        {
            PlayerData playerData = Minetrax.getPlugin().playersDataMap.get(player.getUniqueId().toString());
            if (playerData != null && playerData.is_verified) continue;

            for (String line: Minetrax.getPlugin().getRemindPlayerToLinkMessage())
            {
                line = ChatColor.translateAlternateColorCodes('&', line);
                player.sendMessage(line);
            }
        }
    }
}
