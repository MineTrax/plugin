package com.xinecraft.minetrax.bungee.utils;

import com.xinecraft.minetrax.common.utils.CommonSkinUtil;
import com.xinecraft.minetrax.common.utils.LoggingUtil;
import com.xinecraft.minetrax.common.utils.PlayerHandler;
import com.xinecraft.minetrax.bungee.MinetraxBungee;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.ProxyServer;
import net.skinsrestorer.api.SkinsRestorer;
import net.skinsrestorer.api.SkinsRestorerProvider;
import net.skinsrestorer.api.exception.DataRequestException;
import net.skinsrestorer.api.property.SkinIdentifier;
import net.skinsrestorer.api.property.SkinProperty;
import net.skinsrestorer.api.storage.CacheStorage;
import net.skinsrestorer.api.storage.PlayerStorage;
import net.skinsrestorer.api.storage.SkinStorage;

import java.util.Optional;
import java.util.UUID;

public class BungeeSkinUtil {
    private static final CommonSkinUtil commonSkinUtil = new CommonSkinUtil(SkinsRestorerProvider.get(), new BungeePlayerHandler());

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
        SkinsRestorer skinsRestorerAPI = MinetraxBungee.getPlugin().getSkinsRestorerApi();
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
        SkinsRestorer skinsRestorerApi = MinetraxBungee.getPlugin().getSkinsRestorerApi();
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


