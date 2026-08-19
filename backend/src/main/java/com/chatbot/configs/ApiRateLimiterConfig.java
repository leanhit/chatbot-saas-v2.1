package com.chatbot.configs;

import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * ApiRateLimiterConfig - Configuration for API rate limiting
 * 
 * Provides rate limiting configuration for API endpoints using Resilience4j.
 * Helps prevent API abuse and ensures fair resource allocation.
 */
@Configuration
@Slf4j
public class ApiRateLimiterConfig {

    /**
     * Rate limiter registry configuration
     * Creates rate limiters for different API endpoint categories
     */
    @Bean
    public RateLimiterRegistry rateLimiterRegistry() {
        log.info("🔧 Configuring Rate Limiter Registry");
        
        // Default rate limiter configuration
        RateLimiterConfig defaultConfig = RateLimiterConfig.custom()
            .limitForPeriod(100) // Allow 100 requests per period
            .limitRefreshPeriod(Duration.ofSeconds(1)) // Reset every 1 second
            .timeoutDuration(Duration.ofMillis(500)) // Wait 500ms for permission
            .build();

        RateLimiterRegistry registry = RateLimiterRegistry.of(defaultConfig);
        
        // Create specific rate limiters for different endpoint categories
        
        // Strict rate limiter for authentication endpoints (10 req/sec)
        RateLimiterConfig authConfig = RateLimiterConfig.custom()
            .limitForPeriod(10)
            .limitRefreshPeriod(Duration.ofSeconds(1))
            .timeoutDuration(Duration.ofMillis(100))
            .build();
        registry.rateLimiter("authRateLimit", authConfig);
        
        // Moderate rate limiter for API endpoints (50 req/sec)
        RateLimiterConfig apiConfig = RateLimiterConfig.custom()
            .limitForPeriod(50)
            .limitRefreshPeriod(Duration.ofSeconds(1))
            .timeoutDuration(Duration.ofMillis(200))
            .build();
        registry.rateLimiter("apiRateLimit", apiConfig);
        
        // Lenient rate limiter for read operations (100 req/sec)
        RateLimiterConfig readConfig = RateLimiterConfig.custom()
            .limitForPeriod(100)
            .limitRefreshPeriod(Duration.ofSeconds(1))
            .timeoutDuration(Duration.ofMillis(500))
            .build();
        registry.rateLimiter("readRateLimit", readConfig);
        
        // Strict rate limiter for write operations (20 req/sec)
        RateLimiterConfig writeConfig = RateLimiterConfig.custom()
            .limitForPeriod(20)
            .limitRefreshPeriod(Duration.ofSeconds(1))
            .timeoutDuration(Duration.ofMillis(200))
            .build();
        registry.rateLimiter("writeRateLimit", writeConfig);
        
        // Very strict rate limiter for sensitive operations (5 req/sec)
        RateLimiterConfig sensitiveConfig = RateLimiterConfig.custom()
            .limitForPeriod(5)
            .limitRefreshPeriod(Duration.ofSeconds(1))
            .timeoutDuration(Duration.ofMillis(100))
            .build();
        registry.rateLimiter("sensitiveRateLimit", sensitiveConfig);
        
        log.info("✅ Rate limiters registered: auth (10/sec), api (50/sec), read (100/sec), write (20/sec), sensitive (5/sec)");
        
        return registry;
    }
}
