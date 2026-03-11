package com.example.celestcombat;

import com.shyamstudio.celestCombatPro.api.CelestCombatAPI;
import com.shyamstudio.celestCombatPro.api.CombatAPI;
import com.shyamstudio.celestCombatPro.api.events.*;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Example plugin demonstrating CelestCombat-Pro API integration
 * 
 * Features demonstrated:
 * - Basic combat state checking
 * - Command blocking during combat
 * - Event listening
 * - Safe disconnect functionality
 * - Combat information display
 */
public class ExamplePlugin extends JavaPlugin implements Listener {
    
    private CombatAPI combatAPI;
    
    @Override
    public void onEnable() {
        // Initialize CelestCombat-Pro API
        if (!initializeCombatAPI()) {
            getLogger().warning("CelestCombat-Pro not found! Plugin functionality will be limited.");
            return;
        }
        
        // Register event listeners
        getServer().getPluginManager().registerEvents(this, this);
        
        getLogger().info("ExamplePlugin enabled with CelestCombat-Pro integration!");
    }
    
    /**
     * Initialize the CombatAPI
     * @return true if successful, false otherwise
     */
    private boolean initializeCombatAPI() {
        // Check if CelestCombat-Pro is installed
        if (getServer().getPluginManager().getPlugin("CelestCombat-Pro") == null) {
            return false;
        }
        
        // Get the API instance
        combatAPI = CelestCombatAPI.getInstance();
        return combatAPI != null;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be used by players!");
            return true;
        }
        
        Player player = (Player) sender;
        
