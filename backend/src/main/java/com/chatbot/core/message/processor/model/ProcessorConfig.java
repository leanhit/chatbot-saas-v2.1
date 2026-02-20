package com.chatbot.core.message.processor.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.HashMap;

/**
 * Processor configuration model for message processing
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcessorConfig {
    
    private Long id;
    private String name;
    private String description;
    private Boolean isActive;
    private Integer priority;
    private Integer timeoutMs;
    private Integer maxRetries;
    private Map<String, Object> settings;
    
    // Configuration types
    public enum ConfigType {
        VALIDATION,        // Validation configuration
        TRANSFORMATION,     // Transformation configuration
        ROUTING,           // Routing configuration
        FILTERING,         // Filtering configuration
        ENRICHMENT,        // Enrichment configuration
        ANALYSIS,          // Analysis configuration
        PROCESSING,        // Processing configuration
        DELIVERY           // Delivery configuration
    }
    
    private ConfigType configType;
    
    // Default configurations
    public static ProcessorConfig getDefaultValidationConfig() {
        return ProcessorConfig.builder()
                .name("default-validation")
                .description("Default message validation configuration")
                .configType(ConfigType.VALIDATION)
                .isActive(true)
                .priority(1)
                .timeoutMs(5000)
                .maxRetries(3)
                .settings(createDefaultValidationSettings())
                .build();
    }
    
    public static ProcessorConfig getDefaultTransformationConfig() {
        return ProcessorConfig.builder()
                .name("default-transformation")
                .description("Default message transformation configuration")
                .configType(ConfigType.TRANSFORMATION)
                .isActive(true)
                .priority(2)
                .timeoutMs(3000)
                .maxRetries(2)
                .settings(createDefaultTransformationSettings())
                .build();
    }
    
    public static ProcessorConfig getDefaultRoutingConfig() {
        return ProcessorConfig.builder()
                .name("default-routing")
                .description("Default message routing configuration")
                .configType(ConfigType.ROUTING)
                .isActive(true)
                .priority(3)
                .timeoutMs(2000)
                .maxRetries(1)
                .settings(createDefaultRoutingSettings())
                .build();
    }
    
    // Default settings creators
    private static Map<String, Object> createDefaultValidationSettings() {
        Map<String, Object> settings = new HashMap<>();
        settings.put("maxLength", 1000);
        settings.put("minLength", 1);
        settings.put("allowHtml", false);
        settings.put("allowUrls", true);
        settings.put("allowEmails", true);
        settings.put("checkProfanity", true);
        settings.put("checkSpam", true);
        settings.put("checkXss", true);
        settings.put("checkSqlInjection", true);
        return settings;
    }
    
    private static Map<String, Object> createDefaultTransformationSettings() {
        Map<String, Object> settings = new HashMap<>();
        settings.put("normalizeWhitespace", true);
        settings.put("normalizeCase", true);
        settings.put("removeHtml", true);
        settings.put("removeSpecialChars", false);
        settings.put("extractEntities", true);
        settings.put("applyFilters", true);
        settings.put("formatUrls", true);
        settings.put("formatEmails", true);
        return settings;
    }
    
    private static Map<String, Object> createDefaultRoutingSettings() {
        Map<String, Object> settings = new HashMap<>();
        settings.put("defaultHub", "message");
        settings.put("fallbackHub", "message");
        settings.put("enableLoadBalancing", false);
        settings.put("enableCircuitBreaker", true);
        settings.put("timeoutMs", 2000);
        settings.put("retryAttempts", 1);
        settings.put("enableMetrics", true);
        return settings;
    }
    
    // Utility methods
    public Object getSetting(String key) {
        return settings != null ? settings.get(key) : null;
    }
    
    public Object getSetting(String key, Object defaultValue) {
        return settings != null && settings.containsKey(key) ? settings.get(key) : defaultValue;
    }
    
    public void setSetting(String key, Object value) {
        if (settings == null) {
            settings = new HashMap<>();
        }
        settings.put(key, value);
    }
    
    public boolean isSettingEnabled(String key) {
        Object value = getSetting(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof String) {
            return "true".equalsIgnoreCase((String) value);
        }
        return false;
    }
    
    public int getSettingAsInt(String key, int defaultValue) {
        Object value = getSetting(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }
    
    public long getSettingAsLong(String key, long defaultValue) {
        Object value = getSetting(key);
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        if (value instanceof String) {
            try {
                return Long.parseLong((String) value);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }
}
