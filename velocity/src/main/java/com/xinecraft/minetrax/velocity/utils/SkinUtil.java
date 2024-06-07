package com.xinecraft.minetrax.velocity.utils;

import com.velocitypowered.api.proxy.Player;
import com.xinecraft.minetrax.common.utils.LoggingUtil;
import com.xinecraft.minetrax.velocity.MinetraxVelocity;
import net.skinsrestorer.api.SkinsRestorer;
import net.skinsrestorer.api.SkinsRestorerProvider;
import net.skinsrestorer.api.exception.DataRequestException;
import net.skinsrestorer.api.exception.MineSkinException;
import net.skinsrestorer.api.property.InputDataResult;
import net.skinsrestorer.api.property.SkinProperty;
import net.skinsrestorer.api.storage.PlayerStorage;
import net.skinsrestorer.api.storage.SkinStorage;

import java.util.Optional;
import java.util.UUID;

public class SkinUtil {
    public static void setPlayerSkinUsingUrlOrName(String playerUuid, String value) throws MineSkinException, DataRequestException {
        SkinsRestorer skinsRestorerApi = SkinsRestorerProvider.get();
        SkinStorage skinStorage = skinsRestorerApi.getSkinStorage();
        PlayerStorage playerStorage = skinsRestorerApi.getPlayerStorage();
        Optional<InputDataResult> result = skinStorage.findOrCreateSkinData(value);
        if (result.isEmpty()) {
            return;
        }

        // Assign the skin to player.
        playerStorage.setSkinIdOfPlayer(UUID.fromString(playerUuid), result.get().getIdentifier());

        // Instantly apply skin to the player without requiring the player to rejoin, if online
        Optional<Player> player = MinetraxVelocity.getPlugin().getProxyServer().getPlayer(UUID.fromString(playerUuid));
        if (player.isPresent()) {
            skinsRestorerApi.getSkinApplier(Player.class).applySkin(player.get());
        }
    }

    public static void setPlayerSkinUsingCustom(String playerUuid, String value) throws DataRequestException {
        SkinsRestorer skinsRestorerApi = SkinsRestorerProvider.get();
        SkinStorage skinStorage = skinsRestorerApi.getSkinStorage();
        PlayerStorage playerStorage = skinsRestorerApi.getPlayerStorage();

        // Split the value into skin value and signature separated by :::
        String[] valueParts = value.split(":::");
        String skinValue = valueParts[0];
        String skinSignature = valueParts[1];

        skinStorage.setCustomSkinData(playerUuid, SkinProperty.of(skinValue, skinSignature));
        Optional<InputDataResult> result = skinStorage.findSkinData(playerUuid);
        if (result.isEmpty()) {
            return;
        }

        // Assign the skin to player.
        playerStorage.setSkinIdOfPlayer(UUID.fromString(playerUuid), result.get().getIdentifier());

        // Instantly apply skin to the player without requiring the player to rejoin, if online
        Optional<Player> player = MinetraxVelocity.getPlugin().getProxyServer().getPlayer(UUID.fromString(playerUuid));
        if (player.isPresent()) {
            skinsRestorerApi.getSkinApplier(Player.class).applySkin(player.get());
        }
    }

    public static void clearPlayerSkin(String playerUuid) throws DataRequestException {
        SkinsRestorer skinsRestorerApi = SkinsRestorerProvider.get();
        PlayerStorage playerStorage = skinsRestorerApi.getPlayerStorage();
        playerStorage.removeSkinIdOfPlayer(UUID.fromString(playerUuid));

        // Instantly apply skin to the player without requiring the player to rejoin, if online
        Optional<Player> player = MinetraxVelocity.getPlugin().getProxyServer().getPlayer(UUID.fromString(playerUuid));
        if (player.isPresent()) {
            skinsRestorerApi.getSkinApplier(Player.class).applySkin(player.get());
        }
    }

    public static SkinProperty getSkinForPlayer(UUID playerUuid, String playerName) {
        SkinsRestorer skinsRestorerAPI = SkinsRestorerProvider.get();
        PlayerStorage playerStorage = skinsRestorerAPI.getPlayerStorage();
        try {
            Optional<SkinProperty> skin = playerStorage.getSkinForPlayer(playerUuid, playerName);
            if (skin.isPresent()) {
                return skin.get();
            }
        } catch (Exception e) {
            LoggingUtil.warntrace(e);
        }
        return null;
    }
}
