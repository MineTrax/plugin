package com.xinecraft.minetrax.velocity.utils;

import com.velocitypowered.api.proxy.Player;
import com.xinecraft.minetrax.common.utils.CommonSkinUtil;
import com.xinecraft.minetrax.common.utils.LoggingUtil;
import com.xinecraft.minetrax.common.utils.PlayerHandler;
import com.xinecraft.minetrax.velocity.MinetraxVelocity;
import net.skinsrestorer.api.SkinsRestorer;
import net.skinsrestorer.api.SkinsRestorerProvider;
import net.skinsrestorer.api.property.SkinIdentifier;
import net.skinsrestorer.api.property.SkinProperty;
import net.skinsrestorer.api.storage.CacheStorage;
import net.skinsrestorer.api.storage.PlayerStorage;
import net.skinsrestorer.api.storage.SkinStorage;

import java.util.Optional;
import java.util.UUID;

public class VelocitySkinUtil {
    private static final CommonSkinUtil commonSkinUtil = new CommonSkinUtil(SkinsRestorerProvider.get(), new VelocityPlayerHandler());

    public static void setPlayerSkinUsingUrlOrName(String playerUuid, String value) {
        commonSkinUtil.setPlayerSkinUsingUrlOrName(playerUuid, value);
    }

    public static void setPlayerSkinUsingCustom(String playerUuid, String value) {
        commonSkinUtil.setPlayerSkinUsingCustom(playerUuid, value);
    }

    public static void clearPlayerSkin(String playerUuid) {
        commonSkinUtil.clearPlayerSkin(playerUuid);
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

    public static SkinProperty getSkinOfPlayerFromCache(UUID playerUuid, String playerName) {
        SkinsRestorer skinsRestorerApi = SkinsRestorerProvider.get();
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


