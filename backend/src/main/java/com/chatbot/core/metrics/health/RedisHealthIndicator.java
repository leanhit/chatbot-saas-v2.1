package com.chatbot.core.metrics.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Component;

/**
 * Custom health indicator for Redis connection.
 * Checks if Redis is responsive and can perform basic operations.
 */
@Component
public class RedisHealthIndicator implements HealthIndicator {

    private final RedisConnectionFactory redisConnectionFactory;

    public RedisHealthIndicator(RedisConnectionFactory redisConnectionFactory) {
        this.redisConnectionFactory = redisConnectionFactory;
    }

    @Override
    public Health health() {
        try {
            var connection = redisConnectionFactory.getConnection();
            String ping = connection.ping();
            connection.close();
            
            if ("PONG".equalsIgnoreCase(ping)) {
                return Health.up()
                    .withDetail("status", "Redis is responsive")
                    .withDetail("ping", ping)
                    .build();
            } else {
                return Health.down()
                    .withDetail("status", "Redis ping failed")
                    .withDetail("ping", ping)
                    .build();
            }
        } catch (Exception e) {
            return Health.down()
                .withDetail("status", "Redis connection failed")
                .withDetail("error", e.getMessage())
                .build();
        }
    }
}
