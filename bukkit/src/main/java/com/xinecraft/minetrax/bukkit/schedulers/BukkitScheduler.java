package com.xinecraft.minetrax.bukkit.schedulers;

import com.xinecraft.minetrax.bukkit.MinetraxBukkit;
import com.xinecraft.minetrax.common.interfaces.schedulers.CommonScheduler;
import org.bukkit.Bukkit;

public class BukkitScheduler implements CommonScheduler {
    private final MinetraxBukkit plugin;
    public BukkitScheduler(MinetraxBukkit plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run(Runnable runnable) {
        Bukkit.getScheduler().runTask(plugin, runnable);
    }

    @Override
    public void runAsync(Runnable runnable) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable);
    }
}
