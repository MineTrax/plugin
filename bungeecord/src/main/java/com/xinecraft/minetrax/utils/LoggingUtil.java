package com.xinecraft.minetrax.utils;

import com.xinecraft.minetrax.Minetrax;

public class LoggingUtil {
    public static void syslog(String message) {
        if(Minetrax.getPlugin().getIsDebugMode()) {
            System.out.println(message);
        }
    }
    public static void info(String message) {
        if(Minetrax.getPlugin().getIsDebugMode()) {
            Minetrax.getPlugin().getLogger().info(message);
        }
    }
    public static void warning(String message) {
        if(Minetrax.getPlugin().getIsDebugMode()) {
            Minetrax.getPlugin().getLogger().warning(message);
        }
    }
}
