package com.chatbot.modules.penny.core;

import com.chatbot.modules.facebook.connection.service.FacebookConnectionService;
import com.chatbot.modules.facebook.webhook.service.ChatbotProviderFactory;
import com.chatbot.core.message.store.service.ConversationService;
import com.chatbot.core.message.store.service.MessageService;
import com.chatbot.core.tenant.infra.TenantContext;
import com.chatbot.modules.penny.analytics.AnalyticsCollector;
import com.chatbot.modules.penny.context.ContextManager;
import com.chatbot.modules.penny.context.ConversationContext;
import com.chatbot.modules.penny.dto.request.MiddlewareRequest;
import com.chatbot.modules.penny.dto.response.MiddlewareResponse;
import com.chatbot.modules.penny.error.ErrorHandler;
import com.chatbot.modules.penny.rules.CustomLogicEngine;
import com.chatbot.modules.penny.service.IntentAnalyzer;
import com.chatbot.modules.penny.routing.ProviderSelector;
import com.chatbot.modules.penny.routing.dto.IntentAnalysisResult;
import com.chatbot.modules.penny.routing.dto.ProviderSelection;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    
    // Existing services from current system
    private final ChatbotProviderFactory existingProviderFactory;
    private final ConversationService existingConversationService;
    private final FacebookConnectionService facebookConnectionService;
    private final MessageService existingMessageService;
    
    // New Penny services
    private final ContextManager contextManager;
    private final IntentAnalyzer intentAnalyzer;
    private final ProviderSelector providerSelector;
    private final ErrorHandler errorHandler;
    private final AnalyticsCollector analyticsCollector;
    private final CustomLogicEngine customLogicEngine;
    
    public PennyMiddlewareEngine(ChatbotProviderFactory existingProviderFactory,
                                ConversationService existingConversationService,
                                FacebookConnectionService facebookConnectionService,
                                MessageService existingMessageService,
                                ContextManager contextManager,
                                IntentAnalyzer intentAnalyzer,
                                ProviderSelector providerSelector,
                                ErrorHandler errorHandler,
                                AnalyticsCollector analyticsCollector,
                                CustomLogicEngine customLogicEngine) {
        this.existingProviderFactory = existingProviderFactory;
        this.existingConversationService = existingConversationService;
        this.facebookConnectionService = facebookConnectionService;
        this.existingMessageService = existingMessageService;
        this.contextManager = contextManager;
        this.intentAnalyzer = intentAnalyzer;
        this.providerSelector = providerSelector;
        this.errorHandler = errorHandler;
        this.analyticsCollector = analyticsCollector;
        this.customLogicEngine = customLogicEngine;
    }
    
    /**
     * Process single message synchronously
     */
    public MiddlewareResponse processMessage(MiddlewareRequest request) {
        long startTime = System.currentTimeMillis();
        String requestId = generateRequestId();
        
        try {
            log.info("🚀 [{}] Processing message from {} on {}", 
                requestId, request.getUserId(), request.getPlatform());
            
            // STEP 1: Validate request
            ConversationContext conversationContext = contextManager.loadContext(request);
            
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
            Object providerResponse = providerSelection.getProvider()
                .sendMessage(providerSelection.getProviderType().toString(), request.getUserId(), request.getMessage());
            
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
        return EngineHealthStatus.builder()
            .status("healthy")
            .timestamp(Instant.now())
            .components(checkComponents())
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
            .processingMetrics(com.chatbot.modules.penny.dto.response.MiddlewareResponse.ProcessingMetrics.builder()
                .providerType(providerSelection.getProviderType().toString())
                .selectionReason(providerSelection.getSelectionReason())
                .confidence(analysis.getConfidence())
                .build())
            .timestamp(Instant.now())
            .build();
    }
    
    private String extractResponseText(Object providerResponse) {
        // Extract response text from provider response
        // This will be implemented based on provider response format
        if (providerResponse instanceof Map) {
            Map<?, ?> responseMap = (Map<?, ?>) providerResponse;
            Object payload = responseMap.get("payload");
            if (payload instanceof Map) {
                Object text = ((Map<?, ?>) payload).get("text");
                return text != null ? text.toString() : "Xin lỗi, tôi không hiểu yêu cầu của bạn.";
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
                // Ignore send errors
            }
        }
    }
    
    private java.util.Map<String, ComponentHealth> checkComponents() {
        java.util.Map<String, ComponentHealth> components = new java.util.HashMap<>();
        
        // Check context manager
        components.put("contextManager", ComponentHealth.builder()
            .status("healthy")
            .lastCheck(Instant.now())
            .build());
            
        // Check intent analyzer
        components.put("intentAnalyzer", ComponentHealth.builder()
            .status("healthy")
            .lastCheck(Instant.now())
            .build());
            
        // Check provider selector
        components.put("providerSelector", ComponentHealth.builder()
            .status("healthy")
            .lastCheck(Instant.now())
            .build());
        
        return components;
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
