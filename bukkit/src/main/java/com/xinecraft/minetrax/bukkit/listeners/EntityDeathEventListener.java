package com.xinecraft.minetrax.bukkit.listeners;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.xinecraft.minetrax.bukkit.MinetraxBukkit;
import com.xinecraft.minetrax.bukkit.data.PlayerData;
import com.xinecraft.minetrax.bukkit.data.PlayerDeathData;
import com.xinecraft.minetrax.bukkit.data.PlayerPvpKillData;
import com.xinecraft.minetrax.bukkit.data.PlayerSessionIntelData;
import com.xinecraft.minetrax.bukkit.utils.HttpUtil;
import com.xinecraft.minetrax.bukkit.utils.LoggingUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.Date;

public class EntityDeathEventListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        Gson gson = new GsonBuilder().serializeNulls().create();

        // System.out.println("SIZE OF PLAYER DATA MAP " + Minetrax.getPlugin().playersDataMap.size());
        // System.out.println("SIZE OF PLAYER SESSION MAP " + Minetrax.getPlugin().playerSessionIntelDataMap.size());

        // Player Death
        if (event.getEntity() instanceof Player) {
            Player victim = (Player) event.getEntity();
            PlayerData victimPlayerData = MinetraxBukkit.getPlugin().playersDataMap.get(victim.getUniqueId().toString());
            if(victimPlayerData == null) {
                MinetraxBukkit.getPlugin().getLogger().warning("Failed to send death data. Cannot find player in playerData HashMap");
                return;
            }

            // Increment player death for victim in Session
            PlayerSessionIntelData victimSessionIntelData = MinetraxBukkit.getPlugin().playerSessionIntelDataMap.get(victimPlayerData.session_uuid);
            victimSessionIntelData.deaths = victimSessionIntelData.deaths + 1;
            victimSessionIntelData.deaths_xmin = victimSessionIntelData.deaths_xmin + 1;
            // Minetrax.getPlugin().playerSessionIntelDataMap.put(victimPlayerData.session_uuid, victimSessionIntelData);

            // Init Building Death Data
            PlayerDeathData playerDeathData = new PlayerDeathData();
            playerDeathData.player_uuid = victim.getUniqueId().toString();
            playerDeathData.player_username = victim.getName();
            playerDeathData.died_at = new Date().getTime();
            playerDeathData.session_uuid = victimPlayerData.session_uuid;
            playerDeathData.world_name = victim.getWorld().getName();
            playerDeathData.world_location = gson.toJson(victim.getLocation().serialize());
            playerDeathData.cause = victim.getLastDamageCause() != null ? victim.getLastDamageCause().getCause().name() : null;

            // Player Death by Human: PVP
            if (event.getEntity().getKiller() != null) {
                Player killer = event.getEntity().getKiller();
                playerDeathData.killer_uuid = killer.getUniqueId().toString();
                playerDeathData.killer_username = killer.getName();

                PlayerData killerPlayerData = MinetraxBukkit.getPlugin().playersDataMap.get(killer.getUniqueId().toString());
                if (killerPlayerData != null) {
                    // Increment player pvp kills for killer in Session
                    PlayerSessionIntelData killerSessionIntelData = MinetraxBukkit.getPlugin().playerSessionIntelDataMap.get(killerPlayerData.session_uuid);
                    killerSessionIntelData.player_kills = killerSessionIntelData.player_kills + 1;
                    killerSessionIntelData.player_kills_xmin = killerSessionIntelData.player_kills_xmin + 1;
                    // Minetrax.getPlugin().playerSessionIntelDataMap.put(killerPlayerData.session_uuid, killerSessionIntelData);

                    // Make and send PvP Kill Report
                    PlayerPvpKillData playerPvpKillData = new PlayerPvpKillData();
                    playerPvpKillData.killer_uuid = killer.getUniqueId().toString();
                    playerPvpKillData.killer_username = killer.getName();
                    playerPvpKillData.victim_uuid = victim.getUniqueId().toString();
                    playerPvpKillData.victim_username = victim.getName();
                    playerPvpKillData.killed_at = new Date().getTime();
                    playerPvpKillData.session_uuid = killerPlayerData.session_uuid;
                    playerPvpKillData.world_name = killer.getWorld().getName();
                    playerPvpKillData.world_location = gson.toJson(killer.getLocation().serialize());
                    playerPvpKillData.weapon = killer.getInventory().getItemInMainHand().getType().toString();

                    // REPORT HTTP
                    String playerPvpKillDataJSON = gson.toJson(playerPvpKillData);
                    LoggingUtil.info("---SENDING PLAYER PVP KILL REPORT---");
                    Bukkit.getScheduler().runTaskAsynchronously(MinetraxBukkit.getPlugin(), new Runnable() {
                        @Override
                        public void run() {
                            try {
                                HttpUtil.postJsonWithAuth(MinetraxBukkit.getPlugin().getApiHost() + "/api/v1/intel/player/report/pvp-kill", playerPvpKillDataJSON);
                            } catch (Exception e) {
                                MinetraxBukkit.getPlugin().getLogger().warning(e.getMessage());
                            }
                        }
                    });
                }
            }
            // Player death by Mob
            else if (event.getEntity().getLastDamageCause() instanceof EntityDamageByEntityEvent) {
                EntityDamageByEntityEvent lastDamageCause = (EntityDamageByEntityEvent) event.getEntity().getLastDamageCause();
                playerDeathData.killer_entity_id = lastDamageCause.getDamager().getType().toString();
                playerDeathData.killer_entity_name = lastDamageCause.getDamager().getName();
            }

            LoggingUtil.info("---SENDING PLAYER DEATH REPORT---");
            String playerDeathJSON = gson.toJson(playerDeathData);
            Bukkit.getScheduler().runTaskAsynchronously(MinetraxBukkit.getPlugin(), new Runnable() {
                @Override
                public void run() {
                    try {
                        HttpUtil.postJsonWithAuth(MinetraxBukkit.getPlugin().getApiHost() + "/api/v1/intel/player/report/death", playerDeathJSON);
                    } catch (Exception e) {
                        MinetraxBukkit.getPlugin().getLogger().warning(e.getMessage());
                    }
                }
            });
        }
        // Mob Death by Player
        else if (event.getEntity().getKiller() != null) {
            Player killer = event.getEntity().getKiller();

            // Minetrax.getPlugin().getLogger().info("Mob Died: " + event.getEntity().getName());
            // Minetrax.getPlugin().getLogger().info("Killer: " + event.getEntity().getKiller());

            // Increment player mob kills for killer in Session
            PlayerData killerPlayerData = MinetraxBukkit.getPlugin().playersDataMap.get(killer.getUniqueId().toString());
            if (killerPlayerData != null) {
                PlayerSessionIntelData killerSessionIntelData = MinetraxBukkit.getPlugin().playerSessionIntelDataMap.get(killerPlayerData.session_uuid);
                killerSessionIntelData.mob_kills = killerSessionIntelData.mob_kills + 1;
                killerSessionIntelData.mob_kills_xmin = killerSessionIntelData.mob_kills_xmin + 1;
                // Minetrax.getPlugin().playerSessionIntelDataMap.put(killerPlayerData.session_uuid, killerSessionIntelData);
            }
        }
    }
}
