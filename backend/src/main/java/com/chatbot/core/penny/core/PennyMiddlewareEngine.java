package com.chatbot.core.penny.core;

import com.chatbot.core.penny.analytics.AnalyticsCollector;
import com.chatbot.core.penny.context.ContextManager;
import com.chatbot.core.penny.context.ConversationContext;
import com.chatbot.core.penny.repository.PennyConversationContextRepository;
import com.chatbot.core.penny.dto.request.MiddlewareRequest;
import com.chatbot.core.penny.dto.response.MiddlewareResponse;
import com.chatbot.core.penny.error.ErrorHandler;
import com.chatbot.core.penny.rules.CustomLogicEngine;
import com.chatbot.core.penny.service.IntentAnalyzer;
import com.chatbot.core.penny.routing.ProviderSelector;
import com.chatbot.core.penny.routing.dto.IntentAnalysisResult;
import com.chatbot.core.penny.routing.dto.ProviderSelection;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Core Penny Middleware Engine - Trái tim của hệ thống intelligent routing
 */
@Service
@Slf4j
public class PennyMiddlewareEngine {
    
    // New Penny services
    private final ContextManager contextManager;
    private final IntentAnalyzer intentAnalyzer;
    private final ProviderSelector providerSelector;
    private final ErrorHandler errorHandler;
    private final AnalyticsCollector analyticsCollector;
    private final CustomLogicEngine customLogicEngine;
    private final MeterRegistry meterRegistry;
    private final RedisTemplate<String, Object> redisTemplate;
    private final PennyConversationContextRepository contextRepository;
    
    // Metrics counters
    private final Counter pennyRequestsTotal;
    private final Counter pennyErrorsTotal;
    
    public PennyMiddlewareEngine(ContextManager contextManager,
                                IntentAnalyzer intentAnalyzer,
                                ProviderSelector providerSelector,
                                ErrorHandler errorHandler,
                                AnalyticsCollector analyticsCollector,
                                CustomLogicEngine customLogicEngine,
                                MeterRegistry meterRegistry,
                                RedisTemplate<String, Object> redisTemplate,
                                PennyConversationContextRepository contextRepository) {
        this.contextManager = contextManager;
        this.intentAnalyzer = intentAnalyzer;
        this.providerSelector = providerSelector;
        this.errorHandler = errorHandler;
        this.analyticsCollector = analyticsCollector;
        this.customLogicEngine = customLogicEngine;
        this.meterRegistry = meterRegistry;
        this.redisTemplate = redisTemplate;
        this.contextRepository = contextRepository;
        
        // Initialize metrics counters
        this.pennyRequestsTotal = Counter.builder("penny_requests_total")
            .description("Total number of Penny requests processed")
            .register(meterRegistry);
        
        this.pennyErrorsTotal = Counter.builder("penny_errors_total")
            .description("Total number of Penny processing errors")
            .register(meterRegistry);
    }
    
    /**
     * Process single message synchronously
     */
    public MiddlewareResponse processMessage(MiddlewareRequest request) {
        long startTime = System.currentTimeMillis();
        String requestId = generateRequestId();
        
        try {
            // Increment request counter
            pennyRequestsTotal.increment();
            
            log.info("🚀 [{}] Processing message from {} on {}", 
                requestId, request.getUserId(), request.getPlatform());
            
            // STEP 1: Validate request
            validateRequest(request);
            ConversationContext conversationContext = (ConversationContext) contextManager.loadContext(request);
            
            // STEP 2: Analyze intent và entities
            IntentAnalysisResult analysis = intentAnalyzer.analyze(request, conversationContext);
            
            // STEP 3: Try custom logic first
            MiddlewareResponse customResponse = customLogicEngine.processWithCustomLogic(request, conversationContext, analysis);
            if (customResponse != null) {
                log.info("🎯 Custom logic processed message in {}ms", System.currentTimeMillis() - startTime);
                
                // STEP 4: Update context
                contextManager.updateContext(conversationContext, request, customResponse);
                
                // STEP 5: Collect analytics
                analyticsCollector.collectMetrics(request, customResponse, analysis, startTime);
                
                return customResponse;
            }
            
            // STEP 4: Select appropriate provider (fallback)
            ProviderSelection providerSelection = providerSelector.select(analysis, conversationContext);
            
            // STEP 5: Process with selected provider
            if (providerSelection.getProvider() == null) {
                log.warn("⚠️ [{}] Provider instance is null for type: {}. Using fallback response.", 
                    requestId, providerSelection.getProviderType());
                
                // Build fallback response
                MiddlewareResponse fallbackResponse = MiddlewareResponse.builder()
                    .requestId(generateRequestId())
                    .response("Xin chào! Hiện tại hệ thống đang gặp sự cố kỹ thuật. Vui lòng thử lại sau.")
                    .providerUsed("FALLBACK")
                    .intentAnalysis(analysis)
                    .processingMetrics(com.chatbot.core.penny.dto.response.MiddlewareResponse.ProcessingMetrics.builder()
                        .providerType("FALLBACK")
                        .selectionReason("Provider instance null")
                        .confidence(0.0)
                        .build())
                    .timestamp(Instant.now())
                    .build();
                
                // STEP 6: Update context
                contextManager.updateContext(conversationContext, request, fallbackResponse);
                
                // STEP 7: Collect analytics
                analyticsCollector.collectMetrics(request, fallbackResponse, analysis, startTime);
                
                return fallbackResponse;
            }
            
            Object providerResponse = providerSelection.getProvider()
                .sendMessage(request.getBotId(), request.getUserId(), request.getMessage());
            
            // STEP 6: Build final response
            MiddlewareResponse response = buildResponse(providerResponse, analysis, providerSelection);
            
            // STEP 7: Update context
            contextManager.updateContext(conversationContext, request, response);
            
            // STEP 8: Collect analytics
            analyticsCollector.collectMetrics(request, response, analysis, startTime);
            
            log.info("✅ [{}] Message processed successfully in {}ms", 
                requestId, System.currentTimeMillis() - startTime);
                
            return response;
            
        } catch (Exception e) {
            // Increment error counter
            pennyErrorsTotal.increment();
            log.error("❌ [{}] Error processing message: {}", requestId, e.getMessage(), e);
            return errorHandler.handleError(e, request, startTime);
        }
    }
    
