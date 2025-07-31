package com.Acrobot.ChestShop.Utils;

import com.Acrobot.ChestShop.ChestShop;
import com.Acrobot.ChestShop.Signs.ChestShopSign;
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
 * Utility class for storing and loading ChestShop sign locations
 * 
 * @author Generated
 */
public class SignStorage {
    private static final String SIGNS_FILE = "signs.yml";
    private static final String SIGNS_SECTION = "signs";
    private static final String WORLD_KEY = "world";
    private static final String X_KEY = "x";
    private static final String Y_KEY = "y";
    private static final String Z_KEY = "z";
    
    private static File signsFile;
    private static FileConfiguration signsConfig;
    
    /**
     * Initialize the sign storage system
     */
    public static void initialize() {
        signsFile = new File(ChestShop.getFolder(), SIGNS_FILE);
        signsConfig = YamlConfiguration.loadConfiguration(signsFile);
        
        if (!signsFile.exists()) {
            try {
                signsFile.createNewFile();
                signsConfig.save(signsFile);
            } catch (IOException e) {
                ChestShop.getBukkitLogger().log(Level.SEVERE, "Failed to create signs file", e);
            }
        }
    }
    
    /**
     * Save a sign location to the storage file
     * 
     * @param sign The sign to save
     */
    public static void saveSign(Sign sign) {
        if (sign == null) return;
        
        Location location = sign.getLocation();
        String path = SIGNS_SECTION + "." + getSignKey(location);
        
        signsConfig.set(path + "." + WORLD_KEY, location.getWorld().getName());
        signsConfig.set(path + "." + X_KEY, location.getBlockX());
        signsConfig.set(path + "." + Y_KEY, location.getBlockY());
        signsConfig.set(path + "." + Z_KEY, location.getBlockZ());
        
        saveConfig();
    }
    
    /**
     * Remove a sign location from the storage file
     * 
     * @param sign The sign to remove
     */
    public static void removeSign(Sign sign) {
        if (sign == null) return;
        
        Location location = sign.getLocation();
        String path = SIGNS_SECTION + "." + getSignKey(location);
        
        signsConfig.set(path, null);
        saveConfig();
    }
    
    /**
     * Load all saved sign locations from the storage file
     * 
     * @return List of sign locations
     */
    public static List<Location> loadSigns() {
        List<Location> locations = new ArrayList<>();
        
        if (signsConfig == null) {
            initialize();
        }
        
        if (!signsConfig.contains(SIGNS_SECTION)) {
            return locations;
        }
        
        for (String key : signsConfig.getConfigurationSection(SIGNS_SECTION).getKeys(false)) {
            String path = SIGNS_SECTION + "." + key;
            
            String worldName = signsConfig.getString(path + "." + WORLD_KEY);
            int x = signsConfig.getInt(path + "." + X_KEY);
            int y = signsConfig.getInt(path + "." + Y_KEY);
            int z = signsConfig.getInt(path + "." + Z_KEY);
            
            if (worldName != null) {
                try {
                    Location location = new Location(
                        ChestShop.getBukkitServer().getWorld(worldName), x, y, z
                    );
                    locations.add(location);
                } catch (Exception e) {
                    ChestShop.getBukkitLogger().warning("Failed to load sign location: " + key);
                }
            }
        }
        
        return locations;
    }
    
    /**
     * Get all valid ChestShop signs from the loaded locations
     * 
     * @return List of valid ChestShop signs
     */
    public static List<Sign> getValidSigns() {
        List<Sign> validSigns = new ArrayList<>();
        List<Location> locations = loadSigns();
        
        for (Location location : locations) {
            if (location.getWorld() == null) continue;
            
            if (location.getBlock().getState() instanceof Sign) {
                Sign sign = (Sign) location.getBlock().getState();
                if (ChestShopSign.isValid(sign)) {
                    validSigns.add(sign);
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
            signsConfig.save(signsFile);
        } catch (IOException e) {
            ChestShop.getBukkitLogger().log(Level.SEVERE, "Failed to save signs file", e);
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
} 