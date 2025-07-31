package com.Acrobot.ChestShop.Commands;

import com.Acrobot.ChestShop.ChestShop;
import com.Acrobot.ChestShop.Configuration.Messages;
import com.Acrobot.ChestShop.Configuration.Properties;
import com.Acrobot.ChestShop.Permission;
import com.Acrobot.ChestShop.Utils.StockColorUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Command to manually update stock colors for all ChestShop signs
 * 
 * @author Generated
 */
public class UpdateStockColors implements CommandExecutor {
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!Permission.has(sender, Permission.ADMIN)) {
            sender.sendMessage(Messages.prefix(Messages.ACCESS_DENIED));
            return true;
        }
        
        if (!Properties.ENABLE_STOCK_COLOR_INDICATORS) {
            sender.sendMessage(Messages.prefix("Stock color indicators are disabled in configuration."));
            return true;
        }
        
        sender.sendMessage(Messages.prefix("Updating stock colors for all ChestShop signs..."));
        
        // Run the update asynchronously to avoid blocking the main thread
        ChestShop.getBukkitServer().getScheduler().runTaskAsynchronously(ChestShop.getPlugin(), new Runnable() {
            @Override
            public void run() {
                try {
                    StockColorUtil.updateAllSignColors();
                    
                    // Send completion message on main thread
                    ChestShop.getBukkitServer().getScheduler().runTask(ChestShop.getPlugin(), new Runnable() {
                        @Override
                        public void run() {
                            sender.sendMessage(Messages.prefix("Stock color update completed successfully!"));
                        }
                    });
                } catch (Exception e) {
                    // Send error message on main thread
                    ChestShop.getBukkitServer().getScheduler().runTask(ChestShop.getPlugin(), new Runnable() {
                        @Override
                        public void run() {
                            sender.sendMessage(Messages.prefix("Error updating stock colors: " + e.getMessage()));
                        }
                    });
                }
            }
        });
        
        return true;
    }
} 