package com.chatbot.modules.penny.routing.dto;

import com.chatbot.modules.facebook.webhook.service.ChatbotProviderService;
import com.chatbot.modules.penny.routing.ProviderSelector;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Provider Selection - Kết quả lựa chọn provider
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProviderSelection {
    
    /**
     * Selected provider type
     */
    private ProviderSelector.ProviderType providerType;
    
    /**
     * Provider instance
     */
    private ChatbotProviderService provider;
    
    /**
     * Reason for selection
     */
    private String selectionReason;
    
    /**
     * Confidence in selection (0.0 - 1.0)
     */
    private Double confidence;
    
    /**
     * Fallback providers in case primary fails
     */
    private List<ProviderSelector.ProviderType> fallbackProviders;
    
    /**
     * Estimated processing time
     */
    private Long estimatedProcessingTime;
    
    /**
     * Provider capabilities
     */
    private ProviderCapabilities capabilities;
    
    /**
     * Load information
     */
    private ProviderLoad load;
    
    /**
     * Health status
     */
    private ProviderSelector.ProviderHealth health;
    
    /**
     * Cost information
     */
    private ProviderCost cost;
    
    /**
     * Additional metadata
     */
    private java.util.Map<String, Object> metadata;
    
    // Helper methods
    
    /**
     * Check if selection has high confidence
     */
    public boolean hasHighConfidence() {
        return confidence != null && confidence >= 0.8;
    }
    
    /**
     * Check if selection has fallback providers
     */
    public boolean hasFallbackProviders() {
        return fallbackProviders != null && !fallbackProviders.isEmpty();
    }
    
    /**
     * Check if provider is healthy
     */
    public boolean isHealthy() {
        return health != null && health.isHealthyStatus();
    }
    
    /**
     * Check if provider is available
     */
    public boolean isAvailable() {
        return provider != null && isHealthy();
    }
    
    /**
     * Get first fallback provider
     */
    public ProviderSelector.ProviderType getFirstFallback() {
        return hasFallbackProviders() ? fallbackProviders.get(0) : null;
    }
    
    /**
     * Check if provider supports specific feature
     */
    public boolean supportsFeature(String feature) {
        return capabilities != null && capabilities.supportsFeature(feature);
    }
    
    /**
     * Check if provider is under high load
     */
    public boolean isHighLoad() {
        return load != null && load.getLoadPercentage() > 80.0;
    }
    
    /**
     * Check if provider is cost-effective
     */
    public boolean isCostEffective() {
        return cost != null && cost.getCostPerRequest() < 0.01;
    }
    
    /**
     * Add metadata
     */
    public void addMetadata(String key, Object value) {
        if (metadata == null) {
            metadata = new java.util.HashMap<>();
        }
        metadata.put(key, value);
    }
    
    /**
     * Get metadata value
     */
    @SuppressWarnings("unchecked")
    public <T> T getMetadata(String key, Class<T> type) {
        if (metadata == null || !metadata.containsKey(key)) {
            return null;
        }
        Object value = metadata.get(key);
        if (type.isInstance(value)) {
            return (T) value;
        }
        return null;
    }
    
    @Override
    public String toString() {
        return "ProviderSelection{" +
                "providerType=" + providerType +
                ", selectionReason='" + selectionReason + '\'' +
                ", confidence=" + confidence +
                ", hasFallback=" + hasFallbackProviders() +
                ", healthy=" + isHealthy() +
                '}';
    }
    
    // Inner classes
    
    @lombok.Data
    @lombok.Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProviderCapabilities {
        private List<String> supportedIntents;
        private List<String> supportedLanguages;
        private List<String> supportedMessageTypes;
        private boolean supportsStreaming;
        private boolean supportsContext;
        private boolean supportsAnalytics;
        private int maxMessageLength;
        private double maxProcessingTime;
        
        public boolean supportsFeature(String feature) {
            if (supportedIntents != null && supportedIntents.contains(feature)) {
                return true;
            }
            if (supportedLanguages != null && supportedLanguages.contains(feature)) {
                return true;
            }
            if (supportedMessageTypes != null && supportedMessageTypes.contains(feature)) {
                return true;
            }
            return false;
        }
    }
    
    @lombok.Data
    @lombok.Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProviderLoad {
        private int currentRequests;
        private int maxCapacity;
        private double loadPercentage;
        private long averageResponseTime;
        private int requestsPerSecond;
        private long lastUpdated;
        
        public boolean isOverloaded() {
            return loadPercentage > 90.0;
        }
        
        public boolean hasCapacity() {
            return currentRequests < maxCapacity;
        }
    }
    
    @lombok.Data
    @lombok.Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProviderCost {
        private double costPerRequest;
        private String currency;
        private double monthlyCost;
        private boolean hasFreeTier;
        private int freeTierRequests;
        private double costAfterFreeTier;
        
        public boolean isFreeForRequest(int requestCount) {
            return hasFreeTier && requestCount <= freeTierRequests;
        }
        
        public double calculateCost(int requestCount) {
            if (isFreeForRequest(requestCount)) {
                return 0.0;
            }
            int billableRequests = hasFreeTier ? Math.max(0, requestCount - freeTierRequests) : requestCount;
            return billableRequests * costPerRequest;
        }
    }
}
