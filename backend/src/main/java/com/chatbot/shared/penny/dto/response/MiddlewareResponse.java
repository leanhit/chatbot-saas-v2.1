package com.chatbot.shared.penny.dto.response;

import com.chatbot.shared.penny.routing.dto.IntentAnalysisResult;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

/**
 * Middleware Response - Định dạng response chuẩn từ Penny Middleware Engine
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MiddlewareResponse {
    
    /**
     * Request identifier (same as request)
     */
    private String requestId;
    
    /**
     * Response message content
     */
    private String response;
    
    /**
     * Provider used for processing
     */
    private String providerUsed;
    
    /**
     * Intent analysis result
     */
    private IntentAnalysisResult intentAnalysis;
    
    /**
     * Processing metrics
     */
    private ProcessingMetrics processingMetrics;
    
    /**
     * Response timestamp
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS", timezone = "UTC")
    private Instant timestamp;
    
    /**
     * Response status (success, error, partial)
     */
    private String status;
    
    /**
     * Error message if any
     */
    private String errorMessage;
    
    /**
     * Error code if any
     */
    private String errorCode;
    
    /**
     * Whether response should be sent to user
     */
    private Boolean shouldSendResponse;
    
    /**
     * Response type (text, image, video, etc.)
     */
    private String responseType;
    
    /**
     * Additional metadata
     */
    private Map<String, Object> metadata;
    
    /**
     * Actions to be performed
     */
    private java.util.List<ResponseAction> actions;
    
    /**
     * Quick replies for user interaction
     */
    private java.util.List<QuickReply> quickReplies;
    
    /**
     * Attachments (images, files, etc.)
     */
    private java.util.List<Attachment> attachments;
    
    /**
     * Next steps or suggestions
     */
    private java.util.List<String> suggestions;
    
    /**
     * Confidence score of the response
     */
    private Double confidence;
    
    /**
     * Whether human intervention is needed
     */
    private Boolean needsHumanIntervention;
    
    /**
     * Escalation reason if needed
     */
    private String escalationReason;
    
    /**
     * Session updates
     */
    private Map<String, Object> sessionUpdates;
    
    /**
     * Analytics events to track
     */
    private java.util.List<AnalyticsEvent> analyticsEvents;
    
    // Helper methods
    
    /**
     * Check if response is successful
     */
    public boolean isSuccess() {
        return "success".equals(status) && errorMessage == null;
    }
    
    /**
     * Check if response has error
     */
    public boolean hasError() {
        return errorMessage != null || errorCode != null;
    }
    
    /**
     * Check if response needs escalation
     */
    public boolean needsEscalation() {
        return Boolean.TRUE.equals(needsHumanIntervention);
    }
    
    /**
     * Get metadata value by key
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
     * Add quick reply
     */
    public void addQuickReply(String title, String payload) {
        if (quickReplies == null) {
            quickReplies = new java.util.ArrayList<>();
        }
        quickReplies.add(QuickReply.builder()
            .title(title)
            .payload(payload)
            .build());
    }
    
    /**
     * Add attachment
     */
    public void addAttachment(String type, String url) {
        if (attachments == null) {
            attachments = new java.util.ArrayList<>();
        }
        attachments.add(Attachment.builder()
            .type(type)
            .url(url)
            .build());
    }
    
    /**
     * Add action
     */
    public void addAction(String type, Map<String, Object> data) {
        if (actions == null) {
            actions = new java.util.ArrayList<>();
        }
        actions.add(ResponseAction.builder()
            .type(type)
            .data(data)
            .build());
    }
    
    /**
     * Add analytics event
     */
    public void addAnalyticsEvent(String eventType, Map<String, Object> data) {
        if (analyticsEvents == null) {
            analyticsEvents = new java.util.ArrayList<>();
        }
        analyticsEvents.add(AnalyticsEvent.builder()
            .eventType(eventType)
            .data(data)
            .timestamp(Instant.now())
            .build());
    }
    
    /**
     * Convert to JSON string
     */
    public String toJson() {
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(this);
        } catch (Exception e) {
            return "{\"error\":\"Failed to serialize response\"}";
        }
    }
    
    /**
     * Create error response
     */
    public static MiddlewareResponse error(String requestId, String errorMessage, String errorCode) {
        return MiddlewareResponse.builder()
            .requestId(requestId)
            .status("error")
            .errorMessage(errorMessage)
            .errorCode(errorCode)
            .timestamp(Instant.now())
            .shouldSendResponse(true)
            .build();
    }
    
    /**
     * Create success response
     */
    public static MiddlewareResponse success(String requestId, String response, String providerUsed) {
        return MiddlewareResponse.builder()
            .requestId(requestId)
            .response(response)
            .providerUsed(providerUsed)
            .status("success")
            .timestamp(Instant.now())
            .shouldSendResponse(true)
            .build();
    }
    
    @Override
    public String toString() {
        return "MiddlewareResponse{" +
                "requestId='" + requestId + '\'' +
                ", response='" + (response != null ? response.substring(0, Math.min(50, response.length())) + "..." : "null") + '\'' +
                ", providerUsed='" + providerUsed + '\'' +
                ", status='" + status + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
    
    // Inner classes
    
    @lombok.Data
    @lombok.Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProcessingMetrics {
        private String providerType;
        private String selectionReason;
        private Double confidence;
        private Long processingTime;
        private Integer retryCount;
        private Map<String, Object> additionalMetrics;
    }
    
    @lombok.Data
    @lombok.Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResponseAction {
        private String type;
        private Map<String, Object> data;
        private String description;
    }
    
    @lombok.Data
    @lombok.Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuickReply {
        private String title;
        private String payload;
        private String imageUrl;
        private Map<String, Object> metadata;
    }
    
    @lombok.Data
    @lombok.Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Attachment {
        private String type;
        private String url;
        private String filename;
        private Integer size;
        private String mimeType;
    }
    
    @lombok.Data
    @lombok.Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnalyticsEvent {
        private String eventType;
        private Map<String, Object> data;
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS", timezone = "UTC")
        private Instant timestamp;
    }
}
