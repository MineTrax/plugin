package com.xinecraft.minetrax.bukkit.utils;

import com.google.gson.Gson;
import com.xinecraft.minetrax.bukkit.MinetraxBukkit;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WhoisUtil {
    public static void forPlayer(String username, String uuid, String ipAddress, Boolean shouldBroadcast, Boolean isFromJoinEvent, Player performedBy) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("api_key", MinetraxBukkit.getPlugin().getApiKey());
        params.put("api_secret", MinetraxBukkit.getPlugin().getApiSecret());
        params.put("username", username);
        if (ipAddress != null) {
            params.put("ip_address", ipAddress);
        }
        if (uuid != null) {
            params.put("uuid", uuid);
        }
        if (isFromJoinEvent) {
            params.put("only_exact_result", "true");
        }
        Bukkit.getScheduler().runTaskAsynchronously(MinetraxBukkit.getPlugin(), new Runnable() {
            @Override
            public void run() {
                try {
                    String response = HttpUtil.postForm(MinetraxBukkit.getPlugin().getApiHost() + "/api/v1/player/whois", params);

                    // Parse the response
                    Gson gson = new Gson();
                    WhoisResponseData whoisResponseData = gson.fromJson(response, WhoisResponseData.class);

                    // System.out.println("Data " + whoisResponseData.getData());
                    // System.out.println("Class " + whoisResponseData.getData().getClass());
                    // System.out.println("ClassName " + whoisResponseData.getData().getClass().getName());
                    // System.out.println("Players: " + whoisResponseData.getData().count);

                    if (isFromJoinEvent && !shouldBroadcast) {
                        return;
                    }

                    // Get country, state and city from geodata
                    String playerCountry = whoisResponseData.getData().geo != null && whoisResponseData.getData().geo.country != null ? whoisResponseData.getData().geo.country : "&r&oTerra Incognita&r";
                    String playerStateName = whoisResponseData.getData().geo != null && whoisResponseData.getData().geo.state_name != null ? whoisResponseData.getData().geo.state_name : "&r&oUnknown State&r";
                    String playerCity = whoisResponseData.getData().geo != null && whoisResponseData.getData().geo.city != null ? whoisResponseData.getData().geo.city : "&r&oUnknown City&r";

                    // If count is 0 and its not a join event then no player found message
                    if (whoisResponseData.getData().count <= 0 && !isFromJoinEvent) {
                        String noMatchFoundMessage = MinetraxBukkit.getPlugin().getWhoisNoMatchFoundMessage();
                        noMatchFoundMessage = ChatColor.translateAlternateColorCodes('&', noMatchFoundMessage);
                        Tell(performedBy, noMatchFoundMessage);
                    }

                    // If count is 0 but its a join event then tell geo without player info
                    else if (whoisResponseData.getData().count <= 0 && isFromJoinEvent) {
                        String geoString;
                        geoString = whoisResponseData.getData().geo.city != null ? whoisResponseData.getData().geo.city + ", " : "";
                        geoString += whoisResponseData.getData().geo.state_name != null ? whoisResponseData.getData().geo.state_name + ", " : "";
                        geoString += whoisResponseData.getData().geo.country != null ? whoisResponseData.getData().geo.country : "Terra Incognita";

                        for (String line: MinetraxBukkit.getPlugin().getWhoisPlayerOnFirstJoinMessage()) {
                            line = line.replace("{USERNAME}", username);
                            line = line.replace("{GEO}", geoString);
                            line = line.replace("{COUNTRY}", playerCountry);
                            line = line.replace("{STATE}", playerStateName);
                            line = line.replace("{CITY}", playerCity);
                            line = ChatColor.translateAlternateColorCodes('&', line);
                            Tell(performedBy, line);
                        }
                    }

                    // If count is 1 then show details
                    else if (whoisResponseData.getData().count == 1) {
                        WhoisResponseData.Player player = whoisResponseData.getData().players.get(0);

                        // Make {GEO} Data
                        String geoString;
                        if (whoisResponseData.getData().geo != null) {
                            geoString = whoisResponseData.getData().geo.city != null ? whoisResponseData.getData().geo.city + ", " : "";
                            geoString += whoisResponseData.getData().geo.state_name != null ? whoisResponseData.getData().geo.state_name + ", " : "";
                            geoString += whoisResponseData.getData().geo.country != null ? whoisResponseData.getData().geo.country : "Terra Incognita";
                        } else {
                            geoString = player.country;
                        }

                        List<String> whoisMessageStringList = isFromJoinEvent ? MinetraxBukkit.getPlugin().getWhoisPlayerOnJoinMessage() : MinetraxBukkit.getPlugin().getWhoisPlayerOnCommandMessage();
                        if (!isFromJoinEvent && performedBy != null && (performedBy.hasPermission(MinetraxBukkit.getPlugin().getWhoisAdminPermissionName()) || performedBy.isOp())) {
                            whoisMessageStringList = MinetraxBukkit.getPlugin().getWhoisPlayerOnAdminCommandMessage();
                        }

                        for (String line : whoisMessageStringList) {
                            line = line.replace("{COUNTRY}", playerCountry);
                            line = line.replace("{STATE}", playerStateName);
                            line = line.replace("{CITY}", playerCity);
                            line = line.replace("{USERNAME}", player.username != null ? player.username : "&r&oUnknown&r");
                            line = line.replace("{GEO}", geoString != null ? geoString : "&r&oUnknown&r");
                            line = line.replace("{POSITION}", player.position != null ? player.position : "&r&oNone&r");
                            line = line.replace("{RANK}", player.rank != null ? player.rank : "&r&oNone&r");
                            line = line.replace("{RATING}", player.rating != null ? player.rating : "&r&oNone&r");
                            line = line.replace("{SCORE}", player.total_score != null ? player.total_score : "0");
                            line = line.replace("{USER}", player.user != null ? player.user : "&r&oNone&r");
                            line = line.replace("{URL}", player.url != null ? player.url : "&r&oUnknown&r");
                            line = line.replace("{LAST_SEEN}", player.last_seen_at != null ? player.last_seen_at : "&r&oUnknown&r");
                            line = ChatColor.translateAlternateColorCodes('&', line);
                            Tell(performedBy, line);
                        }
                    }

                    // if count is more than 1 then show list
                    else if (whoisResponseData.getData().count > 1) {
                        String titleMessage = MinetraxBukkit.getPlugin().getWhoisMultiplePlayersTitleMessage().replace("{COUNT}", String.valueOf(whoisResponseData.getData().count));
                        titleMessage = ChatColor.translateAlternateColorCodes('&', titleMessage);
                        Tell(performedBy, titleMessage);
                        String listMessage = MinetraxBukkit.getPlugin().getWhoisMultiplePlayersListMessage();
                        for (WhoisResponseData.Player player: whoisResponseData.getData().players) {
                            String line = listMessage;
                            line = line.replace("{POSITION}", player.position);
                            line = line.replace("{USERNAME}", player.username);
                            line = ChatColor.translateAlternateColorCodes('&', line);
                            Tell(performedBy, line);
                        }
                    } else {
                        Tell(performedBy, ChatColor.RED + "Osho: WTF is going on here?");
                    }

                } catch (Exception e) {
                    MinetraxBukkit.getPlugin().getLogger().warning(e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    public static void Tell(Player player, String message) {
        if (player == null) {
            Bukkit.getServer().broadcastMessage(message);
        } else {
            player.sendMessage(message);
        }
    }
}
