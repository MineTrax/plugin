package com.xinecraft.threads.webquery;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.xinecraft.Minetrax;
import com.xinecraft.threads.data.QueryRequestData;
import com.xinecraft.threads.webquery.handlers.PlayerSkinHandler;
import com.xinecraft.utils.CryptoUtil;
import com.xinecraft.utils.LoggingUtil;
import net.skinsrestorer.api.PropertyUtils;
import net.skinsrestorer.api.SkinsRestorer;
import net.skinsrestorer.api.property.SkinProperty;
import net.skinsrestorer.api.storage.PlayerStorage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * FORMAT: /SECRET/TYPE/OPTIONAL_EXTRA_PARAM
 * <p>
 * TYPE:
 * 1. status
 * -> like a query, returns status of server, some metrics and list of players with UUID.
 * <p>
 * 2. user-say
 * -> Make a user say message
 * <p>
 * 3. command
 * -> Run a custom command
 * <p>
 * 4. broadcast
 * -> Broadcast something to the server
 */
public class WebQueryProtocol {
    public static String processInput(String theInput) {
        String theOutput = null;

        // Try to decrypt the input with api_secret return null if any issue
        String apiSecret = Minetrax.getPlugin().getApiSecret().substring(0, 32);
        String decryptedInput = CryptoUtil.getDecryptedString(apiSecret, theInput);
        if (decryptedInput == null || decryptedInput.isEmpty()) {
            return null;
        }

        // Parse the input
        Gson gson = new Gson();
        QueryRequestData queryRequestData = gson.fromJson(decryptedInput, QueryRequestData.class);

        // Verify if API key is correct return null if any issue
        if (!Minetrax.getPlugin().getApiKey().equals(queryRequestData.api_key)) {
            Minetrax.getPlugin().getLogger().warning("Error: API Key Mismatch");
            return null;
        }

        // Switch case to find what user want to do and handle it accordingly
        LoggingUtil.info("Handing Query of Type: " + queryRequestData.type);
        switch (queryRequestData.type) {
            case "status":
                theOutput = handleStatus();
                break;
            case "user-say":
                theOutput = handleUserSay(queryRequestData);
                break;
            case "broadcast":
                Bukkit.getServer().broadcastMessage(ChatColor.translateAlternateColorCodes('&', queryRequestData.params));
                theOutput = "ok";
                break;
            case "command":
                boolean isAllowOnlyWhitelistedCommandsFromWeb = Minetrax.getPlugin().getIsAllowOnlyWhitelistedCommandsFromWeb();
                List<String> whitelistedCommands = Minetrax.getPlugin().getWhitelistedCommandsFromWeb();
                if (isAllowOnlyWhitelistedCommandsFromWeb) {
                    // Check if params start with any of the whitelisted commands
                    boolean isWhitelisted = false;
                    for (String command : whitelistedCommands) {
                        if (queryRequestData.params.startsWith(command)) {
                            isWhitelisted = true;
                            break;
                        }
                    }
                    if (isWhitelisted) {
                        boolean status = handleCommand(queryRequestData.params);
                        if (status) theOutput = "ok";
                    }
                } else {
                    boolean status = handleCommand(queryRequestData.params);
                    if (status) theOutput = "ok";
                }
                break;
            case "get-player-groups":
                theOutput = getPlayerGroups(queryRequestData.params);
                break;
            case "set-player-skin":
                theOutput = handleSetPlayerSkin(queryRequestData.params);
                break;
            default:
                break;
        }

        // A return null in default case
        return theOutput;
    }

    @NotNull
    private static String handleUserSay(QueryRequestData queryRequestData) {
        String theOutput;
        String[] strings = queryRequestData.params.split("½½½½");
        String messageFormat = Minetrax.getPlugin().getWebMessageFormat();
        String senderName = strings[0];
        String message = String.join(" ", Arrays.copyOfRange(strings, 1, strings.length));
        messageFormat = messageFormat.replace("{USERNAME}", senderName);
        messageFormat = ChatColor.translateAlternateColorCodes('&', messageFormat);
        messageFormat = messageFormat.replace("{MESSAGE}", message);
        Bukkit.getServer().broadcastMessage(messageFormat);
        theOutput = "ok";
        return theOutput;
    }

    private static String getPlayerGroups(String uuid) {
        if (!Minetrax.getVaultPermission().hasGroupSupport()) {
            // System.out.println("No group support found");
            return CryptoUtil.getEncryptedString(Minetrax.getPlugin().getApiKey(), "{}");
        }

        UUID playerUuid = UUID.fromString(uuid);
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerUuid);

