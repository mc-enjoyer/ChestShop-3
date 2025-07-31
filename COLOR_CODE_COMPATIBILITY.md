# Color Code Compatibility System

## Overview

This document explains how the ChestShop plugin handles color codes in sign lines to prevent conflicts between the stock color indicator system and the sign parsing logic.

## Problem

When the stock color indicator system adds `ChatColor.GREEN` or `ChatColor.RED` to sign lines, these color codes could interfere with the existing sign parsing logic that reads:
- Player names (line 0)
- Quantities (line 1) 
- Prices (line 2)
- Item names (line 3)

## Solution

The plugin implements a comprehensive color code stripping system that ensures all parsing methods work correctly regardless of color codes.

## Key Components

### 1. SignUtil Class

**Location**: `src/main/java/com/Acrobot/Breeze/Utils/SignUtil.java`

**Purpose**: Centralized utility for stripping color codes from sign lines

**Methods**:
- `getCleanLine(String line)`: Strips color codes from a sign line
- `getCleanLineSafe(String line)`: Strips color codes and handles null lines safely

### 2. Updated Parsing Methods

All key parsing methods have been updated to use clean lines:

#### PriceUtil.java
- `get(String text, char indicator)`: Now strips color codes before parsing
- `isPrice(String text)`: Now strips color codes before validation

#### MaterialUtil.java  
- `getItem(String itemName)`: Now strips color codes before parsing item names

#### NumberUtil.java
- All number parsing methods work with clean text (no changes needed since they receive clean input)

### 3. Updated Validation Methods

#### ChestShopSign.java
- `isValid(String[] line)`: Now strips color codes from all lines before validation
- `isAdminShop(Sign sign)`: Now uses clean lines for admin shop detection
- `canAccess(Player player, Sign sign)`: Now uses clean lines for access checks

#### PlayerInteract.java
- `preparePreTransactionEvent()`: Now uses clean lines for all sign parsing

### 4. Updated Event Listeners

#### PreShopCreation Listeners
- `QuantityChecker.java`: Now uses clean lines for quantity validation
- `PriceChecker.java`: Now uses clean lines for price validation  
- `ItemChecker.java`: Now uses clean lines for item validation

#### Other Listeners
- `SignBreak.java`: Now uses clean lines for permission checks
- `uBlock.java`: Now uses clean lines for sign comparison

### 5. Updated Commands and Logging

- `ReloadSigns.java`: Now displays clean owner names
- `ChestShop.java`: Now logs clean owner names
- `Plugins/ChestShop.java`: Now uses clean lines for protection checks

## How It Works

### 1. Color Code Stripping

When any parsing method needs to read a sign line, it first calls:
```java
String cleanLine = SignUtil.getCleanLineSafe(sign.getLine(lineIndex));
```

This removes all `ChatColor` codes from the line while preserving the actual content.

### 2. Stock Color Updates

When the stock color system updates a sign:
```java
// StockColorUtil.updateSignColor()
String cleanLine = ChatColor.stripColor(line);
sign.setLine(i, color + cleanLine);
```

This ensures that:
1. Existing color codes are stripped
2. New color codes are added
3. The actual content remains unchanged

### 3. Parsing Compatibility

All parsing methods now receive clean text:
- **Price parsing**: `"§aB 10 S 5"` → `"B 10 S 5"`
- **Item parsing**: `"§cDIAMOND_SWORD"` → `"DIAMOND_SWORD"`  
- **Number parsing**: `"§a64"` → `"64"`
- **Name parsing**: `"§aTestPlayer"` → `"TestPlayer"`

## Testing

A comprehensive test class is available at `src/main/java/com/Acrobot/ChestShop/Utils/ColorCodeTest.java` that verifies:

1. **Color Code Stripping**: Ensures color codes are properly removed
2. **Number Parsing**: Tests integer and double parsing with colors
3. **Price Parsing**: Tests buy/sell price parsing with colors
4. **Item Parsing**: Tests item name parsing with colors
5. **Sign Validation**: Tests complete sign validation with colors

## Benefits

1. **No Conflicts**: Stock colors don't interfere with sign parsing
2. **Backward Compatibility**: Existing signs continue to work
3. **Visual Feedback**: Players can see stock status at a glance
4. **Performance**: Color stripping is fast and efficient
5. **Maintainable**: Centralized color handling in SignUtil

## Configuration

The stock color system can be enabled/disabled via configuration:

```yaml
# Enable stock color indicators on ChestShop signs
ENABLE_STOCK_COLOR_INDICATORS: true

# How often to update stock colors (in minutes)
STOCK_COLOR_UPDATE_INTERVAL: 1

# How many signs to update per batch
STOCK_COLOR_BATCH_SIZE: 10
```

## Troubleshooting

### Signs Not Parsing Correctly
- Ensure all parsing methods use `SignUtil.getCleanLineSafe()`
- Check that color codes are being stripped before validation

### Stock Colors Not Updating
- Verify `ENABLE_STOCK_COLOR_INDICATORS` is enabled
- Check that `StockColorListener` is registered
- Ensure `StockColorUpdateTask` is running

### Performance Issues
- Reduce `STOCK_COLOR_UPDATE_INTERVAL` if updates are too frequent
- Increase `STOCK_COLOR_BATCH_SIZE` if updates are too slow

## Future Enhancements

1. **Custom Colors**: Allow configuration of stock colors
2. **Partial Coloring**: Color only specific parts of sign lines
3. **Animation**: Animate color changes for better visibility
4. **Sound Effects**: Add audio feedback for stock changes 