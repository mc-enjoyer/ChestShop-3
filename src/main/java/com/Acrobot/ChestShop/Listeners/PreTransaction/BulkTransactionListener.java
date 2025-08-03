package com.Acrobot.ChestShop.Listeners.PreTransaction;

import com.Acrobot.Breeze.Utils.InventoryUtil;
import com.Acrobot.ChestShop.ChestShop;
import com.Acrobot.ChestShop.Configuration.Messages;
import com.Acrobot.ChestShop.Configuration.Properties;
import com.Acrobot.ChestShop.Events.PreTransactionEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.WeakHashMap;

import static com.Acrobot.ChestShop.Events.PreTransactionEvent.TransactionOutcome.SPAM_CLICKING_PROTECTION;

/**
 * Handles bulk transactions when players shift-click on shop signs
 * 
 * @author Acrobot
 */
public class BulkTransactionListener implements Listener {
    private final Map<Player, Long> BULK_COOLDOWN = new WeakHashMap<Player, Long>();

    @EventHandler(priority = EventPriority.LOW)
    public void onPreTransaction(PreTransactionEvent event) {
        if (event.isCancelled() || !Properties.ENABLE_BULK_FEATURE) {
            return;
        }

        Player player = event.getClient();
        
        // Check if player is sneaking (shift-clicking)
        if (!player.isSneaking()) {
            return;
        }

        // Check cooldown
        if (BULK_COOLDOWN.containsKey(player)) {
            long timeSinceLastBulk = System.currentTimeMillis() - BULK_COOLDOWN.get(player);
            if (timeSinceLastBulk < (Properties.BULK_COOLDOWN_SECONDS * 1000)) {
                event.setCancelled(SPAM_CLICKING_PROTECTION);
                player.sendMessage(Messages.prefix(Messages.BULK_TRANSACTION_COOLDOWN));
                return;
            }
        }

        // Calculate bulk amount
        int bulkAmount = calculateBulkAmount(event);
        
        if (bulkAmount <= 0) {
            return; // No bulk transaction possible
        }

        // Update the transaction with bulk amount
        ItemStack[] items = event.getStock();
        if (items.length > 0) {
            // Store original price and amount for calculation
            double originalPrice = event.getPrice();
            int originalAmount = items[0].getAmount();
            double pricePerItem = originalPrice / originalAmount;
            
            // Set the new bulk amount
            items[0].setAmount(bulkAmount);
            event.setStock(items);
            
            // Recalculate price based on bulk amount
            double newPrice = pricePerItem * bulkAmount;
            event.setPrice(newPrice);
            
            // Set cooldown
            BULK_COOLDOWN.put(player, System.currentTimeMillis());
        }
    }

    private int calculateBulkAmount(PreTransactionEvent event) {
        Player player = event.getClient();
        ItemStack[] items = event.getStock();
        
        if (items.length == 0) {
            return 0;
        }

        ItemStack item = items[0];
        int originalAmount = item.getAmount();
        double pricePerItem = event.getPrice() / originalAmount;
        
        // Calculate max affordable amount
        com.Acrobot.ChestShop.Events.Economy.CurrencyAmountEvent currencyAmountEvent = new com.Acrobot.ChestShop.Events.Economy.CurrencyAmountEvent(player);
        ChestShop.callEvent(currencyAmountEvent);
        double playerBalance = currencyAmountEvent.getDoubleAmount();
        int maxAffordable = (int) (playerBalance / pricePerItem);
        
        // Calculate max inventory space
        int maxInventorySpace = calculateMaxInventorySpace(player, item);
        
        // Calculate max available in shop
        int maxAvailable = calculateMaxAvailableInShop(event);
        
        // Return the minimum of the three
        return Math.min(Math.min(maxAffordable, maxInventorySpace), maxAvailable);
    }

    private int calculateMaxInventorySpace(Player player, ItemStack item) {
        Inventory inventory = player.getInventory();
        int maxStackSize = item.getMaxStackSize();
        int emptySlots = 0;
        
        // Count empty slots
        for (ItemStack slot : inventory.getContents()) {
            if (slot == null) {
                emptySlots++;
            }
        }
        
        // Count partial stacks of the same item
        int partialStacks = 0;
        for (ItemStack slot : inventory.getContents()) {
            if (slot != null && slot.isSimilar(item)) {
                partialStacks += (maxStackSize - slot.getAmount());
            }
        }
        
        return (emptySlots * maxStackSize) + partialStacks;
    }

    private int calculateMaxAvailableInShop(PreTransactionEvent event) {
        Inventory ownerInventory = event.getOwnerInventory();
        ItemStack[] items = event.getStock();
        
        if (ownerInventory == null || items.length == 0) {
            return 0;
        }

        ItemStack item = items[0];
        
        if (event.getTransactionType() == com.Acrobot.ChestShop.Events.TransactionEvent.TransactionType.BUY) {
            // For buying, check how much is available in the shop
            return InventoryUtil.getAmount(item, ownerInventory);
        } else {
            // For selling, check how much space is available in the shop
            int maxStackSize = item.getMaxStackSize();
            int emptySlots = 0;
            
            for (ItemStack slot : ownerInventory.getContents()) {
                if (slot == null) {
                    emptySlots++;
                }
            }
            
            int partialStacks = 0;
            for (ItemStack slot : ownerInventory.getContents()) {
                if (slot != null && slot.isSimilar(item)) {
                    partialStacks += (maxStackSize - slot.getAmount());
                }
            }
            
            return (emptySlots * maxStackSize) + partialStacks;
        }
    }
} 