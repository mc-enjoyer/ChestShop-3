package com.Acrobot.ChestShop.Listeners;

import com.Acrobot.ChestShop.ChestShop;
import com.Acrobot.ChestShop.Configuration.Properties;
import com.Acrobot.ChestShop.Events.ShopCreatedEvent;
import com.Acrobot.ChestShop.Events.TransactionEvent;
import com.Acrobot.ChestShop.Signs.ChestShopSign;
import com.Acrobot.ChestShop.Utils.StockColorUtil;
import com.Acrobot.ChestShop.Utils.uBlock;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.block.SignChangeEvent;

/**
 * Listener for updating stock colors on ChestShop signs
 * 
 * @author Generated
 */
public class StockColorListener implements Listener {
    
    /**
     * Update sign color when a shop is created
     * 
     * @param event The shop created event
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onShopCreated(ShopCreatedEvent event) {
        if (!Properties.ENABLE_STOCK_COLOR_INDICATORS) {
            return;
        }
        
        Sign sign = event.getSign();
        if (sign != null && ChestShopSign.isValid(sign)) {
            // Schedule the update for the next tick to ensure the sign is fully created
            ChestShop.getBukkitServer().getScheduler().runTask(ChestShop.getPlugin(), new Runnable() {
                @Override
                public void run() {
                    StockColorUtil.updateSignColor(sign);
                }
            });
            
            // Debug: Log successful sign creation
            ChestShop.getBukkitLogger().fine("Shop created event: Updated color for sign at " + sign.getLocation());
        } else {
            // Debug: Log when sign creation event doesn't have a valid sign
            if (sign == null) {
                ChestShop.getBukkitLogger().fine("Shop created event: Sign is null");
            } else {
                ChestShop.getBukkitLogger().fine("Shop created event: Invalid sign at " + sign.getLocation());
            }
        }
    }
    

    
    /**
     * Update sign color when a transaction occurs (buy/sell)
     * 
     * @param event The transaction event
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTransaction(TransactionEvent event) {
        if (!Properties.ENABLE_STOCK_COLOR_INDICATORS) {
            return;
        }
        
        Sign sign = event.getSign();
        if (sign != null && ChestShopSign.isValid(sign)) {
            // Schedule the update for the next tick to ensure inventory changes are processed
            ChestShop.getBukkitServer().getScheduler().runTask(ChestShop.getPlugin(), new Runnable() {
                @Override
                public void run() {
                    StockColorUtil.updateSignColor(sign);
                }
            });
        }
    }
    
    /**
     * Update sign color when items are moved in a container
     * 
     * @param event The inventory click event
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!Properties.ENABLE_STOCK_COLOR_INDICATORS) {
            return;
        }
        
        if (event.getInventory().getHolder() instanceof Chest) {
            Chest chest = (Chest) event.getInventory().getHolder();
            updateSignColorForChest(chest);
        }
    }
    
    /**
     * Update sign color when items are dragged in a container
     * 
     * @param event The inventory drag event
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!Properties.ENABLE_STOCK_COLOR_INDICATORS) {
            return;
        }
        
        if (event.getInventory().getHolder() instanceof Chest) {
            Chest chest = (Chest) event.getInventory().getHolder();
            updateSignColorForChest(chest);
        }
    }
    
    /**
     * Update sign color when a sign is changed/created
     * This is a backup handler in case ShopCreatedEvent doesn't fire
     * 
     * @param event The sign change event
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSignChange(SignChangeEvent event) {
        if (!Properties.ENABLE_STOCK_COLOR_INDICATORS) {
            return;
        }
        
        // Check if this is a valid ChestShop sign
        if (ChestShopSign.isValidPreparedSign(event.getLines())) {
            // Schedule the update for the next tick to ensure the sign is fully created
            ChestShop.getBukkitServer().getScheduler().runTask(ChestShop.getPlugin(), new Runnable() {
                @Override
                public void run() {
                    Sign sign = (Sign) event.getBlock().getState();
                    if (sign != null && ChestShopSign.isValid(sign)) {
                        StockColorUtil.updateSignColor(sign);
                        ChestShop.getBukkitLogger().fine("Sign change event: Updated color for sign at " + sign.getLocation());
                    }
                }
            });
        }
    }
    
    /**
     * Helper method to update sign color for a chest
     * 
     * @param chest The chest to find connected signs for
     */
    private void updateSignColorForChest(Chest chest) {
        Sign sign = uBlock.getConnectedSign(chest);
        
        if (sign != null && ChestShopSign.isValid(sign)) {
            // Schedule the update for the next tick to ensure inventory changes are processed
            ChestShop.getBukkitServer().getScheduler().runTask(ChestShop.getPlugin(), new Runnable() {
                @Override
                public void run() {
                    StockColorUtil.updateSignColor(sign);
                }
            });
        } else {
            // Debug: Log when we can't find a valid sign
            ChestShop.getBukkitLogger().fine("Could not find valid sign for chest at " + chest.getLocation());
        }
    }
} 