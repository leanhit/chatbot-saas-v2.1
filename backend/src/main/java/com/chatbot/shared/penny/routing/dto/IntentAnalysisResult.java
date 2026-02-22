package com.chatbot.shared.penny.routing.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Intent Analysis Result - Kết quả phân tích ý định và entities
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
     * Confidence score (0.0 - 1.0)
     */
    private Double confidence;
    
    /**
     * All possible intents detected
     */
    private List<String> allIntents;
    
    /**
     * Extracted entities
     */
    private Map<String, Object> entities;
    
    /**
     * Message type (text, image, etc.)
     */
    private String messageType;
    
    /**
     * Complexity level (low, medium, high)
     */
    private String complexity;
    
    /**
     * Detected language
     */
    private String language;
    
    /**
     * Processing timestamp
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS", timezone = "UTC")
    private Instant timestamp;
    
    /**
     * Processing time in milliseconds
     */
    private Long processingTime;
    
    /**
     * Alternative intents with confidence scores
     */
    private Map<String, Double> alternativeIntents;
    
    /**
     * Sentiment analysis result
     */
    private String sentiment;
    
    /**
     * Emotion detected
     */
    private String emotion;
    
    /**
     * Urgency level
     */
    private String urgency;
    
    /**
     * Whether this is a follow-up message
     */
    private Boolean isFollowUp;
    
    /**
     * Context relevance score
     */
    private Double contextRelevance;
    
    /**
     * Keywords extracted from message
     */
    private List<String> keywords;
    
    /**
     * Additional metadata
     */
    private Map<String, Object> metadata;
    
    // Helper methods
    
    /**
     * Check if intent has high confidence
     */
    public boolean hasHighConfidence() {
        return confidence != null && confidence >= 0.8;
    }
    
    /**
     * Check if intent has medium confidence
     */
    public boolean hasMediumConfidence() {
        return confidence != null && confidence >= 0.6 && confidence < 0.8;
    }
    
    /**
     * Check if intent has low confidence
     */
    public boolean hasLowConfidence() {
        return confidence != null && confidence < 0.6;
    }
    
    /**
     * Check if analysis is complex
     */
    public boolean isComplex() {
        return "high".equals(complexity);
    }
    
    /**
     * Check if analysis is simple
     */
    public boolean isSimple() {
        return "low".equals(complexity);
    }
    
    /**
     * Get entity value by key
     */
    @SuppressWarnings("unchecked")
    public <T> T getEntity(String key, Class<T> type) {
        if (entities == null || !entities.containsKey(key)) {
            return null;
        }
        Object value = entities.get(key);
        if (type.isInstance(value)) {
            return (T) value;
        }
        return null;
    }
    
    /**
     * Check if entity exists
     */
    public boolean hasEntity(String key) {
        return entities != null && entities.containsKey(key);
    }
    
    /**
     * Get entity as string
     */
    public String getEntityAsString(String key) {
        Object value = getEntity(key, Object.class);
        return value != null ? value.toString() : null;
    }
    
    /**
     * Get entity as list
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> getEntityAsList(String key, Class<T> type) {
        Object value = getEntity(key, Object.class);
        if (value instanceof List) {
            return (List<T>) value;
        }
        return null;
    }
    
    /**
     * Check if this is a business intent
     */
    public boolean isBusinessIntent() {
        return primaryIntent != null && (
            primaryIntent.equals("order_inquiry") ||
            primaryIntent.equals("product_inquiry") ||
            primaryIntent.equals("price_inquiry") ||
            primaryIntent.equals("payment_inquiry") ||
            primaryIntent.equals("shipping_inquiry")
        );
    }
    
    /**
     * Check if this is a support intent
     */
    public boolean isSupportIntent() {
        return primaryIntent != null && (
            primaryIntent.equals("customer_support") ||
            primaryIntent.equals("technical_support") ||
            primaryIntent.equals("complaint") ||
            primaryIntent.equals("refund_request")
        );
    }
    
    /**
     * Check if this is a greeting intent
     */
    public boolean isGreetingIntent() {
        return "greeting".equals(primaryIntent);
    }
    
    /**
     * Check if this is Vietnamese
     */
    public boolean isVietnamese() {
        return "vi".equals(language);
    }
    
    /**
     * Check if this is English
     */
    public boolean isEnglish() {
        return "en".equals(language);
    }
    
    /**
     * Get processing time in seconds
     */
    public double getProcessingTimeSeconds() {
        return processingTime != null ? processingTime / 1000.0 : 0.0;
    }
    
    /**
     * Check if sentiment is positive
     */
    public boolean isPositiveSentiment() {
        return "positive".equals(sentiment);
    }
    
    /**
     * Check if sentiment is negative
     */
    public boolean isNegativeSentiment() {
        return "negative".equals(sentiment);
    }
    
    /**
     * Check if sentiment is neutral
     */
    public boolean isNeutralSentiment() {
        return "neutral".equals(sentiment);
    }
    
    /**
     * Check if urgency is high
     */
    public boolean isHighUrgency() {
        return "high".equals(urgency);
    }
    
    /**
     * Add metadata
     */
    public void addMetadata(String key, Object value) {
        if (metadata == null) {
            metadata = new java.util.HashMap<>();
        }
        metadata.put(key, value);
    }
    
    /**
     * Get metadata value
     */
    @SuppressWarnings("unchecked")
    public <T> T getMetadata(String key, Class<T> type) {
        if (metadata == null || !metadata.containsKey(key)) {
            return null;
        }
        Object value = metadata.get(key);
        if (type.isInstance(value)) {
            return (T) value;
        }
        return null;
    }
    
    @Override
    public String toString() {
        return "IntentAnalysisResult{" +
                "primaryIntent='" + primaryIntent + '\'' +
                ", confidence=" + confidence +
                ", messageType='" + messageType + '\'' +
                ", complexity='" + complexity + '\'' +
                ", language='" + language + '\'' +
                ", entitiesCount=" + (entities != null ? entities.size() : 0) +
                '}';
    }
}
