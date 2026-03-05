package com.chatbot.shared.penny.routing;

import com.chatbot.spokes.facebook.webhook.service.ChatbotProviderService;
import com.chatbot.shared.penny.context.ConversationContext;
import com.chatbot.shared.penny.routing.dto.IntentAnalysisResult;
import com.chatbot.shared.penny.routing.dto.ProviderSelection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Provider Selector - Chọn provider phù hợp dựa trên intent và context
 */
@Service
@Slf4j
public class ProviderSelector {
    
    @Value("${penny.provider.selection.strategy:hybrid}")
    private String selectionStrategy;
    
    @Value("${penny.provider.fallback.enabled:true}")
    private boolean fallbackEnabled;
    
    private final Map<String, ProviderHealth> providerHealthMap = new ConcurrentHashMap<>();
    private final Random random = new Random();
    
    /**
     * Select appropriate provider for processing
     */
    public ProviderSelection select(IntentAnalysisResult analysis, ConversationContext context) {
        log.debug("🎯 Selecting provider using strategy: {}", selectionStrategy);
        
        ProviderType selectedType;
        String selectionReason;
        double confidence;
        
        switch (selectionStrategy.toLowerCase()) {
            case "intent_based":
                selectedType = selectByIntent(analysis, context);
                selectionReason = "Intent-based selection";
                break;
            case "complexity_based":
                selectedType = selectByComplexity(analysis, context);
                selectionReason = "Complexity-based selection";
                break;
            case "context_based":
                selectedType = selectByContext(analysis, context);
                selectionReason = "Context-based selection";
                break;
            case "health_based":
                selectedType = selectByHealth(analysis, context);
                selectionReason = "Health-based selection";
                break;
            case "hybrid":
            default:
                selectedType = selectHybrid(analysis, context);
                selectionReason = "Hybrid selection";
                break;
        }
        
        confidence = calculateSelectionConfidence(selectedType, analysis, context);
        
        ProviderSelection result = ProviderSelection.builder()
            .providerType(selectedType)
            .provider(getProviderInstance(selectedType))
            .selectionReason(selectionReason)
            .confidence(confidence)
            .fallbackProviders(getFallbackProviders(selectedType))
            .build();
        
        log.info("🎯 Provider selected: {} (Reason: {}, Confidence: {})", 
            selectedType, selectionReason, confidence);
        
        return result;
    }
    
    /**
     * Intent-based provider selection
     */
    private ProviderType selectByIntent(IntentAnalysisResult analysis, ConversationContext context) {
        String intent = analysis.getPrimaryIntent();
        
        // Business intents -> PennyBot (has ERP integration)
        if (isBusinessIntent(intent)) {
            return ProviderType.PENNYBOT;
        }
        
        // Support intents -> Rasa (better for structured responses)
        if (isSupportIntent(intent)) {
            return ProviderType.RASA;
        }
        
        // General chat -> PennyBot (default)
        return ProviderType.PENNYBOT;
    }
    
    /**
     * Complexity-based provider selection
     */
    private ProviderType selectByComplexity(IntentAnalysisResult analysis, ConversationContext context) {
        String complexity = analysis.getComplexity();
        
        // High complexity -> PennyBot (has more features)
        if ("high".equals(complexity)) {
            return ProviderType.PENNYBOT;
        }
        
        // Medium complexity -> Rasa (faster for moderate complexity)
        if ("medium".equals(complexity)) {
            return ProviderType.RASA;
        }
        
        // Low complexity -> PennyBot (default)
        return ProviderType.PENNYBOT;
    }
    
    /**
     * Context-based provider selection
     */
    private ProviderType selectByContext(IntentAnalysisResult analysis, ConversationContext context) {
        if (context != null) {
            // Stay with same provider for conversation continuity
            if (context.getPreviousProvider() != null && 
                context.getMessageCountInCurrentSession() < 5) {
                return ProviderType.valueOf(context.getPreviousProvider().toString());
            }
            
            // If user had good experience with previous provider
            if (context.getLastSuccessfulProvider() != null) {
                return ProviderType.valueOf(context.getLastSuccessfulProvider().toString());
            }
        }
        
        // Default to PennyBot
        return ProviderType.PENNYBOT;
    }
    
