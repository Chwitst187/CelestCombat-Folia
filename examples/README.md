# CelestCombat-Pro API Examples

This directory contains example code demonstrating how to integrate with the CelestCombat-Pro API.

## Files

- **ExamplePlugin.java** - Complete example plugin showing API integration
- **plugin.yml** - Plugin configuration for the example
- **README.md** - This file

## Example Plugin Features

The example plugin demonstrates:

### Basic API Usage
- Checking if players are in combat
- Getting combat information (time remaining, opponent)
- Listing all players in combat
- Using the new safe disconnect feature

### Event Handling
- **PreCombatEvent** - Prevent combat in spawn areas, newbie protection
- **CombatEvent** - Custom combat start effects and notifications
- **CombatEndEvent** - Combat end handling with XP rewards
- **CombatLogEvent** - Custom combat log handling with VIP exceptions
- **EnderPearlEvent** - Restrict ender pearl usage in certain areas
- **TridentEvent** - Handle trident usage events

### Command Integration
- Block teleportation commands during combat
- Custom combat info command
- Admin commands for managing combat

### Advanced Features
- Safe player disconnection without penalties
- Combat time-based XP rewards
- Permission-based protection systems
- Location-based restrictions

## Commands

- `/combatinfo` - Show your combat status
- `/combatlist` - List all players in combat (admin)
- `/safedisconnect <player>` - Safely disconnect a player (admin)

## Permissions

- `example.combatinfo` - Use combat info command (default: true)
- `example.admin` - Use admin commands (default: op)
- `example.newbie` - Newbie protection from combat (default: false)
- `example.vip.nopunish` - No combat log punishment (default: false)

## How to Use

1. Copy the ExamplePlugin.java to your plugin source directory
2. Update the package name to match your plugin
3. Copy the plugin.yml and modify as needed
4. Add CelestCombat-Pro as a dependency in your build file
5. Compile and test!

## Integration Tips

1. Always check if the API is available before using it
2. Handle null returns gracefully
3. Use events to react to combat state changes
4. Test with different scenarios (combat start/end, logout, etc.)
5. Consider permission-based features for different player types

## Support

For more detailed documentation, see the main API_USAGE_GUIDE.md file.