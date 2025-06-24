# Gluten-Free Bread Plugin

A comprehensive Minecraft plugin that introduces **Gluten-Free Bread** variants, providing players with healthy alternatives to traditional wheat-based bread. Each variant offers unique crafting recipes and beneficial effects when consumed.

## 🍞 Features

- **Four Bread Variants**: Plain, Sweet, Spicy, and Savory gluten-free bread
- **Custom Recipes**: Each variant requires unique ingredients
- **Beneficial Effects**: Different potion effects based on bread type
- **Overconsumption Protection**: Prevents abuse with negative effects
- **Comprehensive Commands**: Admin tools for management
- **Configuration Support**: Customizable effects and settings
- **Performance Optimized**: Event-driven architecture with caching

## 🎮 Bread Variants

| Variant    | Display Name               | Effects                                   | Recipe Ingredients               |
| ---------- | -------------------------- | ----------------------------------------- | -------------------------------- |
| **Plain**  | §fPlain Gluten-Free Bread  | Regeneration I (10s)                      | Beetroot + Eggs                  |
| **Sweet**  | §dSweet Gluten-Free Bread  | Regeneration II (10s), Speed I (15s)      | Beetroot + Eggs + Honey Bottle   |
| **Spicy**  | §cSpicy Gluten-Free Bread  | Fire Resistance I (20s), Strength I (10s) | Beetroot + Eggs + Blaze Powder   |
| **Savory** | §eSavory Gluten-Free Bread | Absorption I (15s), Regeneration I (10s)  | Beetroot + Eggs + Brown Mushroom |

All breads feature:

- Glowing appearance (harmless enchantment effect)
- Custom lore describing their benefits
- Unique colors for easy identification

## 🛠 Installation

### Requirements

- **Java**: 21 or higher
- **Server**: Paper 1.21.6+, Spigot 1.21+, or Bukkit 1.21+
- **Maven**: For building from source

### Quick Install

1. Download the latest JAR from releases
2. Place in your server's `plugins/` directory
3. Restart your server
4. Configure in `plugins/GlutenFreeBread/config.yml` if needed

### Build from Source

```bash
# Clone the repository
git clone https://github.com/carmelosantana/minecraft-gluten-free-bread
cd gluten-free-bread

# Build the plugin
make build

# Install to test server
make install
```

## 🎯 Usage

### Crafting Recipes

#### Plain Gluten-Free Bread

```
B B B
E E E
B B B
```

Where: B = Beetroot, E = Egg

#### Sweet Gluten-Free Bread

```
B B B
E H E
B B B
```

Where: B = Beetroot, E = Egg, H = Honey Bottle

#### Spicy Gluten-Free Bread

```
B B B
E P E
B B B
```

Where: B = Beetroot, E = Egg, P = Blaze Powder

#### Savory Gluten-Free Bread

```
B B B
E M E
B B B
```

Where: B = Beetroot, E = Egg, M = Brown Mushroom

### Commands

All commands require `glutenfreebread.admin` permission:

- `/gfbread help` - Show command help
- `/gfbread give <type> [amount]` - Give yourself bread
- `/gfbread give <type> <player> <amount>` - Give bread to another player
- `/gfbread list` - List all available bread types
- `/gfbread clear [player]` - Clear active effects
- `/gfbread reload` - Reload configuration
- `/gfbread version` - Show plugin information

**Available types**: `plain`, `sweet`, `spicy`, `savory`

### Examples

```bash
/gfbread give sweet 5           # Give yourself 5 sweet breads
/gfbread give spicy Player 10   # Give Player 10 spicy breads
/gfbread clear                  # Clear your effects
/gfbread list                   # Show all bread types
```

## ⚙️ Configuration

Edit `plugins/GlutenFreeBread/config.yml`:

```yaml
consumption:
  threshold: 5 # Minimum hunger level for effects

recipe:
  enabled: true # Enable custom recipes

logging:
  debug: false # Debug logging

breads:
  plain:
    effects:
      regeneration:
        duration: 10 # seconds
        amplifier: 0 # level - 1
  sweet:
    effects:
      regeneration:
        duration: 10
        amplifier: 1
      speed:
        duration: 15
        amplifier: 0
  # ... (other bread types)
```

## 🔧 Development

### Development Setup

```bash
# Check dependencies
make check-deps

# Set up development environment
make setup

# Quick development cycle
make dev    # build + install + restart
```

### Testing

```bash
# Run unit tests
make test

# Test in Docker
make docker-test

# Validate code
make validate
```

### Server Management

```bash
# Start test server
make start

# View server logs
make logs

# Show server status
make status

# Reset for testing
make reset
```

## 🐳 Docker Support

Test the plugin easily with Docker:

```bash
# Build and test in container
make docker-test

# Or manually
docker-compose up -d
```

Server will be available at:

- **Java Edition**: `localhost:25565`
- **Bedrock Edition**: `localhost:19132`

## 📝 Permissions

- `glutenfreebread.craft` - Allow crafting (default: true)
- `glutenfreebread.consume` - Allow consuming (default: true)
- `glutenfreebread.admin` - Admin commands (default: op)

## 🛡️ Overconsumption Protection

The plugin tracks consumption to prevent abuse:

- **Limit**: 5 breads per 5 minutes
- **Effects**: Nausea and Slowness when exceeded
- **Reset**: Automatically clears after time window

## 🎨 Resource Pack Support

The plugin includes custom model data for resource pack compatibility:

- Plain: `1000`
- Sweet: `1001`
- Spicy: `1002`
- Savory: `1003`

## 🔧 Technical Details

### Architecture

- **Event-driven**: Minimal performance impact
- **Cached items**: Pre-built ItemStacks for efficiency
- **Persistent data**: Reliable item identification
- **Adventure API**: Modern text components

### Compatibility

- **Paper**: 1.21.6+ (recommended)
- **Spigot**: 1.21+
- **Bukkit**: 1.21+
- **Java**: 21+
- **Geyser/Floodgate**: Compatible

### Performance

- Cached ItemStack instances
- Efficient consumption tracking
- Minimal event processing overhead
- Cleanup on player disconnect

## 📋 API Usage

For developers wanting to integrate:

```java
// Check if item is gluten-free bread
if (GlutenFreeBreadItem.isAnyGlutenFreeBread(itemStack)) {
    String type = GlutenFreeBreadItem.getBreadType(itemStack);
    // Handle bread consumption
}

// Get plugin instance
GlutenFreeBreadPlugin plugin = (GlutenFreeBreadPlugin) Bukkit.getPluginManager().getPlugin("GlutenFreeBread");
Map<String, GlutenFreeBreadItem> breads = plugin.getBreadItems();
```

## 🐛 Troubleshooting

### Common Issues

**Plugin not loading:**

- Check Java version (requires 21+)
- Verify Paper/Spigot version (1.21+)
- Check server logs for errors

**Recipes not working:**

- Ensure `recipe.enabled: true` in config
- Restart server after config changes
- Check if other plugins conflict

**Commands not working:**

- Verify permissions
- Check command syntax
- Ensure plugin is enabled

### Debug Mode

Enable debug logging in config.yml:

```yaml
logging:
  debug: true
```

## 📄 License

Creative Commons Attribution-NonCommercial 4.0 International (CC BY-NC 4.0)

## 👥 Credits

- **Author**: Carmelo Santana
- **Website**: https://hv2.world
- **Live Server**: play.hv2.world
- **Docker Container**: [Legendary Minecraft Geyser](https://github.com/TheRemote/Legendary-Java-Minecraft-Geyser-Floodgate)

## 📞 Support

- **Issues**: GitHub Issues
- **Discord**: [HV2 World](https://discord.gg/udbJu8Sbyj)
- **Test Server**: play.hv2.world

---

**Happy crafting! 🍞✨**