    /**
     * Process message with streaming response for real-time updates
     */
    public void processMessageStream(MiddlewareRequest request, ResponseEmitter emitter) {
        String requestId = generateRequestId();
        
        CompletableFuture.runAsync(() -> {
            try {
                // Send processing started event
                emitter.send("{\"status\":\"processing\",\"requestId\":\"" + requestId + "\"}");
                
                // Process message with intermediate updates
                processWithStreamingUpdates(request, emitter, requestId);
                
                // Send completion event
                emitter.send("{\"status\":\"completed\",\"requestId\":\"" + requestId + "\"}");
                    
                emitter.complete();
                
            } catch (Exception e) {
                log.error("❌ [{}] Streaming error: {}", requestId, e.getMessage(), e);
                emitter.completeWithError(e);
            }
        });
    }
    
    /**
     * Get health status of middleware engine
     */
    public EngineHealthStatus getHealthStatus() {
        Map<String, ComponentHealth> components = checkComponents();
        String overall = components.values().stream()
            .map(ComponentHealth::getStatus)
            .anyMatch("unhealthy"::equals) ? "unhealthy"
            : components.values().stream()
                .map(ComponentHealth::getStatus)
                .anyMatch("degraded"::equals) ? "degraded" : "healthy";
        return EngineHealthStatus.builder()
            .status(overall)
            .timestamp(Instant.now())
            .components(components)
            .build();
    }
    
    /**
     * Get engine metrics
     */
    public EngineMetrics getEngineMetrics() {
        AnalyticsCollector.EngineMetrics analyticsMetrics = analyticsCollector.getEngineMetrics();
        
        EngineMetrics metrics = new EngineMetrics();
        metrics.setTotalProcessed(analyticsMetrics.getTotalProcessed());
        metrics.setTotalErrors(analyticsMetrics.getTotalErrors());
        metrics.setErrorRate(analyticsMetrics.getErrorRate());
        metrics.setAverageProcessingTime(analyticsMetrics.getAverageProcessingTime());
        metrics.setProviderUsage(analyticsMetrics.getProviderUsage());
        metrics.setIntentCounts(analyticsMetrics.getIntentCounts());
        metrics.setTimestamp(analyticsMetrics.getTimestamp());
        
        return metrics;
    }
    
    // Private helper methods
    
