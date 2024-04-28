package com.xinecraft.minetrax.bungee.schedulers;

import com.xinecraft.minetrax.bungee.MinetraxBungee;
import com.xinecraft.minetrax.common.interfaces.schedulers.CommonScheduler;

public class BungeeScheduler implements CommonScheduler {
    private final MinetraxBungee plugin;
    public BungeeScheduler(MinetraxBungee plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run(Runnable runnable) {
        plugin.getProxy().getScheduler().runAsync(plugin, runnable);
    }

    @Override
    public void runAsync(Runnable runnable) {
        plugin.getProxy().getScheduler().runAsync(plugin, runnable);
    }
}
