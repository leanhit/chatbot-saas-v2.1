package com.chatbot.shared.penny.providers.adapters;

import com.chatbot.spokes.facebook.webhook.service.ChatbotProviderService;
import com.chatbot.shared.penny.context.ConversationContext;
import com.chatbot.shared.penny.dto.request.MiddlewareRequest;
import com.chatbot.shared.penny.providers.MiddlewareProvider;
import com.chatbot.shared.penny.routing.ProviderSelector;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Adapter for Rasa provider - wraps existing RasaService
 */
@Service
@Slf4j
public class RasaProviderAdapter implements MiddlewareProvider {
    
    private final ChatbotProviderService rasaService;
    
    public RasaProviderAdapter(@Qualifier("rasaService") ChatbotProviderService rasaService) {
        this.rasaService = rasaService;
    }
    
    @Override
    public ProviderResponse processWithContext(ConversationContext context, MiddlewareRequest request) {
        try {
            log.debug("🤖 Processing message with Rasa for user: {}", request.getUserId());
            
            // Extract bot ID from request or context
            String botId = request.getBotId();
            if (botId == null && context != null) {
                botId = context.getBotId();
            }
            
            if (botId == null) {
                // Rasa might not need bot ID, use default
                botId = "default-rasa-bot";
            }
            
            // Call existing Rasa service
            Map<String, Object> response = rasaService.sendMessage(
                botId, 
                request.getUserId(), 
                request.getMessage()
            );
            
            log.debug("✅ Rasa response received for user: {}", request.getUserId());
            
            ProviderResponse providerResponse = new ProviderResponse();
            providerResponse.setProviderType(MiddlewareProvider.ProviderType.RASA);
            providerResponse.setResponse(response);
            providerResponse.setSuccess(true);
            providerResponse.setProcessingTime(System.currentTimeMillis());
            providerResponse.setMetadata(buildMetadata(response));
            
            return providerResponse;
            
        } catch (Exception e) {
            log.error("❌ Rasa provider error for user {}: {}", 
                request.getUserId(), e.getMessage(), e);
            
            ProviderResponse providerResponse = new ProviderResponse();
            providerResponse.setProviderType(MiddlewareProvider.ProviderType.RASA);
            providerResponse.setSuccess(false);
            providerResponse.setErrorMessage(e.getMessage());
            providerResponse.setProcessingTime(System.currentTimeMillis());
            
            return providerResponse;
        }
    }
    
    @Override
    public HealthStatus getHealthStatus() {
        try {
            // Simple health check - try to call health check method if available
            boolean isHealthy = rasaService.healthCheck("health-check-bot");
            
            HealthStatus healthStatus = new HealthStatus();
            healthStatus.setHealthy(isHealthy);
            healthStatus.setMessage(isHealthy ? "Rasa is healthy" : "Rasa health check failed");
            healthStatus.setLastCheck(System.currentTimeMillis());
            
            return healthStatus;
            
        } catch (Exception e) {
            log.error("❌ Rasa health check failed: {}", e.getMessage(), e);
            
            HealthStatus healthStatus = new HealthStatus();
            healthStatus.setHealthy(false);
            healthStatus.setMessage("Health check failed: " + e.getMessage());
            healthStatus.setLastCheck(System.currentTimeMillis());
            
            return healthStatus;
        }
    }
    
    @Override
    public MiddlewareProvider.ProviderType getProviderType() {
        return MiddlewareProvider.ProviderType.RASA;
    }
    
    @Override
    public boolean supportsFeature(MiddlewareProvider.Feature feature) {
        switch (feature) {
            case INTENT_DETECTION:
                return true; // Rasa has excellent NLU capabilities
            case ENTITY_EXTRACTION:
                return true; // Rasa is strong in entity extraction
            case CONTEXT_MANAGEMENT:
                return true; // Rasa supports conversation context
            case MULTILINGUAL:
                return true; // Rasa supports multiple languages
            case STREAMING:
                return false; // Rasa doesn't support streaming in current setup
            case ANALYTICS:
                return true; // Rasa has some analytics capabilities
            case CUSTOM_STORIES:
                return true; // Rasa supports custom stories
            case RULE_BASED_RESPONSES:
                return true; // Rasa supports rules
            default:
                return false;
        }
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
        capabilities.setSupportedMessageTypes(java.util.List.of("text", "image"));
        capabilities.setSupportsStreaming(false);
        capabilities.setSupportsContext(true);
        capabilities.setSupportsAnalytics(true);
        capabilities.setMaxMessageLength(5000);
        capabilities.setMaxProcessingTime(25000); // 25 seconds
        
        return capabilities;
    }
    
    // Private helper methods
    
    private Map<String, Object> buildMetadata(Map<String, Object> response) {
        Map<String, Object> metadata = new HashMap<>();
        
        if (response != null) {
            metadata.put("responseSize", response.toString().length());
            metadata.put("hasText", response.containsKey("text"));
            metadata.put("hasButtons", response.containsKey("buttons"));
            metadata.put("hasImage", response.containsKey("image"));
            metadata.put("hasAttachments", response.containsKey("attachments"));
            
            // Extract specific Rasa response metadata
            if (response.containsKey("text")) {
                Object text = response.get("text");
                if (text instanceof String) {
                    metadata.put("responseLength", ((String) text).length());
                }
            }
        }
        
        metadata.put("provider", "Rasa");
        metadata.put("timestamp", System.currentTimeMillis());
        
        return metadata;
    }
}
