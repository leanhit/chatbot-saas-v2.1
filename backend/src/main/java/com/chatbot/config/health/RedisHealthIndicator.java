package com.chatbot.config.health;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Redis Health Indicator - Checks connectivity to Redis server
 */
@Component
@ConditionalOnBean(name = "redisTemplate")
@Slf4j
public class RedisHealthIndicator implements HealthIndicator {

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public Health health() {
        Map<String, Object> details = new HashMap<>();
        
        if (redisTemplate == null) {
            details.put("status", "NOT_CONFIGURED");
            details.put("message", "RedisTemplate not available");
            return Health.up()
                .withDetails(details)
                .build();
        }
        
        try {
            // Perform a PING command to check connectivity
            String response = redisTemplate.getConnectionFactory()
                .getConnection()
                .ping();
            
            details.put("status", "UP");
            details.put("ping", response);
            
            // Perform a simple SET/GET operation to verify read/write
            String testKey = "health-check:" + System.currentTimeMillis();
            redisTemplate.opsForValue().set(testKey, "ok", 5, TimeUnit.SECONDS);
            String testValue = (String) redisTemplate.opsForValue().get(testKey);
            redisTemplate.delete(testKey);
            
            if ("ok".equals(testValue)) {
                details.put("readWrite", "OK");
            } else {
                details.put("readWrite", "FAILED");
                log.warn("⚠️ Redis read/write test failed");
            }
            
            log.debug("✅ Redis health check passed");
            
            return Health.up()
                .withDetails(details)
                .build();
            
        } catch (Exception e) {
            log.error("❌ Redis health check failed: {}", e.getMessage());
            details.put("status", "DOWN");
            details.put("error", e.getMessage());
            // Return UP to avoid bringing down the entire health check
            return Health.up()
                .withDetails(details)
                .build();
        }
    }
}
