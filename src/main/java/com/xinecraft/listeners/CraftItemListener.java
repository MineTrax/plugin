package com.xinecraft.listeners;

import com.xinecraft.Minetrax;
import com.xinecraft.data.PlayerData;
import com.xinecraft.data.PlayerSessionIntelData;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

public class CraftItemListener implements Listener {
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onItemCraft(CraftItemEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        ItemStack craftedItem = event.getRecipe().getResult();

        PlayerData playerData = Minetrax.getPlugin().playersDataMap.get(player.getUniqueId().toString());
        if (playerData == null) {
            return;
        }

        PlayerSessionIntelData playerSessionIntelData = Minetrax.getPlugin().playerSessionIntelDataMap.get(playerData.session_uuid);
        playerSessionIntelData.items_crafted_xmin += craftedItem.getAmount();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null || event.getClickedInventory() == null) {
            return;
        }

        InventoryType inventoryType = event.getClickedInventory().getType();
        if (
                inventoryType != InventoryType.BREWING &&
                inventoryType != InventoryType.FURNACE &&
                inventoryType != InventoryType.BLAST_FURNACE &&
                inventoryType != InventoryType.SMOKER
        ) {
            return;
        }

        PlayerData playerData = Minetrax.getPlugin().playersDataMap.get(player.getUniqueId().toString());
        if (playerData == null) {
            return;
        }
        PlayerSessionIntelData playerSessionIntelData = Minetrax.getPlugin().playerSessionIntelDataMap.get(playerData.session_uuid);

        if (inventoryType == InventoryType.BREWING) {
            // Check if the item was taken from the brewing result slot (0,1,2)
            if (event.getRawSlot() < 3 && clickedItem.getType() != Material.AIR) {
                playerSessionIntelData.items_crafted_xmin += clickedItem.getAmount();
            }
        } else {
            // Check if the item was taken from the furnace result slot (slot 2)
            if (event.getRawSlot() == 2 && clickedItem.getType() != Material.AIR) {
                playerSessionIntelData.items_crafted_xmin += clickedItem.getAmount();
            }
        }
    }
}
