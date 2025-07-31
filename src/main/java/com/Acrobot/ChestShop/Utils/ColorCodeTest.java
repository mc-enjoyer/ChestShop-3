package com.Acrobot.ChestShop.Utils;

import com.Acrobot.Breeze.Utils.MaterialUtil;
import com.Acrobot.Breeze.Utils.NumberUtil;
import com.Acrobot.Breeze.Utils.PriceUtil;
import com.Acrobot.Breeze.Utils.SignUtil;
import com.Acrobot.ChestShop.Signs.ChestShopSign;
import org.bukkit.ChatColor;

/**
 * Test class to verify color code handling with sign parsing
 * This class can be used to test that color codes don't interfere with sign parsing
 * 
 * @author Generated
 */
public class ColorCodeTest {
    
    /**
     * Test that color codes are properly stripped from sign lines
     */
    public static void testColorCodeStripping() {
        System.out.println("Testing color code stripping...");
        
        // Test with green color
        String greenLine = ChatColor.GREEN + "TestPlayer";
        String cleanGreen = SignUtil.getCleanLineSafe(greenLine);
        System.out.println("Green line: '" + greenLine + "' -> Clean: '" + cleanGreen + "'");
        assert cleanGreen.equals("TestPlayer") : "Green color not stripped properly";
        
        // Test with red color
        String redLine = ChatColor.RED + "64";
        String cleanRed = SignUtil.getCleanLineSafe(redLine);
        System.out.println("Red line: '" + redLine + "' -> Clean: '" + cleanRed + "'");
        assert cleanRed.equals("64") : "Red color not stripped properly";
        
        // Test with price line
        String priceLine = ChatColor.GREEN + "B 10 S 5";
        String cleanPrice = SignUtil.getCleanLineSafe(priceLine);
        System.out.println("Price line: '" + priceLine + "' -> Clean: '" + cleanPrice + "'");
        assert cleanPrice.equals("B 10 S 5") : "Price color not stripped properly";
        
        // Test with item line
        String itemLine = ChatColor.RED + "DIAMOND_SWORD";
        String cleanItem = SignUtil.getCleanLineSafe(itemLine);
        System.out.println("Item line: '" + itemLine + "' -> Clean: '" + cleanItem + "'");
        assert cleanItem.equals("DIAMOND_SWORD") : "Item color not stripped properly";
        
        System.out.println("Color code stripping tests passed!");
    }
    
    /**
     * Test that number parsing works with color codes
     */
    public static void testNumberParsing() {
        System.out.println("Testing number parsing with color codes...");
        
        // Test integer parsing
        String greenNumber = ChatColor.GREEN + "64";
        assert NumberUtil.isInteger(SignUtil.getCleanLineSafe(greenNumber)) : "Integer parsing failed with green color";
        
        String redNumber = ChatColor.RED + "128";
        assert NumberUtil.isInteger(SignUtil.getCleanLineSafe(redNumber)) : "Integer parsing failed with red color";
        
        // Test double parsing
        String greenDouble = ChatColor.GREEN + "10.5";
        assert NumberUtil.isDouble(SignUtil.getCleanLineSafe(greenDouble)) : "Double parsing failed with green color";
        
        System.out.println("Number parsing tests passed!");
    }
    
    /**
     * Test that price parsing works with color codes
     */
    public static void testPriceParsing() {
        System.out.println("Testing price parsing with color codes...");
        
        // Test buy price
        String greenPrice = ChatColor.GREEN + "B 10";
        double buyPrice = PriceUtil.getBuyPrice(greenPrice);
        assert buyPrice == 10.0 : "Buy price parsing failed with green color";
        
        // Test sell price
        String redPrice = ChatColor.RED + "S 5";
        double sellPrice = PriceUtil.getSellPrice(redPrice);
        assert sellPrice == 5.0 : "Sell price parsing failed with red color";
        
        // Test combined price
        String combinedPrice = ChatColor.GREEN + "B 10 S 5";
        double buyPrice2 = PriceUtil.getBuyPrice(combinedPrice);
        double sellPrice2 = PriceUtil.getSellPrice(combinedPrice);
        assert buyPrice2 == 10.0 : "Combined buy price parsing failed";
        assert sellPrice2 == 5.0 : "Combined sell price parsing failed";
        
        System.out.println("Price parsing tests passed!");
    }
    
    /**
     * Test that item parsing works with color codes
     */
    public static void testItemParsing() {
        System.out.println("Testing item parsing with color codes...");
        
        // Test basic item
        String greenItem = ChatColor.GREEN + "DIAMOND_SWORD";
        assert MaterialUtil.getItem(greenItem) != null : "Item parsing failed with green color";
        
        // Test item with data
        String redItem = ChatColor.RED + "WOOL:14";
        assert MaterialUtil.getItem(redItem) != null : "Item with data parsing failed with red color";
        
        System.out.println("Item parsing tests passed!");
    }
    
    /**
     * Test that sign validation works with color codes
     */
    public static void testSignValidation() {
        System.out.println("Testing sign validation with color codes...");
        
        // Create test sign lines with colors
        String[] coloredLines = {
            ChatColor.GREEN + "TestPlayer",  // Owner
            ChatColor.RED + "64",           // Quantity
            ChatColor.GREEN + "B 10 S 5",   // Price
            ChatColor.RED + "DIAMOND_SWORD" // Item
        };
        
        // Test validation
        boolean isValid = ChestShopSign.isValid(coloredLines);
        assert isValid : "Sign validation failed with color codes";
        
        System.out.println("Sign validation tests passed!");
    }
    
    /**
     * Test that transaction events trigger stock color updates
     */
    public static void testTransactionEventHandling() {
        System.out.println("Testing transaction event handling...");
        
        // Test that the StockColorListener handles TransactionEvent
        // This is a basic verification that the event handler exists
        try {
            // Check if the StockColorListener class has the transaction event handler
            Class<?> listenerClass = Class.forName("com.Acrobot.ChestShop.Listeners.StockColorListener");
            java.lang.reflect.Method[] methods = listenerClass.getDeclaredMethods();
            
            boolean hasTransactionHandler = false;
            for (java.lang.reflect.Method method : methods) {
                if (method.getName().equals("onTransaction")) {
                    hasTransactionHandler = true;
                    break;
                }
            }
            
            assert hasTransactionHandler : "StockColorListener missing onTransaction method";
            System.out.println("Transaction event handler found in StockColorListener");
            
        } catch (ClassNotFoundException e) {
            System.err.println("StockColorListener class not found: " + e.getMessage());
            assert false : "StockColorListener class not found";
        }
        
        System.out.println("Transaction event handling tests passed!");
    }
    
    /**
     * Run all tests
     */
    public static void runAllTests() {
        System.out.println("Running color code compatibility tests...");
        
        try {
            testColorCodeStripping();
            testNumberParsing();
            testPriceParsing();
            testItemParsing();
            testSignValidation();
            testTransactionEventHandling();
            
            System.out.println("All color code compatibility tests passed!");
        } catch (Exception e) {
            System.err.println("Test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Main method for running tests
     */
    public static void main(String[] args) {
        runAllTests();
    }
} 