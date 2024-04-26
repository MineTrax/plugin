package com.xinecraft.minetrax.bukkit.listeners;

import com.google.gson.Gson;
import com.viaversion.viaversion.api.Via;
import com.xinecraft.minetrax.bukkit.MinetraxBukkit;
import com.xinecraft.minetrax.common.data.PlayerData;
import com.xinecraft.minetrax.common.data.PlayerSessionIntelData;
import com.xinecraft.minetrax.common.data.PlayerWorldStatsIntelData;
import com.xinecraft.minetrax.bukkit.utils.HttpUtil;
import com.xinecraft.minetrax.bukkit.utils.LoggingUtil;
import com.xinecraft.minetrax.bukkit.utils.VersionUtil;
import com.xinecraft.minetrax.bukkit.utils.WhoisUtil;
import net.skinsrestorer.api.PropertyUtils;
import net.skinsrestorer.api.SkinsRestorer;
import net.skinsrestorer.api.SkinsRestorerProvider;
import net.skinsrestorer.api.property.SkinProperty;
import net.skinsrestorer.api.storage.PlayerStorage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
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

        Player p = event.getPlayer();

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

        // Remove from joinAddressCache
        MinetraxBukkit.getPlugin().joinAddressCache.remove(event.getPlayer().getUniqueId().toString());
    }

    private void postSendChatlog(PlayerEvent event) {
        if (!MinetraxBukkit.getPlugin().getIsChatLogEnabled()) {
            return;
        }

        Map<String, String> params = new HashMap<>();
        params.put("api_key", MinetraxBukkit.getPlugin().getApiKey());
        params.put("api_secret", MinetraxBukkit.getPlugin().getApiSecret());
        if (event instanceof PlayerJoinEvent) {
            params.put("type", "player-join");
            params.put("chat", ((PlayerJoinEvent) event).getJoinMessage());
        } else {
            params.put("type", "player-leave");
            params.put("chat", ((PlayerQuitEvent) event).getQuitMessage());
        }
        params.put("causer_username", event.getPlayer().getName());
        params.put("causer_uuid", event.getPlayer().getUniqueId().toString());
        params.put("server_id", MinetraxBukkit.getPlugin().getApiServerId());
        // Run this async to not block the main thread
        Bukkit.getScheduler().runTaskAsynchronously(MinetraxBukkit.getPlugin(), new Runnable() {
            @Override
            public void run() {
                try {
                    HttpUtil.postForm(MinetraxBukkit.getPlugin().getApiHost() + "/api/v1/server/chat", params);
                } catch (Exception e) {
                    MinetraxBukkit.getPlugin().getLogger().warning(e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    private void broadcastWhoisForPlayer(Player player) {
        String username = player.getName();
        String ipAddress = Objects.requireNonNull(player.getAddress()).getHostString();
        String uuid = player.getUniqueId().toString();

        Boolean shouldBroadcastOnJoin = MinetraxBukkit.getPlugin().getIsWhoisOnPlayerJoinEnabled();
        WhoisUtil.forPlayer(username, uuid, ipAddress, shouldBroadcastOnJoin, true, null);
    }

    private void addPlayerToPlayerDataMapAndStartSession(PlayerJoinEvent event) {
        Map<String, String> params = new HashMap<>();
        params.put("api_key", MinetraxBukkit.getPlugin().getApiKey());
        params.put("api_secret", MinetraxBukkit.getPlugin().getApiSecret());
        params.put("username", event.getPlayer().getName());
        params.put("uuid", event.getPlayer().getUniqueId().toString());
        params.put("server_id", MinetraxBukkit.getPlugin().getApiServerId());
        // Run this async to not block the main thread
        Bukkit.getScheduler().runTaskAsynchronously(MinetraxBukkit.getPlugin(), new Runnable() {
            @Override
            public void run() {
                try {
                    String response = HttpUtil.postForm(MinetraxBukkit.getPlugin().getApiHost() + "/api/v1/player/data", params);
                    Gson gson = MinetraxBukkit.getPlugin().getGson();
                    PlayerData playerData = gson.fromJson(response, PlayerData.class);
                    playerData.last_active_timestamp = System.currentTimeMillis();

                    PlayerSessionIntelData playerSessionIntelData = gson.fromJson(response, PlayerSessionIntelData.class);
                    playerSessionIntelData.session_uuid = UUID.randomUUID().toString();
                    playerSessionIntelData.session_started_at = new Date().getTime();
                    playerSessionIntelData.ip_address = event.getPlayer().getAddress() != null ? event.getPlayer().getAddress().getHostString() : "127.1.1.1";
                    playerSessionIntelData.display_name = ChatColor.stripColor(event.getPlayer().getDisplayName());
                    playerSessionIntelData.session_started_at = new Date().getTime();
                    playerSessionIntelData.is_op = event.getPlayer().isOp();
                    try {
                        playerSessionIntelData.join_address = MinetraxBukkit.getPlugin().joinAddressCache.get(event.getPlayer().getUniqueId().toString());
                    } catch (Exception e) {
                        playerSessionIntelData.join_address = null;
                    }

                    int playerPing;
                    try {
                        playerPing = event.getPlayer().getPing();
                    } catch (NoSuchMethodError e) {
                        playerPing = 0;
                    }
                    playerSessionIntelData.player_ping = playerPing;

                    if (MinetraxBukkit.getPlugin().hasViaVersion) {
                        int playerProtocolVersion = Via.getAPI().getPlayerVersion(event.getPlayer().getUniqueId());
                        playerSessionIntelData.minecraft_version = VersionUtil.getMinecraftVersionFromProtoId(playerProtocolVersion);
                    }
                    if (MinetraxBukkit.getPlugin().hasSkinRestorer) {
                        updateSkinDataInPlayerIntel(playerSessionIntelData, event.getPlayer());
                    }

                    playerSessionIntelData.server_id = MinetraxBukkit.getPlugin().getApiServerId();
                    // Init world stats hashmap for each world
                    playerSessionIntelData.players_world_stat_intel = new HashMap<>();
                    for (World world : MinetraxBukkit.getPlugin().getServer().getWorlds()) {
                        playerSessionIntelData.players_world_stat_intel.put(world.getName(), new PlayerWorldStatsIntelData(world.getName()));
                    }

                    playerData.session_uuid = playerSessionIntelData.session_uuid;
                    MinetraxBukkit.getPlugin().playerSessionIntelDataMap.put(playerSessionIntelData.session_uuid, playerSessionIntelData);
                    MinetraxBukkit.getPlugin().playersDataMap.put(playerData.uuid, playerData);

                    String playerSessionDataJson = gson.toJson(playerSessionIntelData);
                    LoggingUtil.info("--- STARTING SESSION FOR A PLAYER ---");
                    LoggingUtil.info(playerSessionDataJson);
                    String sessionInitResponse = HttpUtil.postJsonWithAuth(MinetraxBukkit.getPlugin().getApiHost() + "/api/v1/intel/player/session-init", playerSessionDataJson);
                    LoggingUtil.info("Session Start Response: " + sessionInitResponse);
                } catch (Exception e) {
                    MinetraxBukkit.getPlugin().getLogger().warning(e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    private void updateSkinDataInPlayerIntel(PlayerSessionIntelData playerSessionIntelData, Player player) {
        SkinsRestorer skinsRestorerAPI = SkinsRestorerProvider.get();
        PlayerStorage playerStorage = skinsRestorerAPI.getPlayerStorage();
        try {
            Optional<SkinProperty> skin = playerStorage.getSkinForPlayer(player.getUniqueId(), player.getName());
            if (skin.isPresent()) {
                playerSessionIntelData.skin_property = MinetraxBukkit.getPlugin().getGson().toJson(skin.get());
                playerSessionIntelData.skin_texture_id = PropertyUtils.getSkinTextureUrlStripped(skin.get());
            }
        } catch (Exception e) {
            MinetraxBukkit.getPlugin().getLogger().warning(e.getMessage());
        }
    }

    private void removePlayerAndSessionFromDataMap(PlayerQuitEvent event) {
        Gson gson = MinetraxBukkit.getPlugin().getGson();
        String key = event.getPlayer().getUniqueId().toString();

        PlayerData playerData = MinetraxBukkit.getPlugin().playersDataMap.get(key);
        if (playerData != null) {
            LoggingUtil.info("REPORT FINAL SESSION END ON PLAYER QUIT");
            PlayerSessionIntelData leftPlayerSessionIntelData = MinetraxBukkit.getPlugin().playerSessionIntelDataMap.get(playerData.session_uuid);
            leftPlayerSessionIntelData.session_ended_at = new Date().getTime();

            // Get Vault Plugin Data.
            leftPlayerSessionIntelData.vault_balance = MinetraxBukkit.getVaultEconomy() != null ? MinetraxBukkit.getVaultEconomy().getBalance(event.getPlayer()) : 0;
            if (MinetraxBukkit.getVaultPermission() != null && MinetraxBukkit.getVaultPermission().hasGroupSupport()) {
                leftPlayerSessionIntelData.vault_groups = MinetraxBukkit.getVaultPermission().getPlayerGroups(event.getPlayer());
            }

            // Player Inventory
            if (MinetraxBukkit.getPlugin().getIsSendInventoryDataToPlayerIntel()) {
                leftPlayerSessionIntelData.inventory = gson.toJson(event.getPlayer().getInventory().getContents());
                leftPlayerSessionIntelData.ender_chest = gson.toJson(event.getPlayer().getEnderChest().getContents());
            }

            // Player World location
            leftPlayerSessionIntelData.world_location = gson.toJson(event.getPlayer().getLocation().serialize());
            leftPlayerSessionIntelData.world_name = event.getPlayer().getWorld().getName();

            String leftPlayerSessionDataJson = gson.toJson(leftPlayerSessionIntelData);
            // REMOVE SESSION TO MAP
            MinetraxBukkit.getPlugin().playerSessionIntelDataMap.remove(playerData.session_uuid);
            Bukkit.getScheduler().runTaskAsynchronously(MinetraxBukkit.getPlugin(), new Runnable() {
                @Override
                public void run() {
                    try {
                        LoggingUtil.info("Final Session Data: " + leftPlayerSessionDataJson);
                        HttpUtil.postJsonWithAuth(MinetraxBukkit.getPlugin().getApiHost() + "/api/v1/intel/player/report/event", leftPlayerSessionDataJson);
                    } catch (Exception e) {
                        MinetraxBukkit.getPlugin().getLogger().warning(e.getMessage());
                    }
                }
            });
        }

        // Remove player data map.
        MinetraxBukkit.getPlugin().playersDataMap.remove(key);
    }
}
