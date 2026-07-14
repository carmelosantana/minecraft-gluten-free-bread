# Gluten Free Bread Plugin

A robust Minecraft plugin that introduces **Gluten Free Bread** variants, providing players with healthy alternatives to traditional wheat-based bread. Each variant offers unique crafting recipes and *enchanting* effects when consumed.

- [Features](#features)
- [Bread Variants](#bread-variants)
- [Installation](#installation)
  - [Requirements](#requirements)
  - [Quick Install](#quick-install)
  - [Build from Source](#build-from-source)
- [Usage](#usage)
  - [Crafting Recipes](#crafting-recipes)
    - [Plain Gluten Free Bread](#plain-gluten-free-bread)
    - [Sweet Gluten Free Bread](#sweet-gluten-free-bread)
    - [Spicy Gluten Free Bread](#spicy-gluten-free-bread)
    - [Savory Gluten Free Bread](#savory-gluten-free-bread)
  - [Commands](#commands)
  - [Examples](#examples)
- [Configuration](#configuration)
- [Contributing](#contributing)
  - [Developer Quick Start](#developer-quick-start)
- [Permissions](#permissions)
- [Overconsumption Protection](#overconsumption-protection)
- [Resource Pack Support](#resource-pack-support)
- [Technical Details](#technical-details)
  - [Architecture](#architecture)
  - [Compatibility](#compatibility)
  - [Performance](#performance)
- [API Usage](#api-usage)
- [Troubleshooting](#troubleshooting)
  - [Possible Issues](#possible-issues)
  - [Debug Mode](#debug-mode)
- [🎓 Learn AI Powered Plugin Development](#-learn-ai-powered-plugin-development)
  - [What You'll Learn](#what-youll-learn)
  - [Course Topics](#course-topics)
  - [Booking Information](#booking-information)
    - [1-on-1 Coaching Sessions Available](#1-on-1-coaching-sessions-available)
    - [What's Included](#whats-included)
  - [Get Started](#get-started)
- [License](#license)


## Features

- **Four Bread Variants**: Plain, Sweet, Spicy, and Savory gluten-free bread
- **Custom Recipes**: Each variant requires unique ingredients
- **Beneficial Effects**: Different potion effects based on bread type
- **Overconsumption Protection**: Prevents abuse with negative effects
- **Comprehensive Commands**: Admin tools for management
- **Configuration Support**: Customizable effects and settings
- **Performance Optimized**: Event-driven architecture with caching

## Bread Variants

| Variant    | Display Name               | Effects                                   | Recipe Ingredients               |
| ---------- | -------------------------- | ----------------------------------------- | -------------------------------- |
| **Plain**  | Plain Gluten Free Bread  | Regeneration I (10s)                      | Beetroot + Eggs                  |
| **Sweet**  | Sweet Gluten Free Bread  | Regeneration II (10s), Speed I (15s)      | Beetroot + Eggs + Honey Bottle   |
| **Spicy**  | Spicy Gluten Free Bread  | Fire Resistance I (20s), Strength I (10s) | Beetroot + Eggs + Blaze Powder   |
| **Savory** | Savory Gluten Free Bread | Absorption I (15s), Regeneration I (10s)  | Beetroot + Eggs + Brown Mushroom |

All breads feature:

- Glowing appearance (harmless enchantment effect)
- Custom lore describing their benefits
- Unique colors for easy identification

## Installation

### Requirements

- **Java**: 21 or higher
- **Server**: Paper 26.1.2+
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

## Usage

### Crafting Recipes

#### Plain Gluten Free Bread

```
B B B
E E E
B B B
```

Where: B = Beetroot, E = Egg

#### Sweet Gluten Free Bread

```
B B B
E H E
B B B
```

Where: B = Beetroot, E = Egg, H = Honey Bottle

#### Spicy Gluten Free Bread

```
B B B
E P E
B B B
```

Where: B = Beetroot, E = Egg, P = Blaze Powder

#### Savory Gluten Free Bread

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

## Configuration

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

## Contributing

Please review [Contributing Guidelines](./CONTRIBUTING.md) for developer documentation. Including;

- Development environment setup
- Code standards and testing procedures
- Pull request process
- Server management and Docker support
- Troubleshooting

### Developer Quick Start

```bash
# Set up development environment
make setup

# Quick development cycle
make dev

# Run tests
make test

# Test in Docker
make docker-test
```

## Permissions

- `glutenfreebread.craft` - Allow crafting (default: true)
- `glutenfreebread.consume` - Allow consuming (default: true)
- `glutenfreebread.admin` - Admin commands (default: op)

## Overconsumption Protection

The plugin tracks consumption to prevent abuse:

- **Limit**: 5 breads per 5 minutes
- **Effects**: Nausea and Slowness when exceeded
- **Reset**: Automatically clears after time window

## Resource Pack Support

The plugin includes custom model data for resource pack compatibility:

- Plain: `1000`
- Sweet: `1001`
- Spicy: `1002`
- Savory: `1003`

## Technical Details

### Architecture

- **Event driven**: Minimal performance impact
- **Cached items**: Prebuilt ItemStacks for efficiency
- **Persistent data**: Reliable item identification
- **Adventure API**: Modern text components

### Compatibility

- **Paper**: 26.1.2+ (recommended)
- **Java**: 21+
- **Geyser/Floodgate**: Compatible

### Performance

- Cached ItemStack instances
- Efficient consumption tracking
- Minimal event processing overhead
- Cleanup on player disconnect

## API Usage

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

## Troubleshooting

### Possible Issues

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

## 🎓 Learn AI Powered Plugin Development

**Want to build your own Minecraft plugins?** I built our plugin collection using generative AI, and I can teach you how to do the same!

### What You'll Learn

- **AI Assisted Coding**: How to effectively use AI tools like GitHub Copilot, ChatGPT, and Claude for plugin development
- **Plugin Architecture**: Best practices for structuring robust, maintainable Minecraft plugins
- **Modern Development**: Paper API, Maven build systems, Docker testing, and CI/CD workflows
- **Problem Solving**: How to break down complex features into manageable tasks
- **Code Quality**: Testing, debugging, and optimizing AI generated code

### Course Topics

- **Getting Started**: Setting up your AI development environment
- **Plugin Fundamentals**: Events, commands, configuration, and permissions
- **Advanced Features**: Custom items, recipes, data persistence, and performance optimization
- **Testing & Deployment**: Docker containers, server management, and release workflows
- **Real Projects**: Build actual plugins from concept to completion

### Booking Information

#### 1-on-1 Coaching Sessions Available

- **Duration**: 1-2 hour sessions
- **Format**: Screen share coding sessions via video call
- **Family Friendly**: Parents are welcome and encouraged to join sessions, especially for younger students and curious parents wanting to learn alongside their children.

#### What's Included

- ✅ Live coding demonstration
- ✅ AI prompt engineering techniques
- ✅ Complete project setup and tooling
- ✅ Plugin publishing and distribution
- ✅ Follow up support and code review

### Get Started

Ready to accelerate your development with AI?

- **[Schedule your call](https://cal.com/carmelosantana/learn-minecraft-with-ai)** - Book a session today!
- **[Discord](https://discord.gg/udbJu8Sbyj)** - Ask questions, see examples.
- **Public SMP Server** - Join us at `play.xp.farm` and test our plugins live!

*Turn your plugin ideas into reality in hours, not weeks!*

## License

This project is licensed under the [GNU Affero General Public License v3.0 or later](LICENSE).
