package com.xinecraft.minetrax.bungee.commander;

import com.xinecraft.minetrax.common.interfaces.commander.CommonCommander;
import net.md_5.bungee.api.ProxyServer;

public class BungeeCommander implements CommonCommander {
    @Override
    public void dispatchCommand(String command) {
        ProxyServer.getInstance().getPluginManager().dispatchCommand(ProxyServer.getInstance().getConsole(), command);
    }
}
