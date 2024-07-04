package com.xinecraft.minetrax.bukkit.utils;

import com.xinecraft.minetrax.bukkit.MinetraxBukkit;
import com.xinecraft.minetrax.common.utils.CommonSkinUtil;
import com.xinecraft.minetrax.common.utils.LoggingUtil;
import com.xinecraft.minetrax.common.utils.PlayerHandler;
import net.skinsrestorer.api.PropertyUtils;
import net.skinsrestorer.api.SkinsRestorer;
import net.skinsrestorer.api.SkinsRestorerProvider;
import net.skinsrestorer.api.property.SkinIdentifier;
import net.skinsrestorer.api.property.SkinProperty;
import net.skinsrestorer.api.storage.CacheStorage;
import net.skinsrestorer.api.storage.PlayerStorage;
import net.skinsrestorer.api.storage.SkinStorage;

import java.util.Optional;
import java.util.UUID;

public class BukkitSkinUtil {
    private static final CommonSkinUtil commonSkinUtil = new CommonSkinUtil(SkinsRestorerProvider.get(), new BukkitPlayerHandler());

    public static void setPlayerSkinUsingUrlOrName(String playerUuid, String value) {
        commonSkinUtil.setPlayerSkinUsingUrlOrName(playerUuid, value);
    }

    public static void setPlayerSkinUsingCustom(String playerUuid, String value) {
        commonSkinUtil.setPlayerSkinUsingCustom(playerUuid, value);
    }

    public static void clearPlayerSkin(String playerUuid) {
        commonSkinUtil.clearPlayerSkin(playerUuid);
    }

    public static String getSkinTextureId(UUID playerUuid, String playerName) {
        try {
            if (MinetraxBukkit.getPlugin().getHasSkinsRestorerInProxyMode()) {
                // get from playerSkinCache (from Bungee)
                String[] skinArr = MinetraxBukkit.getPlugin().getPlayerSkinCache().get(playerUuid.toString());
                if (skinArr != null) {
                    return skinArr[1];
                }
            } else {
                // get from SkinsRestorer API
                SkinProperty skin = getSkinOfPlayerFromCache(playerUuid, playerName);
                if (skin != null) {
                    return PropertyUtils.getSkinTextureUrlStripped(skin);
                }
            }
        } catch (Exception e) {
            LoggingUtil.info("[SkinUtil -> getSkinTextureId] Error getting skin for player: " + e.getMessage());
        }
        return null;
    }
    public static SkinProperty getSkinOfPlayerFromCache(UUID playerUuid, String playerName) {
        SkinsRestorer skinsRestorerApi = MinetraxBukkit.getPlugin().getSkinsRestorerApi();
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
    public static SkinProperty getSkinForPlayer(UUID playerUuid, String playerName) {
        SkinsRestorer skinsRestorerAPI = MinetraxBukkit.getPlugin().getSkinsRestorerApi();
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
