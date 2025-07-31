# ChestShop Sign Storage System

## Overview

The ChestShop plugin now includes an automatic sign storage system that tracks all ChestShop signs in a file-based storage system. This ensures that all valid shop signs are properly tracked and can be managed even if they weren't originally created through the standard shop creation process.

## Features

### Automatic Sign Storage
- **Shop Creation**: Signs are automatically saved to storage when shops are created
- **Shop Destruction**: Signs are automatically removed from storage when shops are destroyed
- **Transaction-Based Storage**: Signs are automatically added to storage when used in buy/sell transactions if they're not already stored

### File Storage
- Signs are stored in `plugins/ChestShop/signs.yml`
- Each sign location includes world, x, y, z coordinates
- Storage is persistent across server restarts

## Implementation Details

### Components

1. **SignStorage.java** - Utility class for managing sign storage
   - `saveSign(Sign sign)` - Save a sign location to storage
   - `removeSign(Sign sign)` - Remove a sign location from storage
   - `getValidSigns()` - Get all valid ChestShop signs from storage
   - `loadSigns()` - Load all saved sign locations

2. **SignStorageListener.java** - Handles shop creation/destruction events
   - Automatically saves signs when shops are created
   - Automatically removes signs when shops are destroyed

3. **SignStorageTransactionListener.java** - NEW - Handles transaction-based storage
   - Automatically adds signs to storage when used in transactions
   - Only adds signs that aren't already in storage
   - Logs when signs are added during transactions

### Event Flow

1. **Shop Creation**: `ShopCreatedEvent` → `SignStorageListener` → `SignStorage.saveSign()`
2. **Shop Destruction**: `ShopDestroyedEvent` → `SignStorageListener` → `SignStorage.removeSign()`
3. **Transaction**: `TransactionEvent` → `SignStorageTransactionListener` → Check if sign exists → `SignStorage.saveSign()` if needed

### Storage Format

The `signs.yml` file uses the following format:

```yaml
signs:
  world_123_64_-456:
    world: "world"
    x: 123
    y: 64
    z: -456
  world_789_65_-789:
    world: "world"
    x: 789
    y: 65
    z: -789
```

## Usage

### Automatic Operation
The system operates automatically without any user intervention. Signs are:
- Saved when shops are created
- Removed when shops are destroyed
- Added when used in transactions (if not already stored)

### Manual Commands
- `/csReloadSigns` - Reloads all signs from storage (useful after manual file edits)

### Logging
The system provides detailed logging:
- Info messages when signs are saved/removed during shop creation/destruction
- Info messages when signs are added during transactions
- Warning messages for invalid sign locations
- Severe error messages for file I/O issues

## Benefits

1. **Comprehensive Tracking**: All ChestShop signs are tracked, even if created outside normal processes
2. **Data Integrity**: Ensures no valid shops are lost due to storage issues
3. **Recovery**: Allows recovery of shop data after server issues
4. **Audit Trail**: Provides logging for all sign storage operations
5. **Performance**: Efficient file-based storage with minimal overhead

## Configuration

The sign storage system is enabled by default and requires no additional configuration. The storage file is automatically created at `plugins/ChestShop/signs.yml` when the plugin starts.

## Troubleshooting

### Signs Not Being Saved
- Check that the plugin has write permissions to the data folder
- Verify that the signs are valid ChestShop signs
- Check the server logs for error messages

### Signs Not Being Loaded
- Verify the `signs.yml` file exists and is readable
- Check that the world names in the file match current world names
- Look for warning messages in the server logs

### Performance Issues
- The system loads all signs on startup
- For servers with many signs, consider periodic cleanup of invalid locations
- Monitor the `signs.yml` file size for very large installations 