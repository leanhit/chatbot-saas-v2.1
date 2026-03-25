package com.chatbot.core.message.router.service;

import com.chatbot.core.message.router.model.Destination;
import com.chatbot.core.message.router.model.Route;
import com.chatbot.core.message.decision.service.HubSelectionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Message Router Service - Routes messages to appropriate hubs
 */
@Service
@Slf4j
public class MessageRouterService {
    
    @Autowired
    private RoutingDecisionService routingDecisionService;
    
    @Autowired
    private HubSelectionService hubSelectionService;
    
    private final ExecutorService routingExecutor = Executors.newFixedThreadPool(10);
    
    /**
     * Route message to appropriate destination
     */
    public CompletableFuture<RoutingResult> routeMessage(String messageId, String content, Map<String, Object> context) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("🚀 [MessageRouter] Starting routing for message: {}", messageId);
                
                // Make routing decision
                var decision = routingDecisionService.makeRoutingDecision(messageId, content, context);
                
                // Execute routing
                RoutingResult result = executeRouting(decision, context);
                
                log.info("✅ [MessageRouter] Routing completed for {}: {} -> {} (success: {})", 
                         messageId, decision.getDestination().getName(), 
                         result.getActualDestination(), result.isSuccess());
                
                return result;
                
            } catch (Exception e) {
                log.error("❌ [MessageRouter] Routing failed for message: {}", messageId, e);
                return RoutingResult.builder()
                        .messageId(messageId)
                        .success(false)
                        .errorMessage(e.getMessage())
                        .build();
            }
        }, routingExecutor);
    }
    
    /**
     * Route message to specific destination (legacy method)
     */
    public void routeMessage(String messageId, String destination) {
        try {
            log.info("🔄 [MessageRouter] Legacy routing message {} to {}", messageId, destination);
            
            // Create basic context
            Map<String, Object> context = Map.of("destination", destination);
            
            // Route using new method
            routeMessage(messageId, "", context);
            
        } catch (Exception e) {
            log.error("❌ [MessageRouter] Legacy routing failed for message: {}", messageId, e);
        }
    }
    
    /**
     * Execute routing based on decision
     */
    private RoutingResult executeRouting(RoutingDecisionService.RoutingDecision decision, Map<String, Object> context) {
        RoutingResult.RoutingResultBuilder resultBuilder = RoutingResult.builder()
                .messageId(decision.getMessageId())
                .destination(decision.getDestination())
                .confidence(decision.getConfidence());
        
        try {
            // Get destination from decision
            Destination destination = decision.getDestination();
            
            // Route based on destination type
            boolean success = routeToDestination(destination, decision, context);
            
            resultBuilder.success(success);
            resultBuilder.actualDestination(destination.getName());
            
            if (!success) {
                // Try fallback routing
                success = handleFallbackRouting(decision, context);
                resultBuilder.success(success);
                resultBuilder.usedFallback(true);
            }
            
            return resultBuilder.build();
            
        } catch (Exception e) {
            log.error("❌ [MessageRouter] Execution failed for message: {}", decision.getMessageId(), e);
            resultBuilder.success(false);
            resultBuilder.errorMessage(e.getMessage());
            return resultBuilder.build();
        }
    }
    
    /**
     * Route to specific destination
     */
    private boolean routeToDestination(Destination destination, RoutingDecisionService.RoutingDecision decision, Map<String, Object> context) {
        try {
            log.debug("🎯 [MessageRouter] Routing to destination: {} ({})", destination.getName(), destination.getDestinationType());
            
            switch (destination.getDestinationType()) {
                case HUB:
                    return routeToHub(destination, decision, context);
                case SERVICE:
                    return routeToService(destination, decision, context);
                case EXTERNAL:
                    return routeToExternal(destination, decision, context);
                case WEBHOOK:
                    return routeToWebhook(destination, decision, context);
                case QUEUE:
                    return routeToQueue(destination, decision, context);
                default:
                    log.warn("⚠️ [MessageRouter] Unknown destination type: {}", destination.getDestinationType());
                    return false;
            }
            
        } catch (Exception e) {
            log.error("❌ [MessageRouter] Failed to route to destination: {}", destination.getName(), e);
            return false;
        }
    }
    
    /**
     * Route to internal hub
     */
    private boolean routeToHub(Destination destination, RoutingDecisionService.RoutingDecision decision, Map<String, Object> context) {
        try {
            String hubName = destination.getName();
            log.debug("🔗 [MessageRouter] Routing to hub: {}", hubName);
            
            // Use hub selection service for intelligent routing
            var processingContext = createProcessingContext(decision, context);
            var hubSelection = hubSelectionService.selectHub(processingContext);
            
            // Route to selected hub
            return executeHubRouting(hubSelection, decision, context);
            
        } catch (Exception e) {
            log.error("❌ [MessageRouter] Hub routing failed: {}", destination.getName(), e);
            return false;
        }
    }
    
    /**
     * Route to microservice
     */
    private boolean routeToService(Destination destination, RoutingDecisionService.RoutingDecision decision, Map<String, Object> context) {
        try {
            log.debug("🔧 [MessageRouter] Routing to service: {}", destination.getEndpoint());
            
            // Implementation would call the microservice via HTTP/gRPC
            // For now, simulate success
            return true;
            
        } catch (Exception e) {
            log.error("❌ [MessageRouter] Service routing failed: {}", destination.getName(), e);
            return false;
        }
    }
    
    /**
     * Route to external API
     */
    private boolean routeToExternal(Destination destination, RoutingDecisionService.RoutingDecision decision, Map<String, Object> context) {
        try {
            log.debug("🌐 [MessageRouter] Routing to external API: {}", destination.getEndpoint());
            
            // Implementation would make HTTP call to external API
            // For now, simulate success
            return true;
            
        } catch (Exception e) {
            log.error("❌ [MessageRouter] External routing failed: {}", destination.getName(), e);
            return false;
        }
    }
    
    /**
     * Route to webhook
     */
    private boolean routeToWebhook(Destination destination, RoutingDecisionService.RoutingDecision decision, Map<String, Object> context) {
        try {
            log.debug("🪝 [MessageRouter] Routing to webhook: {}", destination.getEndpoint());
            
            // Implementation would trigger webhook
            // For now, simulate success
            return true;
            
        } catch (Exception e) {
            log.error("❌ [MessageRouter] Webhook routing failed: {}", destination.getName(), e);
            return false;
        }
    }
    
    /**
     * Route to message queue
     */
    private boolean routeToQueue(Destination destination, RoutingDecisionService.RoutingDecision decision, Map<String, Object> context) {
        try {
            log.debug("📬 [MessageRouter] Routing to queue: {}", destination.getEndpoint());
            
            // Implementation would send message to queue
            // For now, simulate success
            return true;
            
        } catch (Exception e) {
            log.error("❌ [MessageRouter] Queue routing failed: {}", destination.getName(), e);
            return false;
        }
    }
    
    /**
     * Handle fallback routing
     */
    private boolean handleFallbackRouting(RoutingDecisionService.RoutingDecision decision, Map<String, Object> context) {
        try {
            log.info("🔄 [MessageRouter] Attempting fallback routing for message: {}", decision.getMessageId());
            
            // Create fallback destination (message hub)
            Destination fallbackDestination = Destination.builder()
                    .name("message-hub-fallback")
                    .endpoint("message-store")
                    .destinationType(Destination.DestinationType.HUB)
                    .connectionMethod(Destination.ConnectionMethod.GRPC)
                    .isActive(true)
                    .timeout(30000)
                    .retryCount(3)
                    .build();
            
            return routeToHub(fallbackDestination, decision, context);
            
        } catch (Exception e) {
            log.error("❌ [MessageRouter] Fallback routing failed for message: {}", decision.getMessageId(), e);
            return false;
        }
    }
    
    /**
     * Execute hub routing
     */
    private boolean executeHubRouting(HubSelectionService.HubSelection hubSelection, RoutingDecisionService.RoutingDecision decision, Map<String, Object> context) {
        try {
            String hubName = hubSelection.getSelectedHub().getName();
            log.debug("🎯 [MessageRouter] Executing hub routing to: {} (confidence: {})", hubName, hubSelection.getConfidence());
            
            // Implementation would route to the specific hub
            // For now, simulate success based on confidence
            return hubSelection.getConfidence() > 0.5;
            
        } catch (Exception e) {
            log.error("❌ [MessageRouter] Hub routing execution failed", e);
            return false;
        }
    }
    
    /**
     * Create processing context for hub selection
     */
    private com.chatbot.core.message.decision.model.ProcessingContext createProcessingContext(RoutingDecisionService.RoutingDecision decision, Map<String, Object> context) {
        com.chatbot.core.message.decision.model.ProcessingContext processingContext = new com.chatbot.core.message.decision.model.ProcessingContext();
        
        processingContext.setMessageId(decision.getMessageId());
        processingContext.setContent(decision.getAnalysis().getContent());
        processingContext.setMessageType("text"); // Default
        processingContext.setUserId(decision.getAnalysis().getUserId());
        processingContext.setConversationId((String) context.getOrDefault("conversationId", "default"));
        processingContext.setSystemData(context);
        
        return processingContext;
    }
    
    /**
     * Shutdown hook
     */
    public void shutdown() {
        try {
            log.info("🛑 [MessageRouter] Shutting down routing executor...");
            routingExecutor.shutdown();
            routingExecutor.awaitTermination(30, TimeUnit.SECONDS);
            log.info("✅ [MessageRouter] Shutdown completed");
        } catch (InterruptedException e) {
            log.error("❌ [MessageRouter] Shutdown interrupted", e);
            Thread.currentThread().interrupt();
        }
    }
    
    // Data classes
    public static class RoutingResult {
        private String messageId;
        private Destination destination;
        private String actualDestination;
        private boolean success;
        private double confidence;
        private boolean usedFallback;
        private String errorMessage;
        private long timestamp;
        
        public static RoutingResultBuilder builder() {
            return new RoutingResultBuilder();
        }
        
        // Getters and setters
        public String getMessageId() { return messageId; }
        public void setMessageId(String messageId) { this.messageId = messageId; }
        
        public Destination getDestination() { return destination; }
        public void setDestination(Destination destination) { this.destination = destination; }
        
        public String getActualDestination() { return actualDestination; }
        public void setActualDestination(String actualDestination) { this.actualDestination = actualDestination; }
        
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        
        public double getConfidence() { return confidence; }
        public void setConfidence(double confidence) { this.confidence = confidence; }
        
        public boolean isUsedFallback() { return usedFallback; }
        public void setUsedFallback(boolean usedFallback) { this.usedFallback = usedFallback; }
        
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
        
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
        
        public static class RoutingResultBuilder {
            private final RoutingResult result = new RoutingResult();
            
            public RoutingResultBuilder messageId(String messageId) {
                result.messageId = messageId;
                return this;
            }
            
            public RoutingResultBuilder destination(Destination destination) {
                result.destination = destination;
                return this;
            }
            
            public RoutingResultBuilder actualDestination(String actualDestination) {
                result.actualDestination = actualDestination;
                return this;
            }
            
            public RoutingResultBuilder success(boolean success) {
                result.success = success;
                return this;
            }
            
            public RoutingResultBuilder confidence(double confidence) {
                result.confidence = confidence;
                return this;
            }
            
            public RoutingResultBuilder usedFallback(boolean usedFallback) {
                result.usedFallback = usedFallback;
                return this;
            }
            
            public RoutingResultBuilder errorMessage(String errorMessage) {
                result.errorMessage = errorMessage;
                return this;
            }
            
            public RoutingResult build() {
                result.timestamp = System.currentTimeMillis();
                return result;
            }
        }
    }
}
