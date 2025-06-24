#!/bin/bash
# Server Management Script for Gluten-Free Bread Plugin
# Compatible with macOS and Linux

set -euo pipefail

# Configuration
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SERVER_DIR="${SCRIPT_DIR}/server"
PLUGIN_JAR="${SCRIPT_DIR}/target/gluten-free-bread-1.0.1.jar"
SERVER_JAR="${SERVER_DIR}/paper.jar"
WORLD_DIR="${SERVER_DIR}/world"
PLUGINS_DIR="${SERVER_DIR}/plugins"
LOGS_DIR="${SERVER_DIR}/logs"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Logging function
log() {
    echo -e "${GREEN}[$(date +'%Y-%m-%d %H:%M:%S')]${NC} $1"
}

error() {
    echo -e "${RED}[ERROR]${NC} $1" >&2
}

warn() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

# Check if Java is installed and get version
check_java() {
    if ! command -v java &> /dev/null; then
        error "Java is not installed or not in PATH"
        info "Please install Java 21 or higher"
        exit 1
    fi
    
    local java_version
    java_version=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
    
    if [[ "$java_version" -lt 21 ]]; then
        error "Java 21 or higher is required. Current version: $java_version"
        exit 1
    fi
    
    log "Java version: $java_version ✓"
}

# Download ViaVersion plugin for compatibility
download_viaversion() {
    local viaversion_jar="${PLUGINS_DIR}/ViaVersion.jar"
    
    if [[ ! -f "$viaversion_jar" ]]; then
        log "Downloading ViaVersion for better compatibility..."
        mkdir -p "$PLUGINS_DIR"
        
        # Download latest ViaVersion
        local via_url="https://ci.viaversion.com/job/ViaVersion/lastSuccessfulBuild/artifact/build/libs/ViaVersion-5.4.1-SNAPSHOT.jar"
        
        if command -v curl &> /dev/null; then
            curl -L -o "$viaversion_jar" "$via_url" || {
                warn "Failed to download ViaVersion from CI, trying GitHub releases..."
                curl -L -o "$viaversion_jar" "https://github.com/ViaVersion/ViaVersion/releases/download/5.1.1/ViaVersion-5.1.1.jar"
            }
        elif command -v wget &> /dev/null; then
            wget -O "$viaversion_jar" "$via_url" || {
                warn "Failed to download ViaVersion from CI, trying GitHub releases..."
                wget -O "$viaversion_jar" "https://github.com/ViaVersion/ViaVersion/releases/download/5.1.1/ViaVersion-5.1.1.jar"
            }
        else
            error "Neither curl nor wget is available. Please install one of them."
            exit 1
        fi
        
        if [[ -f "$viaversion_jar" ]]; then
            log "ViaVersion downloaded successfully"
        else
            warn "Failed to download ViaVersion - Geyser may have compatibility issues"
        fi
    else
        log "ViaVersion already exists"
    fi
}

# Download Paper server if not exists
download_server() {
    if [[ ! -f "$SERVER_JAR" ]]; then
        log "Downloading Paper server..."
        mkdir -p "$SERVER_DIR"
        
        # Download latest Paper 1.21.6
        local paper_url="https://api.papermc.io/v2/projects/paper/versions/1.21.6/builds/latest/downloads/paper-1.21.6-latest.jar"
        
        if command -v curl &> /dev/null; then
            curl -o "$SERVER_JAR" "$paper_url" || {
                warn "Failed to download latest, trying specific build..."
                curl -o "$SERVER_JAR" "https://api.papermc.io/v2/projects/paper/versions/1.21.6/builds/17/downloads/paper-1.21.6-17.jar"
            }
        elif command -v wget &> /dev/null; then
            wget -O "$SERVER_JAR" "$paper_url" || {
                warn "Failed to download latest, trying specific build..."
                wget -O "$SERVER_JAR" "https://api.papermc.io/v2/projects/paper/versions/1.21.6/builds/17/downloads/paper-1.21.6-17.jar"
            }
        else
            error "Neither curl nor wget is available. Please install one of them."
            exit 1
        fi
        
        log "Paper server downloaded successfully"
    else
        log "Paper server already exists"
    fi
}

