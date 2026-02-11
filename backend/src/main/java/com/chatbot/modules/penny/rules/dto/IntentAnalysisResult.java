package com.chatbot.modules.penny.rules.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Intent Analysis Result DTO - Kết quả phân tích intent cho custom logic
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IntentAnalysisResult {
    
    /**
     * Primary intent detected
     */
    private String primaryIntent;
    
    /**
     * Confidence score for primary intent
     */
    private Double confidence;
    
    /**
     * Alternative intents with scores
     */
    private List<IntentScore> alternativeIntents;
    
    /**
     * Extracted entities
     */
    private Map<String, Object> entities;
    
    /**
     * Language detected
     */
    private String language;
    
    /**
     * Message complexity
     */
    private String complexity;
    
    /**
     * Analysis timestamp
     */
    private Instant timestamp;
    
    /**
     * Analysis metadata
     */
    private Map<String, Object> metadata;
    
    // Inner class for intent scores
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IntentScore {
        private String intent;
        private Double confidence;
        private Map<String, Object> metadata;
    }
}
