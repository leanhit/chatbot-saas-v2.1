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
 * Adapter for Botpress provider - wraps existing BotpressServiceFb
 */
@Service
@Slf4j
public class BotpressProviderAdapter implements MiddlewareProvider {
    
    private final ChatbotProviderService botpressService;
    
    public BotpressProviderAdapter(@Qualifier("botpressServiceFb") ChatbotProviderService botpressService) {
        this.botpressService = botpressService;
    }
    
    @Override
    public ProviderResponse processWithContext(ConversationContext context, MiddlewareRequest request) {
        try {
            log.debug("🤖 Processing message with Botpress for user: {}", request.getUserId());
            
            // Extract bot ID from request or context
            String botId = request.getBotId();
            if (botId == null && context != null) {
                botId = context.getBotId();
            }
            
            if (botId == null) {
                throw new IllegalArgumentException("Bot ID is required for Botpress provider");
            }
            
            // Call existing Botpress service
            Map<String, Object> response = botpressService.sendMessage(
                botId, 
                request.getUserId(), 
                request.getMessage()
            );
            
            log.debug("✅ Botpress response received for user: {}", request.getUserId());
            
            ProviderResponse providerResponse = new ProviderResponse();
            providerResponse.setProviderType(MiddlewareProvider.ProviderType.BOTPRESS);
            providerResponse.setResponse(response);
            providerResponse.setSuccess(true);
            providerResponse.setProcessingTime(System.currentTimeMillis());
            providerResponse.setMetadata(buildMetadata(response));
            
            return providerResponse;
            
        } catch (Exception e) {
            log.error("❌ Botpress provider error for user {}: {}", 
                request.getUserId(), e.getMessage(), e);
            
            ProviderResponse providerResponse = new ProviderResponse();
            providerResponse.setProviderType(MiddlewareProvider.ProviderType.BOTPRESS);
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
            boolean isHealthy = botpressService.healthCheck("health-check-bot");
            
            HealthStatus healthStatus = new HealthStatus();
            healthStatus.setHealthy(isHealthy);
            healthStatus.setMessage(isHealthy ? "Botpress is healthy" : "Botpress health check failed");
            healthStatus.setLastCheck(System.currentTimeMillis());
            
            return healthStatus;
            
        } catch (Exception e) {
            log.error("❌ Botpress health check failed: {}", e.getMessage(), e);
            
            HealthStatus healthStatus = new HealthStatus();
            healthStatus.setHealthy(false);
            healthStatus.setMessage("Health check failed: " + e.getMessage());
            healthStatus.setLastCheck(System.currentTimeMillis());
            
            return healthStatus;
        }
    }
    
    @Override
    public MiddlewareProvider.ProviderType getProviderType() {
        return MiddlewareProvider.ProviderType.BOTPRESS;
    }
    
    @Override
    public boolean supportsFeature(MiddlewareProvider.Feature feature) {
        switch (feature) {
            case INTENT_DETECTION:
                return true; // Botpress has built-in NLU
            case ENTITY_EXTRACTION:
                return true; // Botpress can extract entities
            case CONTEXT_MANAGEMENT:
                return true; // Botpress supports conversation context
            case MULTILINGUAL:
                return true; // Botpress supports multiple languages
            case STREAMING:
                return false; // Botpress doesn't support streaming in current setup
            case ANALYTICS:
                return true; // Botpress has analytics capabilities
            default:
                return false;
        }
    }
    
    @Override
    public ProviderCapabilities getCapabilities() {
        ProviderCapabilities capabilities = new ProviderCapabilities();
        capabilities.setSupportedIntents(java.util.List.of(
            "order_inquiry", "product_inquiry", "price_inquiry", 
            "customer_support", "greeting", "gratitude"
        ));
        capabilities.setSupportedLanguages(java.util.List.of("vi", "en"));
        capabilities.setSupportedMessageTypes(java.util.List.of("text", "image", "video"));
        capabilities.setSupportsStreaming(false);
        capabilities.setSupportsContext(true);
        capabilities.setSupportsAnalytics(true);
        capabilities.setMaxMessageLength(4000);
        capabilities.setMaxProcessingTime(30000); // 30 seconds
        
        return capabilities;
    }
    
    // Private helper methods
    
    private Map<String, Object> buildMetadata(Map<String, Object> response) {
        Map<String, Object> metadata = new HashMap<>();
        
        if (response != null) {
            metadata.put("responseSize", response.toString().length());
            metadata.put("hasPayload", response.containsKey("payload"));
            metadata.put("hasResponses", response.containsKey("responses"));
            
            // Extract specific Botpress response metadata
            if (response.containsKey("payload")) {
                Object payload = response.get("payload");
                if (payload instanceof Map) {
                    Map<?, ?> payloadMap = (Map<?, ?>) payload;
                    metadata.put("payloadType", payloadMap.get("type"));
                    metadata.put("hasText", payloadMap.containsKey("text"));
                }
            }
        }
        
        metadata.put("provider", "Botpress");
        metadata.put("timestamp", System.currentTimeMillis());
        
        return metadata;
    }
}
