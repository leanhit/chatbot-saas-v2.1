package com.chatbot.shared.penny.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Middleware Request - Định dạng request chuẩn cho Penny Middleware Engine
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MiddlewareRequest {
    
    /**
     * Unique request identifier
     */
    private String requestId;
    
    /**
     * User identifier on platform
     */
    private String userId;
    
    /**
     * Platform name (facebook, zalo, website, etc.)
     */
    private String platform;
    
    /**
     * Message content
     */
    private String message;
    
    /**
     * Connection ID for multi-connection support
     */
    private UUID connectionId;
    
    /**
     * Bot ID to route to
     */
    private String botId;
    
    /**
     * Tenant ID for multi-tenant support
     */
    private Long tenantId;
    
    /**
     * Owner ID of the connection
     */
    private String ownerId;
    
    /**
     * Additional metadata
     */
    private Map<String, Object> metadata;
    
    /**
     * Request timestamp
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS", timezone = "UTC")
    private Instant timestamp;
    
    /**
     * Message type (text, image, video, etc.)
     */
    private String messageType;
    
    /**
     * Language code (vi, en, etc.)
     */
    private String language;
    
    /**
     * Session identifier for conversation tracking
     */
    private String sessionId;
    
    /**
     * Facebook Page ID (for Facebook integration)
     */
    private String pageId;
    
    /**
     * Conversation ID from database
     */
    private String conversationId;
    
    /**
     * Priority level (low, medium, high, urgent)
     */
    private String priority;
    
    /**
     * Whether this is a retry request
     */
    private Boolean isRetry;
    
    /**
     * Retry count
     */
    private Integer retryCount;
    
    /**
     * User agent information
     */
    private String userAgent;
    
    /**
     * IP address of the user
     */
    private String ipAddress;
    
    /**
     * Geographic location
     */
    private String location;
    
    /**
     * Device type (mobile, desktop, tablet)
     */
    private String deviceType;
    
    /**
     * Additional context information
     */
    private Map<String, Object> context;
    
    // Helper methods
    
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
     * Check if request has metadata key
     */
    public boolean hasMetadata(String key) {
        return metadata != null && metadata.containsKey(key);
    }
    
    /**
     * Get context value by key
     */
    @SuppressWarnings("unchecked")
    public <T> T getContext(String key, Class<T> type) {
        if (context == null || !context.containsKey(key)) {
            return null;
        }
        Object value = context.get(key);
        if (type.isInstance(value)) {
            return (T) value;
        }
        return null;
    }
    
    /**
     * Add context information
     */
    public void addContext(String key, Object value) {
        if (context == null) {
            context = new java.util.HashMap<>();
        }
        context.put(key, value);
    }
    
    /**
     * Check if this is a high priority request
     */
    public boolean isHighPriority() {
        return "high".equals(priority) || "urgent".equals(priority);
    }
    
    /**
     * Check if this is a text message
     */
    public boolean isTextMessage() {
        return "text".equals(messageType) || messageType == null;
    }
    
    /**
     * Check if this is a Vietnamese message
     */
    public boolean isVietnamese() {
        return "vi".equals(language) || (language == null && isVietnameseText(message));
    }
    
    /**
     * Simple Vietnamese text detection
     */
    private boolean isVietnameseText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }
        // Check for common Vietnamese characters
        return text.matches(".*[àáạảãâầấậẩẫăằắặẳẵèéẹẻẽêềếệểễìíịỉĩòóọỏõôồốộổỗơờớợởỡùúụủũưừứựửữỳýỵỷỹđĐÀÁẠẢÃÂẦẤẬẨẪĂẰẮẶẲẴÈÉẸẺẼÊỀẾỆỂỄÌÍỊỈĨÒÓỌỎÕÔỒỐỘỔỖƠỜỚỢỞỠÙÚỤỦŨƯỪỨỰỬỮỲÝỴỶỸ].*");
    }
    
    /**
     * Get request age in milliseconds
     */
    public long getAgeInMillis() {
        if (timestamp == null) {
            return 0;
        }
        return Instant.now().toEpochMilli() - timestamp.toEpochMilli();
    }
    
    /**
     * Check if request is expired
     */
    public boolean isExpired(long maxAgeMillis) {
        return getAgeInMillis() > maxAgeMillis;
    }
    
    @Override
    public String toString() {
        return "MiddlewareRequest{" +
                "requestId='" + requestId + '\'' +
                ", userId='" + userId + '\'' +
                ", platform='" + platform + '\'' +
                ", message='" + (message != null ? message.substring(0, Math.min(50, message.length())) + "..." : "null") + '\'' +
                ", tenantId=" + tenantId +
                ", timestamp=" + timestamp +
                '}';
    }
}
