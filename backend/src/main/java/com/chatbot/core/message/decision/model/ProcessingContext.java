package com.chatbot.core.message.decision.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;

/**
 * Processing context for decision making
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcessingContext {
    
    private String messageId;
    private String conversationId;
    private String userId;
    private String sessionId;
    private String channel;
    private String messageType;
    private String content;
    private LocalDateTime timestamp;
    
    // Context data
    private Map<String, Object> userData;
    private Map<String, Object> conversationData;
    private Map<String, Object> sessionData;
    private Map<String, Object> systemData;
    
    // Processing metadata
    private Map<String, Object> metadata;
    private Map<String, String> tags;
    private Map<String, Double> scores;
    
    // Processing history
    private Map<String, LocalDateTime> processingHistory;
    private Map<String, String> previousDecisions;
    
    // Constructor with initialization
    public ProcessingContext(String messageId, String conversationId) {
        this.messageId = messageId;
        this.conversationId = conversationId;
        this.userData = new HashMap<>();
        this.conversationData = new HashMap<>();
        this.sessionData = new HashMap<>();
        this.systemData = new HashMap<>();
        this.metadata = new HashMap<>();
        this.tags = new HashMap<>();
        this.scores = new HashMap<>();
        this.processingHistory = new HashMap<>();
        this.previousDecisions = new HashMap<>();
        this.timestamp = LocalDateTime.now();
    }
    
    // Utility methods
    public void addUserData(String key, Object value) {
        this.userData.put(key, value);
    }
    
    public void addConversationData(String key, Object value) {
        this.conversationData.put(key, value);
    }
    
    public void addSystemData(String key, Object value) {
        this.systemData.put(key, value);
    }
    
    public void addTag(String key, String value) {
        this.tags.put(key, value);
    }
    
    public void addScore(String key, Double value) {
        this.scores.put(key, value);
    }
    
    public void recordProcessing(String step) {
        this.processingHistory.put(step, LocalDateTime.now());
    }
}