        switch (command.getName().toLowerCase()) {
            case "combatinfo":
                showCombatInfo(player);
                return true;
                
            case "combatlist":
                if (!player.hasPermission("example.admin")) {
                    player.sendMessage("§cNo permission!");
                    return true;
                }
                showAllCombatPlayers(player);
                return true;
                
            case "safedisconnect":
                if (!player.hasPermission("example.admin")) {
                    player.sendMessage("§cNo permission!");
                    return true;
                }
                if (args.length != 1) {
                    player.sendMessage("§cUsage: /safedisconnect <player>");
                    return true;
                }
                safeDisconnectPlayer(player, args[0]);
                return true;
                
            default:
                return false;
        }
    }
    
    /**
     * Show combat information to a player
     */
    private void showCombatInfo(Player player) {
        if (combatAPI == null) {
            player.sendMessage("§cCombat API not available!");
            return;
        }
        
        if (!combatAPI.isInCombat(player)) {
            player.sendMessage("§aYou are not in combat.");
            return;
        }
        
        int remainingTime = combatAPI.getRemainingCombatTime(player);
        Player opponent = combatAPI.getCombatOpponent(player);
        String opponentName = (opponent != null) ? opponent.getName() : "Unknown";
        
        player.sendMessage("§c§l=== COMBAT INFO ===");
        player.sendMessage("§7Status: §cIn Combat");
        player.sendMessage("§7Time Remaining: §f" + remainingTime + " seconds");
        player.sendMessage("§7Fighting Against: §f" + opponentName);
        player.sendMessage("§c§l==================");
    }
    
    /**
     * Show all players currently in combat (admin command)
     */
    private void showAllCombatPlayers(Player admin) {
        if (combatAPI == null) {
            admin.sendMessage("§cCombat API not available!");
            return;
        }
        
        var playersInCombat = combatAPI.getPlayersInCombat();
        
        if (playersInCombat.isEmpty()) {
            admin.sendMessage("§aNo players are currently in combat.");
            return;
        }
        
        admin.sendMessage("§c§l=== PLAYERS IN COMBAT ===");
        admin.sendMessage("§7Total: §f" + playersInCombat.size() + " players");
        
        for (var uuid : playersInCombat.keySet()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                int remaining = combatAPI.getRemainingCombatTime(player);
                Player opponent = combatAPI.getCombatOpponent(player);
                String opponentName = (opponent != null) ? opponent.getName() : "Unknown";
                
                admin.sendMessage("§7- §f" + player.getName() + " §7vs §f" + opponentName + " §7(" + remaining + "s)");
            }
        }
        admin.sendMessage("§c§l========================");
    }
    
    /**
     * Safely disconnect a player without combat log penalty
     */
    private void safeDisconnectPlayer(Player admin, String targetName) {
        if (combatAPI == null) {
            admin.sendMessage("§cCombat API not available!");
            return;
        }
        
        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            admin.sendMessage("§cPlayer '" + targetName + "' not found!");
            return;
        }
        
        // Use the new safe disconnect API
        combatAPI.disconnectPlayerSafely(target);
        admin.sendMessage("§aSuccessfully disconnected " + target.getName() + " safely!");
        
        // Log the action
        getLogger().info(admin.getName() + " safely disconnected " + target.getName());
    }
    
    // ===== EVENT HANDLERS =====
    
    /**
     * Block certain commands during combat
     */
    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        if (combatAPI == null) return;
        
        Player player = event.getPlayer();
        String command = event.getMessage().toLowerCase();
        
        // List of commands to block during combat
        String[] blockedCommands = {"/home", "/spawn", "/warp", "/tpa", "/tpahere", "/back"};
        
        for (String blocked : blockedCommands) {
            if (command.startsWith(blocked)) {
                if (combatAPI.isInCombat(player)) {
                    int remaining = combatAPI.getRemainingCombatTime(player);
                    player.sendMessage("§c§lCOMBAT BLOCKED!");
                    player.sendMessage("§cYou cannot use " + blocked + " while in combat!");
                    player.sendMessage("§cTime remaining: §f" + remaining + " seconds");
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 0.5f);
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }
    
    /**
     * Handle combat start events
     */
    @EventHandler
    public void onCombatStart(CombatEvent event) {
        Player player = event.getPlayer();
        Player attacker = event.getAttacker();
        
        if (!event.wasAlreadyInCombat()) {
            // First time entering combat
            player.sendMessage("§c§l⚔ COMBAT STARTED ⚔");
            player.sendMessage("§cYou are now fighting " + attacker.getName() + "!");
            player.sendTitle("§c§lCOMBAT", "§7Fighting " + attacker.getName(), 10, 40, 10);
            
            // Play combat sound
            player.playSound(player.getLocation(), Sound.ENTITY_WITHER_HURT, 1.0f, 1.0f);
            
            // Log the combat start
            getLogger().info("Combat started: " + player.getName() + " vs " + attacker.getName());
        }
    }
    
    /**
     * Handle combat end events
     */
    @EventHandler
    public void onCombatEnd(CombatEndEvent event) {
        Player player = event.getPlayer();
        CombatEndEvent.CombatEndReason reason = event.getReason();
        long totalCombatTime = event.getTotalCombatTime();
        
        switch (reason) {
            case EXPIRED:
                player.sendMessage("§a§l✓ COMBAT ENDED ✓");
                player.sendMessage("§aYou are now safe from combat!");
                player.sendTitle("§a§lSAFE", "§7Combat timer expired", 10, 40, 10);
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
                break;
                
            case LOGOUT:
                getLogger().info(player.getName() + " logged out during combat (fought for " + (totalCombatTime / 1000) + "s)");
                break;
                
            case ADMIN_REMOVE:
                player.sendMessage("§a§l✓ COMBAT ENDED ✓");
                player.sendMessage("§aYou were removed from combat by an admin.");
                break;
        }
        
        // Award experience for long fights
        if (totalCombatTime > 30000) { // 30+ seconds
            int expReward = (int) (totalCombatTime / 3000); // 1 XP per 3 seconds
            player.giveExp(expReward);
            player.sendMessage("§a+§f" + expReward + " XP §afor surviving combat!");
        }
    }
    
    /**
     * Handle combat log events
     */
    @EventHandler
    public void onCombatLog(CombatLogEvent event) {
        Player player = event.getPlayer();
        Player lastAttacker = event.getLastAttacker();
        long remainingTime = event.getRemainingTime();
        
        // Notify the attacker
        if (lastAttacker != null && lastAttacker.isOnline()) {
            lastAttacker.sendMessage("§c" + player.getName() + " logged out during combat!");
            lastAttacker.sendMessage("§7They had " + remainingTime + " seconds remaining.");
        }
        
        // Log the combat log
        getLogger().warning("COMBAT LOG: " + player.getName() + " logged out with " + remainingTime + "s remaining");
        
        // You could prevent punishment for VIP players
        if (player.hasPermission("example.vip.nopunish")) {
            event.setShouldPunish(false);
            getLogger().info("Combat log punishment prevented for VIP player: " + player.getName());
        }
    }
    
    /**
     * Handle pre-combat events (before combat starts)
     */
    @EventHandler
    public void onPreCombat(PreCombatEvent event) {
        Player player = event.getPlayer();
        Player attacker = event.getAttacker();
        
        // Example: Prevent combat in spawn area
        if (isInSpawnArea(player.getLocation())) {
            event.setCancelled(true);
            attacker.sendMessage("§cYou cannot attack players in the spawn area!");
            return;
        }
        
        // Example: Prevent combat for new players
        if (player.hasPermission("example.newbie")) {
            event.setCancelled(true);
            attacker.sendMessage("§c" + player.getName() + " has newbie protection!");
            player.sendMessage("§aYou were protected from " + attacker.getName() + "'s attack!");
            return;
        }
        
        // Warn both players
        player.sendMessage("§e⚠ " + attacker.getName() + " is attacking you! Combat will start!");
        attacker.sendMessage("§e⚠ You are attacking " + player.getName() + "! Combat will start!");
    }
    
    /**
     * Handle ender pearl events
     */
    @EventHandler
    public void onEnderPearlUse(EnderPearlEvent event) {
        Player player = event.getPlayer();
        
        // Example: Disable ender pearls in certain areas
        if (isInNoEnderPearlZone(player.getLocation())) {
            event.setCancelled(true);
            player.sendMessage("§cEnder pearls are disabled in this area!");
        }
    }
    
    /**
     * Handle trident events
     */
    @EventHandler
    public void onTridentUse(TridentEvent event) {
        Player player = event.getPlayer();
        
        if (event.isBanned()) {
            player.sendMessage("§cTridents are banned in this world!");
        } else if (event.isInCombat()) {
            player.sendMessage("§eUsing trident while in combat - be careful!");
        }
    }
    
    // ===== UTILITY METHODS =====
    
    /**
     * Check if a location is in the spawn area
     */
    private boolean isInSpawnArea(org.bukkit.Location location) {
        // Example implementation - adjust for your server
        org.bukkit.Location spawn = location.getWorld().getSpawnLocation();
        return location.distance(spawn) < 50; // 50 block radius
    }
    
    /**
     * Check if a location is in a no-ender-pearl zone
     */
    private boolean isInNoEnderPearlZone(org.bukkit.Location location) {
        // Example implementation - you could integrate with WorldGuard, etc.
        return location.getWorld().getName().equals("spawn");
    }
}