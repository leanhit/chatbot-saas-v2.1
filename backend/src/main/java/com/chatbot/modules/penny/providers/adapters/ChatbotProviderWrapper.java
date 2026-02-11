package com.chatbot.modules.penny.providers.adapters;

import com.chatbot.modules.facebook.webhook.service.ChatbotProviderFactory;
import com.chatbot.modules.facebook.webhook.service.ChatbotProviderService;
import com.chatbot.modules.penny.context.ConversationContext;
import com.chatbot.modules.penny.dto.request.MiddlewareRequest;
import com.chatbot.modules.penny.providers.MiddlewareProvider;
import com.chatbot.modules.penny.routing.ProviderSelector;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Wrapper for existing ChatbotProviderService - adapts existing providers to Penny interface
 */
@Service
@Slf4j
public class ChatbotProviderWrapper implements MiddlewareProvider {
    
    private final ChatbotProviderFactory providerFactory;
    
    public ChatbotProviderWrapper(ChatbotProviderFactory providerFactory) {
        this.providerFactory = providerFactory;
    }
    
    @Override
    public ProviderResponse processWithContext(ConversationContext context, MiddlewareRequest request) {
        try {
            log.debug("🤖 Processing message with wrapped provider for user: {}", request.getUserId());
            
            // Get provider type from context or request
            String providerType = determineProviderType(context, request);
            
            // Get provider from factory
            ChatbotProviderService provider = providerFactory.getProvider(providerType);
            
            if (provider == null) {
                throw new IllegalArgumentException("Provider not found: " + providerType);
            }
            
            // Extract bot ID
            String botId = request.getBotId();
            if (botId == null && context != null) {
                botId = context.getBotId();
            }
            
            // Call provider
            Map<String, Object> response = provider.sendMessage(
                botId, 
                request.getUserId(), 
                request.getMessage()
            );
            
            log.debug("✅ Wrapped provider response received for user: {}", request.getUserId());
            
            ProviderResponse providerResponse = new ProviderResponse();
            providerResponse.setProviderType(MiddlewareProvider.ProviderType.valueOf(providerType));
            providerResponse.setResponse(response);
            providerResponse.setSuccess(true);
            providerResponse.setProcessingTime(System.currentTimeMillis());
            providerResponse.setMetadata(buildMetadata(response, providerType));
            
            return providerResponse;
            
        } catch (Exception e) {
            log.error("❌ Wrapped provider error for user {}: {}", 
                request.getUserId(), e.getMessage(), e);
            
            ProviderResponse providerResponse = new ProviderResponse();
            providerResponse.setProviderType(MiddlewareProvider.ProviderType.BOTPRESS); // Default
            providerResponse.setSuccess(false);
            providerResponse.setErrorMessage(e.getMessage());
            providerResponse.setProcessingTime(System.currentTimeMillis());
            
            return providerResponse;
        }
    }
    
    @Override
    public HealthStatus getHealthStatus() {
        try {
            // Check health of all available providers
            Map<String, Boolean> providerHealth = new HashMap<>();
            
            // Try to get health status for each provider type
            String[] providerTypes = {"BOTPRESS", "RASA"};
            
            for (String providerType : providerTypes) {
                try {
                    ChatbotProviderService provider = providerFactory.getProvider(providerType);
                    if (provider != null) {
                        boolean isHealthy = provider.healthCheck("health-check-bot");
                        providerHealth.put(providerType, isHealthy);
                    }
                } catch (Exception e) {
                    log.debug("Health check failed for provider {}: {}", providerType, e.getMessage());
                    providerHealth.put(providerType, false);
                }
            }
            
            // Determine overall health
            boolean allHealthy = providerHealth.values().stream().allMatch(Boolean::booleanValue);
            boolean anyHealthy = providerHealth.values().stream().anyMatch(Boolean::booleanValue);
            
            HealthStatus healthStatus = new HealthStatus();
            healthStatus.setHealthy(anyHealthy);
            healthStatus.setMessage(allHealthy ? "All providers healthy" : "Some providers unhealthy");
            healthStatus.setLastCheck(System.currentTimeMillis());
            healthStatus.setProviderHealth(providerHealth);
            
            return healthStatus;
            
        } catch (Exception e) {
            log.error("❌ Wrapped provider health check failed: {}", e.getMessage(), e);
            
            HealthStatus healthStatus = new HealthStatus();
            healthStatus.setHealthy(false);
            healthStatus.setMessage("Health check failed: " + e.getMessage());
            healthStatus.setLastCheck(System.currentTimeMillis());
            
            return healthStatus;
        }
    }
    
    @Override
    public MiddlewareProvider.ProviderType getProviderType() {
        return MiddlewareProvider.ProviderType.WRAPPED; // Special type for wrapper
    }
    
    @Override
    public boolean supportsFeature(MiddlewareProvider.Feature feature) {
        // Wrapper supports features that any underlying provider supports
        return true; // Conservative approach - assume features are supported
    }
    
    @Override
    public ProviderCapabilities getCapabilities() {
        ProviderCapabilities capabilities = new ProviderCapabilities();
        capabilities.setSupportedIntents(java.util.List.of(
            "order_inquiry", "product_inquiry", "price_inquiry", 
            "customer_support", "greeting", "gratitude",
            "technical_support", "complaint", "refund_request"
        ));
        capabilities.setSupportedLanguages(java.util.List.of("vi", "en"));
        capabilities.setSupportedMessageTypes(java.util.List.of("text", "image", "video"));
        capabilities.setSupportsStreaming(false);
        capabilities.setSupportsContext(true);
        capabilities.setSupportsAnalytics(true);
        capabilities.setMaxMessageLength(4000);
        capabilities.setMaxProcessingTime(30000);
        
        return capabilities;
    }
    
    // Private helper methods
    
    private String determineProviderType(ConversationContext context, MiddlewareRequest request) {
        // Try to get provider type from various sources
        
        // 1. From context
        if (context != null && context.getLastProvider() != null) {
            return context.getLastProvider().toString();
        }
        
        // 2. From request metadata
        if (request.hasMetadata("provider")) {
            return request.getMetadata("provider", String.class);
        }
        
        // 3. From request
        if (request.getMetadata() != null && request.getMetadata().containsKey("provider")) {
            Object provider = request.getMetadata().get("provider");
            if (provider != null) {
                return provider.toString();
            }
        }
        
        // 4. Default to Botpress
        return "BOTPRESS";
    }
    
    private Map<String, Object> buildMetadata(Map<String, Object> response, String providerType) {
        Map<String, Object> metadata = new HashMap<>();
        
        if (response != null) {
            metadata.put("responseSize", response.toString().length());
            metadata.put("hasPayload", response.containsKey("payload"));
            metadata.put("hasResponses", response.containsKey("responses"));
            metadata.put("hasText", response.containsKey("text"));
        }
        
        metadata.put("provider", providerType);
        metadata.put("wrapper", "ChatbotProviderWrapper");
        metadata.put("timestamp", System.currentTimeMillis());
        
        return metadata;
    }
}
