package com.chatbot.core.simplepayment.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.Arrays;

@Configuration
@EnableCaching
public class PackageCacheConfig {

    /**
     * Primary cache manager for package caching
     */
    @Bean
    @Primary
    public CacheManager cacheManager() {
        org.springframework.cache.support.SimpleCacheManager cacheManager = new org.springframework.cache.support.SimpleCacheManager();
        
        // Define caches for different package operations
        cacheManager.setCaches(Arrays.asList(
            new ConcurrentMapCache("packages"),           // General package cache
            new ConcurrentMapCache("activePackages"),     // Active packages cache  
            new ConcurrentMapCache("packageValidation"), // Package validation cache
            new ConcurrentMapCache("packageUsage"),        // Package usage statistics cache
            // Also include location caches to avoid conflicts
            new ConcurrentMapCache("provinces"),
            new ConcurrentMapCache("districts"), 
            new ConcurrentMapCache("wards"),
            new ConcurrentMapCache("tenant-key-to-id")
        ));
        
        return cacheManager;
    }
}
