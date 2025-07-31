package com.Acrobot.ChestShop.Utils;

import com.Acrobot.Breeze.Utils.InventoryUtil;
import com.Acrobot.Breeze.Utils.MaterialUtil;
import com.Acrobot.ChestShop.ChestShop;
import com.Acrobot.ChestShop.Signs.ChestShopSign;
import com.Acrobot.ChestShop.Utils.uBlock;
import org.bukkit.ChatColor;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * Utility class for managing stock color indicators on ChestShop signs
 * 
 * @author Generated
 */
public class StockColorUtil {
    
    /**
     * Check if a sign's connected chest has stock of the specified item
     * 
     * @param sign The sign to check
     * @return true if stock is available, false otherwise
     */
    public static boolean hasStock(Sign sign) {
        if (!ChestShopSign.isValid(sign)) {
            return false;
        }
        
        Chest chest = uBlock.findConnectedChest(sign);
        if (chest == null) {
            return false;
        }
        
        String itemLine = sign.getLine(ChestShopSign.ITEM_LINE);
        ItemStack item = MaterialUtil.getItem(itemLine);
        
        if (item == null) {
            return false;
        }
        
        Inventory inventory = chest.getInventory();
        
        // Check if inventory contains at least 1 of the item
        return inventory.containsAtLeast(item, 1);
    }
    
    /**
     * Update the color of a sign based on stock availability
     * 
     * @param sign The sign to update
     */
    public static void updateSignColor(Sign sign) {
        if (!ChestShopSign.isValid(sign)) {
            return;
        }
        
        boolean hasStock = hasStock(sign);
        ChatColor color = hasStock ? ChatColor.GREEN : ChatColor.RED;
        
        // Update each line with the appropriate color
        for (int i = 0; i < 4; i++) {
            String line = sign.getLine(i);
            if (line != null && !line.isEmpty()) {
                // Remove existing color codes and add new color
                String cleanLine = ChatColor.stripColor(line);
                sign.setLine(i, color + cleanLine);
            }
        }
        
        sign.update(true);
        
        ChestShop.getBukkitLogger().fine("Updated sign color at " + sign.getLocation() + 
            " to " + (hasStock ? "GREEN" : "RED") + " (stock: " + hasStock + ")");
    }
    
    /**
     * Update colors for all valid ChestShop signs
     * This method is designed to be called periodically
     */
    public static void updateAllSignColors() {
        try {
            // Get all stored signs
            java.util.List<org.bukkit.block.Sign> signs = com.Acrobot.ChestShop.Utils.SignStorage.getValidSigns();
            
            for (org.bukkit.block.Sign sign : signs) {
                try {
                    updateSignColor(sign);
                } catch (Exception e) {
                    ChestShop.getBukkitLogger().warning("Failed to update sign color at " + 
                        sign.getLocation() + ": " + e.getMessage());
                }
            }
            
            ChestShop.getBukkitLogger().info("Updated colors for " + signs.size() + " ChestShop signs");
        } catch (Exception e) {
            ChestShop.getBukkitLogger().severe("Failed to update sign colors: " + e.getMessage());
        }
    }
    
    /**
     * Update colors for a batch of signs (for staggered updates)
     * 
     * @param signs List of signs to update
     * @param startIndex Starting index in the batch
     * @param batchSize Number of signs to process in this batch
     */
    public static void updateSignColorsBatch(java.util.List<org.bukkit.block.Sign> signs, int startIndex, int batchSize) {
        int endIndex = Math.min(startIndex + batchSize, signs.size());
        
        for (int i = startIndex; i < endIndex; i++) {
            try {
                org.bukkit.block.Sign sign = signs.get(i);
                updateSignColor(sign);
            } catch (Exception e) {
                ChestShop.getBukkitLogger().warning("Failed to update sign color in batch: " + e.getMessage());
            }
        }
        
        ChestShop.getBukkitLogger().fine("Updated colors for batch " + (startIndex / batchSize + 1) + 
            " (signs " + startIndex + "-" + (endIndex - 1) + ")");
    }
} 