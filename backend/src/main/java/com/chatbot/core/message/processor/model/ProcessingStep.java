package com.chatbot.core.message.processor.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Processing step model for message processing pipeline
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcessingStep {
    
    private Long id;
    private String messageId;
    private String stepName;
    private String stepType;
    private String status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long duration;
    private String inputData;
    private String outputData;
    private String errorMessage;
    private Integer retryCount;
    private Map<String, Object> metadata;
    
    // Step types
    public enum StepType {
        VALIDATION,        // Message validation
        TRANSFORMATION,     // Message transformation
        ROUTING,           // Message routing
        ENRICHMENT,        // Data enrichment
        FILTERING,         // Content filtering
        ANALYSIS,          // Message analysis
        PROCESSING,        // Main processing
        DELIVERY,          // Message delivery
        ARCHIVAL           // Message archival
    }
    
    private StepType type;
    
    // Step status
    public enum StepStatus {
        PENDING,           // Waiting to start
        RUNNING,           // Currently running
        COMPLETED,         // Successfully completed
        FAILED,            // Failed with error
        SKIPPED,           // Skipped due to conditions
        RETRYING,          // Retrying after failure
        TIMEOUT           // Timed out
    }
    
    private StepStatus stepStatus;
    
    // Constructor with initialization
    public ProcessingStep(String messageId, String stepName, StepType type) {
        this.messageId = messageId;
        this.stepName = stepName;
        this.type = type;
        this.stepStatus = StepStatus.PENDING;
        this.startTime = LocalDateTime.now();
        this.retryCount = 0;
        this.metadata = new java.util.HashMap<>();
    }
    
    // Utility methods
    public void start() {
        this.stepStatus = StepStatus.RUNNING;
        this.startTime = LocalDateTime.now();
    }
    
    public void complete(String outputData) {
        this.stepStatus = StepStatus.COMPLETED;
        this.endTime = LocalDateTime.now();
        this.outputData = outputData;
        calculateDuration();
    }
    
    public void fail(String errorMessage) {
        this.stepStatus = StepStatus.FAILED;
        this.endTime = LocalDateTime.now();
        this.errorMessage = errorMessage;
        calculateDuration();
    }
    
    public void retry() {
        this.retryCount++;
        this.stepStatus = StepStatus.RETRYING;
        this.startTime = LocalDateTime.now();
    }
    
    public void skip(String reason) {
        this.stepStatus = StepStatus.SKIPPED;
        this.endTime = LocalDateTime.now();
        this.metadata.put("skipReason", reason);
        calculateDuration();
    }
    
    private void calculateDuration() {
        if (startTime != null && endTime != null) {
            this.duration = java.time.Duration.between(startTime, endTime).toMillis();
        }
    }
    
    public boolean isCompleted() {
        return stepStatus == StepStatus.COMPLETED;
    }
    
    public boolean isFailed() {
        return stepStatus == StepStatus.FAILED;
    }
    
    public boolean isRunning() {
        return stepStatus == StepStatus.RUNNING;
    }
}
