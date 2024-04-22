package com.xinecraft.minetrax.bukkit.utils;

import com.xinecraft.minetrax.bukkit.MinetraxBukkit;

public class LoggingUtil {
    public static void syslog(String message) {
        if(MinetraxBukkit.getPlugin().getIsDebugMode()) {
            System.out.println(message);
        }
    }
    public static void info(String message) {
        if(MinetraxBukkit.getPlugin().getIsDebugMode()) {
            MinetraxBukkit.getPlugin().getLogger().info(message);
        }
    }
    public static void warning(String message) {
        if(MinetraxBukkit.getPlugin().getIsDebugMode()) {
            MinetraxBukkit.getPlugin().getLogger().warning(message);
        }
    }
}
