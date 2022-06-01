package com.xinecraft.commands;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.xinecraft.Minetrax;
import com.xinecraft.utils.HttpUtil;
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
            Bukkit.getLogger().info("Error: Only players can execute that command.");
            return false;
        }

        Player player = (Player) commandSender;

        player.sendMessage(ChatColor.GRAY + "Please wait.. Account verification is instantiating.");
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
                            player.sendMessage(ChatColor.RED + "404! Player not in website database yet.");
                            player.sendMessage(ChatColor.YELLOW + "Please try again after an hour");
                        }

                        // If already linked
                        else if(errorType.equals("player-already-linked")) {
                            player.sendMessage(ChatColor.YELLOW + "Hey! This player is already linked. Plz visit " + ChatColor.UNDERLINE + Minetrax.getPlugin().getApiHost() + ChatColor.RESET + ChatColor.YELLOW + " to know more.");
                        }

                        // Unknown error
                        else {
                            player.sendMessage(ChatColor.RED + "Some unknown error occurred. Please try again after some time.");
                        }
                        return;
                    }

                    // Return the URL to user to click
                    if (responseObj.get("status").getAsString().equals("success")) {
                        String url = responseObj.get("data").getAsString();
                        player.sendMessage(ChatColor.GREEN + "Please open the below link to start your linking process");
                        player.sendMessage(url);
                    }
                } catch (Exception e) {
                    // Tell user something error happen
                    player.sendMessage(ChatColor.RED + "Some unknown error occurred. Please try again after some time.");
                }
            }
        });

        return false;
    }
}
