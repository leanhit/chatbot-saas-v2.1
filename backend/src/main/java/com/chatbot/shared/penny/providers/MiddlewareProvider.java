package com.chatbot.shared.penny.providers;

import com.chatbot.shared.penny.context.ConversationContext;
import com.chatbot.shared.penny.dto.request.MiddlewareRequest;

import java.util.Map;

/**
 * Interface for all middleware providers
 */
public interface MiddlewareProvider {
    
    /**
     * Process message with conversation context
     */
    ProviderResponse processWithContext(ConversationContext context, MiddlewareRequest request);
    
    /**
     * Get health status of provider
     */
    HealthStatus getHealthStatus();
    
    /**
     * Get provider type
     */
    ProviderType getProviderType();
    
    /**
     * Check if provider supports specific feature
     */
    boolean supportsFeature(Feature feature);
    
    /**
     * Get provider capabilities
     */
    ProviderCapabilities getCapabilities();
    
    // Inner classes
    
    enum ProviderType {
        BOTPRESS, RASA, GPT, WRAPPED
    }
    
    enum Feature {
        INTENT_DETECTION,
        ENTITY_EXTRACTION,
        CONTEXT_MANAGEMENT,
        MULTILINGUAL,
        STREAMING,
        ANALYTICS,
        CUSTOM_STORIES,
        RULE_BASED_RESPONSES
    }
    
    class ProviderResponse {
        private ProviderType providerType;
        private Map<String, Object> response;
        private boolean success;
        private String errorMessage;
        private long processingTime;
        private Map<String, Object> metadata;
        
        // Getters and setters
        public ProviderType getProviderType() { return providerType; }
        public void setProviderType(ProviderType providerType) { this.providerType = providerType; }
        
        public Map<String, Object> getResponse() { return response; }
        public void setResponse(Map<String, Object> response) { this.response = response; }
        
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
        
        public long getProcessingTime() { return processingTime; }
        public void setProcessingTime(long processingTime) { this.processingTime = processingTime; }
        
        public Map<String, Object> getMetadata() { return metadata; }
        public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
    }
    
    class HealthStatus {
        private boolean healthy;
        private String message;
        private long lastCheck;
        private Map<String, Boolean> providerHealth;
        
        // Getters and setters
        public boolean isHealthy() { return healthy; }
        public void setHealthy(boolean healthy) { this.healthy = healthy; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public long getLastCheck() { return lastCheck; }
        public void setLastCheck(long lastCheck) { this.lastCheck = lastCheck; }
        
        public Map<String, Boolean> getProviderHealth() { return providerHealth; }
        public void setProviderHealth(Map<String, Boolean> providerHealth) { this.providerHealth = providerHealth; }
    }
    
    class ProviderCapabilities {
        private java.util.List<String> supportedIntents;
        private java.util.List<String> supportedLanguages;
        private java.util.List<String> supportedMessageTypes;
        private boolean supportsStreaming;
        private boolean supportsContext;
        private boolean supportsAnalytics;
        private int maxMessageLength;
        private long maxProcessingTime;
        
        // Getters and setters
        public java.util.List<String> getSupportedIntents() { return supportedIntents; }
        public void setSupportedIntents(java.util.List<String> supportedIntents) { this.supportedIntents = supportedIntents; }
        
        public java.util.List<String> getSupportedLanguages() { return supportedLanguages; }
        public void setSupportedLanguages(java.util.List<String> supportedLanguages) { this.supportedLanguages = supportedLanguages; }
        
        public java.util.List<String> getSupportedMessageTypes() { return supportedMessageTypes; }
        public void setSupportedMessageTypes(java.util.List<String> supportedMessageTypes) { this.supportedMessageTypes = supportedMessageTypes; }
        
        public boolean isSupportsStreaming() { return supportsStreaming; }
        public void setSupportsStreaming(boolean supportsStreaming) { this.supportsStreaming = supportsStreaming; }
        
        public boolean isSupportsContext() { return supportsContext; }
        public void setSupportsContext(boolean supportsContext) { this.supportsContext = supportsContext; }
        
        public boolean isSupportsAnalytics() { return supportsAnalytics; }
        public void setSupportsAnalytics(boolean supportsAnalytics) { this.supportsAnalytics = supportsAnalytics; }
        
        public int getMaxMessageLength() { return maxMessageLength; }
        public void setMaxMessageLength(int maxMessageLength) { this.maxMessageLength = maxMessageLength; }
        
        public long getMaxProcessingTime() { return maxProcessingTime; }
        public void setMaxProcessingTime(long maxProcessingTime) { this.maxProcessingTime = maxProcessingTime; }
    }
}
