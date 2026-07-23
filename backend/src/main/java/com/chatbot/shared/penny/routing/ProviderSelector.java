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

    @Value("${penny.provider.abtesting.enabled:false}")
    private boolean abTestingEnabled;

    @Value("${penny.provider.abtesting.traffic.split:50}")
    private int abTestingTrafficSplit; // Percentage of traffic to variant B

    private final List<ChatbotProviderService> providers;
    private final Map<String, ProviderHealth> providerHealthMap = new ConcurrentHashMap<>();
    private final Map<String, ChatbotProviderService> providerInstanceMap = new ConcurrentHashMap<>();
    private final Map<String, ABTestMetrics> abTestMetricsMap = new ConcurrentHashMap<>();
    private final Random random = new Random();

    public ProviderSelector(List<ChatbotProviderService> providers) {
        this.providers = providers;
    }

    /**
     * Build provider instance map from injected providers after Spring context is ready.
     * Maps getProviderType() string ("BOTPRESS", "PENNYBOT", "GPT") → service instance.
     */
    @jakarta.annotation.PostConstruct
    public void initProviderMap() {
        for (ChatbotProviderService provider : providers) {
            String type = provider.getProviderType();
            if (type != null) {
                providerInstanceMap.put(type.toUpperCase(), provider);
                log.info("✅ Registered provider: {} -> {}", type, provider.getClass().getSimpleName());
            }
        }
        log.info("🗺️ Provider map initialized with {} providers: {}", providerInstanceMap.size(), providerInstanceMap.keySet());
    }
    
    /**
     * Select appropriate provider for processing
     */
    public ProviderSelection select(IntentAnalysisResult analysis, ConversationContext context) {
        log.debug("🎯 Selecting provider using strategy: {}", selectionStrategy);
        
        ProviderType selectedType;
        String selectionReason;
        double confidence;
        
        // Apply A/B testing if enabled
        if (abTestingEnabled) {
            ProviderType abTestSelection = selectByABTest(analysis, context);
            if (abTestSelection != null) {
                selectedType = abTestSelection;
                selectionReason = "A/B test selection";
                confidence = 0.5; // Neutral confidence for A/B test
                recordABTestSelection(selectedType, analysis);
                
                ProviderSelection result = ProviderSelection.builder()
                    .providerType(selectedType)
                    .provider(getProviderInstance(selectedType))
                    .selectionReason(selectionReason)
                    .confidence(confidence)
                    .fallbackProviders(getFallbackProviders(selectedType))
                    .build();
                
                log.info("🎯 Provider selected via A/B test: {} (Reason: {})", selectedType, selectionReason);
                return result;
            }
        }
        
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
        
        // Support intents -> PennyBot
        if (isSupportIntent(intent)) {
            return ProviderType.PENNYBOT;
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
        
        // Medium complexity -> PennyBot
        if ("medium".equals(complexity)) {
            return ProviderType.PENNYBOT;
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

        // Rule 1: Business intents → PennyBot (has ERP / rule engine)
        if (isBusinessIntent(intent)) {
            log.debug("🎯 Business intent → PennyBot");
            return ProviderType.PENNYBOT;
        }

        // Rule 2: Support / complaint intents → PennyBot
        if (isSupportIntent(intent)) {
            log.debug("🎯 Support intent → PennyBot");
            return ProviderType.PENNYBOT;
        }

        // Rule 3: General chat / unknown / greeting → GPT (natural language)
        if (isGeneralIntent(intent)) {
            if (isProviderHealthy(ProviderType.GPT)) {
                log.debug("🎯 General intent → GPT");
                return ProviderType.GPT;
            }
            // GPT down → try Claude
            if (isProviderHealthy(ProviderType.CLAUDE)) {
                log.debug("🎯 GPT unhealthy → Claude fallback");
                return ProviderType.CLAUDE;
            }
            log.debug("🎯 LLM providers unhealthy → PennyBot fallback");
            return ProviderType.PENNYBOT;
        }

        // Rule 4: Context continuity for ongoing conversations
        if (context != null && context.getPreviousProvider() != null &&
                context.getMessageCountInCurrentSession() < 5) {
            try {
                ProviderType previousProvider = ProviderType.valueOf(
                    context.getPreviousProvider().toString());
                log.debug("🎯 Continuing with previous provider: {}", previousProvider);
                return previousProvider;
            } catch (IllegalArgumentException e) {
                log.warn("⚠️ Unknown previous provider: {}", context.getPreviousProvider());
            }
        }

        // Rule 5: High complexity + low confidence → GPT for better reasoning
        if ("high".equals(complexity) && confidence > 0.7 && isProviderHealthy(ProviderType.GPT)) {
            log.debug("🎯 High complexity, high confidence → GPT");
            return ProviderType.GPT;
        }

        // Default: PennyBot
        log.debug("🎯 Default provider: PennyBot");
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
     * Get provider instance from the injected provider map.
     * Returns null and logs a warning if no matching provider is registered.
     */
    private ChatbotProviderService getProviderInstance(ProviderType type) {
        if (type == null) {
            log.warn("⚠️ Cannot get provider instance: ProviderType is null");
            return null;
        }
        ChatbotProviderService instance = providerInstanceMap.get(type.name());
        if (instance == null) {
            log.warn("⚠️ No ChatbotProviderService registered for ProviderType: {}. Available: {}",
                type, providerInstanceMap.keySet());
        }
        return instance;
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
     * Check if intent requires LLM (general/open-ended conversation)
     */
    private boolean isGeneralIntent(String intent) {
        return intent.equals("unknown") ||
               intent.equals("general_chat") ||
               intent.equals("greeting") ||
               intent.equals("gratitude") ||
               intent.equals("goodbye") ||
               intent.equals("smalltalk") ||
               (!isBusinessIntent(intent) && !isSupportIntent(intent));
    }

    /**
     * Check if intent requires default response (legacy alias)
     */
    private boolean isDefaultIntent(String intent) {
        return isGeneralIntent(intent);
    }
    
    /**
     * A/B testing provider selection
     * Routes traffic between two providers based on configured split percentage
     */
    private ProviderType selectByABTest(IntentAnalysisResult analysis, ConversationContext context) {
        // Define A/B test variants (Control: PennyBot, Variant: GPT)
        ProviderType controlProvider = ProviderType.PENNYBOT;
        ProviderType variantProvider = ProviderType.GPT;
        
        // Check if both providers are healthy
        if (!isProviderHealthy(controlProvider) || !isProviderHealthy(variantProvider)) {
            log.debug("⚠️ A/B test providers not healthy, falling back to standard selection");
            return null;
        }
        
        // Use consistent hashing based on conversation/user ID for sticky sessions
        String sessionKey = getSessionKey(context);
        int hash = Math.abs(sessionKey.hashCode());
        int bucket = hash % 100;
        
        // Route to variant based on traffic split
        if (bucket < abTestingTrafficSplit) {
            log.debug("🧪 A/B test: Routing to variant B (GPT) - bucket: {}", bucket);
            return variantProvider;
        } else {
            log.debug("🧪 A/B test: Routing to control A (PennyBot) - bucket: {}", bucket);
            return controlProvider;
        }
    }
    
    /**
     * Generate consistent session key for A/B test sticky routing
     */
    private String getSessionKey(ConversationContext context) {
        if (context != null && context.getUserId() != null) {
            return "user:" + context.getUserId();
        }
        if (context != null && context.getContextId() != null) {
            return "conv:" + context.getContextId();
        }
        // Fallback to random for stateless requests
        return "random:" + random.nextInt(10000);
    }
    
    /**
     * Record A/B test selection for metrics
     */
    private void recordABTestSelection(ProviderType selectedType, IntentAnalysisResult analysis) {
        String testKey = "ab_test_pennybot_vs_gpt";
        ABTestMetrics metrics = abTestMetricsMap.computeIfAbsent(testKey, k -> new ABTestMetrics());
        
        if (selectedType == ProviderType.PENNYBOT) {
            metrics.incrementControlSelections();
        } else if (selectedType == ProviderType.GPT) {
            metrics.incrementVariantSelections();
        }
        
        // Record intent for analysis
        metrics.recordIntent(analysis.getPrimaryIntent());
    }
    
    /**
     * Record A/B test result (success/failure, response time, etc.)
     */
    public void recordABTestResult(ProviderType providerType, boolean success, long responseTimeMs, String userFeedback) {
        String testKey = "ab_test_pennybot_vs_gpt";
        ABTestMetrics metrics = abTestMetricsMap.get(testKey);
        
        if (metrics != null) {
            if (providerType == ProviderType.PENNYBOT) {
                metrics.recordControlResult(success, responseTimeMs, userFeedback);
            } else if (providerType == ProviderType.GPT) {
                metrics.recordVariantResult(success, responseTimeMs, userFeedback);
            }
        }
    }
    
    /**
     * Get A/B test metrics
     */
    public Map<String, ABTestMetrics> getABTestMetrics() {
        return new HashMap<>(abTestMetricsMap);
    }
    
    /**
     * Reset A/B test metrics
     */
    public void resetABTestMetrics(String testKey) {
        if (testKey != null) {
            abTestMetricsMap.remove(testKey);
            log.info("🧪 A/B test metrics reset for: {}", testKey);
        } else {
            abTestMetricsMap.clear();
            log.info("🧪 All A/B test metrics reset");
        }
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
    
    /**
     * Estimate cost for a provider based on message length
     * Uses a simple heuristic: ~4 characters per token for Vietnamese/English
     */
    public ProviderCost estimateProviderCost(ProviderType providerType, String message) {
        int estimatedTokens = estimateTokenCount(message);
        return new ProviderCost(providerType, estimatedTokens);
    }
    
    /**
     * Estimate token count from message
     * Heuristic: ~4 characters per token for Vietnamese/English text
     */
    private int estimateTokenCount(String message) {
        if (message == null || message.isEmpty()) {
            return 0;
        }
        // Simple heuristic: ~4 characters per token
        return Math.max(1, (int) Math.ceil(message.length() / 4.0));
    }
    
    /**
     * Get cost comparison for all providers for a given message
     */
    public Map<String, ProviderCost> getAllProviderCosts(String message) {
        Map<String, ProviderCost> costs = new HashMap<>();
        
        for (ProviderType type : ProviderType.values()) {
            costs.put(type.name(), estimateProviderCost(type, message));
        }
        
        return costs;
    }
    
    /**
     * Select cheapest healthy provider based on cost estimation
     */
    public ProviderType selectCheapestHealthyProvider(String message) {
        Map<String, ProviderCost> costs = getAllProviderCosts(message);
        ProviderType cheapest = null;
        double minCost = Double.MAX_VALUE;
        
        for (Map.Entry<String, ProviderCost> entry : costs.entrySet()) {
            ProviderType type = ProviderType.valueOf(entry.getKey());
            ProviderCost cost = entry.getValue();
            
            if (isProviderHealthy(type) && cost.getEstimatedCost() < minCost) {
                minCost = cost.getEstimatedCost();
                cheapest = type;
            }
        }
        
        return cheapest != null ? cheapest : ProviderType.PENNYBOT;
    }
    
    // Inner classes
    
    public enum ProviderType {
        BOTPRESS("Botpress", 0.001, 0.0),
        PENNYBOT("PennyBot", 0.0005, 0.0),
        GPT("GPT", 0.002, 0.001),
        CLAUDE("Claude", 0.003, 0.0015);

        private final String displayName;
        private final double costPer1kTokens;
        private final double costPerRequest;

        ProviderType(String displayName, double costPer1kTokens, double costPerRequest) {
            this.displayName = displayName;
            this.costPer1kTokens = costPer1kTokens;
            this.costPerRequest = costPerRequest;
        }

        public String getDisplayName() {
            return displayName;
        }

        public double getCostPer1kTokens() {
            return costPer1kTokens;
        }

        public double getCostPerRequest() {
            return costPerRequest;
        }
    }
    
    /**
     * Cost metadata for provider selection
     */
    public static class ProviderCost {
        private final ProviderType providerType;
        private final double estimatedCost;
        private final int estimatedTokens;
        private final double costPer1kTokens;
        private final double costPerRequest;
        
        public ProviderCost(ProviderType providerType, int estimatedTokens) {
            this.providerType = providerType;
            this.estimatedTokens = estimatedTokens;
            this.costPer1kTokens = providerType.getCostPer1kTokens();
            this.costPerRequest = providerType.getCostPerRequest();
            this.estimatedCost = calculateEstimatedCost(estimatedTokens, costPer1kTokens, costPerRequest);
        }
        
        private double calculateEstimatedCost(int tokens, double costPer1kTokens, double costPerRequest) {
            double tokenCost = (tokens / 1000.0) * costPer1kTokens;
            return tokenCost + costPerRequest;
        }
        
        public ProviderType getProviderType() { return providerType; }
        public double getEstimatedCost() { return estimatedCost; }
        public int getEstimatedTokens() { return estimatedTokens; }
        public double getCostPer1kTokens() { return costPer1kTokens; }
        public double getCostPerRequest() { return costPerRequest; }
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
    
    /**
     * A/B Test Metrics - Tracks performance metrics for A/B testing
     */
    public static class ABTestMetrics {
        private long controlSelections = 0;
        private long variantSelections = 0;
        private long controlSuccesses = 0;
        private long variantSuccesses = 0;
        private long controlFailures = 0;
        private long variantFailures = 0;
        private double controlAvgResponseTime = 0.0;
        private double variantAvgResponseTime = 0.0;
        private long controlTotalResponseTime = 0;
        private long variantTotalResponseTime = 0;
        private Map<String, Long> intentDistribution = new ConcurrentHashMap<>();
        private int positiveFeedbackControl = 0;
        private int positiveFeedbackVariant = 0;
        private int negativeFeedbackControl = 0;
        private int negativeFeedbackVariant = 0;
        
        public synchronized void incrementControlSelections() {
            controlSelections++;
        }
        
        public synchronized void incrementVariantSelections() {
            variantSelections++;
        }
        
        public synchronized void recordControlResult(boolean success, long responseTimeMs, String userFeedback) {
            if (success) {
                controlSuccesses++;
            } else {
                controlFailures++;
            }
            controlTotalResponseTime += responseTimeMs;
            controlAvgResponseTime = controlTotalResponseTime / (double) (controlSuccesses + controlFailures);
            
            if (userFeedback != null) {
                if (userFeedback.toLowerCase().contains("good") || userFeedback.toLowerCase().contains("like")) {
                    positiveFeedbackControl++;
                } else if (userFeedback.toLowerCase().contains("bad") || userFeedback.toLowerCase().contains("dislike")) {
                    negativeFeedbackControl++;
                }
            }
        }
        
        public synchronized void recordVariantResult(boolean success, long responseTimeMs, String userFeedback) {
            if (success) {
                variantSuccesses++;
            } else {
                variantFailures++;
            }
            variantTotalResponseTime += responseTimeMs;
            variantAvgResponseTime = variantTotalResponseTime / (double) (variantSuccesses + variantFailures);
            
            if (userFeedback != null) {
                if (userFeedback.toLowerCase().contains("good") || userFeedback.toLowerCase().contains("like")) {
                    positiveFeedbackVariant++;
                } else if (userFeedback.toLowerCase().contains("bad") || userFeedback.toLowerCase().contains("dislike")) {
                    negativeFeedbackVariant++;
                }
            }
        }
        
        public synchronized void recordIntent(String intent) {
            intentDistribution.merge(intent, 1L, Long::sum);
        }
        
        // Getters
        public long getControlSelections() { return controlSelections; }
        public long getVariantSelections() { return variantSelections; }
        public long getControlSuccesses() { return controlSuccesses; }
        public long getVariantSuccesses() { return variantSuccesses; }
        public long getControlFailures() { return controlFailures; }
        public long getVariantFailures() { return variantFailures; }
        public double getControlSuccessRate() {
            long total = controlSuccesses + controlFailures;
            return total > 0 ? (double) controlSuccesses / total : 0.0;
        }
        public double getVariantSuccessRate() {
            long total = variantSuccesses + variantFailures;
            return total > 0 ? (double) variantSuccesses / total : 0.0;
        }
        public double getControlAvgResponseTime() { return controlAvgResponseTime; }
        public double getVariantAvgResponseTime() { return variantAvgResponseTime; }
        public Map<String, Long> getIntentDistribution() { return new HashMap<>(intentDistribution); }
        public int getPositiveFeedbackControl() { return positiveFeedbackControl; }
        public int getPositiveFeedbackVariant() { return positiveFeedbackVariant; }
        public int getNegativeFeedbackControl() { return negativeFeedbackControl; }
        public int getNegativeFeedbackVariant() { return negativeFeedbackVariant; }
    }
}
