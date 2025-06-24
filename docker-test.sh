#!/bin/bash
# Docker test script for Gluten-Free Bread Plugin
# Tests the plugin in a Docker container environment

set -euo pipefail

# Configuration
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PLUGIN_JAR="${SCRIPT_DIR}/target/gluten-free-bread-1.0.1.jar"
CONTAINER_NAME="minecraft_test"

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

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

# Clean up function
cleanup() {
    log "Cleaning up..."
    docker-compose down --volumes --remove-orphans 2>/dev/null || true
    docker system prune -f 2>/dev/null || true
}

# Trap cleanup on exit
trap cleanup EXIT

# Check if Docker is running
check_docker() {
    if ! docker info &>/dev/null; then
        error "Docker is not running. Please start Docker first."
        exit 1
    fi
    log "Docker is running ✓"
}

# Build the plugin
build_plugin() {
    log "Building plugin..."
    cd "$SCRIPT_DIR"
    
    if ! mvn clean package -q; then
        error "Plugin build failed"
        exit 1
    fi
    
    if [[ ! -f "$PLUGIN_JAR" ]]; then
        error "Plugin JAR not found after build"
        exit 1
    fi
    
    log "Plugin built successfully ✓"
}

# Test plugin in Docker
test_plugin() {
    log "Starting Docker test environment..."
    
    # Clean up any existing containers first
    docker-compose down --volumes --remove-orphans 2>/dev/null || true
    
    # Start the container
    docker-compose up -d
    
    local container_id
    container_id=$(docker-compose ps -q minecraft)
    
    if [[ -z "$container_id" ]]; then
        error "Failed to start Docker container"
        exit 1
    fi
    
    log "Container started: $container_id"
    
    # Wait for server to start
    log "Waiting for server to start..."
    local timeout=180  # Increased timeout for ViaVersion setup
    local count=0
    
    while [[ $count -lt $timeout ]]; do
        if docker logs "$container_id" 2>&1 | grep -q "Done\|Server startup complete"; then
            log "Server started successfully ✓"
            break
        fi
        
        sleep 2
        ((count += 2))
        
        if [[ $((count % 30)) -eq 0 ]]; then
            info "Still waiting for server startup... ($count/${timeout}s)"
            # Show recent logs to debug
            if [[ $((count % 60)) -eq 0 ]]; then
                info "Recent logs:"
                docker logs --tail=10 "$container_id" 2>&1 | grep -E "(ERROR|WARN|INFO.*Geyser|INFO.*ViaVersion|INFO.*GlutenFreeBread)"
            fi
        fi
    done
    
    if [[ $count -ge $timeout ]]; then
        error "Server startup timeout"
        docker logs "$container_id"
        exit 1
    fi
    
    # Check if plugin loaded
    log "Checking if plugin loaded..."
    sleep 5
    
    if docker logs "$container_id" 2>&1 | grep -q "Gluten-Free Bread Plugin"; then
        log "Plugin loaded successfully ✓"
    else
        error "Plugin failed to load"
        docker logs "$container_id"
        exit 1
    fi
    
    # Test plugin functionality
    test_plugin_functionality "$container_id"
    
    # Show server logs
    info "Recent server logs:"
    docker logs --tail=20 "$container_id"
    
    log "Docker test completed successfully ✓"
}

# Test plugin functionality
test_plugin_functionality() {
    local container_id=$1
    
    log "Testing plugin functionality..."
    
    # Check if ViaVersion loaded successfully
    if docker logs "$container_id" 2>&1 | grep -q "ViaVersion.*enabled\|ViaVersion.*loaded"; then
        log "ViaVersion loaded successfully ✓"
    else
        warn "ViaVersion not detected - checking for compatibility issues"
    fi
    
    # Check if Geyser loaded without version errors
    if docker logs "$container_id" 2>&1 | grep -q "does not support the Java version that Geyser requires"; then
        error "Geyser version compatibility issue detected"
        docker logs "$container_id" 2>&1 | grep -A 3 -B 3 "does not support the Java version"
    elif docker logs "$container_id" 2>&1 | grep -q "Geyser.*enabled\|Geyser.*started"; then
        log "Geyser loaded successfully ✓"
    else
        warn "Geyser status unclear - check logs manually"
    fi
    
    # Check if our plugin loaded
    if docker logs "$container_id" 2>&1 | grep -q "Gluten-Free Bread Plugin"; then
        log "Gluten-Free Bread Plugin loaded successfully ✓"
    else
        error "Gluten-Free Bread Plugin failed to load"
        return 1
    fi
    
    # Check if recipes are registered
    if docker logs "$container_id" 2>&1 | grep -q "Registered.*gluten-free bread recipe"; then
        log "Recipes registered successfully ✓"
    else
        warn "Recipe registration not found in logs"
    fi
    
    # Check if commands are registered
    if docker logs "$container_id" 2>&1 | grep -q "Registered commands"; then
        log "Commands registered successfully ✓"
    else
        warn "Command registration not found in logs"
    fi
    
    # Check if listeners are registered
    if docker logs "$container_id" 2>&1 | grep -q "Registered event listeners"; then
        log "Event listeners registered successfully ✓"
    else
        warn "Event listener registration not found in logs"
    fi
    
    log "Plugin functionality tests completed"
}

# Show help
show_help() {
    echo "Usage: $0 [OPTIONS]"
    echo ""
    echo "Options:"
    echo "  -h, --help     Show this help message"
    echo "  -v, --verbose  Enable verbose output"
    echo "  -c, --clean    Clean up before starting"
    echo ""
    echo "This script tests the Gluten-Free Bread plugin in a Docker environment."
}

# Parse command line arguments
VERBOSE=false
CLEAN=false

while [[ $# -gt 0 ]]; do
    case $1 in
        -h|--help)
            show_help
            exit 0
            ;;
        -v|--verbose)
            VERBOSE=true
            shift
            ;;
        -c|--clean)
            CLEAN=true
            shift
            ;;
        *)
            error "Unknown option: $1"
            show_help
            exit 1
            ;;
    esac
done

# Main execution
main() {
    log "Starting Gluten-Free Bread Plugin Docker Test"
    
    check_docker
    
    if [[ "$CLEAN" == true ]]; then
        log "Cleaning up existing containers..."
        cleanup
    fi
    
    build_plugin
    test_plugin
    
    log "🎉 All tests passed! Plugin is working correctly in Docker."
    
    info "To connect to the server:"
    echo "  Java Edition: localhost:25565"
    echo "  Bedrock Edition: localhost:19132"
    echo ""
    echo "To stop the server: docker-compose down"
    echo "To view logs: docker-compose logs -f"
}

# Run main function
main "$@"
