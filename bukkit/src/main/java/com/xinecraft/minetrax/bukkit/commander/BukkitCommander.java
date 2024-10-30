package com.xinecraft.minetrax.bukkit.commander;

import com.xinecraft.minetrax.common.interfaces.commander.CommonCommander;
import org.bukkit.Bukkit;

public class BukkitCommander implements CommonCommander {
    @Override
    public void dispatchCommand(String command) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
    }
}
