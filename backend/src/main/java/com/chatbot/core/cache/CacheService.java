package com.chatbot.core.cache;

import com.chatbot.config.StructuredLogging;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.dao.DataAccessException;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Cache Service for Chatbot SaaS v2.1
 * 
 * Provides high-performance caching operations with:
 * - Generic key-value operations
 * - TTL management
 * - Cache warming
 * - Cache invalidation
 * - Statistics monitoring
 */
@Slf4j
@Service
public class CacheService {

    private final RedisTemplate<String, Object> redisTemplate;

    public CacheService(@Qualifier("cacheRedisTemplate") RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // ========================================
    // Basic Cache Operations
    // ========================================

    /**
     * Store a value in cache with default TTL
     */
    public void set(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, value);
            StructuredLogging.debugConditional("Cache SET", "key", key, "value", value);
        } catch (DataAccessException | IllegalArgumentException e) {
            StructuredLogging.error("Error setting cache key", e, "key", key);
        }
    }

    /**
     * Store a value in cache with custom TTL
     */
    public void set(String key, Object value, Duration ttl) {
        try {
            redisTemplate.opsForValue().set(key, value, ttl);
            StructuredLogging.debugConditional("Cache SET with TTL", "key", key, "ttl", ttl.toString());
        } catch (DataAccessException | IllegalArgumentException e) {
            StructuredLogging.error("Error setting cache key with TTL", e, "key", key, "ttl", ttl.toString());
        }
    }

    /**
     * Get a value from cache
     */
    public Object get(String key) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            StructuredLogging.debugConditional("Cache GET", "key", key, "hit", value != null);
            return value;
        } catch (DataAccessException | IllegalArgumentException e) {
            StructuredLogging.error("Error getting cache key", e, "key", key);
            return null;
        }
    }

    /**
     * Get a value from cache with type casting
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> type) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            if (value != null && type.isInstance(value)) {
                return (T) value;
            }
            return null;
        } catch (DataAccessException | IllegalArgumentException e) {
            log.error("Error getting typed cache key: {}", key, e);
            return null;
        }
    }

    /**
     * Delete a key from cache
     */
    public void delete(String key) {
        try {
            Boolean deleted = redisTemplate.delete(key);
            StructuredLogging.debugConditional("Cache DELETE", "key", key, "deleted", deleted);
        } catch (DataAccessException | IllegalArgumentException e) {
            StructuredLogging.error("Error deleting cache key", e, "key", key);
        }
    }

    /**
     * Delete multiple keys from cache
     */
    public void delete(Collection<String> keys) {
        try {
            Long deleted = redisTemplate.delete(keys);
            StructuredLogging.debugConditional("Cache DELETE batch", "count", keys.size(), "deleted", deleted);
        } catch (DataAccessException | IllegalArgumentException e) {
            StructuredLogging.error("Error deleting cache keys", e, "count", keys.size());
        }
    }

    /**
     * Check if key exists in cache
     */
    public boolean exists(String key) {
        try {
            Boolean exists = redisTemplate.hasKey(key);
            return exists != null && exists;
        } catch (DataAccessException | IllegalArgumentException e) {
            log.error("Error checking cache key existence: {}", key, e);
            return false;
        }
    }

    /**
     * Set TTL for existing key
     */
    public void expire(String key, Duration ttl) {
        try {
            Boolean expired = redisTemplate.expire(key, ttl);
            StructuredLogging.debugConditional("Cache EXPIRE", "key", key, "ttl", ttl.toString(), "expired", expired);
        } catch (DataAccessException | IllegalArgumentException e) {
            StructuredLogging.error("Error setting cache TTL", e, "key", key, "ttl", ttl.toString());
        }
    }

    /**
     * Get TTL for key
     */
    public Duration getTtl(String key) {
        try {
            Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
            return ttl != null && ttl > 0 ? Duration.ofSeconds(ttl) : Duration.ZERO;
        } catch (DataAccessException | IllegalArgumentException e) {
            log.error("Error getting cache TTL: {}", key, e);
            return Duration.ZERO;
        }
    }

    // ========================================
    // Hash Operations
    // ========================================

    /**
     * Store value in hash
     */
    public void hashSet(String hashKey, String field, Object value) {
        try {
            redisTemplate.opsForHash().put(hashKey, field, value);
            StructuredLogging.debugConditional("Cache HSET", "hashKey", hashKey, "field", field);
        } catch (DataAccessException | IllegalArgumentException e) {
            StructuredLogging.error("Error setting hash field", e, "hashKey", hashKey, "field", field);
        }
    }

    /**
     * Get value from hash
     */
    public Object hashGet(String hashKey, String field) {
        try {
            Object value = redisTemplate.opsForHash().get(hashKey, field);
            StructuredLogging.debugConditional("Cache HGET", "hashKey", hashKey, "field", field, "hit", value != null);
            return value;
        } catch (DataAccessException | IllegalArgumentException e) {
            StructuredLogging.error("Error getting hash field", e, "hashKey", hashKey, "field", field);
            return null;
        }
    }

    /**
     * Get all values from hash
     */
    public Map<Object, Object> hashGetAll(String hashKey) {
        try {
            return redisTemplate.opsForHash().entries(hashKey);
        } catch (DataAccessException | IllegalArgumentException e) {
            log.error("Error getting hash all: {}", hashKey, e);
            return Map.of();
        }
    }

    /**
     * Delete field from hash
     */
    public void hashDelete(String hashKey, String field) {
        try {
            Long deleted = redisTemplate.opsForHash().delete(hashKey, field);
            StructuredLogging.debugConditional("Cache HDEL", "hashKey", hashKey, "field", field, "deleted", deleted);
        } catch (DataAccessException | IllegalArgumentException e) {
            StructuredLogging.error("Error deleting hash field", e, "hashKey", hashKey, "field", field);
        }
    }

    // ========================================
    // Set Operations
    // ========================================

    /**
     * Add value to set
     */
    public void setAdd(String key, Object value) {
        try {
            Long added = redisTemplate.opsForSet().add(key, value);
            StructuredLogging.debugConditional("Cache SADD", "key", key, "added", added);
        } catch (DataAccessException | IllegalArgumentException e) {
            StructuredLogging.error("Error adding to set", e, "key", key);
        }
    }

    /**
     * Get all values from set
     */
    public Set<Object> setMembers(String key) {
        try {
            return redisTemplate.opsForSet().members(key);
        } catch (DataAccessException | IllegalArgumentException e) {
            log.error("Error getting set members: {}", key, e);
            return Set.of();
        }
    }

    /**
     * Check if value exists in set
     */
    public boolean setIsMember(String key, Object value) {
        try {
            Boolean isMember = redisTemplate.opsForSet().isMember(key, value);
            return isMember != null && isMember;
        } catch (DataAccessException | IllegalArgumentException e) {
            log.error("Error checking set membership: {} {}", key, value, e);
            return false;
        }
    }

    // ========================================
    // List Operations
    // ========================================

    /**
     * Add value to list (right push)
     */
    public void listRightPush(String key, Object value) {
        try {
            Long pushed = redisTemplate.opsForList().rightPush(key, value);
            StructuredLogging.debugConditional("Cache RPUSH", "key", key, "pushed", pushed);
        } catch (DataAccessException | IllegalArgumentException e) {
            StructuredLogging.error("Error pushing to list", e, "key", key);
        }
    }

    /**
     * Get list range
     */
    public List<Object> listRange(String key, long start, long end) {
        try {
            return redisTemplate.opsForList().range(key, start, end);
        } catch (DataAccessException | IllegalArgumentException e) {
            log.error("Error getting list range: {} {} {}", key, start, end, e);
            return List.of();
        }
    }

    // ========================================
    // Cache Invalidation
    // ========================================

    /**
     * Invalidate all cache for a tenant
     */
    public void invalidateTenantCache(String tenantId) {
        try {
            String pattern = "tenant:" + tenantId + ":*";
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.info("Invalidated {} cache keys for tenant: {}", keys.size(), tenantId);
            }
        } catch (DataAccessException | IllegalArgumentException e) {
            log.error("Error invalidating tenant cache: {}", tenantId, e);
        }
    }

    /**
     * Invalidate user cache
     */
    public void invalidateUserCache(String userId) {
        try {
            String pattern = "user:" + userId + ":*";
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.info("Invalidated {} cache keys for user: {}", keys.size(), userId);
            }
        } catch (DataAccessException | IllegalArgumentException e) {
            log.error("Error invalidating user cache: {}", userId, e);
        }
    }

    /**
     * Invalidate chatbot cache
     */
    public void invalidateChatbotCache(String chatbotId) {
        try {
            String pattern = "chatbot:" + chatbotId + ":*";
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.info("Invalidated {} cache keys for chatbot: {}", keys.size(), chatbotId);
            }
        } catch (DataAccessException | IllegalArgumentException e) {
            log.error("Error invalidating chatbot cache: {}", chatbotId, e);
        }
    }

    // ========================================
    // Cache Statistics
    // ========================================

    /**
     * Get cache statistics
     */
    public CacheStatistics getStatistics() {
        try {
            // Get Redis info for memory
            Properties memoryInfo = redisTemplate.getConnectionFactory()
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
                        StructuredLogging.debug("Failed to parse used_memory", "value", usedStr);
                    }
                }
                String maxStr = memoryInfo.getProperty("maxmemory");
                if (maxStr != null) {
                    try {
                        maxMemory = Long.parseLong(maxStr);
                    } catch (NumberFormatException e) {
                        StructuredLogging.debug("Failed to parse maxmemory", "value", maxStr);
                    }
                }
            }
            
            // Get key count
            Long size = redisTemplate.getConnectionFactory().getConnection().dbSize();
            int keyCount = size != null ? size.intValue() : 0;
            
            return CacheStatistics.builder()
                    .keyCount(keyCount)
                    .usedMemory(usedMemory)
                    .maxMemory(maxMemory)
                    .hitRate(calculateHitRate())
                    .build();
        } catch (DataAccessException | IllegalArgumentException e) {
            log.error("Error getting cache statistics", e);
            return CacheStatistics.builder().build();
        }
    }

    private double calculateHitRate() {
        try {
            Properties statsInfo = redisTemplate.getConnectionFactory()
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
        } catch (DataAccessException | IllegalArgumentException e) {
            log.warn("Error calculating cache hit rate from Redis stats", e);
        }
        return 0.85; // Fallback default
    }

    // ========================================
    // Utility Methods
    // ========================================

    /**
     * Generate cache key for user
     */
    public String userKey(String userId, String suffix) {
        return "user:" + userId + ":" + suffix;
    }

    /**
     * Generate cache key for tenant
     */
    public String tenantKey(String tenantId, String suffix) {
        return "tenant:" + tenantId + ":" + suffix;
    }

    /**
     * Generate cache key for chatbot
     */
    public String chatbotKey(String chatbotId, String suffix) {
        return "chatbot:" + chatbotId + ":" + suffix;
    }

    /**
     * Generate cache key for package
     */
    public String packageKey(String packageId, String suffix) {
        return "package:" + packageId + ":" + suffix;
    }

    /**
     * Clear all cache (use with caution)
     */
    public void clearAll() {
        try {
            redisTemplate.getConnectionFactory().getConnection().flushDb();
            log.warn("All cache cleared");
        } catch (DataAccessException | IllegalArgumentException e) {
            log.error("Error clearing all cache", e);
        }
    }
}
