package com.xinecraft.listeners;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.xinecraft.Minetrax;
import com.xinecraft.data.PlayerData;
import com.xinecraft.data.PlayerSessionIntelData;
import com.xinecraft.utils.HttpUtil;
import com.xinecraft.utils.WhoisUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.*;

public class PlayerJoinLeaveListener implements Listener {
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Make playerDataList and add it to list.
        this.addPlayerToPlayerDataMapAndStartSession(event);

        // send chatlog to web
        this.postSendChatlog(event);

        // perform whois & add player to list of linkedPlayers hashmap
        this.broadcastWhoisForPlayer(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerLeave(PlayerQuitEvent event) {
        // send chatlog to web
        this.postSendChatlog(event);

        // remove player from playerDataList list
        this.removePlayerAndSessionFromDataMap(event);
    }

    private void postSendChatlog(PlayerEvent event) {
        if (!Minetrax.getPlugin().getIsChatLogEnabled()) {
            return;
        }

        Map<String, String> params = new HashMap<String, String>();
        params.put("api_key", Minetrax.getPlugin().getApiKey());
        params.put("api_secret", Minetrax.getPlugin().getApiSecret());
        if (event instanceof PlayerJoinEvent) {
            params.put("type", "player-join");
            params.put("chat", ((PlayerJoinEvent) event).getJoinMessage());
        } else {
            params.put("type", "player-leave");
            params.put("chat", ((PlayerQuitEvent) event).getQuitMessage());
        }
        params.put("causer_username", event.getPlayer().getName());
        params.put("causer_uuid", event.getPlayer().getUniqueId().toString());
        params.put("server_id", Minetrax.getPlugin().getApiServerId());
        // Run this async to not block the main thread
        Bukkit.getScheduler().runTaskAsynchronously(Minetrax.getPlugin(), new Runnable() {
            @Override
            public void run() {
                try {
                    HttpUtil.postForm(Minetrax.getPlugin().getApiHost() + "/api/v1/server/chat", params);
                } catch (Exception e) {
                    Bukkit.getLogger().warning(e.getMessage());
                }
            }
        });
    }

    private void broadcastWhoisForPlayer(Player player) {
        String username = player.getName();
        String ipAddress = Objects.requireNonNull(player.getAddress()).getHostString();
        String uuid = player.getUniqueId().toString();

        Boolean shouldBroadcastOnJoin = Minetrax.getPlugin().getIsWhoisOnPlayerJoinEnabled();
        WhoisUtil.forPlayer(username, uuid, ipAddress, shouldBroadcastOnJoin, true, null);
    }

    private void addPlayerToPlayerDataMapAndStartSession(PlayerJoinEvent event) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("api_key", Minetrax.getPlugin().getApiKey());
        params.put("api_secret", Minetrax.getPlugin().getApiSecret());
        params.put("username", event.getPlayer().getName());
        params.put("uuid", event.getPlayer().getUniqueId().toString());
        params.put("server_id", Minetrax.getPlugin().getApiServerId());
        // Run this async to not block the main thread
        Bukkit.getScheduler().runTaskAsynchronously(Minetrax.getPlugin(), new Runnable() {
            @Override
            public void run() {
                try {
                    String response = HttpUtil.postForm(Minetrax.getPlugin().getApiHost() + "/api/v1/player/data", params);
                    Gson gson = new GsonBuilder().serializeNulls().create();
                    PlayerData playerData = gson.fromJson(response, PlayerData.class);

                    PlayerSessionIntelData playerSessionIntelData = gson.fromJson(response, PlayerSessionIntelData.class);
                    playerSessionIntelData.session_uuid = UUID.randomUUID().toString();
                    playerSessionIntelData.session_started_at = new Date().getTime();
                    playerSessionIntelData.ip_address = event.getPlayer().getAddress() != null ? event.getPlayer().getAddress().getHostString() : "127.1.1.1";
                    playerSessionIntelData.display_name = ChatColor.stripColor(event.getPlayer().getDisplayName());
                    playerSessionIntelData.session_started_at = new Date().getTime();
                    playerSessionIntelData.is_op = event.getPlayer().isOp();
                    playerSessionIntelData.player_ping = event.getPlayer().getPing();
                    playerSessionIntelData.server_id = Minetrax.getPlugin().getApiServerId();

                    playerData.session_uuid = playerSessionIntelData.session_uuid;
                    Minetrax.getPlugin().playerSessionIntelDataMap.put(playerSessionIntelData.session_uuid, playerSessionIntelData);
                    Minetrax.getPlugin().playersDataMap.put(playerData.uuid, playerData);

                    String playerSessionDataJson = gson.toJson(playerSessionIntelData);
                    Bukkit.getLogger().info("--- STARTING SESSION FOR A PLAYER ---");
                    Bukkit.getLogger().info(playerSessionDataJson);
                    String sessionInitResponse = HttpUtil.postJsonWithAuth(Minetrax.getPlugin().getApiHost() + "/api/v1/intel/player/session-init", playerSessionDataJson);
                    System.out.println("Session Start Response: " + sessionInitResponse);
                } catch (Exception e) {
                    Bukkit.getLogger().warning(e.getMessage());
                }
            }
        });
    }

    private void removePlayerAndSessionFromDataMap(PlayerQuitEvent event) {
        Gson gson = new GsonBuilder().serializeNulls().create();
        String key = event.getPlayer().getUniqueId().toString();

        PlayerData playerData = Minetrax.getPlugin().playersDataMap.get(key);
        if (playerData != null) {
            System.out.println("REPORT FINAL SESSION END ON PLAYER QUIT");
            PlayerSessionIntelData leftPlayerSessionIntelData = Minetrax.getPlugin().playerSessionIntelDataMap.get(playerData.session_uuid);
            leftPlayerSessionIntelData.session_ended_at = new Date().getTime();
            String leftPlayerSessionDataJson = gson.toJson(leftPlayerSessionIntelData);
            // REMOVE SESSION TO MAP
            Minetrax.getPlugin().playerSessionIntelDataMap.remove(playerData.session_uuid);
            Bukkit.getScheduler().runTaskAsynchronously(Minetrax.getPlugin(), new Runnable() {
                @Override
                public void run() {
                    try {
                        System.out.println("Final Session Data: " + leftPlayerSessionDataJson);
                        HttpUtil.postJsonWithAuth(Minetrax.getPlugin().getApiHost() + "/api/v1/intel/player/report/event", leftPlayerSessionDataJson);
                    } catch (Exception e) {
                        Bukkit.getLogger().warning(e.getMessage());
                    }
                }
            });
        }

        // Remove player data map.
        Minetrax.getPlugin().playersDataMap.remove(key);
    }
}
