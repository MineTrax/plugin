package com.xinecraft.minetrax.bukkit.utils;

import com.xinecraft.minetrax.bukkit.MinetraxBukkit;
import com.xinecraft.minetrax.common.utils.LoggingUtil;
import net.skinsrestorer.api.PropertyUtils;
import net.skinsrestorer.api.SkinsRestorer;
import net.skinsrestorer.api.exception.DataRequestException;
import net.skinsrestorer.api.exception.MineSkinException;
import net.skinsrestorer.api.property.InputDataResult;
import net.skinsrestorer.api.property.SkinProperty;
import net.skinsrestorer.api.storage.PlayerStorage;
import net.skinsrestorer.api.storage.SkinStorage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;

public class SkinUtil {
    public static void setPlayerSkinUsingUrlOrName(String playerUuid, String value) throws MineSkinException, DataRequestException {
        SkinsRestorer skinsRestorerApi = MinetraxBukkit.getPlugin().getSkinsRestorerApi();
        SkinStorage skinStorage = skinsRestorerApi.getSkinStorage();
        PlayerStorage playerStorage = skinsRestorerApi.getPlayerStorage();
        Optional<InputDataResult> result = skinStorage.findOrCreateSkinData(value);
        if (result.isEmpty()) {
            return;
        }

        // Assign the skin to player.
        playerStorage.setSkinIdOfPlayer(UUID.fromString(playerUuid), result.get().getIdentifier());

        // Instantly apply skin to the player without requiring the player to rejoin, if online
        Player player = Bukkit.getPlayer(UUID.fromString(playerUuid));
        if (player != null) {
            skinsRestorerApi.getSkinApplier(Player.class).applySkin(player);
        }
    }

    public static void setPlayerSkinUsingCustom(String playerUuid, String value) throws DataRequestException {
        SkinsRestorer skinsRestorerApi = MinetraxBukkit.getPlugin().getSkinsRestorerApi();
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
        Player player = Bukkit.getPlayer(UUID.fromString(playerUuid));
        if (player != null) {
            skinsRestorerApi.getSkinApplier(Player.class).applySkin(player);
        }
    }

    public static void clearPlayerSkin(String playerUuid) throws DataRequestException {
        SkinsRestorer skinsRestorerApi = MinetraxBukkit.getPlugin().getSkinsRestorerApi();
        PlayerStorage playerStorage = skinsRestorerApi.getPlayerStorage();
        playerStorage.removeSkinIdOfPlayer(UUID.fromString(playerUuid));

        // Instantly apply skin to the player without requiring the player to rejoin, if online
        Player player = Bukkit.getPlayer(UUID.fromString(playerUuid));
        if (player != null) {
            skinsRestorerApi.getSkinApplier(Player.class).applySkin(player);
        }
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
                SkinProperty skin = SkinUtil.getSkinForPlayer(playerUuid, playerName);
                if (skin != null) {
                    return PropertyUtils.getSkinTextureUrlStripped(skin);
                }
            }
        } catch (Exception e) {
            LoggingUtil.info("[SkinUtil -> getSkinTextureId] Error getting skin for player: " + e.getMessage());
        }
        return null;
    }
}
