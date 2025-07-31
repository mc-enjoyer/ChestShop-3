package com.Acrobot.ChestShop.Commands;

import com.Acrobot.ChestShop.ChestShop;
import com.Acrobot.ChestShop.Utils.SignStorage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Command to reload signs from storage
 * 
 * @author Generated
 */
public class ReloadSigns implements CommandExecutor {
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("ChestShop.admin")) {
            sender.sendMessage("§cYou don't have permission to use this command!");
            return true;
        }
        
        try {
            List<org.bukkit.block.Sign> validSigns = SignStorage.getValidSigns();
            sender.sendMessage("§aSuccessfully loaded " + validSigns.size() + " valid ChestShop signs from storage");
            
            if (validSigns.isEmpty()) {
                sender.sendMessage("§eNo valid signs found in storage");
            } else {
                sender.sendMessage("§eSign locations:");
                for (org.bukkit.block.Sign sign : validSigns) {
                    sender.sendMessage("§7- " + sign.getLocation() + " (Owner: " + com.Acrobot.Breeze.Utils.SignUtil.getCleanLineSafe(sign.getLine(0)) + ")");
                }
            }
            
        } catch (Exception e) {
            sender.sendMessage("§cError loading signs: " + e.getMessage());
            ChestShop.getBukkitLogger().severe("Error in reload signs command: " + e.getMessage());
        }
        
        return true;
    }
} 