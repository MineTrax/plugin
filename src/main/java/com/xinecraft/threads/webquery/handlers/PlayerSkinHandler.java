package com.xinecraft.threads.webquery.handlers;

import com.xinecraft.Minetrax;
import com.xinecraft.utils.LoggingUtil;
import net.skinsrestorer.api.SkinsRestorer;
import net.skinsrestorer.api.property.InputDataResult;
import net.skinsrestorer.api.property.SkinProperty;
import net.skinsrestorer.api.storage.PlayerStorage;
import net.skinsrestorer.api.storage.SkinStorage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.Optional;
import java.util.UUID;

public class PlayerSkinHandler {
    public static String setPlayerSkinUsingUrlOrName(String playerUuid, String value) {
        try {
            SkinsRestorer skinsRestorerApi = Minetrax.getPlugin().getSkinsRestorerApi();
            SkinStorage skinStorage = skinsRestorerApi.getSkinStorage();
            PlayerStorage playerStorage = skinsRestorerApi.getPlayerStorage();
            Optional<InputDataResult> result = skinStorage.findOrCreateSkinData(value);
            if (!result.isPresent()) {
                return null;
            }

            // Assign the skin to player.
            playerStorage.setSkinIdOfPlayer(UUID.fromString(playerUuid), result.get().getIdentifier());

            // Instantly apply skin to the player without requiring the player to rejoin, if online
            Player player = Bukkit.getPlayer(UUID.fromString(playerUuid));
            if (player != null) {
                skinsRestorerApi.getSkinApplier(Player.class).applySkin(player);
            }

            return "ok";
        } catch (Exception e) {
            LoggingUtil.warning(e.getMessage());
            return null;
        }
    }

    public static String initSetPlayerSkinUsingCustom(String playerUuid, String value) {
        // Store the skin data in a hashmap for given player uuid key
        try {
            Minetrax.getPlugin().skinRestorerValueCache.put(playerUuid, value);
            return "ok";
        } catch (Exception e) {
            LoggingUtil.warning(e.getMessage());
            return null;
        }
    }

    public static String setPlayerSkinUsingCustom(String playerUuid, String skinSignature) {
        try {
            SkinsRestorer skinsRestorerApi = Minetrax.getPlugin().getSkinsRestorerApi();
            SkinStorage skinStorage = skinsRestorerApi.getSkinStorage();
            PlayerStorage playerStorage = skinsRestorerApi.getPlayerStorage();

            // Find the skin value from cache
            String skinValue = Minetrax.getPlugin().skinRestorerValueCache.get(playerUuid);
            // Error if skin value is null
            if (skinValue == null) {
                return null;
            }

            skinStorage.setCustomSkinData(playerUuid, SkinProperty.of(skinValue, skinSignature));
            Optional<InputDataResult> result = skinStorage.findSkinData(playerUuid);
            if (!result.isPresent()) {
                return null;
            }

            // Assign the skin to player.
            playerStorage.setSkinIdOfPlayer(UUID.fromString(playerUuid), result.get().getIdentifier());

            // Instantly apply skin to the player without requiring the player to rejoin, if online
            Player player = Bukkit.getPlayer(UUID.fromString(playerUuid));
            if (player != null) {
                skinsRestorerApi.getSkinApplier(Player.class).applySkin(player);
            }

            // Delete the skin value from cache
            Minetrax.getPlugin().skinRestorerValueCache.remove(playerUuid);

            return "ok";
        } catch (Exception e) {
            LoggingUtil.warning(e.getMessage());
            return null;
        }
    }

    public static String clearPlayerSkin(String playerUuid) {
        try {
            SkinsRestorer skinsRestorerApi = Minetrax.getPlugin().getSkinsRestorerApi();
            PlayerStorage playerStorage = skinsRestorerApi.getPlayerStorage();
            playerStorage.removeSkinIdOfPlayer(UUID.fromString(playerUuid));

            // Instantly apply skin to the player without requiring the player to rejoin, if online
            Player player = Bukkit.getPlayer(UUID.fromString(playerUuid));
            if (player != null) {
                if (Minetrax.getPlugin().isSkinsRestorerInProxyMode) {
                    scheduleSendMessageToChannel(player, new byte[0]);
                } else {
                    skinsRestorerApi.getSkinApplier(Player.class).applySkin(player);
                }
            }

            return "ok";
        } catch (Exception e) {
            LoggingUtil.warning(e.getMessage());
            return null;
        }
    }


    private static void sendMessageToChannel(Player player, byte[] message) {
        try {
            System.out.println("Sending clearSkin message to " + player.getName());
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(b);
            out.writeUTF("clearSkin");
            out.writeUTF(player.getName());
            player.sendPluginMessage(Minetrax.getPlugin(), "sr:messagechannel", b.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void scheduleSendMessageToChannel(Player player, byte[] message) {
        Bukkit.getScheduler().runTaskAsynchronously(Minetrax.getPlugin(), () -> {
            sendMessageToChannel(player, message);
        });
    }
}
