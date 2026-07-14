package com.chatbot.shared.penny.resilience;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * ApiResilienceConfig - Configuration for resilience patterns
 * 
 * Provides circuit breaker, retry, and timeout configuration for external API calls.
 * Uses Resilience4j library for production-grade resilience patterns.
 */
@Configuration
@Slf4j
public class ApiResilienceConfig {

    /**
     * Circuit breaker configuration for OpenAI API
     */
    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        log.info("🔧 Configuring Circuit Breaker Registry");
        
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
            .failureRateThreshold(50) // Open circuit if 50% of calls fail
            .waitDurationInOpenState(Duration.ofSeconds(30)) // Wait 30s before trying again
            .permittedNumberOfCallsInHalfOpenState(3) // Try 3 calls in half-open state
            .slidingWindowSize(10) // Consider last 10 calls
            .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
            .build();

        return CircuitBreakerRegistry.of(config);
    }

    /**
     * Retry configuration for API calls
     */
    @Bean
    public RetryRegistry retryRegistry() {
        log.info("🔧 Configuring Retry Registry");
        
        RetryConfig config = RetryConfig.custom()
            .maxAttempts(3) // Retry up to 3 times
            .waitDuration(Duration.ofMillis(500)) // Wait 500ms between retries
            .retryOnException(e -> {
                // Retry on specific exceptions
                return e instanceof java.io.IOException 
                    || e instanceof java.net.SocketTimeoutException
                    || e instanceof java.util.concurrent.TimeoutException;
            })
            .ignoreExceptions(IllegalArgumentException.class) // Don't retry on bad requests
            .build();

        return RetryRegistry.of(config);
    }

    /**
     * Time limiter configuration for API calls
     */
    @Bean
    public TimeLimiterConfig timeLimiterConfig() {
        log.info("🔧 Configuring Time Limiter");
        
        return TimeLimiterConfig.custom()
            .timeoutDuration(Duration.ofSeconds(30)) // Timeout after 30s
            .cancelRunningFuture(true) // Cancel running future on timeout
            .build();
    }
}
