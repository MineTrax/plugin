package com.xinecraft.minetrax.velocity.schedulers;

import com.xinecraft.minetrax.common.interfaces.schedulers.CommonScheduler;
import com.xinecraft.minetrax.velocity.MinetraxVelocity;

public class VelocityScheduler implements CommonScheduler {
    private final MinetraxVelocity plugin;

    public VelocityScheduler(MinetraxVelocity plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run(Runnable runnable) {
        plugin.getServer().getScheduler().buildTask(plugin, runnable).schedule();
    }

    @Override
    public void runAsync(Runnable runnable) {
        plugin.getServer().getScheduler().buildTask(plugin, runnable).schedule();
    }
}
