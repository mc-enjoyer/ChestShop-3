package com.Acrobot.ChestShop.Listeners;

import com.Acrobot.Breeze.Utils.InventoryUtil;
import com.Acrobot.Breeze.Utils.MaterialUtil;
import com.Acrobot.ChestShop.ChestShop;
import com.Acrobot.ChestShop.Configuration.Messages;
import com.Acrobot.ChestShop.Configuration.Properties;
import com.Acrobot.ChestShop.Economy.Economy;
import com.Acrobot.ChestShop.Events.ShopDestroyedEvent;
import com.Acrobot.ChestShop.Utils.SellAllStorage;
import com.Acrobot.ChestShop.Utils.uBlock;
import com.Acrobot.ChestShop.Utils.uName;
import com.google.common.base.Joiner;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * Manager for SELL ALL signs that automatically sell items from chests
 * 
 * @author Generated
 */
public class SellAllManager implements Listener {
    
    private static Map<Material, Double> sellableItems = new HashMap<>();
    private static int taskId = -1;
    
    /**
     * Initialize the sellable items map from configuration
     */
    public static void initializeSellableItems() {
        sellableItems.clear();
        
        String config = Properties.SELLABLE_ITEMS;
        if (config == null || config.trim().isEmpty()) {
            ChestShop.getBukkitLogger().warning("No sellable items configured for SELL ALL signs");
            return;
        }
        
        String[] items = config.split(",");
        for (String itemConfig : items) {
            itemConfig = itemConfig.trim();
            if (itemConfig.isEmpty()) continue;
            
            String[] parts = itemConfig.split(":");
            if (parts.length != 2) {
                ChestShop.getBukkitLogger().warning("Invalid sellable item format: " + itemConfig + 
                    " (expected: MaterialName:Price)");
                continue;
            }
            
            try {
                Material material = MaterialUtil.getMaterial(parts[0].trim());
                if (material == null) {
                    ChestShop.getBukkitLogger().warning("Unknown material: " + parts[0].trim());
                    continue;
                }
                
                double price = Double.parseDouble(parts[1].trim());
                if (price < 0) {
                    ChestShop.getBukkitLogger().warning("Price cannot be negative: " + price);
                    continue;
                }
                
                sellableItems.put(material, price);
                ChestShop.getBukkitLogger().info("Registered sellable item: " + material.name() + " at " + price + " per item");
            } catch (NumberFormatException e) {
                ChestShop.getBukkitLogger().warning("Invalid price format: " + parts[1].trim());
            }
        }
        
        ChestShop.getBukkitLogger().info("Loaded " + sellableItems.size() + " sellable items for SELL ALL signs");
    }
    
    /**
     * Handle sign placement - detect SELL ALL signs
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSignChange(SignChangeEvent event) {
        String[] lines = event.getLines();
        
        // Check if second line is "SELL ALL"
        if (lines.length < 2) return;
        
        String secondLine = com.Acrobot.Breeze.Utils.SignUtil.getCleanLineSafe(lines[1]);
        if (!secondLine.equalsIgnoreCase("SELL ALL")) {
            return;
        }
        
        // Store the placer's name on the first line and color code the sign to show it's activated
        String playerName = event.getPlayer().getName();
        event.setLine(0, ChatColor.AQUA + playerName);
        event.setLine(1, ChatColor.AQUA + "SELL ALL");
        
        // Save the sign to storage
        Sign sign = (Sign) event.getBlock().getState();
        SellAllStorage.saveSign(sign, playerName);
        
        // Update sign colors after a tick to ensure the sign is fully created
        ChestShop.getBukkitServer().getScheduler().runTask(ChestShop.getPlugin(), new Runnable() {
            @Override
            public void run() {
                updateSellAllSignColor(sign);
            }
        });
        
        ChestShop.getBukkitLogger().info("SELL ALL sign created by " + playerName + " at " + sign.getLocation());
    }
    
    /**
     * Handle sign break - remove from storage
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSignBreak(BlockBreakEvent event) {
        if (!(event.getBlock().getState() instanceof Sign)) {
            return;
        }
        
        Sign sign = (Sign) event.getBlock().getState();
        String[] lines = sign.getLines();
        
        // Check if it's a SELL ALL sign
        if (lines.length < 2) return;
        
        String secondLine = com.Acrobot.Breeze.Utils.SignUtil.getCleanLineSafe(lines[1]);
        if (secondLine.equalsIgnoreCase("SELL ALL")) {
            SellAllStorage.removeSign(sign);
            ChestShop.getBukkitLogger().info("SELL ALL sign removed at " + sign.getLocation());
        }
    }
    
    /**
     * Handle shop destroyed event - remove SELL ALL sign if it was destroyed
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onShopDestroyed(ShopDestroyedEvent event) {
        Sign sign = event.getSign();
        if (sign == null) return;
        
        String[] lines = sign.getLines();
        if (lines.length < 2) return;
        
        String secondLine = com.Acrobot.Breeze.Utils.SignUtil.getCleanLineSafe(lines[1]);
        if (secondLine.equalsIgnoreCase("SELL ALL")) {
            SellAllStorage.removeSign(sign);
            ChestShop.getBukkitLogger().info("SELL ALL sign destroyed at " + sign.getLocation());
        }
    }
    
    /**
     * Sales accumulated for one owner during a single check round.
     */
    private static class RoundSales {
        final Map<Material, Integer> itemsSold = new HashMap<>();
        double totalPayment = 0.0;

