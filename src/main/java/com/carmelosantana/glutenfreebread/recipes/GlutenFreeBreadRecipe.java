package com.carmelosantana.glutenfreebread.recipes;

import com.carmelosantana.glutenfreebread.items.GlutenFreeBreadItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Handles the registration of all gluten-free bread crafting recipes.
 * 
 * Each bread variant has its own unique recipe pattern and ingredients.
 */
public class GlutenFreeBreadRecipe {
    
    private final Plugin plugin;
    private final List<NamespacedKey> registeredRecipes;
    
    public GlutenFreeBreadRecipe(Plugin plugin) {
        this.plugin = plugin;
        this.registeredRecipes = new ArrayList<>();
    }
    
    /**
     * Register all gluten-free bread recipes
     */
    public void registerAllRecipes(Map<String, GlutenFreeBreadItem> breadItems) {
        for (Map.Entry<String, GlutenFreeBreadItem> entry : breadItems.entrySet()) {
            registerRecipe(entry.getKey(), entry.getValue());
        }
    }
    
    /**
     * Register a specific bread recipe
     */
    private void registerRecipe(String breadType, GlutenFreeBreadItem breadItem) {
        NamespacedKey recipeKey = new NamespacedKey(plugin, "gluten_free_bread_recipe_" + breadType);
        
        try {
            ShapedRecipe recipe = new ShapedRecipe(recipeKey, breadItem.getItem(2));
            
            // Set recipe pattern based on bread type
            switch (breadType.toLowerCase()) {
                case "plain" -> {
                    recipe.shape("BBB", "EEE", "BBB");
                    recipe.setIngredient('B', Material.BEETROOT);
                    recipe.setIngredient('E', Material.EGG);
                }
                case "sweet" -> {
                    recipe.shape("BBB", "EHE", "BBB");
                    recipe.setIngredient('B', Material.BEETROOT);
                    recipe.setIngredient('E', Material.EGG);
                    recipe.setIngredient('H', Material.HONEY_BOTTLE);
                }
                case "spicy" -> {
                    recipe.shape("BBB", "EPE", "BBB");
                    recipe.setIngredient('B', Material.BEETROOT);
                    recipe.setIngredient('E', Material.EGG);
                    recipe.setIngredient('P', Material.BLAZE_POWDER);
                }
                case "savory" -> {
                    recipe.shape("BBB", "EME", "BBB");
                    recipe.setIngredient('B', Material.BEETROOT);
                    recipe.setIngredient('E', Material.EGG);
                    recipe.setIngredient('M', Material.BROWN_MUSHROOM);
                }
                default -> {
                    plugin.getLogger().warning("Unknown bread type: " + breadType);
                    return;
                }
            }
            
            // Add recipe to server
            Bukkit.addRecipe(recipe);
            registeredRecipes.add(recipeKey);
            plugin.getLogger().info("Successfully registered " + breadType + " gluten-free bread recipe");
            
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to register " + breadType + " gluten-free bread recipe: " + e.getMessage());
        }
    }
    
    /**
     * Remove all registered recipes from the server
     */
    public void unregisterAllRecipes() {
        for (NamespacedKey recipeKey : registeredRecipes) {
            try {
                Bukkit.removeRecipe(recipeKey);
                plugin.getLogger().info("Successfully unregistered recipe: " + recipeKey.getKey());
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to unregister recipe " + recipeKey.getKey() + ": " + e.getMessage());
            }
        }
        registeredRecipes.clear();
    }
    
    /**
     * Get all registered recipe keys
     * @return List of registered NamespacedKeys
     */
    public List<NamespacedKey> getRegisteredRecipes() {
        return new ArrayList<>(registeredRecipes);
    }
}
