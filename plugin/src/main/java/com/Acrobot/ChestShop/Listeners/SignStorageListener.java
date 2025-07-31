package com.Acrobot.ChestShop.Listeners;

import com.Acrobot.ChestShop.ChestShop;
import com.Acrobot.ChestShop.Events.ShopCreatedEvent;
import com.Acrobot.ChestShop.Events.ShopDestroyedEvent;
import com.Acrobot.ChestShop.Utils.SignStorage;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/**
 * Listener for automatically storing and removing sign locations
 * when shops are created or destroyed
 * 
 * @author Generated
 */
public class SignStorageListener implements Listener {
    
    /**
     * Save sign location when a shop is created
     * 
     * @param event The shop created event
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onShopCreated(ShopCreatedEvent event) {
        SignStorage.saveSign(event.getSign());
        ChestShop.getBukkitLogger().info("Saved sign location for shop created by " + 
            event.getPlayer().getName() + " at " + event.getSign().getLocation());
    }
    
    /**
     * Remove sign location when a shop is destroyed
     * 
     * @param event The shop destroyed event
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onShopDestroyed(ShopDestroyedEvent event) {
        SignStorage.removeSign(event.getSign());
        String destroyerName = event.getDestroyer() != null ? event.getDestroyer().getName() : "Unknown";
        ChestShop.getBukkitLogger().info("Removed sign location for shop destroyed by " + 
            destroyerName + " at " + event.getSign().getLocation());
    }
} 