        void addSale(Map<Material, Integer> signItemsSold, double payment) {
            totalPayment += payment;
            for (Map.Entry<Material, Integer> entry : signItemsSold.entrySet()) {
                itemsSold.put(entry.getKey(),
                    itemsSold.getOrDefault(entry.getKey(), 0) + entry.getValue());
            }
        }
    }

    /**
     * Check all SELL ALL signs and process sellable items
     */
    public static void checkAllSigns() {
        if (sellableItems.isEmpty()) {
            return;
        }

        Map<String, RoundSales> salesByOwner = new HashMap<>();
        List<SellAllStorage.SellAllSignData> signs = SellAllStorage.getValidSigns();

        for (SellAllStorage.SellAllSignData signData : signs) {
            try {
                processSellAllSign(signData, salesByOwner);
            } catch (Exception e) {
                ChestShop.getBukkitLogger().log(Level.WARNING,
                    "Error processing SELL ALL sign at " + signData.getLocation() + ": " + e.getMessage(), e);
            }
        }

        for (Map.Entry<String, RoundSales> entry : salesByOwner.entrySet()) {
            sendCondensedSellAllMessage(entry.getKey(), entry.getValue());
        }
    }
    
    /**
     * Process a single SELL ALL sign - check chest and sell items
     */
    private static void processSellAllSign(SellAllStorage.SellAllSignData signData,
                                           Map<String, RoundSales> salesByOwner) {
        // Verify the sign still exists and is valid
        if (signData.getLocation().getWorld() == null) {
            return;
        }
        
        // Check if chunk is loaded
        if (!signData.getLocation().getWorld().isChunkLoaded(
                signData.getLocation().getBlockX() >> 4,
                signData.getLocation().getBlockZ() >> 4)) {
            return;
        }
        
        // Check if the block is still a sign
        if (!(signData.getLocation().getBlock().getState() instanceof Sign)) {
            // Sign was broken, it will be cleaned up by the break event handler
            return;
        }
        
        Sign sign = (Sign) signData.getLocation().getBlock().getState();
        
        // Verify it's still a SELL ALL sign
        String[] lines = sign.getLines();
        if (lines.length < 2) {
            // Sign was changed, remove from storage
            SellAllStorage.removeSign(sign);
            return;
        }
        
        String secondLine = com.Acrobot.Breeze.Utils.SignUtil.getCleanLineSafe(lines[1]);
        if (!secondLine.equalsIgnoreCase("SELL ALL")) {
            // Sign was changed, remove from storage
            SellAllStorage.removeSign(sign);
            return;
        }
        
        // Update sign color to show it's active
        updateSellAllSignColor(sign);
        
        // Find connected chest
        Chest chest = uBlock.findConnectedChest(sign);
        if (chest == null) {
            return;
        }
        
        Inventory inventory = chest.getInventory();
        if (inventory == null) {
            return;
        }
        
        // Process all items in the chest
        double totalPayment = 0.0;
        Map<Material, Integer> itemsSold = new HashMap<>();
        
        ItemStack[] contents = inventory.getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            if (item == null || item.getType() == Material.AIR) {
                continue;
            }
            
            Material material = item.getType();
            if (sellableItems.containsKey(material)) {
                int amount = item.getAmount();
                double pricePerItem = sellableItems.get(material);
                double payment = amount * pricePerItem;
                
                totalPayment += payment;
                
                // Track items sold
                itemsSold.put(material, itemsSold.getOrDefault(material, 0) + amount);
                
                // Remove the item from inventory
                inventory.setItem(i, null);
            }
        }
        
