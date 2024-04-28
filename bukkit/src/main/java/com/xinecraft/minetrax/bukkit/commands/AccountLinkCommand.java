package com.xinecraft.minetrax.bukkit.commands;

import com.xinecraft.minetrax.bukkit.MinetraxBukkit;
import com.xinecraft.minetrax.common.actions.LinkAccount;
import com.xinecraft.minetrax.common.responses.GenericApiResponse;
import com.xinecraft.minetrax.common.utils.MinetraxHttpUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class AccountLinkCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, String[] strings) {
        if (!(commandSender instanceof Player player)) {
            MinetraxBukkit.getPlugin().getLogger().info("Error: Only players can execute that command.");
            return false;
        }

        // Send Init message if only /link
        if (strings.length == 0) {
            for (String line : MinetraxBukkit.getPlugin().getPlayerLinkInitMessage()) {
                line = line.replace("{LINK_URL}", MinetraxHttpUtil.getUrl(MinetraxHttpUtil.ACCOUNT_LINK_ROUTE));
                line = ChatColor.translateAlternateColorCodes('&', line);
                player.sendMessage(line);
            }
            return true;
        }

        // Send Linking message if /link <otp>
        String otpCode = strings[0];
        Bukkit.getScheduler().runTaskAsynchronously(MinetraxBukkit.getPlugin(), () -> {
            try {
                GenericApiResponse response = LinkAccount.link(
                        player.getUniqueId().toString(),
                        otpCode,
                        MinetraxBukkit.getPlugin().getApiServerId()
                );

                // not-found
                if (response.getCode() != 200) {
                    for (String line : MinetraxBukkit.getPlugin().getPlayerLinkErrorMessage()) {
                        line = line.replace("{ERROR_MESSAGE}", response.getMessage());
                        line = ChatColor.translateAlternateColorCodes('&', line);
                        player.sendMessage(line);
                    }
                    return;
                } else {
                    for (String line : MinetraxBukkit.getPlugin().getPlayerLinkSuccessMessage()) {
                        line = line.replace("{LINK_URL}", MinetraxHttpUtil.getUrl(MinetraxHttpUtil.ACCOUNT_LINK_ROUTE));
                        line = ChatColor.translateAlternateColorCodes('&', line);
                        player.sendMessage(line);
                    }
                }
            } catch (Exception e) {
                for (String line : MinetraxBukkit.getPlugin().getPlayerLinkErrorMessage()) {
                    line = line.replace("{WEB_URL}", MinetraxBukkit.getPlugin().getApiHost());
                    line = line.replace("{ERROR_MESSAGE}", " ");
                    line = ChatColor.translateAlternateColorCodes('&', line);
                    player.sendMessage(line);
                }
            }
        });

        return true;
    }
}
