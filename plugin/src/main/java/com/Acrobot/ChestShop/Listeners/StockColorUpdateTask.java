package com.Acrobot.ChestShop.Listeners;

import com.Acrobot.ChestShop.ChestShop;
import com.Acrobot.ChestShop.Configuration.Properties;
import com.Acrobot.ChestShop.Utils.StockColorUtil;
import org.bukkit.block.Sign;

import java.util.List;

/**
 * Background task for periodically updating stock colors on ChestShop signs
 * Updates signs in batches to prevent server lag
 * 
 * @author Generated
 */
public class StockColorUpdateTask implements Runnable {
    
    private int currentBatchIndex = 0;
    private List<Sign> allSigns = null;
    
    @Override
    public void run() {
        if (!Properties.ENABLE_STOCK_COLOR_INDICATORS) {
            return;
        }
        
        try {
            // Get all signs if we haven't already
            if (allSigns == null) {
                allSigns = com.Acrobot.ChestShop.Utils.SignStorage.getValidSigns();
                currentBatchIndex = 0;
                
                if (allSigns.isEmpty()) {
                    return;
                }
                
                ChestShop.getBukkitLogger().info("Starting periodic stock color update for " + allSigns.size() + " signs");
            }
            
            // Process current batch
            StockColorUtil.updateSignColorsBatch(allSigns, currentBatchIndex, Properties.STOCK_COLOR_BATCH_SIZE);
            
            currentBatchIndex += Properties.STOCK_COLOR_BATCH_SIZE;
            
            // If we've processed all signs, reset for next cycle
            if (currentBatchIndex >= allSigns.size()) {
                ChestShop.getBukkitLogger().info("Completed periodic stock color update for " + allSigns.size() + " signs");
                allSigns = null;
                currentBatchIndex = 0;
            }
            
        } catch (Exception e) {
            ChestShop.getBukkitLogger().severe("Error in stock color update task: " + e.getMessage());
            // Reset on error
            allSigns = null;
            currentBatchIndex = 0;
        }
    }
    
    /**
     * Start the periodic stock color update task
     */
    public static void startTask() {
        if (!Properties.ENABLE_STOCK_COLOR_INDICATORS) {
            return;
        }
        
        // Convert minutes to ticks (20 ticks per second, 60 seconds per minute)
        long intervalTicks = (long) (Properties.STOCK_COLOR_UPDATE_INTERVAL * 60 * 20);
        
        // Start the task
        ChestShop.getBukkitServer().getScheduler().runTaskTimerAsynchronously(
            ChestShop.getPlugin(), 
            new StockColorUpdateTask(), 
            intervalTicks, 
            intervalTicks
        );
        
        ChestShop.getBukkitLogger().info("Started stock color update task with " + 
            Properties.STOCK_COLOR_UPDATE_INTERVAL + " minute interval and " + 
            Properties.STOCK_COLOR_BATCH_SIZE + " signs per batch");
    }
    
    /**
     * Stop the periodic stock color update task
     */
    public static void stopTask() {
        ChestShop.getBukkitServer().getScheduler().cancelTasks(ChestShop.getPlugin());
        ChestShop.getBukkitLogger().info("Stopped stock color update task");
    }
} 