        // Pay the owner if there are items to sell
        if (totalPayment > 0) {
            String owner = signData.getOwner();
            if (owner != null && !owner.isEmpty()) {
                Economy.add(owner, sign.getWorld(), totalPayment);
                
                // Build item list for message
                List<ItemStack> soldItemsList = new ArrayList<>();
                for (Map.Entry<Material, Integer> entry : itemsSold.entrySet()) {
                    soldItemsList.add(new ItemStack(entry.getKey(), entry.getValue()));
                }
                salesByOwner.computeIfAbsent(owner, k -> new RoundSales())
                    .addSale(itemsSold, totalPayment);

                // Log the transaction
                StringBuilder logMessage = new StringBuilder("SELL ALL: Sold items for " + owner + 
                    " at " + sign.getLocation() + " - Total: " + totalPayment);
                for (Map.Entry<Material, Integer> entry : itemsSold.entrySet()) {
                    logMessage.append(", ").append(entry.getKey().name()).append(": ").append(entry.getValue());
                }
                ChestShop.getBukkitLogger().info(logMessage.toString());
            }
        }
    }
    
    /**
     * Start the periodic checking task
     * Uses the configured interval from Properties.SELL_ALL_CHECK_INTERVAL
     */
    public static void startTask() {
        if (taskId != -1) {
            // Task already running
            return;
        }
        
        initializeSellableItems();
        
        // Convert seconds to ticks (20 ticks per second)
        long intervalTicks = Properties.SELL_ALL_CHECK_INTERVAL * 20L;
        
        // Ensure minimum interval of 1 second (20 ticks) to prevent server lag
        if (intervalTicks < 20) {
            intervalTicks = 20;
            ChestShop.getBukkitLogger().warning("SELL_ALL_CHECK_INTERVAL was less than 1 second, using 1 second minimum");
        }
        
        // Run at the configured interval
        taskId = ChestShop.getBukkitServer().getScheduler().scheduleSyncRepeatingTask(
            ChestShop.getPlugin(),
            new Runnable() {
                @Override
                public void run() {
                    checkAllSigns();
                }
            },
            intervalTicks, // Start after configured interval
            intervalTicks  // Repeat every configured interval
        );
        
        ChestShop.getBukkitLogger().info("Started SELL ALL checking task (every " + 
            Properties.SELL_ALL_CHECK_INTERVAL + " seconds)");
    }
    
    /**
     * Stop the periodic checking task
     */
    public static void stopTask() {
        if (taskId != -1) {
            ChestShop.getBukkitServer().getScheduler().cancelTask(taskId);
            taskId = -1;
            ChestShop.getBukkitLogger().info("Stopped SELL ALL checking task");
        }
    }
    
    /**
     * Send one condensed message per owner for all sales in a check round.
     */
    private static void sendCondensedSellAllMessage(String owner, RoundSales sales) {
        String ownerName = uName.getName(owner);
        Player player = Bukkit.getPlayerExact(ownerName);

        if (player == null || !player.isOnline()) {
            return;
        }

        List<ItemStack> soldItemsList = new ArrayList<>();
        for (Map.Entry<Material, Integer> entry : sales.itemsSold.entrySet()) {
            soldItemsList.add(new ItemStack(entry.getKey(), entry.getValue()));
        }
        ItemStack[] soldItemsArray = soldItemsList.toArray(new ItemStack[soldItemsList.size()]);
        String itemInfo = parseItemInformation(soldItemsArray);
        String formattedPrice = Economy.formatBalance(sales.totalPayment);

        String message = Messages.prefix(ChatColor.GREEN + "Your SELL ALL signs sold " + ChatColor.YELLOW + itemInfo
            + ChatColor.GREEN + " for " + ChatColor.YELLOW + formattedPrice + ChatColor.GREEN + "!");
        player.sendMessage(message);
    }
    
    /**
     * Parse item information into a readable string format
     * Similar to TransactionMessageSender.parseItemInformation
     * 
     * @param items Array of items sold
     * @return Formatted item string
     */
    private static String parseItemInformation(ItemStack[] items) {
        ItemStack[] stock = InventoryUtil.mergeSimilarStacks(items);
        
        StringBuilder message = new StringBuilder(15);
        Joiner joiner = Joiner.on(' ');
        
        for (ItemStack item : stock) {
            if (message.length() > 0) {
                message.append(", ");
            }
            joiner.appendTo(message, item.getAmount(), MaterialUtil.getName(item));
        }
        
        return message.toString();
    }
    
    /**
     * Update the color of a SELL ALL sign to show it's activated
     * Uses AQUA color to indicate active status
     * 
     * @param sign The sign to update
     */
    private static void updateSellAllSignColor(Sign sign) {
        if (sign == null) {
            return;
        }
        
        // Check if chunk is loaded
        if (!sign.getWorld().isChunkLoaded(sign.getX() >> 4, sign.getZ() >> 4)) {
            return;
        }
        
        // Verify it's still a SELL ALL sign
        String[] lines = sign.getLines();
        if (lines.length < 2) {
            return;
        }
        
        String secondLine = com.Acrobot.Breeze.Utils.SignUtil.getCleanLineSafe(lines[1]);
        if (!secondLine.equalsIgnoreCase("SELL ALL")) {
            return;
        }
        
        // Use AQUA color to indicate active status
        ChatColor activeColor = ChatColor.AQUA;
        
        // Update each line with the active color
        for (int i = 0; i < 4; i++) {
            String line = sign.getLine(i);
            if (line != null && !line.isEmpty()) {
                // Remove existing color codes and add active color
                String cleanLine = ChatColor.stripColor(line);
                sign.setLine(i, activeColor + cleanLine);
            }
        }
        
        sign.update(true);
    }
}

