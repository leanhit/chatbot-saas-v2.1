package com.chatbot.shared.penny.resilience;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;

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
     * Includes Micrometer metrics integration for monitoring
     */
    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        log.info("🔧 Configuring Circuit Breaker Registry with Micrometer metrics");
        
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
            .failureRateThreshold(50) // Open circuit if 50% of calls fail
            .waitDurationInOpenState(Duration.ofSeconds(30)) // Wait 30s before trying again
            .permittedNumberOfCallsInHalfOpenState(3) // Try 3 calls in half-open state
            .slidingWindowSize(10) // Consider last 10 calls
            .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
            .build();

        CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(config);
        
        // Circuit breaker metrics are automatically exposed via Spring Boot Actuator
        // Configured in application.properties with resilience4j.circuitbreaker.configs.default.metrics.*
        log.info("✅ Circuit breaker metrics enabled via Spring Boot Actuator");

        return registry;
    }

    /**
     * Retry configuration for API calls
     * Includes HTTP 429 (rate limiting) handling with exponential backoff
     */
    @Bean
    public RetryRegistry retryRegistry() {
        log.info("🔧 Configuring Retry Registry with HTTP 429 support");
        
        RetryConfig config = RetryConfig.custom()
            .maxAttempts(3) // Retry up to 3 times
            .intervalFunction(io.github.resilience4j.core.IntervalFunction.ofExponentialBackoff(500, 2, 10000)) // Exponential backoff: 500ms base, multiplier 2, max 10s
            .retryOnException(e -> {
                // Retry on specific exceptions
                return e instanceof java.io.IOException 
                    || e instanceof java.net.SocketTimeoutException
                    || e instanceof java.util.concurrent.TimeoutException;
            })
            .retryOnResult(result -> {
                // Retry on HTTP 429 (rate limiting)
                if (result instanceof org.springframework.http.ResponseEntity) {
                    org.springframework.http.ResponseEntity<?> response = (org.springframework.http.ResponseEntity<?>) result;
                    return HttpStatus.TOO_MANY_REQUESTS.equals(response.getStatusCode());
                }
                return false;
            })
            .ignoreExceptions(IllegalArgumentException.class) // Don't retry on bad requests
            .build();

        RetryRegistry registry = RetryRegistry.of(config);
        
        // Retry metrics are automatically exposed via Spring Boot Actuator
        // Configured in application.properties with resilience4j.retry.configs.default.metrics.*
        log.info("✅ Retry metrics enabled via Spring Boot Actuator");

        return registry;
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
