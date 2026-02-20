package com.chatbot.core.message.decision.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Decision model for message processing decisions
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Decision {
    
    private Long id;
    private String messageId;
    private String conversationId;
    private String decisionType;
    private String selectedHub;
    private String selectedService;
    private String reason;
    private Map<String, Object> context;
    private LocalDateTime createdAt;
    private LocalDateTime executedAt;
    private Boolean isExecuted;
    private Integer executionTime;
    
    // Decision types
    public enum DecisionType {
        ROUTE,           // Routing decision
        TAKEOVER,         // Takeover decision
        TRANSFORM,         // Transformation decision
        VALIDATE,         // Validation decision
        ESCALATE,         // Escalation decision
        BLOCK,            // Block decision
        FORWARD           // Forward decision
    }
    
    // Decision status
    public enum DecisionStatus {
        PENDING,          // Waiting for execution
        EXECUTED,         // Successfully executed
        FAILED,           // Execution failed
        CANCELLED,        // Decision cancelled
        TIMEOUT           // Execution timeout
    }
    
    private DecisionStatus status;
    
    // Priority levels
    public enum Priority {
        LOW,              // Low priority
        MEDIUM,           // Medium priority
        HIGH,             // High priority
        CRITICAL          // Critical priority
    }
    
    private Priority priority;
}