# Setup server environment
setup_server() {
    log "Setting up server environment..."
    
    check_java
    download_server
    download_viaversion
    
    # Create necessary directories
    mkdir -p "$PLUGINS_DIR" "$LOGS_DIR"
    
    # Create eula.txt
    echo "eula=true" > "${SERVER_DIR}/eula.txt"
    
    # Create server.properties
    cat > "${SERVER_DIR}/server.properties" << EOF
# Minecraft Server Properties
server-port=25565
gamemode=survival
difficulty=normal
spawn-protection=16
max-players=20
online-mode=true
white-list=false
spawn-monsters=true
spawn-animals=true
spawn-npcs=true
pvp=true
level-name=world
level-seed=
level-type=minecraft:normal
generator-settings=
motd=Gluten-Free Bread Test Server
enable-command-block=true
enable-query=false
enable-rcon=false
op-permission-level=4
player-idle-timeout=0
max-world-size=29999984
network-compression-threshold=256
resource-pack=
resource-pack-sha1=
server-ip=
allow-flight=false
view-distance=10
simulation-distance=10
max-build-height=320
enforce-whitelist=false
entity-broadcast-range-percentage=100
rate-limit=0
max-tick-time=60000
use-native-transport=true
enable-status=true
broadcast-rcon-to-ops=true
sync-chunk-writes=true
enable-jmx-monitoring=false
broadcast-console-to-ops=true
enforce-secure-profile=true
log-ips=true
hide-online-players=false
EOF

    log "Server setup completed"
}

# Build the plugin
build_plugin() {
    log "Building plugin..."
    
    if [[ ! -f "${SCRIPT_DIR}/pom.xml" ]]; then
        error "pom.xml not found. Are you in the plugin directory?"
        exit 1
    fi
    
    cd "$SCRIPT_DIR"
    mvn clean package -q
    
    if [[ ! -f "$PLUGIN_JAR" ]]; then
        error "Plugin build failed. JAR file not found."
        exit 1
    fi
    
    log "Plugin built successfully"
}

# Install plugin to server
install_plugin() {
    if [[ ! -f "$PLUGIN_JAR" ]]; then
        warn "Plugin JAR not found. Building plugin first..."
        build_plugin
    fi
    
    if [[ ! -d "$PLUGINS_DIR" ]]; then
        warn "Plugins directory not found. Setting up server first..."
        setup_server
    fi
    
    log "Installing plugin to server..."
    cp "$PLUGIN_JAR" "$PLUGINS_DIR/"
    log "Plugin installed successfully"
}

# Start the server
start_server() {
    if [[ ! -f "$SERVER_JAR" ]]; then
        warn "Server not set up. Running setup first..."
        setup_server
    fi
    
    if is_server_running; then
        warn "Server is already running"
        return 0
    fi
    
    log "Starting Minecraft server..."
    cd "$SERVER_DIR"
    
    # Start server in background
    nohup java -Xmx2G -Xms1G -jar "$SERVER_JAR" nogui > "${LOGS_DIR}/server.log" 2>&1 &
    echo $! > "${SERVER_DIR}/server.pid"
    
    # Wait a bit and check if server started
    sleep 5
    if is_server_running; then
        log "Server started successfully (PID: $(cat "${SERVER_DIR}/server.pid"))"
        log "Server logs: tail -f ${LOGS_DIR}/server.log"
    else
        error "Failed to start server"
        exit 1
    fi
}

# Stop the server
stop_server() {
    if ! is_server_running; then
        warn "Server is not running"
        return 0
    fi
    
    log "Stopping Minecraft server..."
    
    local pid
    pid=$(cat "${SERVER_DIR}/server.pid" 2>/dev/null || echo "")
    
    if [[ -n "$pid" ]]; then
        # Send stop command to server
        echo "stop" > "${SERVER_DIR}/server.stdin" 2>/dev/null || true
        
        # Wait for graceful shutdown
        sleep 10
        
        # Force kill if still running
        if kill -0 "$pid" 2>/dev/null; then
            warn "Server didn't stop gracefully, force killing..."
            kill -9 "$pid" 2>/dev/null || true
        fi
        
        rm -f "${SERVER_DIR}/server.pid"
    fi
    
    log "Server stopped"
}

