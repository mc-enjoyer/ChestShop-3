package com.Acrobot.ChestShop.Listeners;

import com.Acrobot.ChestShop.ChestShop;
import com.Acrobot.ChestShop.Configuration.Properties;
import com.Acrobot.ChestShop.Utils.SignStorage;
import com.Acrobot.ChestShop.Utils.StockColorUtil;
import org.bukkit.block.Sign;

import java.util.List;

/**
 * Periodic task for updating stock colors on ChestShop signs.
 * Runs on the main server thread in batches to avoid lag and unsafe async world access.
 */
public class StockColorUpdateTask implements Runnable {

    private static StockColorUpdateTask instance;
    private static int taskId = -1;

    private int currentBatchIndex = 0;
    private List<Sign> allSigns = null;

    @Override
    public void run() {
        if (!Properties.ENABLE_STOCK_COLOR_INDICATORS) {
            return;
        }

        try {
            if (allSigns == null) {
                allSigns = SignStorage.getValidSigns();
                currentBatchIndex = 0;

                if (allSigns.isEmpty()) {
                    return;
                }

                ChestShop.getBukkitLogger().info(
                    "Starting periodic stock color update for " + allSigns.size() + " signs");
            }

            int batchSize = Properties.STOCK_COLOR_BATCH_SIZE;
            StockColorUtil.updateSignColorsBatch(allSigns, currentBatchIndex, batchSize);
            currentBatchIndex += batchSize;

            if (currentBatchIndex >= allSigns.size()) {
                ChestShop.getBukkitLogger().info(
                    "Completed periodic stock color update for " + allSigns.size() + " signs");
                allSigns = null;
                currentBatchIndex = 0;
            }
        } catch (Exception e) {
            ChestShop.getBukkitLogger().severe("Error in stock color update task: " + e.getMessage());
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

        stopTask();

        long intervalTicks = (long) (Properties.STOCK_COLOR_UPDATE_INTERVAL * 60 * 20);

        instance = new StockColorUpdateTask();
        taskId = ChestShop.getBukkitServer().getScheduler().scheduleSyncRepeatingTask(
            ChestShop.getPlugin(),
            instance,
            intervalTicks,
            intervalTicks
        );

        ChestShop.getBukkitLogger().info("Started stock color update task with "
            + Properties.STOCK_COLOR_UPDATE_INTERVAL + " minute interval and "
            + Properties.STOCK_COLOR_BATCH_SIZE + " signs per batch");
    }

    /**
     * Stop the periodic stock color update task
     */
    public static void stopTask() {
        if (taskId != -1) {
            ChestShop.getBukkitServer().getScheduler().cancelTask(taskId);
            taskId = -1;
        }
        instance = null;
    }
}
