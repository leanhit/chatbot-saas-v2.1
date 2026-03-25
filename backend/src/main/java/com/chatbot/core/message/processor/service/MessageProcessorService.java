package com.chatbot.core.message.processor.service;

import com.chatbot.core.message.processor.model.ProcessingStep;
import com.chatbot.core.message.processor.model.ProcessorConfig;
import com.chatbot.core.message.processor.service.MessageValidator.ValidationResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Message Processor Service - Processes and transforms messages
 */
@Service
@Slf4j
public class MessageProcessorService {
    
    @Autowired
    private MessageTransformer messageTransformer;
    
    @Autowired
    private MessageValidator messageValidator;
    
    private final ExecutorService processingExecutor = Executors.newFixedThreadPool(20);
    
    /**
     * Process message with full pipeline
     */
    public CompletableFuture<ProcessingResult> processMessage(String rawMessage, Map<String, Object> context) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("⚙️ [MessageProcessor] Starting processing for message");
                
                ProcessingResult result = new ProcessingResult();
                result.setOriginalMessage(rawMessage);
                result.setContext(context);
                
                // Step 1: Validation
                ValidationResult validationResult = messageValidator.validateMessage(rawMessage, context);
                result.setValidationResult(validationResult);
                
                if (!validationResult.isValid()) {
                    log.warn("❌ [MessageProcessor] Message validation failed: {}", validationResult.getErrors());
                    result.setSuccess(false);
                    result.setErrorMessage("Validation failed: " + String.join(", ", validationResult.getErrors()));
                    return result;
                }
                
                // Step 2: Transformation
                String transformedMessage = messageTransformer.transformMessage(rawMessage, context);
                result.setTransformedMessage(transformedMessage);
                
                // Step 3: Entity extraction
                Map<String, Object> entities = messageTransformer.extractEntities(transformedMessage);
                result.setEntities(entities);
                
                // Step 4: Enrichment
                Map<String, Object> enrichedContext = enrichContext(transformedMessage, entities, context);
                result.setEnrichedContext(enrichedContext);
                
                // Step 5: Processing steps
                List<ProcessingStep> processingSteps = createProcessingSteps(rawMessage, transformedMessage, entities);
                result.setProcessingSteps(processingSteps);
                
                result.setSuccess(true);
                result.setProcessedMessage(transformedMessage);
                
