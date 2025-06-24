package com.carmelosantana.glutenfreebread.commands;

import com.carmelosantana.glutenfreebread.GlutenFreeBreadPlugin;
import com.carmelosantana.glutenfreebread.items.GlutenFreeBreadItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Command executor for the /gfbread command.
 * 
 * Provides comprehensive commands for managing gluten-free bread items and plugin functionality.
 */
public class GFBreadCommand implements CommandExecutor, TabCompleter {
    
    private final GlutenFreeBreadPlugin plugin;
    private final Map<String, GlutenFreeBreadItem> breadItems;
    
    public GFBreadCommand(GlutenFreeBreadPlugin plugin, Map<String, GlutenFreeBreadItem> breadItems) {
        this.plugin = plugin;
        this.breadItems = breadItems;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            return handleHelp(sender);
        }
        
        String subCommand = args[0].toLowerCase();
        
        return switch (subCommand) {
            case "help" -> handleHelp(sender);
            case "give" -> handleGive(sender, args);
            case "reload" -> handleReload(sender);
            case "version" -> handleVersion(sender);
            case "clear" -> handleClear(sender, args);
            case "list" -> handleList(sender);
            default -> {
                sender.sendMessage(Component.text("Unknown command. Use /gfbread help for usage.", NamedTextColor.RED));
                yield true;
            }
        };
    }
    
    /**
     * Handle the help command
     */
    private boolean handleHelp(CommandSender sender) {
        sender.sendMessage(Component.text("=== Gluten-Free Bread Plugin Commands ===", NamedTextColor.GOLD));
        sender.sendMessage(Component.text("/gfbread help", NamedTextColor.YELLOW)
            .append(Component.text(" - Show this help message", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("/gfbread give <type> [amount]", NamedTextColor.YELLOW)
            .append(Component.text(" - Give yourself gluten-free bread", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("/gfbread give <type> <player> <amount>", NamedTextColor.YELLOW)
            .append(Component.text(" - Give bread to another player", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("/gfbread list", NamedTextColor.YELLOW)
            .append(Component.text(" - List all available bread types", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("/gfbread clear [player]", NamedTextColor.YELLOW)
            .append(Component.text(" - Clear active effects", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("/gfbread reload", NamedTextColor.YELLOW)
            .append(Component.text(" - Reload plugin configuration", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("/gfbread version", NamedTextColor.YELLOW)
            .append(Component.text(" - Show plugin version", NamedTextColor.GRAY)));
        
        sender.sendMessage(Component.text("Available bread types: ", NamedTextColor.AQUA)
            .append(Component.text(String.join(", ", breadItems.keySet()), NamedTextColor.WHITE)));
        
        return true;
    }
    
    /**
     * Handle the give command
     */
    private boolean handleGive(CommandSender sender, String[] args) {
        if (!sender.hasPermission("glutenfreebread.admin")) {
            sender.sendMessage(Component.text("You don't have permission to use this command!", NamedTextColor.RED));
            return true;
        }
        
        if (args.length < 2) {
            sender.sendMessage(Component.text("Usage: /gfbread give <type> [amount] or /gfbread give <type> <player> <amount>", NamedTextColor.RED));
            return true;
        }
        
        String breadType = args[1].toLowerCase();
        GlutenFreeBreadItem breadItem = breadItems.get(breadType);
        
        if (breadItem == null) {
            sender.sendMessage(Component.text("Invalid bread type! Available types: " + String.join(", ", breadItems.keySet()), NamedTextColor.RED));
            return true;
        }
        
        // Parse arguments based on count
        if (args.length == 2) {
            // /gfbread give <type> - Give 1 to sender
            return giveBreadToPlayer(sender, sender, breadItem, 1);
        } else if (args.length == 3) {
            // Could be /gfbread give <type> <amount> or /gfbread give <type> <player>
            try {
                int amount = Integer.parseInt(args[2]);
                return giveBreadToPlayer(sender, sender, breadItem, amount);
            } catch (NumberFormatException e) {
                // Treat as player name
                Player target = Bukkit.getPlayer(args[2]);
                if (target == null) {
                    sender.sendMessage(Component.text("Player not found: " + args[2], NamedTextColor.RED));
                    return true;
                }
                return giveBreadToPlayer(sender, target, breadItem, 1);
            }
        } else if (args.length == 4) {
            // /gfbread give <type> <player> <amount>
            Player target = Bukkit.getPlayer(args[2]);
            if (target == null) {
                sender.sendMessage(Component.text("Player not found: " + args[2], NamedTextColor.RED));
                return true;
            }
            
            try {
                int amount = Integer.parseInt(args[3]);
                return giveBreadToPlayer(sender, target, breadItem, amount);
            } catch (NumberFormatException e) {
                sender.sendMessage(Component.text("Invalid amount: " + args[3], NamedTextColor.RED));
                return true;
            }
        }
        
        sender.sendMessage(Component.text("Invalid usage. Use /gfbread help for correct syntax.", NamedTextColor.RED));
        return true;
    }
    
    /**
     * Give bread to a player
     */
    private boolean giveBreadToPlayer(CommandSender sender, CommandSender target, GlutenFreeBreadItem breadItem, int amount) {
        if (!(target instanceof Player)) {
            sender.sendMessage(Component.text("Target must be a player!", NamedTextColor.RED));
            return true;
        }
        
        if (amount <= 0 || amount > 64) {
            sender.sendMessage(Component.text("Amount must be between 1 and 64!", NamedTextColor.RED));
            return true;
        }
        
        Player targetPlayer = (Player) target;
        ItemStack breadStack = breadItem.getItem(amount);
        targetPlayer.getInventory().addItem(breadStack);
        
        // Send success messages
        String displayName = breadItem.getDisplayName();
        
        if (sender.equals(target)) {
            sender.sendMessage(Component.text("You have been given " + amount + " ", NamedTextColor.GREEN)
                .append(Component.text(displayName, NamedTextColor.GOLD))
                .append(Component.text(amount == 1 ? "!" : "s!", NamedTextColor.GREEN)));
        } else {
            sender.sendMessage(Component.text("Given " + amount + " ", NamedTextColor.GREEN)
                .append(Component.text(displayName, NamedTextColor.GOLD))
                .append(Component.text(amount == 1 ? "" : "s"))
                .append(Component.text(" to " + targetPlayer.getName(), NamedTextColor.GREEN)));
            
            targetPlayer.sendMessage(Component.text("You have received " + amount + " ", NamedTextColor.GREEN)
                .append(Component.text(displayName, NamedTextColor.GOLD))
                .append(Component.text(amount == 1 ? "!" : "s!", NamedTextColor.GREEN)));
        }
        
        return true;
    }
    
    /**
     * Handle the reload command
     */
    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission("glutenfreebread.admin")) {
            sender.sendMessage(Component.text("You don't have permission to use this command!", NamedTextColor.RED));
            return true;
        }
        
        plugin.reloadConfiguration();
        sender.sendMessage(Component.text("Plugin configuration reloaded!", NamedTextColor.GREEN));
        return true;
    }
    
    /**
     * Handle the version command
     */
    private boolean handleVersion(CommandSender sender) {
        sender.sendMessage(Component.text("=== Gluten-Free Bread Plugin ===", NamedTextColor.GOLD));
        sender.sendMessage(Component.text("Version: ", NamedTextColor.YELLOW)
            .append(Component.text(plugin.getDescription().getVersion(), NamedTextColor.WHITE)));
        sender.sendMessage(Component.text("Author: ", NamedTextColor.YELLOW)
            .append(Component.text("Carmelo Santana", NamedTextColor.WHITE)));
        sender.sendMessage(Component.text("Website: ", NamedTextColor.YELLOW)
            .append(Component.text("https://hv2.world", NamedTextColor.AQUA)));
        sender.sendMessage(Component.text("Bread variants: ", NamedTextColor.YELLOW)
            .append(Component.text(breadItems.size() + "", NamedTextColor.WHITE)));
        return true;
    }
    
    /**
     * Handle the clear command
     */
    private boolean handleClear(CommandSender sender, String[] args) {
        if (!sender.hasPermission("glutenfreebread.admin")) {
            sender.sendMessage(Component.text("You don't have permission to use this command!", NamedTextColor.RED));
            return true;
        }
        
        Player target;
        if (args.length > 1) {
            target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage(Component.text("Player not found: " + args[1], NamedTextColor.RED));
                return true;
            }
        } else {
            if (!(sender instanceof Player)) {
                sender.sendMessage(Component.text("You must specify a player when using this command from console!", NamedTextColor.RED));
                return true;
            }
            target = (Player) sender;
        }
        
        // Clear all active potion effects
        target.getActivePotionEffects().forEach(effect -> target.removePotionEffect(effect.getType()));
        
        // Clear consumption tracking
        plugin.getConsumptionTracker().clearPlayerData(target);
        
        target.sendMessage(Component.text("All active effects have been cleared!", NamedTextColor.GREEN));
        if (!sender.equals(target)) {
            sender.sendMessage(Component.text("Cleared all effects for " + target.getName(), NamedTextColor.GREEN));
        }
        
        return true;
    }
    
    /**
     * Handle the list command
     */
    private boolean handleList(CommandSender sender) {
        sender.sendMessage(Component.text("=== Available Gluten-Free Bread Types ===", NamedTextColor.GOLD));
        
        for (Map.Entry<String, GlutenFreeBreadItem> entry : breadItems.entrySet()) {
            String type = entry.getKey();
            GlutenFreeBreadItem item = entry.getValue();
            
            sender.sendMessage(Component.text("• " + type + ": ", NamedTextColor.YELLOW)
                .append(Component.text(item.getDisplayName(), NamedTextColor.WHITE)));
        }
        
        return true;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            // First argument - subcommands
            List<String> subCommands = Arrays.asList("help", "give", "reload", "version", "clear", "list");
            return subCommands.stream()
                .filter(cmd -> cmd.toLowerCase().startsWith(args[0].toLowerCase()))
                .collect(Collectors.toList());
        } else if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            // Second argument for give command - bread types
            return breadItems.keySet().stream()
                .filter(type -> type.toLowerCase().startsWith(args[1].toLowerCase()))
                .collect(Collectors.toList());
        } else if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            // Third argument for give command - player names or amounts
            List<String> players = Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(name -> name.toLowerCase().startsWith(args[2].toLowerCase()))
                .collect(Collectors.toList());
            
            // Also suggest common amounts
            List<String> amounts = Arrays.asList("1", "5", "10", "16", "32", "64");
            amounts.stream()
                .filter(amount -> amount.startsWith(args[2]))
                .forEach(players::add);
            
            return players;
        } else if (args.length == 2 && args[0].equalsIgnoreCase("clear")) {
            // Second argument for clear command - player names
            return Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                .collect(Collectors.toList());
        }
        
        return completions;
    }
}
