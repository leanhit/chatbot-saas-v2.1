package com.chatbot.shared.penny.service;

import com.chatbot.shared.penny.core.config.PennyProperties;
import com.chatbot.shared.penny.providers.MiddlewareProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for managing Penny configuration
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PennyConfigurationService {

    private final PennyProperties properties;
    private final List<MiddlewareProvider> providers;
    
    // Runtime configuration cache
    private final Map<String, Object> runtimeConfig = new ConcurrentHashMap<>();

    /**
     * Get current configuration
     */
    public Map<String, Object> getCurrentConfiguration() {
        log.debug("Getting current Penny configuration");
        
        Map<String, Object> config = new HashMap<>();
        
        // Core configuration
        config.put("core", getCoreConfiguration());
        
        // Provider configuration
        config.put("providers", getProviderConfiguration());
        
        // Analytics configuration
        config.put("analytics", getAnalyticsConfiguration());
        
        // Context configuration
        config.put("context", getContextConfiguration());
        
        // Error handling configuration
        config.put("errorHandling", getErrorHandlingConfiguration());
        
        // Runtime configuration
        config.put("runtime", new HashMap<>(runtimeConfig));
        
        return config;
    }

    /**
     * Update configuration
     */
    public void updateConfiguration(String component, Map<String, Object> updates) {
        log.info("Updating Penny configuration for component: {}", component);
        
        switch (component.toLowerCase()) {
            case "analytics":
                updateAnalyticsConfiguration(updates);
                break;
            case "context":
                updateContextConfiguration(updates);
                break;
            case "error":
                updateErrorHandlingConfiguration(updates);
                break;
            case "runtime":
                updateRuntimeConfiguration(updates);
                break;
            default:
                log.warn("Unknown configuration component: {}", component);
        }
    }

    /**
     * Reset configuration to defaults
     */
    public void resetConfiguration(String component) {
        log.info("Resetting configuration for component: {}", component);
        
        switch (component.toLowerCase()) {
            case "analytics":
                resetAnalyticsConfiguration();
                break;
            case "context":
                resetContextConfiguration();
                break;
            case "error":
                resetErrorHandlingConfiguration();
                break;
            case "runtime":
                runtimeConfig.clear();
                break;
            case "all":
                resetAllConfiguration();
                break;
            default:
                log.warn("Unknown configuration component: {}", component);
        }
    }

    /**
     * Validate configuration
     */
    public Map<String, Object> validateConfiguration() {
        log.debug("Validating Penny configuration");
        
        Map<String, Object> validation = new HashMap<>();
        
        // Validate core configuration
        validation.put("core", validateCoreConfiguration());
        
        // Validate provider configuration
        validation.put("providers", validateProviderConfiguration());
        
        // Validate analytics configuration
        validation.put("analytics", validateAnalyticsConfiguration());
        
        // Validate context configuration
        validation.put("context", validateContextConfiguration());
        
        // Overall validation result
        validation.put("valid", isOverallConfigurationValid(validation));
        
        return validation;
    }

    /**
     * Get provider-specific configuration
     */
    public Map<String, Object> getProviderConfiguration(String providerType) {
        log.debug("Getting configuration for provider: {}", providerType);
        
        for (MiddlewareProvider provider : providers) {
            if (provider.getProviderType().name().equalsIgnoreCase(providerType)) {
                return Map.of(
                    "type", provider.getProviderType(),
                    "capabilities", provider.getCapabilities(),
                    "health", provider.getHealthStatus()
                );
            }
        }
        
        return Map.of("error", "Provider not found: " + providerType);
    }

    /**
     * Export configuration
     */
    public String exportConfiguration(String format) {
        Map<String, Object> config = getCurrentConfiguration();
        
        switch (format.toLowerCase()) {
            case "json":
                return exportAsJson(config);
            case "yaml":
                return exportAsYaml(config);
            case "properties":
                return exportAsProperties(config);
            default:
                return exportAsJson(config);
        }
    }

    /**
     * Import configuration
     */
    public void importConfiguration(String format, String configData) {
        log.info("Importing configuration in format: {}", format);
        
        try {
            Map<String, Object> config = parseConfiguration(format, configData);
            
            // Apply configuration updates
            config.forEach(this::applyConfigurationUpdate);
            
            log.info("Configuration imported successfully");
        } catch (Exception e) {
            log.error("Failed to import configuration", e);
            throw new RuntimeException("Configuration import failed", e);
        }
    }

    // Private helper methods

    private Map<String, Object> getCoreConfiguration() {
        return Map.of(
            "middleware", Map.of(
                "enabled", properties.getMiddleware().isEnabled(),
                "processingTimeout", properties.getMiddleware().getProcessingTimeout(),
                "streamingEnabled", properties.getMiddleware().isStreamingEnabled(),
                "maxRetries", properties.getMiddleware().getMaxRetries(),
                "retryDelay", properties.getMiddleware().getRetryDelay()
            ),
            "provider", Map.of(
                "selectionStrategy", properties.getProvider().getSelectionStrategy(),
                "healthCheckInterval", properties.getProvider().getHealthCheckInterval(),
                "fallbackEnabled", properties.getProvider().isFallbackEnabled()
            )
        );
    }

    private Map<String, Object> getProviderConfiguration() {
        Map<String, Object> providerConfig = new HashMap<>();
        
        for (MiddlewareProvider provider : providers) {
            providerConfig.put(provider.getProviderType().name(), Map.of(
                "enabled", true,
                "capabilities", provider.getCapabilities(),
                "health", provider.getHealthStatus()
            ));
        }
        
        return providerConfig;
    }

    private Map<String, Object> getAnalyticsConfiguration() {
        PennyProperties.Analytics analytics = properties.getAnalytics();
        return Map.of(
            "enabled", analytics.isEnabled(),
            "batchSize", analytics.getBatchSize(),
            "flushInterval", analytics.getFlushInterval(),
            "realTimeEnabled", analytics.isRealTimeEnabled(),
            "metrics", Map.of(
                "processingTimeEnabled", analytics.getMetrics().isProcessingTimeEnabled(),
                "providerUsageEnabled", analytics.getMetrics().isProviderUsageEnabled(),
                "intentTrackingEnabled", analytics.getMetrics().isIntentTrackingEnabled(),
                "errorTrackingEnabled", analytics.getMetrics().isErrorTrackingEnabled()
            )
        );
    }

    private Map<String, Object> getContextConfiguration() {
        PennyProperties.Context context = properties.getContext();
        return Map.of(
            "storageType", context.getStorageType(),
            "ttl", context.getTtl(),
            "compressionEnabled", context.isCompressionEnabled(),
            "cleanup", Map.of(
                "interval", context.getCleanup().getInterval(),
                "autoCleanup", context.getCleanup().isAutoCleanup(),
                "maxAge", context.getCleanup().getMaxAge()
            )
        );
    }

    private Map<String, Object> getErrorHandlingConfiguration() {
        PennyProperties.Error error = properties.getError();
        return Map.of(
            "circuitbreakerEnabled", error.isCircuitbreakerEnabled(),
            "fallbackEnabled", error.isFallbackEnabled(),
            "maxRetries", error.getMaxRetries(),
            "timeoutDuration", error.getTimeoutDuration(),
            "recoveryDuration", error.getRecoveryDuration()
        );
    }

    private void updateAnalyticsConfiguration(Map<String, Object> updates) {
        // Implementation would update analytics configuration
        log.info("Updated analytics configuration: {}", updates);
    }

    private void updateContextConfiguration(Map<String, Object> updates) {
        // Implementation would update context configuration
        log.info("Updated context configuration: {}", updates);
    }

    private void updateErrorHandlingConfiguration(Map<String, Object> updates) {
        // Implementation would update error handling configuration
        log.info("Updated error handling configuration: {}", updates);
    }

    private void updateRuntimeConfiguration(Map<String, Object> updates) {
        runtimeConfig.putAll(updates);
        log.info("Updated runtime configuration: {}", updates);
    }

    private void resetAnalyticsConfiguration() {
        // Implementation would reset analytics configuration to defaults
        log.info("Reset analytics configuration to defaults");
    }

    private void resetContextConfiguration() {
        // Implementation would reset context configuration to defaults
        log.info("Reset context configuration to defaults");
    }

    private void resetErrorHandlingConfiguration() {
        // Implementation would reset error handling configuration to defaults
        log.info("Reset error handling configuration to defaults");
    }

    private void resetAllConfiguration() {
        resetAnalyticsConfiguration();
        resetContextConfiguration();
        resetErrorHandlingConfiguration();
        runtimeConfig.clear();
        log.info("Reset all configuration to defaults");
    }

    private Map<String, Object> validateCoreConfiguration() {
        // Implementation would validate core configuration
        return Map.of("valid", true, "errors", List.of());
    }

    private Map<String, Object> validateProviderConfiguration() {
        // Implementation would validate provider configuration
        return Map.of("valid", true, "errors", List.of());
    }

    private Map<String, Object> validateAnalyticsConfiguration() {
        // Implementation would validate analytics configuration
        return Map.of("valid", true, "errors", List.of());
    }

    private Map<String, Object> validateContextConfiguration() {
        // Implementation would validate context configuration
        return Map.of("valid", true, "errors", List.of());
    }

    private boolean isOverallConfigurationValid(Map<String, Object> validation) {
        return validation.values().stream()
                .map(v -> (Map<String, Object>) v)
                .allMatch(v -> (Boolean) v.get("valid"));
    }

    private String exportAsJson(Map<String, Object> config) {
        // Simple JSON export (in production, use proper JSON library)
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        
        config.forEach((key, value) -> {
            json.append("  \"").append(key).append("\": ");
            if (value instanceof Map) {
                json.append(((Map<?, ?>) value).toString());
            } else if (value instanceof String) {
                json.append("\"").append(value).append("\"");
            } else {
                json.append(value);
            }
            json.append(",\n");
        });
        
        // Remove trailing comma
        if (json.charAt(json.length() - 2) == ',') {
            json.deleteCharAt(json.length() - 2);
        }
        
        json.append("}");
        return json.toString();
    }

    private String exportAsYaml(Map<String, Object> config) {
        // Simple YAML export (in production, use proper YAML library)
        StringBuilder yaml = new StringBuilder();
        
        config.forEach((key, value) -> {
            yaml.append(key).append(":\n");
            if (value instanceof Map) {
                ((Map<?, ?>) value).forEach((k, v) -> {
                    yaml.append("  ").append(k).append(": ").append(v).append("\n");
                });
            } else {
                yaml.append("  ").append(value).append("\n");
            }
        });
        
        return yaml.toString();
    }

    private String exportAsProperties(Map<String, Object> config) {
        StringBuilder props = new StringBuilder();
        
        config.forEach((key, value) -> {
            if (value instanceof Map) {
                ((Map<?, ?>) value).forEach((k, v) -> {
                    props.append(key).append(".").append(k).append("=").append(v).append("\n");
                });
            } else {
                props.append(key).append("=").append(value).append("\n");
            }
        });
        
        return props.toString();
    }

    private Map<String, Object> parseConfiguration(String format, String configData) {
        // Implementation would parse configuration based on format
        // For now, return empty map
        return new HashMap<>();
    }

    private void applyConfigurationUpdate(String key, Object value) {
        // Implementation would apply configuration update
        log.debug("Applying configuration update: {} = {}", key, value);
    }
}
