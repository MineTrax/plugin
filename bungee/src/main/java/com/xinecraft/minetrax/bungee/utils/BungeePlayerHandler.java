package com.xinecraft.minetrax.bungee.utils;

import com.xinecraft.minetrax.bungee.MinetraxBungee;
import com.xinecraft.minetrax.common.utils.PlayerHandler;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.skinsrestorer.api.SkinsRestorer;
import net.skinsrestorer.api.exception.DataRequestException;

import java.util.Optional;
import java.util.UUID;

public class BungeePlayerHandler implements PlayerHandler {
    private final SkinsRestorer skinsRestorerApi;

    public BungeePlayerHandler() {
        this.skinsRestorerApi = MinetraxBungee.getPlugin().getSkinsRestorerApi();
    }

    @Override
    public Optional<Object> getPlayer(UUID playerUuid) {
        return Optional.ofNullable(ProxyServer.getInstance().getPlayer(playerUuid));
    }

    @Override
    public void applySkin(Object player) {
        if (player instanceof ProxiedPlayer) {
            try {
                skinsRestorerApi.getSkinApplier(ProxiedPlayer.class).applySkin((ProxiedPlayer) player);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


}
