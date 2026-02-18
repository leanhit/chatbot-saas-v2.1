package com.chatbot.modules.penny.service;

import com.chatbot.modules.penny.core.PennyMiddlewareEngine;
import com.chatbot.modules.penny.providers.MiddlewareProvider;
import com.chatbot.modules.penny.context.ContextManager;
import com.chatbot.modules.penny.analytics.AnalyticsCollector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Service for monitoring Penny system health
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PennyHealthService {

    private final PennyMiddlewareEngine middlewareEngine;
    private final ContextManager contextManager;
    private final AnalyticsCollector analyticsCollector;
    private final List<MiddlewareProvider> providers;

    /**
     * Get overall system health
     */
    public Map<String, Object> getSystemHealth() {
        log.debug("Checking Penny system health");
        
        Map<String, Object> health = new HashMap<>();
        
        // Engine health
        health.put("engine", getEngineHealth());
        
        // Context manager health
        health.put("contextManager", getContextManagerHealth());
        
        // Analytics health
        health.put("analytics", getAnalyticsHealth());
        
        // Providers health
        health.put("providers", getProvidersHealth());
        
        // Overall status
        health.put("overall", calculateOverallHealth(health));
        health.put("timestamp", Instant.now());
        
        return health;
    }

    /**
     * Get detailed component health
     */
    public Map<String, Object> getDetailedHealth() {
        log.debug("Getting detailed Penny health check");
        
        Map<String, Object> health = new HashMap<>();
        
        // Engine metrics
        health.put("engineMetrics", middlewareEngine.getEngineMetrics());
        
        // Context storage health
        health.put("contextStorage", getContextStorageHealth());
        
        // Provider-specific health
        health.put("providerDetails", getProviderDetails());
        
        // Performance metrics
        health.put("performance", getPerformanceHealth());
        
        // Resource usage
        health.put("resources", getResourceUsage());
        
        return health;
    }

    /**
     * Perform async health check
     */
    public CompletableFuture<Map<String, Object>> getSystemHealthAsync() {
        return CompletableFuture.supplyAsync(this::getSystemHealth);
    }

    /**
     * Check if system is healthy
     */
    public boolean isSystemHealthy() {
        try {
            Map<String, Object> health = getSystemHealth();
            String overallStatus = (String) health.get("overall");
            return "HEALTHY".equals(overallStatus) || "DEGRADED".equals(overallStatus);
        } catch (Exception e) {
            log.error("Health check failed", e);
            return false;
        }
    }

    /**
     * Get health status for specific component
     */
    public Map<String, Object> getComponentHealth(String component) {
        log.debug("Getting health for component: {}", component);
        
        switch (component.toLowerCase()) {
            case "engine":
                return getEngineHealth();
            case "context":
                return getContextManagerHealth();
            case "analytics":
                return getAnalyticsHealth();
            case "providers":
                return getProvidersHealth();
            default:
                return Map.of("status", "UNKNOWN", "message", "Unknown component: " + component);
        }
    }

    // Private helper methods

    private Map<String, Object> getEngineHealth() {
        try {
            PennyMiddlewareEngine.EngineHealthStatus engineHealth = middlewareEngine.getHealthStatus();
            
            return Map.of(
                "status", engineHealth.getStatus(),
                "timestamp", engineHealth.getTimestamp(),
                "components", engineHealth.getComponents()
            );
        } catch (Exception e) {
            log.error("Failed to get engine health", e);
            return Map.of("status", "ERROR", "error", e.getMessage());
        }
    }

    private Map<String, Object> getContextManagerHealth() {
        try {
            // Check context manager health - using placeholder values
            return Map.of(
                "status", "HEALTHY",
                "activeContexts", 50, // Placeholder
                "totalContexts", 100, // Placeholder
                "memoryUsage", "256MB", // Placeholder
                "lastCleanup", System.currentTimeMillis() - 3600000 // Placeholder
            );
        } catch (Exception e) {
            log.error("Failed to get context manager health", e);
            return Map.of("status", "ERROR", "error", e.getMessage());
        }
    }

    private Map<String, Object> getAnalyticsHealth() {
        try {
            AnalyticsCollector.EngineMetrics metrics = analyticsCollector.getEngineMetrics();
            
            return Map.of(
                "status", "HEALTHY",
                "totalEvents", metrics.getTotalProcessed(),
                "errorRate", metrics.getErrorRate(),
                "averageProcessingTime", metrics.getAverageProcessingTime(),
                "lastEvent", metrics.getTimestamp()
            );
        } catch (Exception e) {
            log.error("Failed to get analytics health", e);
            return Map.of("status", "ERROR", "error", e.getMessage());
        }
    }

    private Map<String, Object> getProvidersHealth() {
        Map<String, Object> providersHealth = new HashMap<>();
        
        for (MiddlewareProvider provider : providers) {
            try {
                MiddlewareProvider.HealthStatus health = provider.getHealthStatus();
                providersHealth.put(provider.getProviderType().name(), Map.of(
                    "status", health.isHealthy() ? "HEALTHY" : "UNHEALTHY",
                    "message", health.getMessage(),
                    "lastCheck", health.getLastCheck()
                ));
            } catch (Exception e) {
                providersHealth.put(provider.getProviderType().name(), Map.of(
                    "status", "ERROR",
                    "error", e.getMessage()
                ));
            }
        }
        
        return providersHealth;
    }

    private Map<String, Object> getContextStorageHealth() {
        try {
            return Map.of(
                "status", "HEALTHY",
                "storageType", "redis", // Placeholder
                "connectionPool", "healthy", // Placeholder
                "cacheHitRate", 85.5 // Placeholder
            );
        } catch (Exception e) {
            log.error("Failed to get context storage health", e);
            return Map.of("status", "ERROR", "error", e.getMessage());
        }
    }

    private Map<String, Object> getProviderDetails() {
        Map<String, Object> details = new HashMap<>();
        
        for (MiddlewareProvider provider : providers) {
            try {
                details.put(provider.getProviderType().name(), Map.of(
                    "capabilities", provider.getCapabilities(),
                    "health", provider.getHealthStatus()
                ));
            } catch (Exception e) {
                details.put(provider.getProviderType().name(), Map.of(
                    "error", e.getMessage()
                ));
            }
        }
        
        return details;
    }

    private Map<String, Object> getPerformanceHealth() {
        Runtime runtime = Runtime.getRuntime();
        
        return Map.of(
            "memory", Map.of(
                "total", runtime.totalMemory(),
                "free", runtime.freeMemory(),
                "used", runtime.totalMemory() - runtime.freeMemory(),
                "max", runtime.maxMemory()
            ),
            "threads", Thread.activeCount(),
            "processors", runtime.availableProcessors()
        );
    }

    private Map<String, Object> getResourceUsage() {
        return Map.of(
            "cpu", getCpuUsage(),
            "memory", getMemoryUsagePercentage(),
            "disk", getDiskUsage(),
            "network", getNetworkStatus()
        );
    }

    private String calculateOverallHealth(Map<String, Object> health) {
        int healthyComponents = 0;
        int totalComponents = 0;
        
        // Check engine
        Map<String, Object> engine = (Map<String, Object>) health.get("engine");
        if ("HEALTHY".equals(engine.get("status"))) {
            healthyComponents++;
        }
        totalComponents++;
        
        // Check context manager
        Map<String, Object> context = (Map<String, Object>) health.get("contextManager");
        if ("HEALTHY".equals(context.get("status"))) {
            healthyComponents++;
        }
        totalComponents++;
        
        // Check analytics
        Map<String, Object> analytics = (Map<String, Object>) health.get("analytics");
        if ("HEALTHY".equals(analytics.get("status"))) {
            healthyComponents++;
        }
        totalComponents++;
        
        // Check providers
        Map<String, Object> providers = (Map<String, Object>) health.get("providers");
        long healthyProviders = providers.values().stream()
                .map(p -> (Map<String, Object>) p)
                .filter(p -> "HEALTHY".equals(p.get("status")))
                .count();
        
        if (providers.size() > 0) {
            healthyComponents += healthyProviders;
            totalComponents += providers.size();
        }
        
        double healthPercentage = (double) healthyComponents / totalComponents * 100;
        
        if (healthPercentage >= 90) {
            return "HEALTHY";
        } else if (healthPercentage >= 70) {
            return "DEGRADED";
        } else {
            return "UNHEALTHY";
        }
    }

    private double calculateErrorRate() {
        AnalyticsCollector.EngineMetrics metrics = analyticsCollector.getEngineMetrics();
        return metrics.getErrorRate();
    }

    private double getCpuUsage() {
        // Implementation would use system monitoring library
        return 45.5; // Placeholder
    }

    private double getMemoryUsagePercentage() {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        
        return (double) usedMemory / totalMemory * 100;
    }

    private Map<String, Object> getDiskUsage() {
        // Implementation would check disk space
        return Map.of(
            "total", 1000000000L,
            "used", 500000000L,
            "free", 500000000L,
            "percentage", 50.0
        );
    }

    private Map<String, Object> getNetworkStatus() {
        // Implementation would check network connectivity
        return Map.of(
            "status", "CONNECTED",
            "latency", 25.5,
            "bandwidth", 1000.0
        );
    }
}
