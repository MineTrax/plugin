package com.xinecraft.minetrax.velocity.commands;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import com.xinecraft.minetrax.common.MinetraxCommon;
import com.xinecraft.minetrax.common.enums.BanWardenSyncType;
import com.xinecraft.minetrax.velocity.MinetraxVelocity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class MinetraxAdminCommand implements SimpleCommand {
    private final MinetraxVelocity plugin;

    public MinetraxAdminCommand(MinetraxVelocity plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        if (args.length == 0) {
            usage(source);
            return;
        }

        String firstArg = args[0];
        if (firstArg.equalsIgnoreCase("help") || firstArg.equalsIgnoreCase("?")) {
            usage(source);
            return;
        }

        if (firstArg.equalsIgnoreCase("banwarden:sync")) {
            if (!(source instanceof ConsoleCommandSource)) {
                source.sendMessage(Component.text("This command can only be run from console.", NamedTextColor.RED));
                return;
            }

            String secondArg = args.length > 1 ? args[1].toLowerCase() : "all";
            source.sendMessage(Component.text("[BanWarden] Syncing " + secondArg + " punishments to web, plz check server logs for progress...", NamedTextColor.GREEN));
            banwardenSyncBans(secondArg);
            return;
        }
    }

    private void usage(CommandSource source) {
        source.sendMessage(Component.text("Minetrax Admin Commands:", NamedTextColor.AQUA));
        source.sendMessage(Component.text("/mtxv banwarden:sync", NamedTextColor.GREEN));
        source.sendMessage(Component.text("   Sync bans from ban plugin to minetrax website.", NamedTextColor.GRAY));
        source.sendMessage(Component.text("/mtxv help", NamedTextColor.GREEN));
        source.sendMessage(Component.text("   Shows help message.", NamedTextColor.GRAY));
    }

    private void banwardenSyncBans(String typeString) {
        if (!plugin.getIsBanWardenEnabled()) {
            plugin.getLogger().warn("BanWarden is not enabled, cannot sync bans.");
            return;
        }

        BanWardenSyncType syncType = switch (typeString) {
            case "active" -> BanWardenSyncType.ACTIVE;
            case "inactive" -> BanWardenSyncType.INACTIVE;
            default -> BanWardenSyncType.ALL;
        };
        MinetraxCommon.getInstance().getBanWarden().sync(syncType);
    }

    @Override
    public boolean hasPermission(final Invocation invocation) {
        return invocation.source().hasPermission("minetrax.admin");
    }
}