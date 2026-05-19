package com.Acrobot.ChestShop.Utils;

import com.Acrobot.ChestShop.ChestShop;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * Utility class for storing and loading SELL ALL sign locations
 * 
 * @author Generated
 */
public class SellAllStorage {
    private static final String SELL_ALL_FILE = "sellall.yml";
    private static final String SIGNS_SECTION = "signs";
    private static final String WORLD_KEY = "world";
    private static final String X_KEY = "x";
    private static final String Y_KEY = "y";
    private static final String Z_KEY = "z";
    private static final String OWNER_KEY = "owner";
    
    private static File sellAllFile;
    private static FileConfiguration sellAllConfig;
    
    /**
     * Initialize the SELL ALL sign storage system
     */
    public static void initialize() {
        sellAllFile = new File(ChestShop.getFolder(), SELL_ALL_FILE);
        sellAllConfig = YamlConfiguration.loadConfiguration(sellAllFile);
        
        if (!sellAllFile.exists()) {
            try {
                sellAllFile.createNewFile();
                sellAllConfig.save(sellAllFile);
            } catch (IOException e) {
                ChestShop.getBukkitLogger().log(Level.SEVERE, "Failed to create sellall file", e);
            }
        }
    }
    
    /**
     * Save a SELL ALL sign location to the storage file
     * 
     * @param sign The sign to save
     * @param owner The owner's username
     */
    public static void saveSign(Sign sign, String owner) {
        if (sign == null) return;
        
        Location location = sign.getLocation();
        String path = SIGNS_SECTION + "." + getSignKey(location);
        
        sellAllConfig.set(path + "." + WORLD_KEY, location.getWorld().getName());
        sellAllConfig.set(path + "." + X_KEY, location.getBlockX());
        sellAllConfig.set(path + "." + Y_KEY, location.getBlockY());
        sellAllConfig.set(path + "." + Z_KEY, location.getBlockZ());
        sellAllConfig.set(path + "." + OWNER_KEY, owner);
        
        saveConfig();
    }
    
    /**
     * Remove a SELL ALL sign location from the storage file
     * 
     * @param sign The sign to remove
     */
    public static void removeSign(Sign sign) {
        if (sign == null) return;
        
        Location location = sign.getLocation();
        String path = SIGNS_SECTION + "." + getSignKey(location);
        
        sellAllConfig.set(path, null);
        saveConfig();
    }
    
    /**
     * Get the owner of a SELL ALL sign
     * 
     * @param sign The sign
     * @return Owner username or null if not found
     */
    public static String getOwner(Sign sign) {
        if (sign == null) return null;
        
        Location location = sign.getLocation();
        String path = SIGNS_SECTION + "." + getSignKey(location);
        
        return sellAllConfig.getString(path + "." + OWNER_KEY);
    }
    
    /**
     * Load all saved SELL ALL sign locations from the storage file
     * 
     * @return List of sign locations with owners
     */
    public static List<SellAllSignData> loadSigns() {
        List<SellAllSignData> signDataList = new ArrayList<>();
        
        if (sellAllConfig == null) {
            initialize();
        }
        
        if (!sellAllConfig.contains(SIGNS_SECTION)) {
            return signDataList;
        }
        
        for (String key : sellAllConfig.getConfigurationSection(SIGNS_SECTION).getKeys(false)) {
            String path = SIGNS_SECTION + "." + key;
            
            String worldName = sellAllConfig.getString(path + "." + WORLD_KEY);
            int x = sellAllConfig.getInt(path + "." + X_KEY);
            int y = sellAllConfig.getInt(path + "." + Y_KEY);
            int z = sellAllConfig.getInt(path + "." + Z_KEY);
            String owner = sellAllConfig.getString(path + "." + OWNER_KEY);
            
            if (worldName != null && owner != null) {
                try {
                    Location location = new Location(
                        ChestShop.getBukkitServer().getWorld(worldName), x, y, z
                    );
                    signDataList.add(new SellAllSignData(location, owner));
                } catch (Exception e) {
                    ChestShop.getBukkitLogger().warning("Failed to load SELL ALL sign location: " + key);
                }
            }
        }
        
        return signDataList;
    }
    
    /**
     * Get all valid SELL ALL signs from the loaded locations
     * 
     * @return List of valid SELL ALL signs with owners
     */
    public static List<SellAllSignData> getValidSigns() {
        List<SellAllSignData> validSigns = new ArrayList<>();
        List<SellAllSignData> signDataList = loadSigns();
        
        for (SellAllSignData signData : signDataList) {
            Location location = signData.getLocation();
            if (location.getWorld() == null) continue;
            
            if (location.getBlock().getState() instanceof Sign) {
                Sign sign = (Sign) location.getBlock().getState();
                // Check if it's still a SELL ALL sign
                String[] lines = sign.getLines();
                if (lines.length > 1 && lines[1] != null && 
                    com.Acrobot.Breeze.Utils.SignUtil.getCleanLineSafe(lines[1]).equalsIgnoreCase("SELL ALL")) {
                    validSigns.add(signData);
                }
            }
        }
        
        return validSigns;
    }
    
    /**
     * Save the configuration to file
     */
    private static void saveConfig() {
        try {
            sellAllConfig.save(sellAllFile);
        } catch (IOException e) {
            ChestShop.getBukkitLogger().log(Level.SEVERE, "Failed to save sellall file", e);
        }
    }
    
    /**
     * Generate a unique key for a sign location
     * 
     * @param location The sign location
     * @return Unique key string
     */
    private static String getSignKey(Location location) {
        return location.getWorld().getName() + "_" + 
               location.getBlockX() + "_" + 
               location.getBlockY() + "_" + 
               location.getBlockZ();
    }
    
    /**
     * Data class for storing SELL ALL sign information
     */
    public static class SellAllSignData {
        private final Location location;
        private final String owner;
        
        public SellAllSignData(Location location, String owner) {
            this.location = location;
            this.owner = owner;
        }
        
        public Location getLocation() {
            return location;
        }
        
        public String getOwner() {
            return owner;
        }
    }
}