    private void validateRequest(MiddlewareRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }
        if (request.getUserId() == null || request.getUserId().trim().isEmpty()) {
            throw new IllegalArgumentException("User ID is required");
        }
        if (request.getMessage() == null || request.getMessage().trim().isEmpty()) {
            throw new IllegalArgumentException("Message cannot be empty");
        }
        if (request.getPlatform() == null) {
            throw new IllegalArgumentException("Platform is required");
        }
    }
    
    private String generateRequestId() {
        return "penny-" + UUID.randomUUID().toString().substring(0, 8);
    }
    
    private MiddlewareResponse buildResponse(Object providerResponse, 
                                           IntentAnalysisResult analysis,
                                           ProviderSelection providerSelection) {
        return MiddlewareResponse.builder()
            .requestId(generateRequestId())
            .response(extractResponseText(providerResponse))
            .providerUsed(providerSelection.getProviderType().toString())
            .intentAnalysis(analysis)
            .processingMetrics(com.chatbot.core.penny.dto.response.MiddlewareResponse.ProcessingMetrics.builder()
                .providerType(providerSelection.getProviderType().toString())
                .selectionReason(providerSelection.getSelectionReason())
                .confidence(analysis.getConfidence())
                .build())
            .timestamp(Instant.now())
            .build();
    }
    
    private String extractResponseText(Object providerResponse) {
        // Extract response text from provider response
        // Support multiple provider response formats
        if (providerResponse instanceof Map) {
            Map<?, ?> responseMap = (Map<?, ?>) providerResponse;
            
            // Try PennyBotProviderService format: {response: "..."}
            Object response = responseMap.get("response");
            if (response != null) {
                return response.toString();
            }
            
            // Try Botpress format: {payload: {text: "..."}}
            Object payload = responseMap.get("payload");
            if (payload instanceof Map) {
                Object text = ((Map<?, ?>) payload).get("text");
                if (text != null) {
                    return text.toString();
                }
            }
            
            // Try direct text format: {text: "..."}
            Object text = responseMap.get("text");
            if (text != null) {
                return text.toString();
            }
        }
        return "Xin lỗi, tôi không hiểu yêu cầu của bạn.";
    }
    
    private void processWithStreamingUpdates(MiddlewareRequest request, 
                                          ResponseEmitter emitter, 
                                          String requestId) {
        try {
            // Send intent analysis update
            emitter.send("{\"status\":\"analyzing\",\"requestId\":\"" + requestId + "\"}");
            
            // Process message
            MiddlewareResponse response = processMessage(request);
            
            // Send final response
            emitter.send(response.toJson());
                
        } catch (Exception e) {
            try {
                emitter.send("{\"error\":\"" + e.getMessage() + "\",\"requestId\":\"" + requestId + "\"}");
            } catch (Exception ex) {
                log.warn("⚠️ [{}] Failed to send streaming error response: {}", requestId, ex.getMessage());
            }
        }
    }
    
    private java.util.Map<String, ComponentHealth> checkComponents() {
        java.util.Map<String, ComponentHealth> components = new java.util.HashMap<>();
        
        // Context manager: real probe of Redis + context DB
        components.put("contextManager", checkContextStorage());
            
        // Intent analyzer: in-process component, healthy if bean is wired
        components.put("intentAnalyzer", ComponentHealth.builder()
            .status(intentAnalyzer != null ? "healthy" : "unhealthy")
            .lastCheck(Instant.now())
            .message(intentAnalyzer != null ? "In-process analyzer" : "Bean not initialized")
            .build());
            
        // Provider selector: in-process component, healthy if bean is wired
        components.put("providerSelector", ComponentHealth.builder()
            .status(providerSelector != null ? "healthy" : "unhealthy")
            .lastCheck(Instant.now())
            .message(providerSelector != null ? "In-process selector" : "Bean not initialized")
            .build());
        
        return components;
    }
    
    /**
     * Probe Redis connectivity and context DB reachability.
     * degraded = one storage down (fallback may still work),
     * unhealthy = both down.
     */
    private ComponentHealth checkContextStorage() {
        boolean redisOk = false;
        boolean dbOk = false;
        StringBuilder message = new StringBuilder();
        
        try {
            String pong = redisTemplate.getConnectionFactory()
                .getConnection().ping();
            redisOk = "PONG".equalsIgnoreCase(pong);
        } catch (Exception e) {
            message.append("redis: ").append(e.getMessage()).append("; ");
        }
        
        try {
            contextRepository.count();
            dbOk = true;
        } catch (Exception e) {
            message.append("db: ").append(e.getMessage());
        }
        
        String status = (redisOk && dbOk) ? "healthy" : (!redisOk && !dbOk) ? "unhealthy" : "degraded";
        return ComponentHealth.builder()
            .status(status)
            .lastCheck(Instant.now())
            .message(message.length() > 0 ? message.toString() : "redis+db ok")
            .build();
    }
    
    // Inner classes
    
    @lombok.Data
    @lombok.Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EngineHealthStatus {
        private String status;
        private Instant timestamp;
        private Map<String, ComponentHealth> components;
    }
    
    @lombok.Data
    @lombok.Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComponentHealth {
        private String status;
        private Instant lastCheck;
        private String message;
    }
    
    @lombok.Data
    @lombok.Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EngineMetrics {
        private long totalProcessed;
        private long totalErrors;
        private double errorRate;
        private double averageProcessingTime;
        private Map<String, Long> providerUsage;
        private Map<String, Long> intentCounts;
        private Instant timestamp;
    }
    
    /**
     * Response emitter for streaming responses
     */
    public interface ResponseEmitter {
        void send(Object data) throws java.io.IOException;
        void complete();
        void completeWithError(Throwable throwable);
    }
}
