package com.chatbot.shared.penny.resilience;

import com.chatbot.core.tenant.infra.TenantContext;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.retry.RetryConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * TenantResilienceConfigService - Dynamic per-tenant resilience configuration
 * 
 * Provides tenant-specific circuit breaker and retry configurations.
 * Allows different tenants to have customized resilience settings based on their package tier.
 * 
 * Default configurations are used for tenants without custom settings.
 */
@Service
@Slf4j
public class TenantResilienceConfigService {

    // Cache for tenant-specific configurations
    private final Map<Long, TenantResilienceConfig> tenantConfigCache = new ConcurrentHashMap<>();
    
    // Default configuration for all tenants
    private static final TenantResilienceConfig DEFAULT_CONFIG = new TenantResilienceConfig(
        50, // failureRateThreshold
        Duration.ofSeconds(30), // waitDurationInOpenState
        3, // permittedNumberOfCallsInHalfOpenState
        10, // slidingWindowSize
        3, // maxAttempts
        Duration.ofMillis(500), // waitDuration
        Duration.ofSeconds(5) // slowCallDurationThreshold
    );
    
    // Premium tier configuration (more lenient for high-volume tenants)
    private static final TenantResilienceConfig PREMIUM_CONFIG = new TenantResilienceConfig(
        60, // failureRateThreshold (higher threshold for premium)
        Duration.ofSeconds(20), // waitDurationInOpenState (shorter wait)
        5, // permittedNumberOfCallsInHalfOpenState (more attempts)
        20, // slidingWindowSize (larger window)
        5, // maxAttempts (more retries)
        Duration.ofMillis(300), // waitDuration (faster retry)
        Duration.ofSeconds(8) // slowCallDurationThreshold (more tolerant)
    );
    
    /**
     * Get circuit breaker configuration for current tenant
     */
    public CircuitBreakerConfig getCircuitBreakerConfig(String instanceName) {
        Long tenantId = TenantContext.getTenantId();
        TenantResilienceConfig config = getTenantConfig(tenantId);
        
        log.debug("Using circuit breaker config for tenant {} (instance: {}): failureRate={}, waitDuration={}",
            tenantId, instanceName, config.failureRateThreshold, config.waitDurationInOpenState);
        
        return CircuitBreakerConfig.custom()
            .failureRateThreshold(config.failureRateThreshold)
            .waitDurationInOpenState(config.waitDurationInOpenState)
            .permittedNumberOfCallsInHalfOpenState(config.permittedNumberOfCallsInHalfOpenState)
            .slidingWindowSize(config.slidingWindowSize)
            .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
            .slowCallRateThreshold(80)
            .slowCallDurationThreshold(config.slowCallDurationThreshold)
            .build();
    }
    
    /**
     * Get retry configuration for current tenant
     */
    public RetryConfig getRetryConfig(String instanceName) {
        Long tenantId = TenantContext.getTenantId();
        TenantResilienceConfig config = getTenantConfig(tenantId);
        
        log.debug("Using retry config for tenant {} (instance: {}): maxAttempts={}, waitDuration={}",
            tenantId, instanceName, config.maxAttempts, config.waitDuration);
        
        return RetryConfig.custom()
            .maxAttempts(config.maxAttempts)
            .waitDuration(config.waitDuration)
            .intervalFunction(io.github.resilience4j.core.IntervalFunction.ofExponentialBackoff(
                config.waitDuration.toMillis(), 2, 10000))
            .retryOnException(e -> {
                return e instanceof java.io.IOException 
                    || e instanceof java.net.SocketTimeoutException
                    || e instanceof java.util.concurrent.TimeoutException;
            })
            .retryOnResult(result -> {
                // Retry on HTTP 429 (rate limiting)
                if (result instanceof org.springframework.http.ResponseEntity) {
                    org.springframework.http.ResponseEntity<?> response = (org.springframework.http.ResponseEntity<?>) result;
                    return org.springframework.http.HttpStatus.TOO_MANY_REQUESTS.equals(response.getStatusCode());
                }
                return false;
            })
            .ignoreExceptions(IllegalArgumentException.class)
            .build();
    }
    
    /**
     * Get tenant-specific configuration or default
     */
    private TenantResilienceConfig getTenantConfig(Long tenantId) {
        if (tenantId == null) {
            return DEFAULT_CONFIG;
        }
        
        // Check cache first
        return tenantConfigCache.computeIfAbsent(tenantId, id -> {
            // In a real implementation, this would query the database or configuration service
            // to determine the tenant's package tier and return appropriate config
            // For now, we use a simple hash-based selection for demo purposes
            if (id % 10 == 0) { // 10% of tenants get premium config
                log.info("Tenant {} assigned PREMIUM resilience configuration", id);
                return PREMIUM_CONFIG;
            }
            return DEFAULT_CONFIG;
        });
    }
    
    /**
     * Set custom configuration for a specific tenant
     * This can be called by an admin endpoint or configuration service
     */
    public void setTenantConfig(Long tenantId, TenantResilienceConfig config) {
        log.info("Setting custom resilience configuration for tenant {}", tenantId);
        tenantConfigCache.put(tenantId, config);
    }
    
    /**
     * Reset tenant configuration to default
     */
    public void resetTenantConfig(Long tenantId) {
        log.info("Resetting resilience configuration to default for tenant {}", tenantId);
        tenantConfigCache.remove(tenantId);
    }
    
    /**
     * Clear all cached configurations (useful for configuration refresh)
     */
    public void clearCache() {
        log.info("Clearing all tenant resilience configuration cache");
        tenantConfigCache.clear();
    }
    
    /**
     * Inner class to hold tenant-specific resilience configuration
     */
    public static class TenantResilienceConfig {
        private final int failureRateThreshold;
        private final Duration waitDurationInOpenState;
        private final int permittedNumberOfCallsInHalfOpenState;
        private final int slidingWindowSize;
        private final int maxAttempts;
        private final Duration waitDuration;
        private final Duration slowCallDurationThreshold;
        
        public TenantResilienceConfig(int failureRateThreshold, Duration waitDurationInOpenState,
                                     int permittedNumberOfCallsInHalfOpenState, int slidingWindowSize,
                                     int maxAttempts, Duration waitDuration, Duration slowCallDurationThreshold) {
            this.failureRateThreshold = failureRateThreshold;
            this.waitDurationInOpenState = waitDurationInOpenState;
            this.permittedNumberOfCallsInHalfOpenState = permittedNumberOfCallsInHalfOpenState;
            this.slidingWindowSize = slidingWindowSize;
            this.maxAttempts = maxAttempts;
            this.waitDuration = waitDuration;
            this.slowCallDurationThreshold = slowCallDurationThreshold;
        }
        
        // Getters
        public int getFailureRateThreshold() { return failureRateThreshold; }
        public Duration getWaitDurationInOpenState() { return waitDurationInOpenState; }
        public int getPermittedNumberOfCallsInHalfOpenState() { return permittedNumberOfCallsInHalfOpenState; }
        public int getSlidingWindowSize() { return slidingWindowSize; }
        public int getMaxAttempts() { return maxAttempts; }
        public Duration getWaitDuration() { return waitDuration; }
        public Duration getSlowCallDurationThreshold() { return slowCallDurationThreshold; }
    }
}