        //  System.out.println(offlinePlayer.getName());

        if (!offlinePlayer.hasPlayedBefore()) {
            // System.out.println("Offline player is null");
            return CryptoUtil.getEncryptedString(Minetrax.getPlugin().getApiKey(), "{}");
        }

        String primaryGroup = Minetrax.getVaultPermission().getPrimaryGroup(null, offlinePlayer);
        String[] allGroups = Minetrax.getVaultPermission().getPlayerGroups(null, offlinePlayer);

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("primary", primaryGroup);
        JsonArray jsonArray = new JsonArray();
        for (String g : allGroups) {
            jsonArray.add(g);
        }
        jsonObject.add("groups", jsonArray);

        Gson gson = new Gson();
        String jsonString = gson.toJson(jsonObject);

        // Encrypt
        jsonString = CryptoUtil.getEncryptedString(Minetrax.getPlugin().getApiKey(), jsonString);
        return jsonString;
    }

    private static boolean handleCommand(String command) {
        try {
            boolean success = Bukkit.getScheduler().callSyncMethod(Minetrax.getPlugin(), () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command)).get();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private static String handleStatus() {
        JsonArray jsonArray = new JsonArray();
        // Get list of online players
        List<Player> playerList = new ArrayList<>(Bukkit.getOnlinePlayers());
        for (Player player : playerList) {
            JsonObject playerJsonObject = new JsonObject();
            playerJsonObject.addProperty("username", player.getName());
            playerJsonObject.addProperty("display_name", player.getDisplayName());
            playerJsonObject.addProperty("id", player.getUniqueId().toString());
            playerJsonObject.addProperty("is_op", player.isOp());
            int playerPing;
            try {
                playerPing = player.getPing();
            } catch (NoSuchMethodError e) {
                playerPing = 0;
            }
            playerJsonObject.addProperty("ping", playerPing);
            playerJsonObject.addProperty("ip_address", Objects.requireNonNull(player.getAddress()).getHostString());

            // If SkinRestorer is enabled, then add skin data
            if (Minetrax.getPlugin().getHasSkinRestorer()) {
                SkinsRestorer skinsRestorerApi = Minetrax.getPlugin().getSkinsRestorerApi();
                PlayerStorage playerStorage = skinsRestorerApi.getPlayerStorage();
                try {
                    Optional<SkinProperty> skin = playerStorage.getSkinForPlayer(player.getUniqueId(), player.getName());
                    skin.ifPresent(skinProperty -> playerJsonObject.addProperty("skin_texture_id", PropertyUtils.getSkinTextureUrlStripped(skinProperty)));
                } catch (Exception e) {
                    LoggingUtil.info("[WebQuery -> status] Error getting skin for player: " + player.getName());
                    e.printStackTrace();
                }
            }

            jsonArray.add(playerJsonObject);
        }

        Gson gson = new Gson();
        String jsonString = gson.toJson(jsonArray);
        // Encrypt
        jsonString = CryptoUtil.getEncryptedString(Minetrax.getPlugin().getApiKey(), jsonString);
        return jsonString;
    }

    private static String handleSetPlayerSkin(String params) {
        String[] strings = params.split("½½½½");
        String playerUuid = strings[0];
        String commandType = strings[1];
        String value = null;
        if (strings.length >= 3) {
            value = strings[2];
        }
        String theOutput = null;

        LoggingUtil.info("Setting skin for player: " + playerUuid + " with type: " + commandType + " and value: " + value);
        // Eg:player_uuid½½½½type½½½½value
        // type can:
        // 1. url -> url value of a skin site like namemc.com or mineskin.org url
        // 2. username -> copy from username of a premium account
        // 3a upload:init -> custom skin texture value. Eg: set-player-skin½½½½player_uuid½½½½custom½½½½value
        // 3b upload -> custom skin signature value. Eg: set-player-skin½½½½player_uuid½½½½upload½½½½signature
        // 4. clear -> reset the skin to default Eg: set-player-skin½½½½player_uuid½½½½clear
        switch (commandType) {
            case "url":
            case "username":
                theOutput = PlayerSkinHandler.setPlayerSkinUsingUrlOrName(playerUuid, value);
                break;
            case "upload:init":
                theOutput = PlayerSkinHandler.initSetPlayerSkinUsingCustom(playerUuid, value);
                break;
            case "upload":
                theOutput = PlayerSkinHandler.setPlayerSkinUsingCustom(playerUuid, value);
                break;
            case "clear":
                theOutput = PlayerSkinHandler.clearPlayerSkin(playerUuid);
                break;
            default:
                break;
        }
        return theOutput;
    }
}
