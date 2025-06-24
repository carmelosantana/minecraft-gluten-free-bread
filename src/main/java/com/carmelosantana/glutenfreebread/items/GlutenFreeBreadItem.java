package com.carmelosantana.glutenfreebread.items;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;
import java.util.List;

/**
 * Represents the Gluten-Free Bread items with custom properties and identification.
 * 
 * This class handles the creation and identification of different gluten-free bread variants,
 * using persistent data containers for reliable item identification.
 */
public class GlutenFreeBreadItem {
    
    private final Plugin plugin;
    private final String breadType;
    private final NamespacedKey itemKey;
    private final ItemStack cachedItemStack;
    
    // Bread type configurations
    private static final BreadConfig PLAIN_CONFIG = new BreadConfig(
        "Plain Gluten-Free Bread", 
        NamedTextColor.WHITE,
        "Simple and pure."
    );
    
    private static final BreadConfig SWEET_CONFIG = new BreadConfig(
        "Sweet Gluten-Free Bread", 
        NamedTextColor.LIGHT_PURPLE,
        "Light and fast."
    );
    
    private static final BreadConfig SPICY_CONFIG = new BreadConfig(
        "Spicy Gluten-Free Bread", 
        NamedTextColor.RED,
        "Hot and bold."
    );
    
    private static final BreadConfig SAVORY_CONFIG = new BreadConfig(
        "Savory Gluten-Free Bread", 
        NamedTextColor.GOLD,
        "Warm and hearty."
    );
    
    public GlutenFreeBreadItem(Plugin plugin, String breadType) {
        this.plugin = plugin;
        this.breadType = breadType.toLowerCase();
        this.itemKey = new NamespacedKey(plugin, "gluten_free_bread_" + this.breadType);
        this.cachedItemStack = createGlutenFreeBread();
    }
    
    /**
     * Creates a new gluten-free bread ItemStack based on the bread type
     * @return ItemStack representing gluten-free bread
     */
    private ItemStack createGlutenFreeBread() {
        ItemStack item = new ItemStack(Material.BREAD);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            BreadConfig config = getBreadConfig();
            
            // Set display name with color formatting
            Component displayName = Component.text(config.displayName(), config.nameColor());
            meta.displayName(displayName);
            
            // Set lore with bread-specific description
            List<Component> lore = Arrays.asList(
                Component.text("A healthy alternative to regular bread", NamedTextColor.GRAY),
                Component.text(config.description(), NamedTextColor.YELLOW),
                Component.text("Made with special ingredients", NamedTextColor.GREEN),
                Component.text("Provides unique benefits when consumed", NamedTextColor.AQUA)
            );
            meta.lore(lore);
            
            // Add persistent data for identification
            meta.getPersistentDataContainer().set(itemKey, PersistentDataType.STRING, breadType);
            
            // Set custom model data for resource pack compatibility
            meta.setCustomModelData(1000 + getBreadTypeId());
            
            // Add harmless enchantment for visual effect (glowing)
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    /**
     * Get bread configuration based on type
     */
    private BreadConfig getBreadConfig() {
        return switch (breadType) {
            case "sweet" -> SWEET_CONFIG;
            case "spicy" -> SPICY_CONFIG;
            case "savory" -> SAVORY_CONFIG;
            default -> PLAIN_CONFIG;
        };
    }
    
    /**
     * Get bread type ID for custom model data
     */
    private int getBreadTypeId() {
        return switch (breadType) {
            case "sweet" -> 1;
            case "spicy" -> 2;
            case "savory" -> 3;
            default -> 0;
        };
    }
    
    /**
     * Get a new gluten-free bread ItemStack
     * @param amount Number of items to create
     * @return ItemStack with specified amount
     */
    public ItemStack getItem(int amount) {
        ItemStack item = cachedItemStack.clone();
        item.setAmount(amount);
        return item;
    }
    
    /**
     * Get a single gluten-free bread ItemStack
     * @return Single ItemStack
     */
    public ItemStack getItem() {
        return getItem(1);
    }
    
    /**
     * Check if an ItemStack is a gluten-free bread of this type
     * @param item ItemStack to check
     * @return true if the item is gluten-free bread of this type
     */
    public boolean isGlutenFreeBread(ItemStack item) {
        if (item == null || item.getType() != Material.BREAD) {
            return false;
        }
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }
        
        // Check persistent data container
        String storedType = meta.getPersistentDataContainer().get(itemKey, PersistentDataType.STRING);
        return breadType.equals(storedType);
    }
    
    /**
     * Check if an ItemStack is any gluten-free bread variant
     * @param item ItemStack to check
     * @return true if the item is any gluten-free bread variant
     */
    public static boolean isAnyGlutenFreeBread(ItemStack item) {
        if (item == null || item.getType() != Material.BREAD) {
            return false;
        }
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }
        
        // Check if any gluten-free bread key exists
        return meta.getPersistentDataContainer().getKeys().stream()
            .anyMatch(key -> key.getKey().startsWith("gluten_free_bread_"));
    }
    
    /**
     * Get the bread type from an ItemStack
     * @param item ItemStack to check
     * @return bread type or null if not a gluten-free bread
     */
    public static String getBreadType(ItemStack item) {
        if (item == null || item.getType() != Material.BREAD) {
            return null;
        }
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return null;
        }
        
        // Find the gluten-free bread key and extract type
        return meta.getPersistentDataContainer().getKeys().stream()
            .filter(key -> key.getKey().startsWith("gluten_free_bread_"))
            .findFirst()
            .map(key -> key.getKey().substring("gluten_free_bread_".length()))
            .orElse(null);
    }
    
    /**
     * Get the NamespacedKey used for this item
     * @return NamespacedKey for identification
     */
    public NamespacedKey getItemKey() {
        return itemKey;
    }
    
    /**
     * Get the bread type
     * @return bread type string
     */
    public String getBreadType() {
        return breadType;
    }
    
    /**
     * Get the display name of the item
     * @return Formatted display name as string
     */
    public String getDisplayName() {
        return getBreadConfig().displayName();
    }
    
    /**
     * Configuration record for bread variants
     */
    private record BreadConfig(String displayName, TextColor nameColor, String description) {}
}
