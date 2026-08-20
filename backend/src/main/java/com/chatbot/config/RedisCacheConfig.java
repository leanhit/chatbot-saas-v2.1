package com.chatbot.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Redis Cache Configuration
 * Configures cache TTLs and serialization for different cache types
 */
@Configuration
@EnableCaching
public class RedisCacheConfig {

    /**
     * Configure Redis Cache Manager with custom TTLs per cache
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        // Default cache configuration
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30)) // Default 30 minutes TTL
                .disableCachingNullValues()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
                        new GenericJackson2JsonRedisSerializer(objectMapper())));

        // Custom TTL configurations for different cache types
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        // Tenant data - rarely changes, cache for 1 hour
        cacheConfigurations.put("tenants", defaultConfig.entryTtl(Duration.ofHours(1)));
        cacheConfigurations.put("tenant-key-to-id", defaultConfig.entryTtl(Duration.ofHours(1)));
        
        // Tenant profiles - changes infrequently, cache for 30 minutes
        cacheConfigurations.put("tenant-profiles", defaultConfig.entryTtl(Duration.ofMinutes(30)));
        cacheConfigurations.put("tenant-profiles-batch", defaultConfig.entryTtl(Duration.ofMinutes(30)));
        
        // User data - changes occasionally, cache for 15 minutes
        cacheConfigurations.put("users", defaultConfig.entryTtl(Duration.ofMinutes(15)));
        
        // User roles - changes rarely, cache for 30 minutes
        cacheConfigurations.put("user-roles", defaultConfig.entryTtl(Duration.ofMinutes(30)));
        
        // Tenant roles/membership - changes occasionally, cache for 15 minutes
        cacheConfigurations.put("tenant-roles", defaultConfig.entryTtl(Duration.ofMinutes(15)));
        
        // Package constraints - rarely changes, cache for 1 hour
        cacheConfigurations.put("packages", defaultConfig.entryTtl(Duration.ofHours(1)));
        cacheConfigurations.put("packages-active", defaultConfig.entryTtl(Duration.ofHours(1)));
        cacheConfigurations.put("packages-all", defaultConfig.entryTtl(Duration.ofHours(1)));
        
        // API responses - short cache for 5 minutes
        cacheConfigurations.put("apiResponses", defaultConfig.entryTtl(Duration.ofMinutes(5)));

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .transactionAware()
                .build();
    }

    /**
     * Configure ObjectMapper for JSON serialization with type information
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );
        return mapper;
    }
}
