package com.xinecraft.minetrax.bungee.utils;

import com.xinecraft.minetrax.bungee.MinetraxBungee;
import com.xinecraft.minetrax.common.MinetraxCommon;
import com.xinecraft.minetrax.common.utils.LoggingUtil;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.skinsrestorer.api.SkinsRestorer;
import net.skinsrestorer.api.SkinsRestorerProvider;
import net.skinsrestorer.api.exception.DataRequestException;
import net.skinsrestorer.api.exception.MineSkinException;
import net.skinsrestorer.api.property.InputDataResult;
import net.skinsrestorer.api.property.SkinIdentifier;
import net.skinsrestorer.api.property.SkinProperty;
import net.skinsrestorer.api.storage.CacheStorage;
import net.skinsrestorer.api.storage.PlayerStorage;
import net.skinsrestorer.api.storage.SkinStorage;

import java.util.Optional;
import java.util.UUID;

public class SkinUtil {
    public static void setPlayerSkinUsingUrlOrName(String playerUuid, String value) throws MineSkinException, DataRequestException {
        SkinsRestorer skinsRestorerApi = MinetraxCommon.getInstance().getSkinsRestorerApi();
        SkinStorage skinStorage = skinsRestorerApi.getSkinStorage();
        PlayerStorage playerStorage = skinsRestorerApi.getPlayerStorage();
        Optional<InputDataResult> result = skinStorage.findOrCreateSkinData(value);
        if (result.isEmpty()) {
            return;
        }

        // Assign the skin to player.
        playerStorage.setSkinIdOfPlayer(UUID.fromString(playerUuid), result.get().getIdentifier());

        // Instantly apply skin to the player without requiring the player to rejoin, if online
        ProxiedPlayer player = ProxyServer.getInstance().getPlayer(UUID.fromString(playerUuid));
        if (player != null) {
            skinsRestorerApi.getSkinApplier(ProxiedPlayer.class).applySkin(player);
        }
    }

    public static void setPlayerSkinUsingCustom(String playerUuid, String value) throws DataRequestException {
        SkinsRestorer skinsRestorerApi = MinetraxCommon.getInstance().getSkinsRestorerApi();
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
        ProxiedPlayer player = ProxyServer.getInstance().getPlayer(UUID.fromString(playerUuid));
        if (player != null) {
            skinsRestorerApi.getSkinApplier(ProxiedPlayer.class).applySkin(player);
        }
    }

    public static void clearPlayerSkin(String playerUuid) throws DataRequestException {
        SkinsRestorer skinsRestorerApi = MinetraxCommon.getInstance().getSkinsRestorerApi();
        PlayerStorage playerStorage = skinsRestorerApi.getPlayerStorage();
        playerStorage.removeSkinIdOfPlayer(UUID.fromString(playerUuid));

        // Instantly apply skin to the player without requiring the player to rejoin, if online
        ProxiedPlayer player = ProxyServer.getInstance().getPlayer(UUID.fromString(playerUuid));
        if (player != null) {
            skinsRestorerApi.getSkinApplier(ProxiedPlayer.class).applySkin(player);
        }
    }

    public static SkinProperty getSkinForPlayer(UUID playerUuid, String playerName) {
        SkinsRestorer skinsRestorerAPI = MinetraxCommon.getInstance().getSkinsRestorerApi();
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

    public static SkinProperty getSkinOfPlayerFromCache(UUID playerUuid, String playerName) {
        SkinsRestorer skinsRestorerApi = MinetraxCommon.getInstance().getSkinsRestorerApi();
        PlayerStorage playerStorage = skinsRestorerApi.getPlayerStorage();
        SkinStorage skinStorage = skinsRestorerApi.getSkinStorage();
        CacheStorage cacheStorage = skinsRestorerApi.getCacheStorage();
        try {
            SkinIdentifier skinIdentifier;
            Optional<SkinIdentifier> tempIdentifier = playerStorage.getSkinIdOfPlayer(playerUuid);
            UUID cacheUuid = cacheStorage.getUUID(playerName, true).orElseGet(() -> playerUuid);
            skinIdentifier = tempIdentifier.orElseGet(() -> SkinIdentifier.ofPlayer(cacheUuid));
            Optional<SkinProperty> skin = skinStorage.getSkinDataByIdentifier(skinIdentifier);
            if (skin.isPresent()) {
                return skin.get();
            }
        } catch (Exception e) {
            LoggingUtil.debug("[SkinUtil] Error getting cached skin for player: " + playerName + " : " + e.getMessage());
        }
        return null;
    }
}
