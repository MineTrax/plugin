package com.xinecraft.threads;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.xinecraft.Minetrax;
import com.xinecraft.threads.data.QueryRequestData;
import com.xinecraft.utils.CryptoUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * FORMAT: /SECRET/TYPE/OPTIONAL_EXTRA_PARAM
 * <p>
 * TYPE:
 * 1. status
 * -> like a query, returns status of server, some metrics and list of players with UUID.
 *
 * 2. user-say
 * -> Make a user say message
 *
 * 3. command
 * -> Run a custom command
 *
 * 4. broadcast
 * -> Broadcast something to the server
 */
public class WebQueryProtocol {
    public static String processInput(String theInput) {
        String theOutput = null;

        // Try to decrypt the input with api_secret return null if any issue
        String decryptedInput = CryptoUtil.getDecryptedString(Minetrax.getPlugin().getApiKey(), theInput);
        if (decryptedInput == null || decryptedInput.isEmpty()) {
            return null;
        }

        // Parse the input
        Gson gson = new Gson();
        QueryRequestData queryRequestData = gson.fromJson(decryptedInput, QueryRequestData.class);

        // Verify if API secret is correct return null if any issue
        if (!Minetrax.getPlugin().getApiSecret().equals(queryRequestData.secret)) {
            Minetrax.getPlugin().getLogger().warning("Error: Secret Mismatch");
            return null;
        }

        // Switch case to find what user want to do and handle it accordingly
        Minetrax.getPlugin().getLogger().info("Handing Query of Type: " + queryRequestData.type);
        switch (queryRequestData.type) {
            case "status":
                theOutput = handleStatus();
                break;
            case "user-say":
                String[] strings = queryRequestData.params.split("½½½½");
                String messageFormat = Minetrax.getPlugin().getWebMessageFormat();
                String senderName = strings[0];
                String message = String.join(" ", Arrays.copyOfRange(strings, 1, strings.length));
                messageFormat = messageFormat.replace("{USERNAME}", senderName);
                messageFormat = ChatColor.translateAlternateColorCodes('&', messageFormat);
                messageFormat = messageFormat.replace("{MESSAGE}", message);
                Bukkit.getServer().broadcastMessage(messageFormat);
                theOutput = "ok";
                break;
            case "broadcast":
                Bukkit.getServer().broadcastMessage(ChatColor.translateAlternateColorCodes('&', queryRequestData.params));
                theOutput = "ok";
                break;
            case "command":
                boolean status = handleCommand(queryRequestData.params);
                if (status) theOutput = "ok";
                break;
            case "get-player-groups":
                theOutput = getPlayerGroups(queryRequestData.params);
                break;
            default:
                break;
        }

        // A return null in default case
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
        for (String g: allGroups) {
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
        for( Player player: playerList)
        {
            JsonObject playerJsonObject = new JsonObject();
            playerJsonObject.addProperty("username", player.getName());
            playerJsonObject.addProperty("id", player.getUniqueId().toString());
            playerJsonObject.addProperty("is_op", player.isOp());
            playerJsonObject.addProperty("ping", player.getPing());
            playerJsonObject.addProperty("ip_address", Objects.requireNonNull(player.getAddress()).getHostString());

            jsonArray.add(playerJsonObject);
        }

        Gson gson = new Gson();
        String jsonString = gson.toJson(jsonArray);
        // Encrypt
        jsonString = CryptoUtil.getEncryptedString(Minetrax.getPlugin().getApiKey(), jsonString);
        return jsonString;
    }
}
