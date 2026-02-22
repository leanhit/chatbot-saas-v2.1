package com.chatbot.core.config.runtime.service;

import com.chatbot.core.config.runtime.dto.ConfigResponse;
import com.chatbot.core.config.runtime.service.RuntimeConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConfigCacheService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final RuntimeConfigService runtimeConfigService;
    
    // Local cache for frequently accessed configs
    private final Map<String, String> localCache = new ConcurrentHashMap<>();
    private static final Duration CACHE_TTL = Duration.ofMinutes(30);
    private static final String CACHE_PREFIX = "config:";

    public String getConfigValue(String configKey, Long tenantId, Long userId) {
        String cacheKey = buildCacheKey(configKey, tenantId, userId);
        
        // Try local cache first
        String value = localCache.get(cacheKey);
        if (value != null) {
            return value;
        }

        // Try Redis cache
        try {
            value = (String) redisTemplate.opsForValue().get(cacheKey);
            if (value != null) {
                localCache.put(cacheKey, value);
                return value;
            }
        } catch (Exception e) {
            log.warn("Redis cache error, falling back to database", e);
        }

        // Fallback to database
        value = runtimeConfigService.getConfigValue(configKey, tenantId, userId);
        if (value != null) {
            // Cache the value
            localCache.put(cacheKey, value);
            try {
                redisTemplate.opsForValue().set(cacheKey, value, CACHE_TTL);
            } catch (Exception e) {
                log.warn("Failed to cache in Redis", e);
            }
        }

        return value;
    }

    public void invalidateConfig(String configKey, Long tenantId, Long userId) {
        String cacheKey = buildCacheKey(configKey, tenantId, userId);
        
        // Remove from local cache
        localCache.remove(cacheKey);
        
        // Remove from Redis
        try {
            redisTemplate.delete(cacheKey);
        } catch (Exception e) {
            log.warn("Failed to invalidate Redis cache", e);
        }
        
        log.info("Invalidated cache for config: {}", cacheKey);
    }

    public void invalidateTenantConfigs(Long tenantId) {
        // Invalidate all tenant-specific configs
        try {
            String pattern = CACHE_PREFIX + "*:tenant:" + tenantId + ":*";
            redisTemplate.delete(redisTemplate.keys(pattern));
        } catch (Exception e) {
            log.warn("Failed to invalidate tenant configs in Redis", e);
        }

        // Clear local cache entries for this tenant
        localCache.entrySet().removeIf(entry -> 
            entry.getKey().contains(":tenant:" + tenantId + ":"));
        
        log.info("Invalidated all configs for tenant: {}", tenantId);
    }

    public void invalidateUserConfigs(Long userId) {
        // Invalidate all user-specific configs
        try {
            String pattern = CACHE_PREFIX + "*:user:" + userId + ":*";
            redisTemplate.delete(redisTemplate.keys(pattern));
        } catch (Exception e) {
            log.warn("Failed to invalidate user configs in Redis", e);
        }

        // Clear local cache entries for this user
        localCache.entrySet().removeIf(entry -> 
            entry.getKey().contains(":user:" + userId + ":"));
        
        log.info("Invalidated all configs for user: {}", userId);
    }

    public void warmupCache(Long tenantId, Long userId) {
        try {
            List<ConfigResponse> configs = runtimeConfigService.getAllConfigs(tenantId, userId);
            
            for (ConfigResponse config : configs) {
                String cacheKey = buildCacheKey(config.getConfigKey(), tenantId, userId);
                String value = config.getConfigValue() != null ? config.getConfigValue() : config.getDefaultValue();
                
                if (value != null) {
                    localCache.put(cacheKey, value);
                    try {
                        redisTemplate.opsForValue().set(cacheKey, value, CACHE_TTL);
                    } catch (Exception e) {
                        log.warn("Failed to warmup cache for config: {}", config.getConfigKey(), e);
                    }
                }
            }
            
            log.info("Warmed up cache for {} configs", configs.size());
        } catch (Exception e) {
            log.error("Failed to warmup cache", e);
        }
    }

    private String buildCacheKey(String configKey, Long tenantId, Long userId) {
        StringBuilder keyBuilder = new StringBuilder(CACHE_PREFIX);
        keyBuilder.append(configKey);
        
        if (tenantId != null) {
            keyBuilder.append(":tenant:").append(tenantId);
        }
        
        if (userId != null) {
            keyBuilder.append(":user:").append(userId);
        }
        
        return keyBuilder.toString();
    }
}
