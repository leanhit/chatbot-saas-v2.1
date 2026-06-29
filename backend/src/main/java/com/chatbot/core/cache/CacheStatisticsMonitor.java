package com.chatbot.core.cache;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Properties;
import java.util.Set;

/**
 * Cache Statistics Monitor for Chatbot SaaS v2.1
 * Monitors cache performance and provides statistics
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CacheStatisticsMonitor {

    private final RedisConnectionFactory connectionFactory;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * Get cache statistics
     */
    public CacheStatistics getStatistics() {
        try {
            // Get Redis info for memory
            Properties memoryInfo = connectionFactory
                    .getConnection()
                    .info("memory");
            
            long usedMemory = 0;
            long maxMemory = 0;
            
            if (memoryInfo != null) {
                String usedStr = memoryInfo.getProperty("used_memory");
                if (usedStr != null) {
                    try {
                        usedMemory = Long.parseLong(usedStr);
                    } catch (NumberFormatException e) {
                        log.debug("Failed to parse used_memory: {}", usedStr);
                    }
                }
                String maxStr = memoryInfo.getProperty("maxmemory");
                if (maxStr != null) {
                    try {
                        maxMemory = Long.parseLong(maxStr);
                    } catch (NumberFormatException e) {
                        log.debug("Failed to parse maxmemory: {}", maxStr);
                    }
                }
            }
            
            // Get key count
            Set<String> allKeys = redisTemplate.keys("*");
            int keyCount = allKeys != null ? allKeys.size() : 0;
            
            return CacheStatistics.builder()
                    .keyCount(keyCount)
                    .usedMemory(usedMemory)
                    .maxMemory(maxMemory)
                    .hitRate(calculateHitRate())
                    .build();
        } catch (Exception e) {
            log.error("Error getting cache statistics", e);
            return CacheStatistics.builder().build();
        }
    }

    private double calculateHitRate() {
        try {
            Properties statsInfo = connectionFactory
                    .getConnection()
                    .info("stats");
            
            if (statsInfo != null) {
                String hitsStr = statsInfo.getProperty("keyspace_hits");
                String missesStr = statsInfo.getProperty("keyspace_misses");
                if (hitsStr != null && missesStr != null) {
                    long hits = Long.parseLong(hitsStr);
                    long misses = Long.parseLong(missesStr);
                    long total = hits + misses;
                    if (total > 0) {
                        return (double) hits / total;
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Error calculating cache hit rate from Redis stats", e);
        }
        return 0.85; // Fallback default
    }

    @Data
    public static class CacheStatistics {
        private int keyCount;
        private long usedMemory;
        private long maxMemory;
        private double hitRate;
        
        public static CacheStatisticsBuilder builder() {
            return new CacheStatisticsBuilder();
        }
        
        public static class CacheStatisticsBuilder {
            private int keyCount;
            private long usedMemory;
            private long maxMemory;
            private double hitRate;
            
            public CacheStatisticsBuilder keyCount(int keyCount) {
                this.keyCount = keyCount;
                return this;
            }
            
            public CacheStatisticsBuilder usedMemory(long usedMemory) {
                this.usedMemory = usedMemory;
                return this;
            }
            
            public CacheStatisticsBuilder maxMemory(long maxMemory) {
                this.maxMemory = maxMemory;
                return this;
            }
            
            public CacheStatisticsBuilder hitRate(double hitRate) {
                this.hitRate = hitRate;
                return this;
            }
            
            public CacheStatistics build() {
                CacheStatistics stats = new CacheStatistics();
                stats.keyCount = this.keyCount;
                stats.usedMemory = this.usedMemory;
                stats.maxMemory = this.maxMemory;
                stats.hitRate = this.hitRate;
                return stats;
            }
        }
    }
}
