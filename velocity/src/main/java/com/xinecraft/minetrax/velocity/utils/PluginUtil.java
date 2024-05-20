package com.xinecraft.minetrax.velocity.utils;

import com.velocitypowered.api.proxy.ProxyServer;
import com.xinecraft.minetrax.velocity.MinetraxVelocity;

public class PluginUtil {
    public static boolean checkIfPluginEnabled(String plugin) {
        ProxyServer proxyServer = MinetraxVelocity.getPlugin().getProxyServer();
        return proxyServer.getPluginManager().getPlugin(plugin).isPresent();
    }
}
