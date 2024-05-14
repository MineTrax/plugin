package com.xinecraft.minetrax.bungee.utils;

import net.md_5.bungee.api.ProxyServer;

public class PluginUtil {
    public static boolean checkIfPluginEnabled(String plugin) {
        ProxyServer proxyServer = ProxyServer.getInstance();

        return proxyServer.getPluginManager().getPlugin(plugin) != null;
    }
}
