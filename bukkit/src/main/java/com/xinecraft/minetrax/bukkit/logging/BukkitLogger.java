package com.xinecraft.minetrax.bukkit.logging;

import com.xinecraft.minetrax.bukkit.MinetraxBukkit;
import com.xinecraft.minetrax.common.interfaces.logging.CommonLogger;

import java.util.logging.Level;

public class BukkitLogger implements CommonLogger {
    private final MinetraxBukkit plugin;

    public BukkitLogger(MinetraxBukkit plugin) {
        this.plugin = plugin;
    }

    @Override
    public void log(String message) {
        plugin.getLogger().log(Level.FINE, message);
    }

    @Override
    public void info(String message) {
        plugin.getLogger().info(message);
    }

    @Override
    public void warning(String message) {
        plugin.getLogger().warning(message);
    }

    @Override
    public void error(String message) {
        plugin.getLogger().severe(message);
    }
}
