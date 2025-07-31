package com.Acrobot.ChestShop.Listeners.PostTransaction;

import com.Acrobot.ChestShop.ChestShop;
import com.Acrobot.ChestShop.Events.TransactionEvent;
import com.Acrobot.ChestShop.Utils.SignStorage;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.List;

/**
 * Listener for automatically storing sign locations when they're used in transactions
 * but aren't already in the file storage system
 * 
 * @author Generated
 */
public class SignStorageTransactionListener implements Listener {
    
    /**
     * Check if a sign used in a transaction is already in storage, and add it if not
     * 
     * @param event The transaction event
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTransaction(TransactionEvent event) {
        Sign sign = event.getSign();
        if (sign == null) return;
        
        // Check if this sign is already in storage
        List<org.bukkit.block.Sign> storedSigns = SignStorage.getValidSigns();
        boolean signAlreadyStored = false;
        
        for (org.bukkit.block.Sign storedSign : storedSigns) {
            if (storedSign.getLocation().equals(sign.getLocation())) {
                signAlreadyStored = true;
                break;
            }
        }
        
        // If sign is not in storage, add it
        if (!signAlreadyStored) {
            SignStorage.saveSign(sign);
            ChestShop.getBukkitLogger().info("Added sign to storage during transaction - " +
                "Location: " + sign.getLocation() + 
                ", Owner: " + event.getOwner().getName() + 
                ", Client: " + event.getClient().getName() + 
                ", Type: " + event.getTransactionType());
        }
    }
} 