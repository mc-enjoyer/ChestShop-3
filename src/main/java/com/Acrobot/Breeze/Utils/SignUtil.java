package com.Acrobot.Breeze.Utils;

import org.bukkit.ChatColor;

/**
 * Utility class for handling sign line parsing with color code support
 * 
 * @author Generated
 */
public class SignUtil {
    
    /**
     * Gets a sign line with color codes stripped for parsing
     * 
     * @param line The raw sign line
     * @return The line with color codes removed
     */
    public static String getCleanLine(String line) {
        if (line == null) {
            return "";
        }
        return ChatColor.stripColor(line);
    }
    
    /**
     * Gets a clean sign line for parsing, handling null lines
     * 
     * @param line The raw sign line (can be null)
     * @return The line with color codes removed, or empty string if null
     */
    public static String getCleanLineSafe(String line) {
        if (line == null) {
            return "";
        }
        return ChatColor.stripColor(line).trim();
    }
} 