    /**
     * Health-based provider selection
     */
    private ProviderType selectByHealth(IntentAnalysisResult analysis, ConversationContext context) {
        List<ProviderType> healthyProviders = getHealthyProviders();
        
        if (healthyProviders.isEmpty()) {
            log.warn("⚠️ No healthy providers available, defaulting to PennyBot");
            return ProviderType.PENNYBOT;
        }
        
        // Select randomly from healthy providers for load balancing
        return healthyProviders.get(random.nextInt(healthyProviders.size()));
    }
    
    /**
     * Hybrid selection strategy (recommended)
     */
    private ProviderType selectHybrid(IntentAnalysisResult analysis, ConversationContext context) {
        String intent = analysis.getPrimaryIntent();
        String complexity = analysis.getComplexity();
        double confidence = analysis.getConfidence();
        
        // Rule 1: Business intents always go to PennyBot
        if (isBusinessIntent(intent)) {
            log.debug("🎯 Business intent detected, selecting PennyBot");
            return ProviderType.PENNYBOT;
        }
        
        // Rule 2: High complexity with low confidence -> PennyBot (more robust)
        if ("high".equals(complexity) && confidence < 0.7) {
            log.debug("🎯 High complexity + low Confidence, selecting PennyBot");
            return ProviderType.PENNYBOT;
        }
        
        // Rule 3: Support intents -> Rasa (better for structured support)
        if (isSupportIntent(intent)) {
            log.debug("🎯 Support intent detected, selecting Rasa");
            return ProviderType.RASA;
        }
        
        // Rule 4: Context continuity for ongoing conversations
        if (context != null && context.getPreviousProvider() != null && 
            context.getMessageCountInCurrentSession() < 5) {
            ProviderType previousProvider = ProviderType.valueOf(context.getPreviousProvider().toString());
            log.debug("🎯 Maintaining conversation continuity with previous provider: {}", previousProvider);
            return previousProvider;
        }
        
        // Rule 5: Health-based fallback
        List<ProviderType> healthyProviders = getHealthyProviders();
        if (!healthyProviders.isEmpty()) {
            // Prefer PennyBot if healthy, otherwise use Rasa
            if (healthyProviders.contains(ProviderType.PENNYBOT)) {
                log.debug("🎯 PennyBot is healthy, selecting PennyBot");
                return ProviderType.PENNYBOT;
            } else if (healthyProviders.contains(ProviderType.RASA)) {
                log.debug("🎯 PennyBot not healthy, selecting Rasa");
                return ProviderType.RASA;
            }
        }
        
        // Default fallback
        log.debug("🎯 Using default provider: PennyBot");
        return ProviderType.PENNYBOT;
    }
    
    /**
     * Calculate selection confidence
     */
    private double calculateSelectionConfidence(ProviderType selectedType, 
                                              IntentAnalysisResult analysis, 
                                              ConversationContext context) {
        double confidence = 0.5; // Base confidence
        
        // Boost confidence based on intent clarity
        confidence += analysis.getConfidence() * 0.3;
        
        // Boost confidence based on context continuity
        if (context != null && selectedType.equals(context.getPreviousProvider())) {
            confidence += 0.2;
        }
        
        // Boost confidence based on provider health
        ProviderHealth health = providerHealthMap.get(selectedType.toString());
        if (health != null && health.isHealthy()) {
            confidence += 0.2;
        }
        
        return Math.min(confidence, 1.0);
    }
    
