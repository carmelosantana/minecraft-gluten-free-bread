package com.carmelosantana.glutenfreebread.listeners;

import com.carmelosantana.glutenfreebread.items.GlutenFreeBreadItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Map;

/**
 * Handles consumption events for all gluten-free bread variants.
 * 
 * This listener applies different effects based on the bread type consumed
 * and handles overconsumption protection.
 */
public class ConsumptionListener implements Listener {
    
    private final Plugin plugin;
    private final Map<String, GlutenFreeBreadItem> breadItems;
    private final ConsumptionTracker consumptionTracker;
    
    // Overconsumption effects
    private static final int NAUSEA_DURATION = 15 * 20; // 15 seconds
    private static final int SLOWNESS_DURATION = 20 * 20; // 20 seconds
    private static final int SLOWNESS_AMPLIFIER = 0; // Level 1
    
    public ConsumptionListener(Plugin plugin, Map<String, GlutenFreeBreadItem> breadItems, 
                              ConsumptionTracker consumptionTracker) {
        this.plugin = plugin;
        this.breadItems = breadItems;
        this.consumptionTracker = consumptionTracker;
    }
    
    /**
     * Handle player item consumption events
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        
        // Check if the consumed item is any gluten-free bread
        if (!GlutenFreeBreadItem.isAnyGlutenFreeBread(event.getItem())) {
            return;
        }
        
        // Check permissions
        if (!player.hasPermission("glutenfreebread.consume")) {
            event.setCancelled(true);
            player.sendMessage(Component.text("You don't have permission to consume gluten-free bread!", NamedTextColor.RED));
            return;
        }
        
        // Get the bread type
        String breadType = GlutenFreeBreadItem.getBreadType(event.getItem());
        if (breadType == null) {
            plugin.getLogger().warning("Could not determine bread type for consumed item");
            return;
        }
        
        // Record consumption and check for overconsumption
        boolean isOverconsuming = consumptionTracker.recordConsumption(player);
        
        if (isOverconsuming) {
            applyOverconsumptionEffects(player, breadType);
        } else {
            applyNormalEffects(player, breadType);
        }
    }
    
    /**
     * Apply normal consumption effects based on bread type
     */
    private void applyNormalEffects(Player player, String breadType) {
        ConfigurationSection breadConfig = plugin.getConfig().getConfigurationSection("breads." + breadType);
        if (breadConfig == null) {
            plugin.getLogger().warning("No configuration found for bread type: " + breadType);
            return;
        }
        
        ConfigurationSection effectsConfig = breadConfig.getConfigurationSection("effects");
        if (effectsConfig == null) {
            plugin.getLogger().warning("No effects configuration found for bread type: " + breadType);
            return;
        }
        
        // Apply each configured effect
        for (String effectName : effectsConfig.getKeys(false)) {
            ConfigurationSection effectConfig = effectsConfig.getConfigurationSection(effectName);
            if (effectConfig == null) continue;
            
            int duration = effectConfig.getInt("duration", 10) * 20; // Convert to ticks
            int amplifier = effectConfig.getInt("amplifier", 0);
            
            PotionEffectType effectType = getPotionEffectType(effectName);
            if (effectType != null) {
                PotionEffect effect = new PotionEffect(
                    effectType,
                    duration,
                    amplifier,
                    false, // ambient
                    true,  // particles
                    true   // icon
                );
                player.addPotionEffect(effect);
            }
        }
        
        // Send positive feedback
        GlutenFreeBreadItem breadItem = breadItems.get(breadType);
        String displayName = breadItem != null ? breadItem.getDisplayName() : "Gluten-Free Bread";
        
        player.sendMessage(Component.text("You feel energized after eating ", NamedTextColor.GREEN)
            .append(Component.text(displayName, NamedTextColor.GOLD))
            .append(Component.text("!", NamedTextColor.GREEN)));
        
        plugin.getLogger().info(player.getName() + " consumed " + breadType + " gluten-free bread");
    }
    
    /**
     * Apply overconsumption effects (nausea and slowness)
     */
    private void applyOverconsumptionEffects(Player player, String breadType) {
        // Apply nausea effect
        PotionEffect nausea = new PotionEffect(
            PotionEffectType.NAUSEA,
            NAUSEA_DURATION,
            0, // Level 1
            false, // ambient
            true,  // particles
            true   // icon
        );
        
        // Apply slowness effect  
        PotionEffect slowness = new PotionEffect(
            PotionEffectType.SLOWNESS,
            SLOWNESS_DURATION,
            SLOWNESS_AMPLIFIER,
            false, // ambient
            true,  // particles
            true   // icon
        );
        
        player.addPotionEffect(nausea);
        player.addPotionEffect(slowness);
        
        // Send warning message
        player.sendMessage(Component.text("You've eaten too much gluten-free bread!", NamedTextColor.RED));
        player.sendMessage(Component.text("You feel nauseous and sluggish from overconsumption.", NamedTextColor.YELLOW));
        
        // Get current consumption count for feedback
        int consumptionCount = consumptionTracker.getConsumptionCount(player);
        int timeWindow = consumptionTracker.getConsumptionWindowSeconds() / 60; // Convert to minutes
        
        player.sendMessage(Component.text("You've consumed " + consumptionCount + 
                          " gluten-free bread in the last " + timeWindow + " minutes.", NamedTextColor.GRAY));
        
        plugin.getLogger().info(player.getName() + " is over-consuming " + breadType + " gluten-free bread (" + 
                               consumptionCount + " in " + timeWindow + " minutes)");
    }
    
    /**
     * Convert effect name string to PotionEffectType
     */
    private PotionEffectType getPotionEffectType(String effectName) {
        return switch (effectName.toLowerCase()) {
            case "regeneration" -> PotionEffectType.REGENERATION;
            case "speed" -> PotionEffectType.SPEED;
            case "fire_resistance" -> PotionEffectType.FIRE_RESISTANCE;
            case "strength" -> PotionEffectType.STRENGTH;
            case "absorption" -> PotionEffectType.ABSORPTION;
            case "night_vision" -> PotionEffectType.NIGHT_VISION;
            case "water_breathing" -> PotionEffectType.WATER_BREATHING;
            case "jump_boost" -> PotionEffectType.JUMP_BOOST;
            case "resistance" -> PotionEffectType.RESISTANCE;
            default -> {
                plugin.getLogger().warning("Unknown potion effect type: " + effectName);
                yield null;
            }
        };
    }
    
    /**
     * Clean up player data when they quit
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        consumptionTracker.clearPlayerData(event.getPlayer());
    }
}
