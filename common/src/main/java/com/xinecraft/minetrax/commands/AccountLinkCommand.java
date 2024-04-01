package com.xinecraft.minetrax.commands;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.xinecraft.minetrax.Minetrax;
import com.xinecraft.minetrax.utils.HttpUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class AccountLinkCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {

        if (!(commandSender instanceof Player)) {
            Minetrax.getPlugin().getLogger().info("Error: Only players can execute that command.");
            return false;
        }

        Player player = (Player) commandSender;

        // Send Init message
        for (String line : Minetrax.getPlugin().getPlayerLinkInitMessage()) {
            line = line.replace("{WEB_URL}", Minetrax.getPlugin().getApiHost());
            line = ChatColor.translateAlternateColorCodes('&', line);
            player.sendMessage(line);
        }

        // Hit the API
        Map<String, String> params = new HashMap<String, String>();
        params.put("api_key", Minetrax.getPlugin().getApiKey());
        params.put("api_secret", Minetrax.getPlugin().getApiSecret());
        params.put("uuid", player.getUniqueId().toString());
        params.put("server_id", Minetrax.getPlugin().getApiServerId());
        // Run this async to not block the main thread
        Bukkit.getScheduler().runTaskAsynchronously(Minetrax.getPlugin(), new Runnable() {
            @Override
            public void run() {
                try {
                    String response = HttpUtil.postForm(Minetrax.getPlugin().getApiHost() + "/api/v1/account-link/init", params);

                    // Parses the response
                    JsonObject responseObj = new JsonParser().parse(response).getAsJsonObject();

                    // Check Response to see what web say if user is linked or not.
                    if (responseObj.get("status").getAsString().equals("error")) {
                        String errorType = responseObj.get("type").getAsString();

                        // If player not found
                        if (errorType.equals("not-found")) {
                            for (String line : Minetrax.getPlugin().getPlayerLinkNotFoundMessage()) {
                                line = line.replace("{WEB_URL}", Minetrax.getPlugin().getApiHost());
                                line = ChatColor.translateAlternateColorCodes('&', line);
                                player.sendMessage(line);
                            }
                        }

                        // If already linked
                        else if (errorType.equals("player-already-linked")) {
                            for (String line : Minetrax.getPlugin().getPlayerLinkAlreadyLinkedMessage()) {
                                line = line.replace("{WEB_URL}", Minetrax.getPlugin().getApiHost());
                                line = ChatColor.translateAlternateColorCodes('&', line);
                                player.sendMessage(line);
                            }
                        }

                        // Unknown error
                        else {
                            for (String line : Minetrax.getPlugin().getPlayerLinkUnknownErrorMessage()) {
                                line = line.replace("{WEB_URL}", Minetrax.getPlugin().getApiHost());
                                line = ChatColor.translateAlternateColorCodes('&', line);
                                player.sendMessage(line);
                            }
                        }
                        return;
                    }

                    // Return the URL to user to click
                    if (responseObj.get("status").getAsString().equals("success")) {
                        String url = responseObj.get("data").getAsString();
                        if (Minetrax.getPlugin().getIsShortenAccountLinkUrl()) {
                            url = HttpUtil.shortenUrl(url);
                        }
                        for (String line : Minetrax.getPlugin().getPlayerLinkFinalActionMessage()) {
                            line = line.replace("{WEB_URL}", Minetrax.getPlugin().getApiHost());
                            line = line.replace("{LINK_URL}", url);
                            line = ChatColor.translateAlternateColorCodes('&', line);
                            player.sendMessage(line);
                        }
                    }
                } catch (Exception e) {
                    // Tell user something error happen
                    for (String line : Minetrax.getPlugin().getPlayerLinkUnknownErrorMessage()) {
                        line = line.replace("{WEB_URL}", Minetrax.getPlugin().getApiHost());
                        line = ChatColor.translateAlternateColorCodes('&', line);
                        player.sendMessage(line);
                    }
                }
            }
        });

        return false;
    }
}