    /**
     * Get fallback providers
     */
    private List<ProviderType> getFallbackProviders(ProviderType primaryProvider) {
        if (!fallbackEnabled) {
            return Collections.emptyList();
        }
        
        List<ProviderType> fallbacks = new ArrayList<>();
        
        // Add all other providers as fallbacks
        for (ProviderType type : ProviderType.values()) {
            if (!type.equals(primaryProvider) && isProviderHealthy(type)) {
                fallbacks.add(type);
            }
        }
        
        return fallbacks;
    }
    
    /**
     * Get list of healthy providers
     */
    private List<ProviderType> getHealthyProviders() {
        List<ProviderType> healthyProviders = new ArrayList<>();
        
        for (ProviderType type : ProviderType.values()) {
            if (isProviderHealthy(type)) {
                healthyProviders.add(type);
            }
        }
        
        return healthyProviders;
    }
    
    /**
     * Check if provider is healthy
     */
    private boolean isProviderHealthy(ProviderType type) {
        ProviderHealth health = providerHealthMap.get(type.toString());
        return health != null && health.isHealthy();
    }
    
    /**
     * Get provider instance (this would be injected in real implementation)
     */
    private ChatbotProviderService getProviderInstance(ProviderType type) {
        // This would be implemented to get actual provider instances
        // For now, return null - will be properly injected
        return null;
    }
    
    /**
     * Check if intent is business-related
     */
    private boolean isBusinessIntent(String intent) {
        return intent.equals("order_inquiry") ||
               intent.equals("product_inquiry") ||
               intent.equals("price_inquiry") ||
               intent.equals("payment_inquiry") ||
               intent.equals("shipping_inquiry");
    }
    
    /**
     * Check if intent is support-related
     */
    private boolean isSupportIntent(String intent) {
        return intent.equals("customer_support") ||
               intent.equals("technical_support") ||
               intent.equals("complaint") ||
               intent.equals("refund_request");
    }
    
    /**
     * Check if intent requires default response
     */
    private boolean isDefaultIntent(String intent) {
        return intent.equals("unknown") ||
               intent.equals("greeting") ||
               intent.equals("gratitude") ||
               intent.equals("goodbye") ||
               intent.equals("smalltalk") ||
               !isBusinessIntent(intent) && !isSupportIntent(intent);
    }
    
    /**
     * Update provider health status
     */
    public void updateProviderHealth(ProviderType type, boolean isHealthy, String message) {
        ProviderHealth health = providerHealthMap.computeIfAbsent(
            type.toString(), 
            k -> new ProviderHealth()
        );
        health.update(isHealthy, message);
        
        log.debug("🏥 Provider health updated: {} -> {} ({})", 
            type, isHealthy ? "healthy" : "unhealthy", message);
    }
    
    /**
     * Get provider health status
     */
    public Map<String, ProviderHealth> getAllProviderHealth() {
        return new HashMap<>(providerHealthMap);
    }
    
    // Inner classes
    
    public enum ProviderType {
        BOTPRESS("Botpress"),
        PENNYBOT("PennyBot"),
        RASA("Rasa"),
        GPT("GPT");
        
        private final String displayName;
        
        ProviderType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    public static class ProviderHealth {
        private boolean healthy = true;
        private String lastMessage;
        private long lastCheck;
        private int consecutiveFailures = 0;
        
        public void update(boolean isHealthy, String message) {
            this.healthy = isHealthy;
            this.lastMessage = message;
            this.lastCheck = System.currentTimeMillis();
            
            if (isHealthy) {
                this.consecutiveFailures = 0;
            } else {
                this.consecutiveFailures++;
            }
        }
        
        public boolean isHealthy() {
            return healthy && consecutiveFailures < 3; // Allow up to 3 consecutive failures
        }
        
        // Getters
        public boolean isHealthyStatus() { return healthy; }
        public String getLastMessage() { return lastMessage; }
        public long getLastCheck() { return lastCheck; }
        public int getConsecutiveFailures() { return consecutiveFailures; }
    }
}
