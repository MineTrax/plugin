package com.xinecraft.minetrax.common.utils;

import com.xinecraft.minetrax.common.MinetraxCommon;

public class LoggingUtil {
    private static final MinetraxCommon common = MinetraxCommon.getInstance();

    public static void syslog(String message) {
        if (common.getPlugin().getIsDebugMode()) {
            System.out.println(message);
        }
    }

    public static void debug(String message) {
        if (common.getPlugin().getIsDebugMode()) {
            common.getLogger().info(message);
        }
    }

    public static void info(String message) {
        common.getLogger().info(message);
    }

    public static void warning(String message) {
        common.getLogger().warning(message);
    }

    public static void error(String message) {
        common.getLogger().error(message);
    }


    public static void trace(Exception e) {
        if (common.getPlugin().getIsDebugMode()) {
            e.printStackTrace();
        }
    }

    public static void warntrace(Exception e) {
        common.getLogger().warning(e.getMessage());
        if (common.getPlugin().getIsDebugMode()) {
            e.printStackTrace();
        }
    }
}
