package com.xinecraft.minetrax.bukkit.commander;

import com.xinecraft.minetrax.common.MinetraxCommon;
import com.xinecraft.minetrax.common.interfaces.commander.CommonCommander;
import org.bukkit.Bukkit;

public class BukkitCommander implements CommonCommander {
    @Override
    public void dispatchCommand(String command) {
        MinetraxCommon.getInstance().getScheduler().run(() -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command));
    }
}
