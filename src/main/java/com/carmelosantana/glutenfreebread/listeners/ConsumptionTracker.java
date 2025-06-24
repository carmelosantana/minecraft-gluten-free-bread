package com.carmelosantana.glutenfreebread.listeners;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks player consumption of gluten-free bread to prevent overconsumption.
 * 
 * This class maintains a rolling window of consumption events per player
 * to detect when a player is consuming too much bread in a short time period.
 */
public class ConsumptionTracker {
    
    // Time window for tracking consumption (5 minutes in milliseconds)
    private static final long CONSUMPTION_WINDOW = 5 * 60 * 1000;
    
    // Maximum consumptions allowed within the time window
    private static final int MAX_CONSUMPTIONS = 5;
    
    // Player consumption data: UUID -> List of consumption timestamps
    private final Map<UUID, ConsumptionData> playerConsumptions;
    
    public ConsumptionTracker() {
        this.playerConsumptions = new ConcurrentHashMap<>();
    }
    
    /**
     * Record a consumption event for a player
     * @param player The player who consumed bread
     * @return true if the player is over-consuming, false otherwise
     */
    public boolean recordConsumption(Player player) {
        UUID playerId = player.getUniqueId();
        long currentTime = System.currentTimeMillis();
        
        // Get or create consumption data for the player
        ConsumptionData data = playerConsumptions.computeIfAbsent(playerId, k -> new ConsumptionData());
        
        // Clean up old consumption records
        data.cleanupOldRecords(currentTime);
        
        // Add new consumption record
        data.addConsumption(currentTime);
        
        // Check if over-consuming
        return data.isOverConsuming();
    }
    
    /**
     * Get the current consumption count for a player within the time window
     * @param player The player to check
     * @return Number of consumptions in the time window
     */
    public int getConsumptionCount(Player player) {
        UUID playerId = player.getUniqueId();
        ConsumptionData data = playerConsumptions.get(playerId);
        
        if (data == null) {
            return 0;
        }
        
        // Clean up old records first
        data.cleanupOldRecords(System.currentTimeMillis());
        return data.getConsumptionCount();
    }
    
    /**
     * Clear consumption data for a player (called when player quits)
     * @param player The player whose data should be cleared
     */
    public void clearPlayerData(Player player) {
        playerConsumptions.remove(player.getUniqueId());
    }
    
    /**
     * Clear all consumption data (for plugin reload/disable)
     */
    public void clearAllData() {
        playerConsumptions.clear();
    }
    
    /**
     * Get the consumption time window in seconds
     * @return time window in seconds
     */
    public int getConsumptionWindowSeconds() {
        return (int) (CONSUMPTION_WINDOW / 1000);
    }
    
    /**
     * Get the maximum allowed consumptions within the time window
     * @return maximum consumptions
     */
    public int getMaxConsumptions() {
        return MAX_CONSUMPTIONS;
    }
    
    /**
     * Internal class to track consumption data for a single player
     */
    private static class ConsumptionData {
        private final Map<Long, Integer> consumptions;
        
        public ConsumptionData() {
            this.consumptions = new HashMap<>();
        }
        
        /**
         * Add a consumption event at the specified time
         */
        public void addConsumption(long timestamp) {
            // Round timestamp to nearest second to group consumptions
            long roundedTime = (timestamp / 1000) * 1000;
            consumptions.merge(roundedTime, 1, Integer::sum);
        }
        
        /**
         * Remove consumption records older than the time window
         */
        public void cleanupOldRecords(long currentTime) {
            long cutoffTime = currentTime - CONSUMPTION_WINDOW;
            consumptions.entrySet().removeIf(entry -> entry.getKey() < cutoffTime);
        }
        
        /**
         * Get the total number of consumptions in the current time window
         */
        public int getConsumptionCount() {
            return consumptions.values().stream().mapToInt(Integer::intValue).sum();
        }
        
        /**
         * Check if the player is over-consuming based on current consumption count
         */
        public boolean isOverConsuming() {
            return getConsumptionCount() > MAX_CONSUMPTIONS;
        }
    }
}
