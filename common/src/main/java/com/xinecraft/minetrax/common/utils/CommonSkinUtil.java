package com.xinecraft.minetrax.common.utils;

import net.skinsrestorer.api.PropertyUtils;
import net.skinsrestorer.api.SkinsRestorer;
import net.skinsrestorer.api.exception.DataRequestException;
import net.skinsrestorer.api.exception.MineSkinException;
import net.skinsrestorer.api.property.InputDataResult;
import net.skinsrestorer.api.property.SkinProperty;
import net.skinsrestorer.api.storage.PlayerStorage;
import net.skinsrestorer.api.storage.SkinStorage;

import java.util.Optional;
import java.util.UUID;

public class CommonSkinUtil {
    private final SkinsRestorer skinsRestorerApi;
    private final PlayerHandler playerHandler;

    public CommonSkinUtil(SkinsRestorer skinsRestorerApi, PlayerHandler playerHandler) {
        this.skinsRestorerApi = skinsRestorerApi;
        this.playerHandler = playerHandler;
    }


    public void setPlayerSkinUsingUrlOrName(String playerUuid, String value) {
        try {
            SkinStorage skinStorage = skinsRestorerApi.getSkinStorage();
            PlayerStorage playerStorage = skinsRestorerApi.getPlayerStorage();
            Optional<InputDataResult> result = skinStorage.findOrCreateSkinData(value);
            if (result.isEmpty()) {
                return;
            }

            playerStorage.setSkinIdOfPlayer(UUID.fromString(playerUuid), result.get().getIdentifier());

            Optional<Object> player = playerHandler.getPlayer(UUID.fromString(playerUuid));
            player.ifPresent(playerHandler::applySkin);
        } catch (MineSkinException | DataRequestException e) {
            e.printStackTrace();
        }
    }

    public void setPlayerSkinUsingCustom(String playerUuid, String value) {
        try {
            SkinStorage skinStorage = skinsRestorerApi.getSkinStorage();
            PlayerStorage playerStorage = skinsRestorerApi.getPlayerStorage();

            String[] valueParts = value.split(":::");
            String skinValue = valueParts[0];
            String skinSignature = valueParts[1];

            skinStorage.setCustomSkinData(playerUuid, SkinProperty.of(skinValue, skinSignature));
            Optional<InputDataResult> result = skinStorage.findSkinData(playerUuid);
            if (result.isEmpty()) {
                return;
            }

            playerStorage.setSkinIdOfPlayer(UUID.fromString(playerUuid), result.get().getIdentifier());

            Optional<Object> player = playerHandler.getPlayer(UUID.fromString(playerUuid));
            player.ifPresent(playerHandler::applySkin);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void clearPlayerSkin(String playerUuid) {
        PlayerStorage playerStorage = skinsRestorerApi.getPlayerStorage();
        playerStorage.removeSkinIdOfPlayer(UUID.fromString(playerUuid));

        Optional<Object> player = playerHandler.getPlayer(UUID.fromString(playerUuid));
        player.ifPresent(playerHandler::applySkin);
    }


    public SkinProperty getSkinForPlayer(UUID playerUuid, String playerName) {
        PlayerStorage playerStorage = skinsRestorerApi.getPlayerStorage();
        try {
            Optional<SkinProperty> skin = playerStorage.getSkinForPlayer(playerUuid, playerName);
            return skin.orElse(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }



}
