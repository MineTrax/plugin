package com.xinecraft.minetrax.velocity.commander;

import com.xinecraft.minetrax.common.interfaces.commander.CommonCommander;
import com.xinecraft.minetrax.velocity.MinetraxVelocity;

public class VelocityCommander implements CommonCommander {
    private final MinetraxVelocity plugin;

    public VelocityCommander(MinetraxVelocity plugin) {
        this.plugin = plugin;
    }

    @Override
    public void dispatchCommand(String command) {
        this.plugin.getProxyServer().getCommandManager().executeAsync(this.plugin.getProxyServer().getConsoleCommandSource(), command);
    }
}
