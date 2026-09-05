package com.chatbot.config;

import com.chatbot.core.cache.CacheWarmer;
import com.chatbot.core.cache.CacheStatisticsMonitor;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.caffeine.CaffeineCacheManager;

/**
 * Cache Configuration — single source of truth for RedisCacheManager.
 *
 * Redis connection infrastructure (RedisConnectionFactory, RedisTemplate, StringRedisTemplate)
 * is owned by {@link UnifiedRedisConfig} only.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Primary Redis Cache Manager with custom TTL configurations.
     */
    @Bean
    @Primary
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.activateDefaultTyping(
                objectMapper.getPolymorphicTypeValidator(),
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );

        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(objectMapper);

        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30))
                .disableCachingNullValues()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer));

        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        // --- Application caches ---
        cacheConfigurations.put("userSessions",       defaultConfig.entryTtl(Duration.ofHours(1)));
        cacheConfigurations.put("apiResponses",        defaultConfig.entryTtl(Duration.ofMinutes(5)));
        cacheConfigurations.put("databaseQueries",     defaultConfig.entryTtl(Duration.ofMinutes(10)));
        cacheConfigurations.put("staticContent",       defaultConfig.entryTtl(Duration.ofDays(1)));
        cacheConfigurations.put("rateLimiting",        defaultConfig.entryTtl(Duration.ofHours(1)));
        cacheConfigurations.put("tenants",             defaultConfig.entryTtl(Duration.ofMinutes(15)));
        cacheConfigurations.put("tenant-key-info",     defaultConfig.entryTtl(Duration.ofMinutes(15)));
        cacheConfigurations.put("tenant-profiles",      defaultConfig.entryTtl(Duration.ofMinutes(30)));
        cacheConfigurations.put("tenant-profiles-batch", defaultConfig.entryTtl(Duration.ofMinutes(30)));
        cacheConfigurations.put("user-roles",          defaultConfig.entryTtl(Duration.ofMinutes(30)));
        cacheConfigurations.put("tenant-roles",        defaultConfig.entryTtl(Duration.ofMinutes(15)));
        cacheConfigurations.put("chatbots",            defaultConfig.entryTtl(Duration.ofMinutes(10)));
        cacheConfigurations.put("messageStats",        defaultConfig.entryTtl(Duration.ofMinutes(5)));
        cacheConfigurations.put("payments",            defaultConfig.entryTtl(Duration.ofMinutes(2)));
        cacheConfigurations.put("subscriptions",       defaultConfig.entryTtl(Duration.ofHours(1)));
        cacheConfigurations.put("auditLogs",           defaultConfig.entryTtl(Duration.ofMinutes(30)));
        cacheConfigurations.put("users",               defaultConfig.entryTtl(Duration.ofMinutes(30)));
        cacheConfigurations.put("conversations",       defaultConfig.entryTtl(Duration.ofMinutes(10)));

        // --- Package caches (merged from PackageCacheConfig) ---
        cacheConfigurations.put("packages",            defaultConfig.entryTtl(Duration.ofMinutes(30)));
        cacheConfigurations.put("packages-active",     defaultConfig.entryTtl(Duration.ofHours(1)));
        cacheConfigurations.put("packages-all",        defaultConfig.entryTtl(Duration.ofHours(1)));
        cacheConfigurations.put("activePackages",      defaultConfig.entryTtl(Duration.ofMinutes(30)));
        cacheConfigurations.put("packageValidation",   defaultConfig.entryTtl(Duration.ofMinutes(30)));
        cacheConfigurations.put("packageUsage",        defaultConfig.entryTtl(Duration.ofMinutes(30)));
        cacheConfigurations.put("activeDiscounts",     defaultConfig.entryTtl(Duration.ofMinutes(30)));
        cacheConfigurations.put("tenantPackages",       defaultConfig.entryTtl(Duration.ofMinutes(5)));
        cacheConfigurations.put("tenantPackageInfo",    defaultConfig.entryTtl(Duration.ofMinutes(5)));
        cacheConfigurations.put("systemConfig",        defaultConfig.entryTtl(Duration.ofHours(2)));

        // --- Location caches ---
        cacheConfigurations.put("provinces",           defaultConfig.entryTtl(Duration.ofDays(1)));
        cacheConfigurations.put("districts",           defaultConfig.entryTtl(Duration.ofDays(1)));
        cacheConfigurations.put("wards",               defaultConfig.entryTtl(Duration.ofDays(1)));
        cacheConfigurations.put("tenant-key-to-id",    RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(15))
                .disableCachingNullValues()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new org.springframework.data.redis.serializer.GenericToStringSerializer<>(Long.class))));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }

    /**
     * Local In-Memory Cache Manager using Caffeine.
     * Ideal for highly frequent reads that don't need distributed caching.
     */
    @Bean("localCacheManager")
    public CacheManager caffeineCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofMinutes(5))
                .maximumSize(1000)
                .recordStats());
        return cacheManager;
    }



    /**
     * Cache statistics monitor
     */
    @Bean
    public CacheStatisticsMonitor cacheStatisticsMonitor(RedisConnectionFactory connectionFactory,
                                                          RedisTemplate<String, Object> redisTemplate) {
        return new CacheStatisticsMonitor(connectionFactory, redisTemplate);
    }
}
