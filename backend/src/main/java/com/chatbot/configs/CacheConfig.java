package com.chatbot.configs;

import com.chatbot.core.cache.CacheWarmer;
import com.chatbot.core.cache.CacheStatisticsMonitor;
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

/**
 * Cache Configuration for Chatbot SaaS v2.1
 * 
 * Provides Redis-based caching with different TTL strategies:
 * - User sessions: 1 hour
 * - API responses: 5 minutes
 * - Database queries: 10 minutes
 * - Static content: 1 day
 * - Rate limiting: 1 hour
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Primary Redis Cache Manager with custom TTL configurations
     */
    @Bean
    @Primary
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // Create ObjectMapper with JSR310 module for LocalDateTime support
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.activateDefaultTyping(
                objectMapper.getPolymorphicTypeValidator(),
                ObjectMapper.DefaultTyping.NON_FINAL,
                com.fasterxml.jackson.annotation.JsonTypeInfo.As.PROPERTY
        );
        
        // Create custom serializer with JSR310 support
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(objectMapper);
        
        // Default cache configuration
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30)) // Default 30 minutes
                .disableCachingNullValues()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer));

        // Specific cache configurations with different TTLs
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        
        // User sessions - 1 hour
        cacheConfigurations.put("userSessions", defaultConfig.entryTtl(Duration.ofHours(1)));
        
        // API responses - 5 minutes
        cacheConfigurations.put("apiResponses", defaultConfig.entryTtl(Duration.ofMinutes(5)));
        
        // Database queries - 10 minutes
        cacheConfigurations.put("databaseQueries", defaultConfig.entryTtl(Duration.ofMinutes(10)));
        
        // Static content - 1 day
        cacheConfigurations.put("staticContent", defaultConfig.entryTtl(Duration.ofDays(1)));
        
        // Rate limiting - 1 hour
        cacheConfigurations.put("rateLimiting", defaultConfig.entryTtl(Duration.ofHours(1)));
        
        // Package configurations - 30 minutes
        cacheConfigurations.put("packages", defaultConfig.entryTtl(Duration.ofMinutes(30)));
        
        // Tenant configurations - 15 minutes
        cacheConfigurations.put("tenants", defaultConfig.entryTtl(Duration.ofMinutes(15)));
        
        // Chatbot configurations - 10 minutes
        cacheConfigurations.put("chatbots", defaultConfig.entryTtl(Duration.ofMinutes(10)));
        
        // Message statistics - 5 minutes
        cacheConfigurations.put("messageStats", defaultConfig.entryTtl(Duration.ofMinutes(5)));
        
        // Payment processing - 2 minutes
        cacheConfigurations.put("payments", defaultConfig.entryTtl(Duration.ofMinutes(2)));
        
        // Subscription data - 1 hour
        cacheConfigurations.put("subscriptions", defaultConfig.entryTtl(Duration.ofHours(1)));
        
        // Audit logs - 30 minutes
        cacheConfigurations.put("auditLogs", defaultConfig.entryTtl(Duration.ofMinutes(30)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }

    /**
     * Redis Template for custom caching operations
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        // Create ObjectMapper with JSR310 module for LocalDateTime support
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.activateDefaultTyping(
                objectMapper.getPolymorphicTypeValidator(),
                ObjectMapper.DefaultTyping.NON_FINAL,
                com.fasterxml.jackson.annotation.JsonTypeInfo.As.PROPERTY
        );
        
        // Create custom serializer with JSR310 support
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(objectMapper);
        
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        // Use String serializer for keys
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        
        // Use JSON serializer for values with JSR310 support
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);
        
        template.afterPropertiesSet();
        return template;
    }

    /**
     * Cache warming configuration
     */
    @Bean
    public CacheWarmer cacheWarmer(RedisTemplate<String, Object> redisTemplate) {
        return new CacheWarmer(redisTemplate);
    }

    /**
     * Cache statistics monitor
     */
    @Bean
    public CacheStatisticsMonitor cacheStatisticsMonitor(RedisConnectionFactory connectionFactory, RedisTemplate<String, Object> redisTemplate) {
        return new CacheStatisticsMonitor(connectionFactory, redisTemplate);
    }
}
