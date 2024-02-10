package com.xinecraft.listeners;

import com.google.gson.Gson;
import com.viaversion.viaversion.api.Via;
import com.xinecraft.Minetrax;
import com.xinecraft.data.PlayerData;
import com.xinecraft.data.PlayerSessionIntelData;
import com.xinecraft.data.PlayerWorldStatsIntelData;
import com.xinecraft.utils.HttpUtil;
import com.xinecraft.utils.LoggingUtil;
import com.xinecraft.utils.VersionUtil;
import com.xinecraft.utils.WhoisUtil;
import net.skinsrestorer.api.PropertyUtils;
import net.skinsrestorer.api.SkinsRestorer;
import net.skinsrestorer.api.SkinsRestorerProvider;
import net.skinsrestorer.api.property.SkinProperty;
import net.skinsrestorer.api.storage.PlayerStorage;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.util.Vector;

import java.util.*;

import static org.bukkit.FireworkEffect.Type.CREEPER;

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

        // fireworks effect when player joins
        if (!p.hasPlayedBefore() && Minetrax.getPlugin().getIsFireworkOnPlayerFirstJoin()) {
            spawnFireworks(p, Minetrax.getPlugin().getConfig().getInt(Minetrax.getPlugin().getFireworkSendAmount()));
        } else {
            if (Minetrax.getPlugin().getIsFireworkOnPlayerJoin()) {
                spawnFireworks(p, Minetrax.getPlugin().getConfig().getInt(Minetrax.getPlugin().getFireworkSendAmount()));
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerLeave(PlayerQuitEvent event) {
        // send chatlog to web
        this.postSendChatlog(event);

        // remove player from playerDataList list
        this.removePlayerAndSessionFromDataMap(event);

        // Remove from joinAddressCache
        Minetrax.getPlugin().joinAddressCache.remove(event.getPlayer().getUniqueId().toString());
    }

    private void postSendChatlog(PlayerEvent event) {
        if (!Minetrax.getPlugin().getIsChatLogEnabled()) {
            return;
        }

        Map<String, String> params = new HashMap<>();
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
                    Minetrax.getPlugin().getLogger().warning(e.getMessage());
                    e.printStackTrace();
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
        Map<String, String> params = new HashMap<>();
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
                    Gson gson = Minetrax.getPlugin().getGson();
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
                        playerSessionIntelData.join_address = Minetrax.getPlugin().joinAddressCache.get(event.getPlayer().getUniqueId().toString());
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

                    if (Minetrax.getPlugin().hasViaVersion) {
                        int playerProtocolVersion = Via.getAPI().getPlayerVersion(event.getPlayer().getUniqueId());
                        playerSessionIntelData.minecraft_version = VersionUtil.getMinecraftVersionFromProtoId(playerProtocolVersion);
                    }
                    if (Minetrax.getPlugin().hasSkinRestorer) {
                        updateSkinDataInPlayerIntel(playerSessionIntelData, event.getPlayer());
                    }

                    playerSessionIntelData.server_id = Minetrax.getPlugin().getApiServerId();
                    // Init world stats hashmap for each world
                    playerSessionIntelData.players_world_stat_intel = new HashMap<>();
                    for (World world : Minetrax.getPlugin().getServer().getWorlds()) {
                        playerSessionIntelData.players_world_stat_intel.put(world.getName(), new PlayerWorldStatsIntelData(world.getName()));
                    }

                    playerData.session_uuid = playerSessionIntelData.session_uuid;
                    Minetrax.getPlugin().playerSessionIntelDataMap.put(playerSessionIntelData.session_uuid, playerSessionIntelData);
                    Minetrax.getPlugin().playersDataMap.put(playerData.uuid, playerData);

                    String playerSessionDataJson = gson.toJson(playerSessionIntelData);
                    LoggingUtil.info("--- STARTING SESSION FOR A PLAYER ---");
                    LoggingUtil.info(playerSessionDataJson);
                    String sessionInitResponse = HttpUtil.postJsonWithAuth(Minetrax.getPlugin().getApiHost() + "/api/v1/intel/player/session-init", playerSessionDataJson);
                    LoggingUtil.info("Session Start Response: " + sessionInitResponse);
                } catch (Exception e) {
                    Minetrax.getPlugin().getLogger().warning(e.getMessage());
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
                playerSessionIntelData.skin_property = Minetrax.getPlugin().getGson().toJson(skin.get());
                playerSessionIntelData.skin_texture_id = PropertyUtils.getSkinTextureUrlStripped(skin.get());
            }
        } catch (Exception e) {
            Minetrax.getPlugin().getLogger().warning(e.getMessage());
        }
    }

    private void removePlayerAndSessionFromDataMap(PlayerQuitEvent event) {
        Gson gson = Minetrax.getPlugin().getGson();
        String key = event.getPlayer().getUniqueId().toString();

        PlayerData playerData = Minetrax.getPlugin().playersDataMap.get(key);
        if (playerData != null) {
            LoggingUtil.info("REPORT FINAL SESSION END ON PLAYER QUIT");
            PlayerSessionIntelData leftPlayerSessionIntelData = Minetrax.getPlugin().playerSessionIntelDataMap.get(playerData.session_uuid);
            leftPlayerSessionIntelData.session_ended_at = new Date().getTime();

            // Get Vault Plugin Data.
            leftPlayerSessionIntelData.vault_balance = Minetrax.getVaultEconomy() != null ? Minetrax.getVaultEconomy().getBalance(event.getPlayer()) : 0;
            if (Minetrax.getVaultPermission() != null && Minetrax.getVaultPermission().hasGroupSupport()) {
                leftPlayerSessionIntelData.vault_groups = Minetrax.getVaultPermission().getPlayerGroups(event.getPlayer());
            }

            // Player Inventory
            if (Minetrax.getPlugin().getIsSendInventoryDataToPlayerIntel()) {
                leftPlayerSessionIntelData.inventory = gson.toJson(event.getPlayer().getInventory().getContents());
                leftPlayerSessionIntelData.ender_chest = gson.toJson(event.getPlayer().getEnderChest().getContents());
            }

            // Player World location
            leftPlayerSessionIntelData.world_location = gson.toJson(event.getPlayer().getLocation().serialize());
            leftPlayerSessionIntelData.world_name = event.getPlayer().getWorld().getName();

            String leftPlayerSessionDataJson = gson.toJson(leftPlayerSessionIntelData);
            // REMOVE SESSION TO MAP
            Minetrax.getPlugin().playerSessionIntelDataMap.remove(playerData.session_uuid);
            Bukkit.getScheduler().runTaskAsynchronously(Minetrax.getPlugin(), new Runnable() {
                @Override
                public void run() {
                    try {
                        LoggingUtil.info("Final Session Data: " + leftPlayerSessionDataJson);
                        HttpUtil.postJsonWithAuth(Minetrax.getPlugin().getApiHost() + "/api/v1/intel/player/report/event", leftPlayerSessionDataJson);
                    } catch (Exception e) {
                        Minetrax.getPlugin().getLogger().warning(e.getMessage());
                    }
                }
            });
        }

        // Remove player data map.
        Minetrax.getPlugin().playersDataMap.remove(key);
    }

    private static void spawnFireworks(Player p, Integer amount) {
        int diameter = 10;

        Firework fw = p.getWorld().spawn(p.getLocation(), Firework.class);
        FireworkMeta fwm = fw.getFireworkMeta();
        FireworkEffect.Builder builder = FireworkEffect.builder();

        fwm.addEffect(builder.flicker(true).withColor(Color.BLUE).build());
        fwm.addEffect(builder.trail(true).build());
        fwm.addEffect(builder.withFade(Color.WHITE).build());
        fwm.addEffect(builder.with(CREEPER).build());
        fwm.setPower(2);
        fw.setFireworkMeta(fwm);

        for (int i = 0; i < amount; i++) {
            Location newLocation = p.getLocation().add(new Vector(Math.random() - 0.5, 0, Math.random() - 0.5).multiply(diameter));
            Firework fw2 = (Firework) Objects.requireNonNull(p.getLocation().getWorld()).spawnEntity(newLocation, EntityType.FIREWORK);
            fw2.setFireworkMeta(fwm);
        }
    }
}
