package com.xinecraft.minetrax.velocity.utils;

import com.velocitypowered.api.proxy.Player;
import com.xinecraft.minetrax.common.utils.PlayerHandler;
import com.xinecraft.minetrax.velocity.MinetraxVelocity;
import net.skinsrestorer.api.SkinsRestorer;
import net.skinsrestorer.api.SkinsRestorerProvider;

import java.util.Optional;
import java.util.UUID;

public class VelocityPlayerHandler implements PlayerHandler {
    private final SkinsRestorer skinsRestorerApi;

    public VelocityPlayerHandler() {
        this.skinsRestorerApi = SkinsRestorerProvider.get();
    }

    @Override
    public Optional<Object> getPlayer(UUID playerUuid) {
        return MinetraxVelocity.getPlugin().getProxyServer().getPlayer(playerUuid).map(player -> (Object) player);
    }

    @Override
    public void applySkin(Object player) {
        if (player instanceof Player) {
            try {
                skinsRestorerApi.getSkinApplier(Player.class).applySkin((Player) player);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }




}
