package com.xinecraft.minetrax.bukkit.utils;

import com.xinecraft.minetrax.common.utils.PlayerHandler;
import com.xinecraft.minetrax.bukkit.MinetraxBukkit;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;

public class BukkitPlayerHandler implements PlayerHandler {
    @Override
    public Optional<Object> getPlayer(UUID playerUuid) {
        return Optional.ofNullable(Bukkit.getPlayer(playerUuid)).map(player -> (Object) player);
    }

    @SneakyThrows
    @Override
    public void applySkin(Object player) {
        if (player instanceof Player) {
            MinetraxBukkit.getPlugin().getSkinsRestorerApi().getSkinApplier(Player.class).applySkin((Player) player);
        }
    }
}