                log.info("✅ [MessageProcessor] Processing completed successfully");
                return result;
                
            } catch (Exception e) {
                log.error("❌ [MessageProcessor] Processing failed", e);
                
                ProcessingResult result = new ProcessingResult();
                result.setOriginalMessage(rawMessage);
                result.setContext(context);
                result.setSuccess(false);
                result.setErrorMessage(e.getMessage());
                
                return result;
            }
        }, processingExecutor);
    }
    
    /**
     * Legacy method for simple processing
     */
    public String processMessage(String rawMessage) {
        try {
            log.info("🔄 [MessageProcessor] Legacy processing for message");
            
            Map<String, Object> context = new HashMap<>();
            context.put("channel", "legacy");
            context.put("messageType", "text");
            
            ProcessingResult result = processMessage(rawMessage, context).get(5, TimeUnit.SECONDS);
            
            return result.isSuccess() ? result.getProcessedMessage() : rawMessage;
            
        } catch (Exception e) {
            log.error("❌ [MessageProcessor] Legacy processing failed", e);
            return rawMessage; // Return original on failure
        }
    }
    
    /**
     * Process message batch
     */
    public CompletableFuture<List<ProcessingResult>> processMessageBatch(List<String> messages, Map<String, Object> context) {
        return CompletableFuture.supplyAsync(() -> {
            List<ProcessingResult> results = new ArrayList<>();
            
            for (String message : messages) {
                try {
                    ProcessingResult result = processMessage(message, context).get(5, TimeUnit.SECONDS);
                    results.add(result);
                } catch (Exception e) {
                    log.error("❌ [MessageProcessor] Batch processing failed for message", e);
                    
                    ProcessingResult failedResult = new ProcessingResult();
                    failedResult.setOriginalMessage(message);
                    failedResult.setContext(context);
                    failedResult.setSuccess(false);
                    failedResult.setErrorMessage(e.getMessage());
                    
                    results.add(failedResult);
                }
            }
            
            log.info("✅ [MessageProcessor] Batch processing completed: {} messages", results.size());
            return results;
        }, processingExecutor);
    }
    
    /**
     * Enrich message context
     */
    private Map<String, Object> enrichContext(String message, Map<String, Object> entities, Map<String, Object> originalContext) {
        Map<String, Object> enriched = new HashMap<>(originalContext);
        
        // Add processing metadata
        enriched.put("processedAt", System.currentTimeMillis());
        enriched.put("messageLength", message.length());
        enriched.put("wordCount", message.split("\\s+").length);
        
        // Add extracted entities
        enriched.putAll(entities);
        
        // Add analysis results
        enriched.put("sentiment", analyzeSentiment(message));
        enriched.put("intent", detectIntent(message));
        enriched.put("language", detectLanguage(message));
        enriched.put("urgency", assessUrgency(message, entities));
        
        // Add processing flags
        enriched.put("hasUrls", entities.containsKey("hasUrls") && (Boolean) entities.get("hasUrls"));
        enriched.put("hasEmails", entities.containsKey("hasEmails") && (Boolean) entities.get("hasEmails"));
        enriched.put("hasPhones", entities.containsKey("hasPhones") && (Boolean) entities.get("hasPhones"));
        
        return enriched;
    }
    
    /**
     * Create processing steps
     */
    private List<ProcessingStep> createProcessingSteps(String original, String transformed, Map<String, Object> entities) {
        List<ProcessingStep> steps = new ArrayList<>();
        
        // Validation step
        steps.add(ProcessingStep.builder()
                .stepName("validation")
                .status("completed")
                .startTime(java.time.LocalDateTime.now())
                .build());
        
        // Transformation step
        steps.add(ProcessingStep.builder()
                .stepName("transformation")
                .status("completed")
                .startTime(java.time.LocalDateTime.now())
                .inputData(original)
                .outputData(transformed)
                .build());
        
        // Entity extraction step
        steps.add(ProcessingStep.builder()
                .stepName("entity_extraction")
                .status("completed")
                .startTime(java.time.LocalDateTime.now())
                .metadata(entities)
                .build());
        
        // Enrichment step
        steps.add(ProcessingStep.builder()
                .stepName("enrichment")
                .status("completed")
                .startTime(java.time.LocalDateTime.now())
                .build());
        
        return steps;
    }
    
    /**
     * Analyze message sentiment
     */
    private String analyzeSentiment(String message) {
        String lowerMessage = message.toLowerCase();
        
        if (lowerMessage.contains("angry") || lowerMessage.contains("frustrated") || 
            lowerMessage.contains("upset") || lowerMessage.contains("disappointed") ||
            lowerMessage.contains("terrible") || lowerMessage.contains("awful")) {
            return "negative";
        }
        
        if (lowerMessage.contains("happy") || lowerMessage.contains("good") || 
            lowerMessage.contains("great") || lowerMessage.contains("excellent") ||
            lowerMessage.contains("love") || lowerMessage.contains("awesome")) {
            return "positive";
        }
        
        return "neutral";
    }
    
    /**
     * Detect message intent
     */
    private String detectIntent(String message) {
        String lowerMessage = message.toLowerCase();
        
        if (lowerMessage.contains("?") || lowerMessage.contains("how") || 
            lowerMessage.contains("what") || lowerMessage.contains("when") ||
            lowerMessage.contains("why") || lowerMessage.contains("where")) {
            return "question";
        }
        
        if (lowerMessage.contains("help") || lowerMessage.contains("support") ||
            lowerMessage.contains("assist") || lowerMessage.contains("problem")) {
            return "help_request";
        }
        
        if (lowerMessage.contains("hello") || lowerMessage.contains("hi") ||
            lowerMessage.contains("hey") || lowerMessage.contains("greetings")) {
            return "greeting";
        }
        
        if (lowerMessage.contains("bye") || lowerMessage.contains("goodbye") ||
            lowerMessage.contains("farewell") || lowerMessage.contains("see you")) {
            return "farewell";
        }
        
        if (lowerMessage.contains("thank") || lowerMessage.contains("thanks") ||
            lowerMessage.contains("appreciate")) {
            return "gratitude";
        }
        
        return "statement";
    }
    
    /**
     * Detect message language
     */
    private String detectLanguage(String message) {
        // Simple language detection based on character patterns
        if (message.matches(".*[àáạảãâầấậẩẫăằắặẳẵèéẹẻẽêềếệểễìíịỉĩòóọỏõôồốộổỗơờớợởỡùúụủũưừứựửữỳýỵỷỹ].*")) {
            return "vietnamese";
        }
        
        if (message.matches(".*[ñáéíóúü].*")) {
            return "spanish";
        }
        
        if (message.matches(".*[àâäçéèêëïîôöùûüÿ].*")) {
            return "french";
        }
        
        if (message.matches(".*[äöüß].*")) {
            return "german";
        }
        
        return "english"; // Default
    }
    
    /**
     * Assess message urgency
     */
    private String assessUrgency(String message, Map<String, Object> entities) {
        String lowerMessage = message.toLowerCase();
        
        // Urgent keywords
        if (lowerMessage.contains("urgent") || lowerMessage.contains("emergency") ||
            lowerMessage.contains("asap") || lowerMessage.contains("immediately") ||
            lowerMessage.contains("critical") || lowerMessage.contains("important")) {
            return "high";
        }
        
        // Time-sensitive indicators
        if (lowerMessage.contains("today") || lowerMessage.contains("now") ||
            lowerMessage.contains("soon") || lowerMessage.contains("quickly")) {
            return "medium";
        }
        
        // Contact information (might indicate urgent need)
        if ((entities.containsKey("hasPhones") && (Boolean) entities.get("hasPhones")) ||
            (entities.containsKey("hasEmails") && (Boolean) entities.get("hasEmails"))) {
            return "medium";
        }
        
        return "low";
    }
    
    /**
     * Shutdown hook
     */
    public void shutdown() {
        try {
            log.info("🛑 [MessageProcessor] Shutting down processing executor...");
            processingExecutor.shutdown();
            processingExecutor.awaitTermination(30, TimeUnit.SECONDS);
            log.info("✅ [MessageProcessor] Shutdown completed");
        } catch (InterruptedException e) {
            log.error("❌ [MessageProcessor] Shutdown interrupted", e);
            Thread.currentThread().interrupt();
        }
    }
    
    // Data classes
    public static class ProcessingResult {
        private String originalMessage;
        private String processedMessage;
        private String transformedMessage;
        private Map<String, Object> context;
        private Map<String, Object> enrichedContext;
        private Map<String, Object> entities;
        private ValidationResult validationResult;
        private List<ProcessingStep> processingSteps;
        private boolean success;
        private String errorMessage;
        private long timestamp;
        
        // Getters and setters
        public String getOriginalMessage() { return originalMessage; }
        public void setOriginalMessage(String originalMessage) { this.originalMessage = originalMessage; }
        
        public String getProcessedMessage() { return processedMessage; }
        public void setProcessedMessage(String processedMessage) { this.processedMessage = processedMessage; }
        
        public String getTransformedMessage() { return transformedMessage; }
        public void setTransformedMessage(String transformedMessage) { this.transformedMessage = transformedMessage; }
        
        public Map<String, Object> getContext() { return context; }
        public void setContext(Map<String, Object> context) { this.context = context; }
        
        public Map<String, Object> getEnrichedContext() { return enrichedContext; }
        public void setEnrichedContext(Map<String, Object> enrichedContext) { this.enrichedContext = enrichedContext; }
        
        public Map<String, Object> getEntities() { return entities; }
        public void setEntities(Map<String, Object> entities) { this.entities = entities; }
        
        public ValidationResult getValidationResult() { return validationResult; }
        public void setValidationResult(ValidationResult validationResult) { this.validationResult = validationResult; }
        
        public List<ProcessingStep> getProcessingSteps() { return processingSteps; }
        public void setProcessingSteps(List<ProcessingStep> processingSteps) { this.processingSteps = processingSteps; }
        
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
        
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    }
}
