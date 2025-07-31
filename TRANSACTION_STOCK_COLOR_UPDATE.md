# Transaction-Based Stock Color Update

## Overview

This document explains how the ChestShop plugin automatically updates sign colors when transactions (buy/sell) occur, ensuring that stock indicators remain accurate after each transaction.

## How It Works

### Transaction Event Handling

The `StockColorListener` class includes a `TransactionEvent` handler that automatically updates sign colors whenever a transaction occurs:

```java
@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
public void onTransaction(TransactionEvent event) {
    if (!Properties.ENABLE_STOCK_COLOR_INDICATORS) {
        return;
    }
    
    Sign sign = event.getSign();
    if (sign != null && ChestShopSign.isValid(sign)) {
        // Schedule the update for the next tick to ensure inventory changes are processed
        ChestShop.getBukkitServer().getScheduler().runTask(ChestShop.getPlugin(), new Runnable() {
            @Override
            public void run() {
                StockColorUtil.updateSignColor(sign);
            }
        });
    }
}
```

### Key Features

1. **Automatic Trigger**: The color update is triggered automatically whenever a buy or sell transaction occurs
2. **Configuration Check**: Only updates colors if `ENABLE_STOCK_COLOR_INDICATORS` is enabled
3. **Sign Validation**: Ensures the sign is a valid ChestShop sign before updating
4. **Delayed Execution**: Schedules the update for the next tick to ensure inventory changes are fully processed
5. **Error Handling**: The update is wrapped in a try-catch block to prevent transaction failures

### Update Process

When a transaction occurs:

1. **Transaction Completes**: Player buys or sells items from the shop
2. **Event Fired**: `TransactionEvent` is fired with the sign and transaction details
3. **Listener Triggered**: `StockColorListener.onTransaction()` is called
4. **Color Check**: `StockColorUtil.updateSignColor()` checks the connected chest's inventory
5. **Color Applied**: Sign text is colored green (in stock) or red (out of stock)
6. **Sign Updated**: The sign is updated on the server

### Integration with Other Systems

This feature works seamlessly with other stock color update mechanisms:

- **Shop Creation**: Colors are set when shops are first created
- **Container Events**: Colors update when chests are opened/closed
- **Periodic Updates**: Background task updates all signs periodically
- **Manual Updates**: Admin command can trigger full updates

## Benefits

1. **Real-Time Accuracy**: Sign colors immediately reflect current stock after transactions
2. **User Experience**: Players can instantly see if items are available
3. **Server Performance**: Updates are scheduled efficiently to avoid lag
4. **Reliability**: Multiple update mechanisms ensure colors stay accurate

## Configuration

The feature is controlled by the `ENABLE_STOCK_COLOR_INDICATORS` property in `Properties.java`:

```java
@ConfigurationComment("Enable stock color indicators on ChestShop signs (green = in stock, red = out of stock)")
public static boolean ENABLE_STOCK_COLOR_INDICATORS = true;
```

## Technical Details

### Event Priority

The listener uses `EventPriority.MONITOR` to ensure it runs after the transaction is fully processed but before other monitoring listeners.

### Thread Safety

The color update is scheduled on the main thread using `runTask()` to ensure thread safety when accessing Bukkit APIs.

### Error Prevention

The update is wrapped in error handling to prevent transaction failures if the color update encounters issues.

## Example Scenarios

### Buy Transaction
1. Player buys 64 diamonds from a shop
2. Diamonds are removed from the chest
3. `TransactionEvent` is fired
4. `StockColorListener` updates the sign color
5. If no diamonds remain, sign turns red
6. If diamonds still remain, sign stays green

### Sell Transaction
1. Player sells 32 emeralds to a shop
2. Emeralds are added to the chest
3. `TransactionEvent` is fired
4. `StockColorListener` updates the sign color
5. Sign turns green (now has stock)

## Troubleshooting

### Colors Not Updating
- Check that `ENABLE_STOCK_COLOR_INDICATORS` is enabled
- Verify the sign is a valid ChestShop sign
- Check server logs for error messages

### Performance Issues
- The update is scheduled for the next tick to avoid blocking the transaction
- Only valid signs are processed
- Error handling prevents cascading failures

## Related Components

- `StockColorListener`: Main listener for transaction events
- `StockColorUtil`: Utility class for color updates
- `TransactionEvent`: Bukkit event fired after transactions
- `Properties`: Configuration management
- `ChestShopSign`: Sign validation utilities 