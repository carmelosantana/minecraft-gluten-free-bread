package com.carmelosantana.glutenfreebread;

import com.carmelosantana.glutenfreebread.commands.GFBreadCommand;
import com.carmelosantana.glutenfreebread.items.GlutenFreeBreadItem;
import com.carmelosantana.glutenfreebread.listeners.ConsumptionListener;
import com.carmelosantana.glutenfreebread.listeners.ConsumptionTracker;
import com.carmelosantana.glutenfreebread.recipes.GlutenFreeBreadRecipe;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

/**
 * Main plugin class for the Gluten-Free Bread plugin.
 * 
 * This plugin adds custom food items "Gluten-Free Bread" variants that provide
 * unique regeneration and other effects when consumed, with built-in overconsumption protection.
 * 
 * @author Carmelo Santana
 * @version 1.0.1
 */
public class GlutenFreeBreadPlugin extends JavaPlugin {
    
    private Map<String, GlutenFreeBreadItem> breadItems;
    private ConsumptionTracker consumptionTracker;
    private GlutenFreeBreadRecipe recipeManager;
    
    @Override
    public void onEnable() {
        getLogger().info("Enabling Gluten-Free Bread Plugin...");
        
        // Save default config
        saveDefaultConfig();
        
        // Initialize components
        initializeComponents();
        
        // Register recipes
        registerRecipes();
        
        // Register event listeners
        registerEventListeners();
        
        // Register commands
        registerCommands();
        
        getLogger().info("Gluten-Free Bread Plugin enabled successfully!");
        getLogger().info("Available bread types: " + String.join(", ", breadItems.keySet()));
    }
    
    @Override
    public void onDisable() {
        // Unregister recipes
        if (recipeManager != null) {
            recipeManager.unregisterAllRecipes();
        }
        
        getLogger().info("Gluten-Free Bread Plugin disabled.");
    }
    
    /**
     * Initialize plugin components
     */
    private void initializeComponents() {
        breadItems = new HashMap<>();
        consumptionTracker = new ConsumptionTracker();
        
        // Initialize bread items
        breadItems.put("plain", new GlutenFreeBreadItem(this, "plain"));
        breadItems.put("sweet", new GlutenFreeBreadItem(this, "sweet"));
        breadItems.put("spicy", new GlutenFreeBreadItem(this, "spicy"));
        breadItems.put("savory", new GlutenFreeBreadItem(this, "savory"));
        
        getLogger().info("Initialized " + breadItems.size() + " bread variants");
    }
    
    /**
     * Register custom recipes
     */
    private void registerRecipes() {
        if (!getConfig().getBoolean("recipe.enabled", true)) {
            getLogger().info("Recipes disabled in configuration");
            return;
        }
        
        recipeManager = new GlutenFreeBreadRecipe(this);
        recipeManager.registerAllRecipes(breadItems);
        getLogger().info("Registered all gluten-free bread recipes");
    }
    
    /**
     * Register event listeners
     */
    private void registerEventListeners() {
        getServer().getPluginManager().registerEvents(
            new ConsumptionListener(this, breadItems, consumptionTracker), this);
        getLogger().info("Registered event listeners");
    }
    
    /**
     * Register commands
     */
    private void registerCommands() {
        getCommand("gfbread").setExecutor(new GFBreadCommand(this, breadItems));
        getLogger().info("Registered commands");
    }
    
    /**
     * Get all bread items
     * @return Map of bread type to GlutenFreeBreadItem
     */
    public Map<String, GlutenFreeBreadItem> getBreadItems() {
        return breadItems;
    }
    
    /**
     * Get a specific bread item by type
     * @param type The bread type
     * @return GlutenFreeBreadItem or null if not found
     */
    public GlutenFreeBreadItem getBreadItem(String type) {
        return breadItems.get(type.toLowerCase());
    }
    
    /**
     * Get the consumption tracker instance
     * @return ConsumptionTracker instance
     */
    public ConsumptionTracker getConsumptionTracker() {
        return consumptionTracker;
    }
    
    /**
     * Reload the plugin configuration
     */
    public void reloadConfiguration() {
        reloadConfig();
        getLogger().info("Configuration reloaded");
    }
}
