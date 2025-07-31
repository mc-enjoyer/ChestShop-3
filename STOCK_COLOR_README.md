# ChestShop Stock Color Indicators

## Overview

The ChestShop plugin now includes an automatic stock color indicator system that visually shows whether a shop has items in stock by changing the sign text color.

## Features

### Automatic Color Updates
- **Green Text**: Indicates the shop has stock of the specified item
- **Red Text**: Indicates the shop is out of stock
- **Real-time Updates**: Colors update when containers are opened/closed
- **Sign Creation**: Colors are set when shops are created

### Periodic Updates
- **Background Task**: Automatically checks all signs every minute (configurable)
- **Batch Processing**: Updates signs in small batches to prevent server lag
- **Configurable**: Update interval and batch size can be adjusted

### Manual Updates
- **Admin Command**: `/csUpdateStockColors` to manually update all signs
- **Async Processing**: Command runs asynchronously to avoid blocking the server

## Configuration

### Properties (config.yml)

```yaml
# Enable stock color indicators on ChestShop signs (green = in stock, red = out of stock)
ENABLE_STOCK_COLOR_INDICATORS: true

# How often to update stock colors for all signs (in minutes)
STOCK_COLOR_UPDATE_INTERVAL: 0.5

# How many signs to update per batch during periodic updates (to prevent lag)
STOCK_COLOR_BATCH_SIZE: 10
```

### Configuration Options

- **ENABLE_STOCK_COLOR_INDICATORS**: Enable/disable the entire system
- **STOCK_COLOR_UPDATE_INTERVAL**: How often to check all signs (in minutes)
- **STOCK_COLOR_BATCH_SIZE**: Number of signs to process per batch

## How It Works

### Stock Detection
1. **Item Parsing**: The system reads the item name from the sign's third line
2. **Inventory Check**: Searches the connected chest for at least 1 of the specified item
3. **Color Assignment**: Sets all sign text to green (in stock) or red (out of stock)

### Update Triggers
1. **Sign Creation**: Colors are set when a new shop is created (handles both ShopCreatedEvent and SignChangeEvent)
2. **Inventory Changes**: Colors update when items are moved in chests
3. **Transaction Events**: Colors update immediately after buy/sell transactions
4. **Periodic Updates**: Background task checks all signs every 30 seconds
5. **Manual Updates**: Admin command for immediate updates

### Performance Optimization
- **Batch Processing**: Signs are updated in small groups to prevent lag
- **Async Operations**: Background tasks run asynchronously
- **Early Termination**: Stops checking after finding 1 item (efficiency)
- **Error Handling**: Individual sign failures don't stop the entire process

## Commands

### `/csUpdateStockColors`
- **Permission**: `ChestShop.admin`
- **Description**: Manually updates stock colors for all ChestShop signs
- **Usage**: `/csUpdateStockColors`
- **Response**: Confirmation message when complete

## Implementation Details

### Components

1. **StockColorUtil.java** - Core utility class
   - `hasStock(Sign sign)` - Check if a sign has stock
   - `updateSignColor(Sign sign)` - Update a single sign's color
   - `updateAllSignColors()` - Update all signs at once
   - `updateSignColorsBatch()` - Update signs in batches

2. **StockColorListener.java** - Event listener
   - Handles sign creation events (ShopCreatedEvent and SignChangeEvent)
   - Handles inventory click/drag events
   - Handles transaction events (buy/sell)
   - Schedules updates for the next tick

3. **StockColorUpdateTask.java** - Background task
   - Runs periodically to update all signs
   - Processes signs in configurable batches
   - Handles errors gracefully

4. **UpdateStockColors.java** - Admin command
   - Manual trigger for color updates
   - Runs asynchronously to avoid blocking

### Event Flow

1. **Sign Creation**: `ShopCreatedEvent`/`SignChangeEvent` → `StockColorListener` → `StockColorUtil.updateSignColor()`
2. **Inventory Changes**: `InventoryClickEvent`/`InventoryDragEvent` → `StockColorListener` → `StockColorUtil.updateSignColor()`
3. **Transaction**: `TransactionEvent` → `StockColorListener` → `StockColorUtil.updateSignColor()`
4. **Periodic Update**: `StockColorUpdateTask` → `StockColorUtil.updateSignColorsBatch()`

## Benefits

1. **Visual Feedback**: Players can quickly see which shops have stock
2. **Immediate Updates**: Colors update instantly after buy/sell transactions
3. **Reduced Lag**: Batch processing prevents server performance issues
4. **Automatic Updates**: No manual intervention required
5. **Configurable**: Server admins can adjust settings as needed
6. **Error Resilient**: Individual failures don't break the system

## Troubleshooting

### Signs Not Updating Colors
- Check that `ENABLE_STOCK_COLOR_INDICATORS` is set to `true`
- Verify the signs are valid ChestShop signs
- Check server logs for error messages
- Try the manual update command: `/csUpdateStockColors`

### Performance Issues
- Reduce `STOCK_COLOR_BATCH_SIZE` for slower servers
- Increase `STOCK_COLOR_UPDATE_INTERVAL` to check less frequently
- Monitor server logs for batch processing messages

### Color Not Changing
- Ensure the connected chest contains the correct item
- Check that the item name on the sign matches the item in the chest
- Verify the chest is properly connected to the sign

## Notes

- Colors are applied to all 4 lines of the sign
- The system only checks for 1 item (doesn't count total quantity)
- Updates are scheduled for the next tick to ensure inventory changes are processed
- The system works with both single and double chests
- Admin shops are also supported 