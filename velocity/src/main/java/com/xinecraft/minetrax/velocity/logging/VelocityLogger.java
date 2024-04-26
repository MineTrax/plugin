package com.xinecraft.minetrax.velocity.logging;

import com.xinecraft.minetrax.common.interfaces.logging.CommonLogger;
import com.xinecraft.minetrax.velocity.MinetraxVelocity;

public class VelocityLogger implements CommonLogger {
    private final MinetraxVelocity plugin;

    public VelocityLogger(MinetraxVelocity plugin) {
        this.plugin = plugin;
    }

    @Override
    public void log(String message) {
        plugin.getLogger().info(message);
    }

    @Override
    public void info(String message) {
        plugin.getLogger().info(message);
    }

    @Override
    public void warning(String message) {
        plugin.getLogger().warn(message);
    }

    @Override
    public void error(String message) {
        plugin.getLogger().error(message);
    }
}
