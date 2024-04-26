package com.xinecraft.minetrax.bungee.logging;

import com.xinecraft.minetrax.bungee.MinetraxBungee;
import com.xinecraft.minetrax.common.interfaces.logging.CommonLogger;

import java.util.logging.Level;

public class BungeeLogger implements CommonLogger {
    private final MinetraxBungee plugin;

    public BungeeLogger(MinetraxBungee plugin) {
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
