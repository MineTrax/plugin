package com.xinecraft.minetrax.bukkit.listeners;

import com.google.gson.Gson;
import com.viaversion.viaversion.api.Via;
import com.xinecraft.minetrax.bukkit.MinetraxBukkit;
import com.xinecraft.minetrax.bukkit.utils.PlayerUtil;
import com.xinecraft.minetrax.bukkit.utils.SkinUtil;
import com.xinecraft.minetrax.common.utils.LoggingUtil;
import com.xinecraft.minetrax.common.utils.VersionUtil;
import com.xinecraft.minetrax.common.actions.FetchPlayerData;
import com.xinecraft.minetrax.common.actions.ReportPlayerIntel;
import com.xinecraft.minetrax.common.actions.ReportServerChat;
import com.xinecraft.minetrax.common.data.PlayerData;
import com.xinecraft.minetrax.common.data.PlayerSessionIntelData;
import com.xinecraft.minetrax.common.data.PlayerWorldStatsIntelData;
import com.xinecraft.minetrax.common.responses.GenericApiResponse;
import com.xinecraft.minetrax.common.utils.WhoisUtil;
import de.themoep.minedown.adventure.MineDown;
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

        // Only send whois if player is not vanished
        if (!PlayerUtil.isVanished(p)) {
            // send chatlog to web
            this.postSendChatlog(event);

            // perform whois & add player to list of linkedPlayers hashmap
            this.broadcastWhoisForPlayer(event.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerLeave(PlayerQuitEvent event) {
        // send chatlog to web
        this.postSendChatlog(event);

        // remove player from playerDataList list
        this.removePlayerAndSessionFromDataMap(event);

        // Remove from joinAddressCache
        MinetraxBukkit.getPlugin().joinAddressCache.remove(event.getPlayer().getUniqueId().toString());

        // Remove from playerSkinCache
        MinetraxBukkit.getPlugin().getPlayerSkinCache().remove(event.getPlayer().getUniqueId().toString());
    }

    private void postSendChatlog(PlayerEvent event) {
        if (!MinetraxBukkit.getPlugin().getIsChatLogEnabled()) {
            return;
        }

        String chatMessage = "";
        String chatType = "";
        if (event instanceof PlayerJoinEvent) {
            chatType = "player-join";
            chatMessage = ((PlayerJoinEvent) event).getJoinMessage();
        } else {
            chatType = "player-leave";
            chatMessage = ((PlayerQuitEvent) event).getQuitMessage();
        }
        ReportServerChat.reportAsync(
                chatType,
                chatMessage,
                event.getPlayer().getName(),
                event.getPlayer().getUniqueId().toString()
        );
    }

    private void broadcastWhoisForPlayer(Player player) {
        String username = player.getName();
        String ipAddress = Objects.requireNonNull(player.getAddress()).getHostString();
        String uuid = player.getUniqueId().toString();
        Boolean shouldBroadcastOnJoin = MinetraxBukkit.getPlugin().getIsWhoisOnPlayerJoinEnabled();

        Bukkit.getScheduler().runTaskAsynchronously(MinetraxBukkit.getPlugin(), () -> {
            List<String> sayList = WhoisUtil.forPlayerSync(
                    username,
                    uuid,
                    ipAddress,
                    shouldBroadcastOnJoin,
                    true,
                    null,
                    MinetraxBukkit.getPlugin().getWhoisNoMatchFoundMessage(),
                    MinetraxBukkit.getPlugin().getWhoisPlayerOnFirstJoinMessage(),
                    MinetraxBukkit.getPlugin().getWhoisPlayerOnJoinMessage(),
                    MinetraxBukkit.getPlugin().getWhoisPlayerOnCommandMessage(),
                    MinetraxBukkit.getPlugin().getWhoisPlayerOnAdminCommandMessage(),
                    MinetraxBukkit.getPlugin().getWhoisMultiplePlayersTitleMessage(),
                    MinetraxBukkit.getPlugin().getWhoisMultiplePlayersListMessage()
            );
            if (sayList != null) {
                for (String line : sayList) {
                    MinetraxBukkit.getPlugin().adventure().players().sendMessage(MineDown.parse(line));
                }
            }
        });
    }

    private void addPlayerToPlayerDataMapAndStartSession(PlayerJoinEvent event) {
        String userName = event.getPlayer().getName();
        String uuid = event.getPlayer().getUniqueId().toString();
        Bukkit.getScheduler().runTaskAsynchronously(MinetraxBukkit.getPlugin(), () -> {
            try {
                GenericApiResponse response = FetchPlayerData.getSync(userName, uuid);
                PlayerData playerData = MinetraxBukkit.getPlugin().getCommon().getGson().fromJson(response.getData(), PlayerData.class);
                playerData.last_active_timestamp = System.currentTimeMillis();

                PlayerSessionIntelData playerSessionIntelData = MinetraxBukkit.getPlugin().getCommon().getGson().fromJson(response.getData(), PlayerSessionIntelData.class);
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

                if (MinetraxBukkit.getPlugin().getHasViaVersion()) {
                    int playerProtocolVersion = Via.getAPI().getPlayerVersion(event.getPlayer().getUniqueId());
                    playerSessionIntelData.minecraft_version = VersionUtil.getMinecraftVersionFromProtoId(playerProtocolVersion);
                }
                if (MinetraxBukkit.getPlugin().getHasSkinsRestorer()) {
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

                ReportPlayerIntel.initSessionSync(playerSessionIntelData);
            } catch (Exception e) {
                LoggingUtil.warntrace(e);
            }
        });
    }

    private void updateSkinDataInPlayerIntel(PlayerSessionIntelData playerSessionIntelData, Player player) {
        if (MinetraxBukkit.getPlugin().getHasSkinsRestorerInProxyMode()) {
            // get from playerSkinCache (from Bungee)
            String[] skinArr = MinetraxBukkit.getPlugin().getPlayerSkinCache().get(player.getUniqueId().toString());
            if (skinArr != null) {
                playerSessionIntelData.skin_property = skinArr[0];
                playerSessionIntelData.skin_texture_id = skinArr[1];
            }
        } else {
            // get from SkinsRestorer API
            SkinProperty skin = SkinUtil.getSkinForPlayer(player.getUniqueId(), player.getName());
            if (skin != null) {
                playerSessionIntelData.skin_property = MinetraxBukkit.getPlugin().getGson().toJson(skin);
                playerSessionIntelData.skin_texture_id = PropertyUtils.getSkinTextureUrlStripped(skin);
            }
        }
    }

    private void removePlayerAndSessionFromDataMap(PlayerQuitEvent event) {
        Gson gson = MinetraxBukkit.getPlugin().getGson();
        String key = event.getPlayer().getUniqueId().toString();

        PlayerData playerData = MinetraxBukkit.getPlugin().playersDataMap.get(key);
        if (playerData != null) {
            LoggingUtil.debug("REPORT FINAL SESSION END ON PLAYER QUIT");
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

            // REMOVE SESSION TO MAP
            MinetraxBukkit.getPlugin().playerSessionIntelDataMap.remove(playerData.session_uuid);
            Bukkit.getScheduler().runTaskAsynchronously(MinetraxBukkit.getPlugin(), new Runnable() {
                @Override
                public void run() {
                    try {
                        ReportPlayerIntel.reportEventSync(leftPlayerSessionIntelData);
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
