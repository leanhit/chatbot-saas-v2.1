package com.chatbot.core.grpc.resilience;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.concurrent.TimeoutException;

/**
 * GrpcResilienceConfig - Configuration for gRPC resilience patterns
 * 
 * Provides circuit breaker, retry, and timeout configuration specifically for gRPC calls.
 * Uses Resilience4j library for production-grade resilience patterns.
 */
@Configuration
@Slf4j
public class GrpcResilienceConfig {

    /**
     * Circuit breaker configuration for gRPC services
     */
    @Bean
    public CircuitBreakerRegistry grpcCircuitBreakerRegistry() {
        log.info("🔧 Configuring gRPC Circuit Breaker Registry");
        
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
            .failureRateThreshold(50) // Open circuit if 50% of calls fail
            .waitDurationInOpenState(Duration.ofSeconds(30)) // Wait 30s before trying again
            .permittedNumberOfCallsInHalfOpenState(3) // Try 3 calls in half-open state
            .slidingWindowSize(10) // Consider last 10 calls
            .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
            .ignoreExceptions(IllegalArgumentException.class) // Don't open circuit for bad requests
            .build();

        CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(config);
        
        // Create specific circuit breakers for each gRPC service
        registry.circuitBreaker("tenantGrpcService", config);
        registry.circuitBreaker("identityGrpcService", config);
        registry.circuitBreaker("userGrpcService", config);
        registry.circuitBreaker("messageGrpcService", config);
        registry.circuitBreaker("appGrpcService", config);
        
        log.info("✅ gRPC Circuit Breakers registered: tenant, identity, user, message, app");
        
        return registry;
    }

    /**
     * Retry configuration for gRPC calls
     */
    @Bean
    public RetryRegistry grpcRetryRegistry() {
        log.info("🔧 Configuring gRPC Retry Registry");
        
        RetryConfig config = RetryConfig.custom()
            .maxAttempts(3) // Retry up to 3 times
            .waitDuration(Duration.ofMillis(200)) // Wait 200ms between retries
            .retryOnException(e -> {
                // Retry on gRPC-specific exceptions
                if (e instanceof io.grpc.StatusRuntimeException) {
                    io.grpc.StatusRuntimeException grpcEx = (io.grpc.StatusRuntimeException) e;
                    io.grpc.Status.Code code = grpcEx.getStatus().getCode();
                    // Retry on UNAVAILABLE, DEADLINE_EXCEEDED, ABORTED
                    return code == io.grpc.Status.Code.UNAVAILABLE 
                        || code == io.grpc.Status.Code.DEADLINE_EXCEEDED
                        || code == io.grpc.Status.Code.ABORTED
                        || code == io.grpc.Status.Code.INTERNAL;
                }
                // Also retry on network exceptions
                return e instanceof java.io.IOException 
                    || e instanceof java.net.SocketTimeoutException
                    || e instanceof TimeoutException;
            })
            .ignoreExceptions(IllegalArgumentException.class) // Don't retry on bad requests
            .ignoreExceptions(io.grpc.StatusRuntimeException.class) // Will be handled by retryOnException
            .build();

        RetryRegistry registry = RetryRegistry.of(config);
        
        // Create specific retry instances for each gRPC service
        registry.retry("tenantGrpcService", config);
        registry.retry("identityGrpcService", config);
        registry.retry("userGrpcService", config);
        registry.retry("messageGrpcService", config);
        registry.retry("appGrpcService", config);
        
        log.info("✅ gRPC Retry instances registered: tenant, identity, user, message, app");
        
        return registry;
    }

    /**
     * Time limiter configuration for gRPC calls
     */
    @Bean
    public TimeLimiterConfig grpcTimeLimiterConfig() {
        log.info("🔧 Configuring gRPC Time Limiter");
        
        return TimeLimiterConfig.custom()
            .timeoutDuration(Duration.ofSeconds(10)) // Timeout after 10s for gRPC calls
            .cancelRunningFuture(true) // Cancel running future on timeout
            .build();
    }

    /**
     * Get circuit breaker for a specific gRPC service
     */
    @Bean
    public CircuitBreaker tenantGrpcCircuitBreaker(CircuitBreakerRegistry grpcCircuitBreakerRegistry) {
        return grpcCircuitBreakerRegistry.circuitBreaker("tenantGrpcService");
    }

    @Bean
    public CircuitBreaker identityGrpcCircuitBreaker(CircuitBreakerRegistry grpcCircuitBreakerRegistry) {
        return grpcCircuitBreakerRegistry.circuitBreaker("identityGrpcService");
    }

    @Bean
    public CircuitBreaker userGrpcCircuitBreaker(CircuitBreakerRegistry grpcCircuitBreakerRegistry) {
        return grpcCircuitBreakerRegistry.circuitBreaker("userGrpcService");
    }

    @Bean
    public CircuitBreaker messageGrpcCircuitBreaker(CircuitBreakerRegistry grpcCircuitBreakerRegistry) {
        return grpcCircuitBreakerRegistry.circuitBreaker("messageGrpcService");
    }

    @Bean
    public CircuitBreaker appGrpcCircuitBreaker(CircuitBreakerRegistry grpcCircuitBreakerRegistry) {
        return grpcCircuitBreakerRegistry.circuitBreaker("appGrpcService");
    }

    /**
     * Get retry for a specific gRPC service
     */
    @Bean
    public Retry tenantGrpcRetry(RetryRegistry grpcRetryRegistry) {
        return grpcRetryRegistry.retry("tenantGrpcService");
    }

    @Bean
    public Retry identityGrpcRetry(RetryRegistry grpcRetryRegistry) {
        return grpcRetryRegistry.retry("identityGrpcService");
    }

    @Bean
    public Retry userGrpcRetry(RetryRegistry grpcRetryRegistry) {
        return grpcRetryRegistry.retry("userGrpcService");
    }

    @Bean
    public Retry messageGrpcRetry(RetryRegistry grpcRetryRegistry) {
        return grpcRetryRegistry.retry("messageGrpcService");
    }

    @Bean
    public Retry appGrpcRetry(RetryRegistry grpcRetryRegistry) {
        return grpcRetryRegistry.retry("appGrpcService");
    }
}
