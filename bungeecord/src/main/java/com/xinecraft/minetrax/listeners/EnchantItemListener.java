package com.xinecraft.minetrax.listeners;

import com.xinecraft.minetrax.Minetrax;
import com.xinecraft.minetrax.data.PlayerData;
import com.xinecraft.minetrax.data.PlayerSessionIntelData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;

public class EnchantItemListener implements Listener {
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onItemEnchant(EnchantItemEvent event) {
        Player player = event.getEnchanter();

        PlayerData playerData = Minetrax.getPlugin().playersDataMap.get(player.getUniqueId().toString());
        if (playerData == null) {
            return;
        }

        PlayerSessionIntelData playerSessionIntelData = Minetrax.getPlugin().playerSessionIntelDataMap.get(playerData.session_uuid);
        playerSessionIntelData.items_enchanted_xmin = playerSessionIntelData.items_enchanted_xmin + 1;
    }
}
