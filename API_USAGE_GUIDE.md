# CelestCombat-Pro API Usage Guide

This guide explains how to use the CelestCombat-Pro API in your own plugins to interact with the combat system.

## Table of Contents
- [Getting Started](#getting-started)
- [Basic Usage](#basic-usage)
- [Combat Management](#combat-management)
- [Cooldown Systems](#cooldown-systems)
- [Event Handling](#event-handling)
- [Advanced Features](#advanced-features)
- [Code Examples](#code-examples)

## Getting Started

### 1. Add Dependency

First, add CelestCombat-Pro as a dependency in your plugin.

**Maven (pom.xml):**
```xml
<dependencies>
    <dependency>
        <groupId>com.shyamstudio</groupId>
        <artifactId>celestcombat-pro</artifactId>
        <version>LATEST</version>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

**Gradle (build.gradle):**
```gradle
dependencies {
    compileOnly 'com.shyamstudio:celestcombat-pro:LATEST'
}
```

### 2. Plugin Dependencies

Add CelestCombat-Pro as a dependency in your `plugin.yml`:

```yaml
name: YourPlugin
version: 1.0.0
main: com.yourpackage.YourPlugin
depend: [CelestCombat-Pro]
# or use soft-depend if it's optional
soft-depend: [CelestCombat-Pro]
```

### 3. Getting the API Instance

```java
import com.shyamstudio.celestCombatPro.api.CelestCombatAPI;
import com.shyamstudio.celestCombatPro.api.CombatAPI;

public class YourPlugin extends JavaPlugin {
    private CombatAPI combatAPI;
    
    @Override
    public void onEnable() {
        // Check if CelestCombat-Pro is available
        if (getServer().getPluginManager().getPlugin("CelestCombat-Pro") == null) {
            getLogger().warning("CelestCombat-Pro not found! Disabling integration.");
            return;
        }
        
        // Get the API instance
        combatAPI = CelestCombatAPI.getInstance();
        
        if (combatAPI == null) {
            getLogger().warning("Failed to get CelestCombat-Pro API!");
            return;
        }
        
        getLogger().info("CelestCombat-Pro API integration enabled!");
    }
}
```

## Basic Usage

### Check if Player is in Combat

```java
import org.bukkit.entity.Player;

public boolean isPlayerInCombat(Player player) {
    return combatAPI.isInCombat(player);
}

// Usage example
@EventHandler
public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
    Player player = event.getPlayer();
    String command = event.getMessage().toLowerCase();
    
    if (command.startsWith("/home") || command.startsWith("/spawn")) {
        if (combatAPI.isInCombat(player)) {
            player.sendMessage("§cYou cannot use this command while in combat!");
            event.setCancelled(true);
        }
    }
}
```

### Get Combat Information

```java
public void showCombatInfo(Player player) {
    if (!combatAPI.isInCombat(player)) {
        player.sendMessage("§aYou are not in combat.");
        return;
    }
    
    // Get remaining combat time
    int remainingTime = combatAPI.getRemainingCombatTime(player);
    
    // Get combat opponent
    Player opponent = combatAPI.getCombatOpponent(player);
    String opponentName = (opponent != null) ? opponent.getName() : "Unknown";
    
    player.sendMessage("§cCombat Info:");
    player.sendMessage("§7- Time remaining: §f" + remainingTime + "s");
    player.sendMessage("§7- Fighting against: §f" + opponentName);
}
```

## Combat Management

### Tag Players in Combat

```java
import com.shyamstudio.celestCombatPro.api.events.PreCombatEvent;

public void tagPlayersInCombat(Player player1, Player player2) {
    // Tag both players in combat with each other
    combatAPI.tagPlayer(player1, player2, PreCombatEvent.CombatCause.PLAYER_ATTACK);
    combatAPI.tagPlayer(player2, player1, PreCombatEvent.CombatCause.PLAYER_ATTACK);
}

// Example: Custom PvP arena system
@EventHandler
public void onArenaFight(CustomArenaStartEvent event) {
    Player player1 = event.getPlayer1();
    Player player2 = event.getPlayer2();
    
    // Tag both players in combat when arena fight starts
    tagPlayersInCombat(player1, player2);
    
    player1.sendMessage("§cArena fight started! You are now in combat.");
    player2.sendMessage("§cArena fight started! You are now in combat.");
}
```

### Remove Players from Combat

```java
public void removeFromCombat(Player player, boolean silent) {
    if (!combatAPI.isInCombat(player)) {
        return;
    }
    
    if (silent) {
        // Remove silently (no message to player)
        combatAPI.removeFromCombatSilently(player);
    } else {
        // Remove with message
        combatAPI.removeFromCombat(player);
    }
}

// Example: Safe zone protection
@EventHandler
public void onPlayerEnterSafeZone(PlayerMoveEvent event) {
    Player player = event.getPlayer();
    Location to = event.getTo();
    
    if (isInSafeZone(to)) {
        if (combatAPI.isInCombat(player)) {
            // Remove from combat when entering safe zone
            removeFromCombat(player, false);
            player.sendMessage("§aYou entered a safe zone and are no longer in combat.");
        }
    }
}
```

### Safe Disconnect (NEW)

```java
public void safelyDisconnectPlayer(Player player, String reason) {
    // This disconnects the player without combat log punishment
    // Their combat tag remains active and continues after reconnect
    combatAPI.disconnectPlayerSafely(player);
    
    // Note: The method handles the disconnection internally
    // You can also add custom logic before calling it
    getLogger().info("Safely disconnected " + player.getName() + ": " + reason);
}

// Example: Maintenance mode
public void enableMaintenanceMode() {
    for (Player player : Bukkit.getOnlinePlayers()) {
        if (combatAPI.isInCombat(player)) {
            // Safely disconnect combat players during maintenance
            safelyDisconnectPlayer(player, "Server maintenance");
        } else {
            player.kickPlayer("Server is entering maintenance mode");
        }
    }
}
```

## Cooldown Systems

### Ender Pearl Cooldowns

```java
public void handleEnderPearlUsage(Player player) {
    // Check if ender pearl is on cooldown
    if (combatAPI.isEnderPearlOnCooldown(player)) {
        int remaining = combatAPI.getRemainingEnderPearlCooldown(player);
        player.sendMessage("§cEnder Pearl is on cooldown for " + remaining + " seconds!");
        return;
    }
    
    // Set cooldown after use
    combatAPI.setEnderPearlCooldown(player);
    
    // Your custom ender pearl logic here
    player.sendMessage("§aEnder Pearl used!");
}
```

### Trident Cooldowns

```java
public void handleTridentUsage(Player player) {
    // Check if trident is banned in this world
    if (combatAPI.isTridentBanned(player)) {
        player.sendMessage("§cTridents are banned in this world!");
        return;
    }
    
    // Check cooldown
    if (combatAPI.isTridentOnCooldown(player)) {
        int remaining = combatAPI.getRemainingTridentCooldown(player);
        player.sendMessage("§cTrident is on cooldown for " + remaining + " seconds!");
        return;
    }
    
    // Set cooldown after use
    combatAPI.setTridentCooldown(player);
}
```

## Event Handling

### Listen to Combat Events

```java
import com.shyamstudio.celestCombatPro.api.events.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class CombatListener implements Listener {
    
    @EventHandler
    public void onPreCombat(PreCombatEvent event) {
        Player player = event.getPlayer();
        Player attacker = event.getAttacker();
        
        // You can cancel the combat here
        if (player.hasPermission("combat.bypass")) {
            event.setCancelled(true);
            attacker.sendMessage("§c" + player.getName() + " cannot be tagged in combat!");
            return;
        }
        
        // Custom logic before combat starts
        player.sendMessage("§cYou are about to enter combat with " + attacker.getName() + "!");
    }
    
    @EventHandler
    public void onCombatStart(CombatEvent event) {
        Player player = event.getPlayer();
        Player attacker = event.getAttacker();
        boolean wasAlreadyInCombat = event.wasAlreadyInCombat();
        
        if (!wasAlreadyInCombat) {
            // First time entering combat
            player.sendMessage("§cYou are now in combat with " + attacker.getName() + "!");
            
            // Custom effects
            player.playSound(player.getLocation(), Sound.ENTITY_WITHER_HURT, 1.0f, 1.0f);
        }
    }
    
    @EventHandler
    public void onCombatEnd(CombatEndEvent event) {
        Player player = event.getPlayer();
        CombatEndEvent.CombatEndReason reason = event.getReason();
        long totalCombatTime = event.getTotalCombatTime();
        
        switch (reason) {
            case EXPIRED:
                player.sendMessage("§aCombat timer expired. You are safe!");
                break;
            case LOGOUT:
                // Player logged out during combat
                getLogger().info(player.getName() + " logged out during combat");
                break;
            case ADMIN_REMOVE:
                player.sendMessage("§aYou were removed from combat by an admin.");
                break;
        }
        
        // Award experience based on combat time
        if (totalCombatTime > 30000) { // 30 seconds
            player.giveExp(10);
            player.sendMessage("§a+10 XP for surviving combat!");
        }
    }
    
    @EventHandler
    public void onCombatLog(CombatLogEvent event) {
        Player player = event.getPlayer();
        Player lastAttacker = event.getLastAttacker();
        
        // You can prevent punishment here
        if (player.hasPermission("combat.nopunish")) {
            event.setShouldPunish(false);
            return;
        }
        
        // Custom combat log handling
        if (lastAttacker != null) {
            lastAttacker.sendMessage("§a" + player.getName() + " logged out during combat!");
        }
    }
}
```

### Ender Pearl and Trident Events

```java
@EventHandler
public void onEnderPearlUse(EnderPearlEvent event) {
    Player player = event.getPlayer();
    
    // Custom restrictions
    if (player.getWorld().getName().equals("spawn")) {
        event.setCancelled(true);
        player.sendMessage("§cEnder pearls are disabled in spawn!");
    }
}

@EventHandler
public void onTridentUse(TridentEvent event) {
    Player player = event.getPlayer();
    
    if (event.isBanned()) {
        player.sendMessage("§cTridents are banned in this world!");
    }
}
```

## Advanced Features

### Get All Players in Combat

```java
import java.util.Map;
import java.util.UUID;

public void showAllCombatPlayers(CommandSender sender) {
    Map<UUID, Long> playersInCombat = combatAPI.getPlayersInCombat();
    
    if (playersInCombat.isEmpty()) {
        sender.sendMessage("§aNo players are currently in combat.");
        return;
    }
    
    sender.sendMessage("§cPlayers in combat (" + playersInCombat.size() + "):");
    
    for (UUID uuid : playersInCombat.keySet()) {
        Player player = Bukkit.getPlayer(uuid);
        if (player != null) {
            int remaining = combatAPI.getRemainingCombatTime(player);
            Player opponent = combatAPI.getCombatOpponent(player);
            String opponentName = (opponent != null) ? opponent.getName() : "Unknown";
            
            sender.sendMessage("§7- §f" + player.getName() + " §7(vs " + opponentName + ", " + remaining + "s)");
        }
    }
}
```

### Configuration Access

```java
public void showCombatSettings(CommandSender sender) {
    long combatDuration = combatAPI.getCombatDuration();
    long pearlCooldown = combatAPI.getEnderPearlCooldownDuration();
    long tridentCooldown = combatAPI.getTridentCooldownDuration();
    boolean flightDisabled = combatAPI.isFlightDisabledInCombat();
    
    sender.sendMessage("§eCombat Settings:");
    sender.sendMessage("§7- Combat Duration: §f" + combatDuration + "s");
    sender.sendMessage("§7- Pearl Cooldown: §f" + pearlCooldown + "s");
    sender.sendMessage("§7- Trident Cooldown: §f" + tridentCooldown + "s");
    sender.sendMessage("§7- Flight Disabled: §f" + flightDisabled);
}

public boolean isFeatureEnabledInWorld(String worldName) {
    boolean pearlEnabled = combatAPI.isEnderPearlCooldownEnabledInWorld(worldName);
    boolean tridentEnabled = combatAPI.isTridentCooldownEnabledInWorld(worldName);
    boolean tridentBanned = combatAPI.isTridentBannedInWorld(worldName);
    
    return pearlEnabled || tridentEnabled || !tridentBanned;
}
```

## Code Examples

### Complete Integration Example

```java
package com.yourpackage.yourplugin;

import com.shyamstudio.celestCombatPro.api.CelestCombatAPI;
import com.shyamstudio.celestCombatPro.api.CombatAPI;
import com.shyamstudio.celestCombatPro.api.events.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class YourPlugin extends JavaPlugin implements Listener {
    private CombatAPI combatAPI;
    
    @Override
    public void onEnable() {
        // Initialize API
        if (!initializeCombatAPI()) {
            getLogger().warning("CelestCombat-Pro integration disabled!");
            return;
        }
        
        // Register events
        getServer().getPluginManager().registerEvents(this, this);
        
        getLogger().info("CelestCombat-Pro integration enabled!");
    }
    
    private boolean initializeCombatAPI() {
        if (getServer().getPluginManager().getPlugin("CelestCombat-Pro") == null) {
            return false;
        }
        
        combatAPI = CelestCombatAPI.getInstance();
        return combatAPI != null;
    }
    
    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String command = event.getMessage().toLowerCase();
        
        // Block certain commands during combat
        if (isBlockedCommand(command) && combatAPI.isInCombat(player)) {
            int remaining = combatAPI.getRemainingCombatTime(player);
            player.sendMessage("§cYou cannot use this command while in combat! (" + remaining + "s remaining)");
            event.setCancelled(true);
        }
    }
    
    private boolean isBlockedCommand(String command) {
        return command.startsWith("/home") || 
               command.startsWith("/spawn") || 
               command.startsWith("/warp") ||
               command.startsWith("/tpa");
    }
    
    @EventHandler
    public void onCombatStart(CombatEvent event) {
        Player player = event.getPlayer();
        
        if (!event.wasAlreadyInCombat()) {
            // Custom combat start effects
            player.sendTitle("§cCOMBAT", "§7You are now in combat!", 10, 40, 10);
        }
    }
    
    @EventHandler
    public void onCombatEnd(CombatEndEvent event) {
        Player player = event.getPlayer();
        
        // Custom combat end effects
        player.sendTitle("§aSAFE", "§7You are no longer in combat", 10, 40, 10);
    }
    
    // Public API for other plugins
    public boolean isPlayerInCombat(Player player) {
        return combatAPI != null && combatAPI.isInCombat(player);
    }
    
    public void removePlayerFromCombat(Player player) {
        if (combatAPI != null) {
            combatAPI.removeFromCombat(player);
        }
    }
}
```

### Custom Arena Integration

```java
public class ArenaManager {
    private final CombatAPI combatAPI;
    
    public ArenaManager() {
        this.combatAPI = CelestCombatAPI.getInstance();
    }
    
    public void startArenaFight(Player player1, Player player2) {
        // Tag both players in combat
        combatAPI.tagPlayer(player1, player2, PreCombatEvent.CombatCause.PLAYER_ATTACK);
        combatAPI.tagPlayer(player2, player1, PreCombatEvent.CombatCause.PLAYER_ATTACK);
        
        // Teleport to arena
        Location arena = getArenaLocation();
        player1.teleport(arena.clone().add(5, 0, 0));
        player2.teleport(arena.clone().add(-5, 0, 0));
        
        // Send messages
        player1.sendMessage("§cArena fight started against " + player2.getName() + "!");
        player2.sendMessage("§cArena fight started against " + player1.getName() + "!");
    }
    
    public void endArenaFight(Player winner, Player loser) {
        // Remove both from combat
        combatAPI.removeFromCombatSilently(winner);
        combatAPI.removeFromCombatSilently(loser);
        
        // Teleport back to lobby
        Location lobby = getLobbyLocation();
        winner.teleport(lobby);
        loser.teleport(lobby);
        
        // Send results
        winner.sendMessage("§aYou won the arena fight!");
        loser.sendMessage("§cYou lost the arena fight!");
    }
    
    private Location getArenaLocation() {
        // Your arena location logic
        return new Location(Bukkit.getWorld("arena"), 0, 100, 0);
    }
    
    private Location getLobbyLocation() {
        // Your lobby location logic
        return new Location(Bukkit.getWorld("world"), 0, 64, 0);
    }
}
```

## Best Practices

1. **Always check if the API is available** before using it
2. **Handle null returns** gracefully
3. **Use events** to react to combat state changes
4. **Respect player permissions** when modifying combat behavior
5. **Test thoroughly** with different scenarios
6. **Document your integration** for other developers

## Troubleshooting

### Common Issues

1. **API returns null**: Make sure CelestCombat-Pro is loaded before your plugin
2. **Events not firing**: Check if you registered your listener properly
3. **Permission errors**: Ensure your plugin has the necessary permissions
4. **Version compatibility**: Make sure you're using compatible versions

### Debug Tips

```java
// Enable debug logging
if (combatAPI == null) {
    getLogger().severe("CombatAPI is null! Check if CelestCombat-Pro is installed.");
    return;
}

// Log combat state changes
getLogger().info("Player " + player.getName() + " combat state: " + combatAPI.isInCombat(player));
```


*This guide covers CelestCombat-Pro API version 1.0+*