# Check if server is running
is_server_running() {
    if [[ -f "${SERVER_DIR}/server.pid" ]]; then
        local pid
        pid=$(cat "${SERVER_DIR}/server.pid")
        kill -0 "$pid" 2>/dev/null
    else
        return 1
    fi
}

# Show server status
show_status() {
    if is_server_running; then
        local pid
        pid=$(cat "${SERVER_DIR}/server.pid")
        log "Server is RUNNING (PID: $pid)"
        
        # Show resource usage if available
        if command -v ps &> /dev/null; then
            local mem_usage
            mem_usage=$(ps -p "$pid" -o rss= 2>/dev/null | awk '{print int($1/1024)}' || echo "unknown")
            info "Memory usage: ${mem_usage}MB"
        fi
    else
        warn "Server is STOPPED"
    fi
}

# Show server logs
show_logs() {
    if [[ -f "${LOGS_DIR}/server.log" ]]; then
        tail -n 50 "${LOGS_DIR}/server.log"
    else
        warn "No server logs found"
    fi
}

# Clean server data
clean_server() {
    if is_server_running; then
        error "Cannot clean while server is running. Stop the server first."
        exit 1
    fi
    
    warn "This will remove all server data including worlds and player data."
    read -p "Are you sure? (y/N): " -n 1 -r
    echo
    
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        log "Cleaning server data..."
        rm -rf "$SERVER_DIR"
        log "Server data cleaned"
    else
        log "Clean operation cancelled"
    fi
}

# Show network information
show_network() {
    info "Network Configuration:"
    echo "  Local server: localhost:25565"
    echo "  LAN server: $(hostname -I | awk '{print $1}' 2>/dev/null || echo "IP not found"):25565"
    echo ""
    echo "To connect from other devices on your network:"
    echo "  1. Find your local IP address"
    echo "  2. Make sure port 25565 is open in your firewall"
    echo "  3. Connect to <your-ip>:25565"
}

# Show online players
show_players() {
    if ! is_server_running; then
        warn "Server is not running"
        return 0
    fi
    
    info "Online players (check server logs for details):"
    grep -E "(joined|left) the game" "${LOGS_DIR}/server.log" 2>/dev/null | tail -10 || echo "No recent player activity"
}

# Main command handler
main() {
    case "${1:-help}" in
        setup)
            setup_server
            ;;
        build)
            build_plugin
            ;;
        install)
            install_plugin
            ;;
        start)
            start_server
            ;;
        stop)
            stop_server
            ;;
        restart)
            stop_server
            sleep 2
            start_server
            ;;
        status)
            show_status
            ;;
        logs)
            show_logs
            ;;
        clean)
            clean_server
            ;;
        reset)
            stop_server
            rm -rf "$WORLD_DIR" "${PLUGINS_DIR:?}/"*.jar
            install_plugin
            start_server
            ;;
        network)
            show_network
            ;;
        players)
            show_players
            ;;
        help|*)
            echo "Usage: $0 {setup|build|install|start|stop|restart|status|logs|clean|reset|network|players|help}"
            echo ""
            echo "Commands:"
            echo "  setup    - Set up server environment"
            echo "  build    - Build the plugin"
            echo "  install  - Install plugin to server"
            echo "  start    - Start the server"
            echo "  stop     - Stop the server"
            echo "  restart  - Restart the server"
            echo "  status   - Show server status"
            echo "  logs     - Show recent server logs"
            echo "  clean    - Remove all server data"
            echo "  reset    - Reset world and restart with fresh plugin"
            echo "  network  - Show network configuration"
            echo "  players  - Show recent player activity"
            echo "  help     - Show this help message"
            ;;
    esac
}

# Run main function with all arguments
main "$@"
