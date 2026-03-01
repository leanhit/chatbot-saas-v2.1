package com.chatbot.shared.penny.context;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.chatbot.shared.utils.DateUtils;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Conversation Context - Đối tượng lưu trữ ngữ cảnh hội thoại
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationContext {
    
    /**
     * Unique context identifier
     */
    private String contextId;
    
    /**
     * Tenant ID for multi-tenant support
     */
    private Long tenantId;
    
    /**
     * User identifier
     */
    private String userId;
    
    /**
     * Platform name
     */
    private String platform;
    
    /**
     * Connection ID
     */
    private UUID connectionId;
    
    /**
     * Bot ID
     */
    private String botId;
    
    /**
     * Owner ID
     */
    private String ownerId;
    
    /**
     * Session identifier
     */
    private String sessionId;
    
    /**
     * Conversation status
     */
    private String status;
    
    /**
     * Last detected intent
     */
    private String lastIntent;
    
    /**
     * Last used provider
     */
    private ProviderType lastProvider;
    
    /**
     * Last successful provider
     */
    private ProviderType lastSuccessfulProvider;
    
    /**
     * Previous provider (for continuity)
     */
    private ProviderType previousProvider;
    
    /**
     * Language code
     */
    private String language;
    
    /**
     * Creation timestamp
     */
    @JsonFormat(pattern = DateUtils.STANDARD_JSON_FORMAT, timezone = DateUtils.STANDARD_TIMEZONE)
    private Instant createdAt;
    
    /**
     * Last activity timestamp
     */
    @JsonFormat(pattern = DateUtils.STANDARD_JSON_FORMAT, timezone = DateUtils.STANDARD_TIMEZONE)
    private Instant lastActivity;
    
    /**
     * Message count in current session
     */
    private Integer messageCount;
    
    /**
     * Message count in current session with current provider
     */
    private Integer messageCountInCurrentSession;
    
    /**
     * Intent history
     */
    private List<String> intentHistory;
    
    /**
     * Provider history
     */
    private List<String> providerHistory;
    
    /**
     * User preferences
     */
    private Map<String, Object> userPreferences;
    
    /**
     * Conversation metadata
     */
    private Map<String, Object> metadata;
    
    /**
     * Session data
     */
    private Map<String, Object> sessionData;
    
    /**
     * Context variables
     */
    private Map<String, Object> variables;
    
    /**
     * Tags for categorization
     */
    private Set<String> tags;
    
    /**
     * Priority level
     */
    private String priority;
    
    /**
     * Escalation level
     */
    private Integer escalationLevel;
    
    /**
     * Whether human intervention is needed
     */
    private Boolean needsHumanIntervention;
    
    /**
     * Satisfaction score
     */
    private Double satisfactionScore;
    
    /**
     * Total processing time
     */
    private Long totalProcessingTime;
    
    /**
     * Error count
     */
    private Integer errorCount;
    
    /**
     * Expiration time
     */
    @JsonFormat(pattern = DateUtils.STANDARD_JSON_FORMAT, timezone = DateUtils.STANDARD_TIMEZONE)
    private Instant expiresAt;
    
    // Helper methods
    
    /**
     * Add intent to history
     */
    public void addIntentToHistory(String intent) {
        if (intentHistory == null) {
            intentHistory = new ArrayList<>();
        }
        intentHistory.add(intent);
        // Keep only last 50 intents
        if (intentHistory.size() > 50) {
            intentHistory = intentHistory.subList(intentHistory.size() - 50, intentHistory.size());
        }
    }
    
    /**
     * Add provider to history
     */
    public void addProviderToHistory(String provider) {
        if (providerHistory == null) {
            providerHistory = new ArrayList<>();
        }
        providerHistory.add(provider);
        // Keep only last 20 providers
        if (providerHistory.size() > 20) {
            providerHistory = providerHistory.subList(providerHistory.size() - 20, providerHistory.size());
        }
    }
    
    /**
     * Increment message count
     */
    public void incrementMessageCount() {
        if (messageCount == null) {
            messageCount = 0;
        }
        messageCount++;
        
        // Also increment session message count
        if (messageCountInCurrentSession == null) {
            messageCountInCurrentSession = 0;
        }
        messageCountInCurrentSession++;
    }
    
    /**
     * Reset session message count
     */
    public void resetSessionMessageCount() {
        messageCountInCurrentSession = 0;
    }
    
    /**
     * Add metadata
     */
    public void addMetadata(String key, Object value) {
        if (metadata == null) {
            metadata = new HashMap<>();
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
    
    /**
     * Add session data
     */
    public void addSessionData(String key, Object value) {
        if (sessionData == null) {
            sessionData = new HashMap<>();
        }
        sessionData.put(key, value);
    }
    
    /**
     * Get session data
     */
    @SuppressWarnings("unchecked")
    public <T> T getSessionData(String key, Class<T> type) {
        if (sessionData == null || !sessionData.containsKey(key)) {
            return null;
        }
        Object value = sessionData.get(key);
        if (type.isInstance(value)) {
            return (T) value;
        }
        return null;
    }
    
    /**
     * Set variable
     */
    public void setVariable(String key, Object value) {
        if (variables == null) {
            variables = new HashMap<>();
        }
        variables.put(key, value);
    }
    
    /**
     * Get variable
     */
    @SuppressWarnings("unchecked")
    public <T> T getVariable(String key, Class<T> type) {
        if (variables == null || !variables.containsKey(key)) {
            return null;
        }
        Object value = variables.get(key);
        if (type.isInstance(value)) {
            return (T) value;
        }
        return null;
    }
    
    /**
     * Add tag
     */
    public void addTag(String tag) {
        if (tags == null) {
            tags = new HashSet<>();
        }
        tags.add(tag);
    }
    
    /**
     * Remove tag
     */
    public void removeTag(String tag) {
        if (tags != null) {
            tags.remove(tag);
        }
    }
    
    /**
     * Check if has tag
     */
    public boolean hasTag(String tag) {
        return tags != null && tags.contains(tag);
    }
    
    /**
     * Set user preference
     */
    public void setUserPreference(String key, Object value) {
        if (userPreferences == null) {
            userPreferences = new HashMap<>();
        }
        userPreferences.put(key, value);
    }
    
    /**
     * Get user preference
     */
    @SuppressWarnings("unchecked")
    public <T> T getUserPreference(String key, Class<T> type) {
        if (userPreferences == null || !userPreferences.containsKey(key)) {
            return null;
        }
        Object value = userPreferences.get(key);
        if (type.isInstance(value)) {
            return (T) value;
        }
        return null;
    }
    
    /**
     * Check if context is expired
     */
    public boolean isExpired() {
        if (expiresAt == null) {
            return false;
        }
        return Instant.now().isAfter(expiresAt);
    }
    
    /**
     * Check if context is active
     */
    public boolean isActive() {
        return "active".equals(status) && !isExpired();
    }
    
    /**
     * Check if context needs escalation
     */
    public boolean needsEscalation() {
        return Boolean.TRUE.equals(needsHumanIntervention) || 
               (escalationLevel != null && escalationLevel > 0);
    }
    
    /**
     * Increment error count
     */
    public void incrementErrorCount() {
        if (errorCount == null) {
            errorCount = 0;
        }
        errorCount++;
    }
    
    /**
     * Add processing time
     */
    public void addProcessingTime(long processingTime) {
        if (totalProcessingTime == null) {
            totalProcessingTime = 0L;
        }
        totalProcessingTime += processingTime;
    }
    
    /**
     * Get average processing time
     */
    public double getAverageProcessingTime() {
        if (totalProcessingTime == null || messageCount == null || messageCount == 0) {
            return 0.0;
        }
        return (double) totalProcessingTime / messageCount;
    }
    
    /**
     * Get context age in minutes
     */
    public long getAgeInMinutes() {
        if (createdAt == null) {
            return 0;
        }
        return ChronoUnit.MINUTES.between(createdAt, Instant.now());
    }
    
    /**
     * Get context age in hours
     */
    public double getAgeInHours() {
        if (createdAt == null) {
            return 0.0;
        }
        return ChronoUnit.MINUTES.between(createdAt, Instant.now()) / 60.0;
    }
    
    /**
     * Get idle time in minutes
     */
    public long getIdleTimeInMinutes() {
        if (lastActivity == null) {
            return 0;
        }
        return ChronoUnit.MINUTES.between(lastActivity, Instant.now());
    }
    
    /**
     * Check if context is idle (no activity for more than 30 minutes)
     */
    public boolean isIdle() {
        return getIdleTimeInMinutes() > 30;
    }
    
    /**
     * Get last N intents
     */
    public List<String> getLastIntents(int count) {
        if (intentHistory == null || intentHistory.isEmpty()) {
            return new ArrayList<>();
        }
        int fromIndex = Math.max(0, intentHistory.size() - count);
        return new ArrayList<>(intentHistory.subList(fromIndex, intentHistory.size()));
    }
    
    /**
     * Get last N providers
     */
    public List<String> getLastProviders(int count) {
        if (providerHistory == null || providerHistory.isEmpty()) {
            return new ArrayList<>();
        }
        int fromIndex = Math.max(0, providerHistory.size() - count);
        return new ArrayList<>(providerHistory.subList(fromIndex, providerHistory.size()));
    }
    
    /**
     * Check if this is a new conversation
     */
    public boolean isNewConversation() {
        return messageCount == null || messageCount <= 1;
    }
    
    /**
     * Check if this is a returning user
     */
    public boolean isReturningUser() {
        return messageCount != null && messageCount > 1;
    }
    
    @Override
    public String toString() {
        return "ConversationContext{" +
                "contextId='" + contextId + '\'' +
                ", userId='" + userId + '\'' +
                ", platform='" + platform + '\'' +
                ", status='" + status + '\'' +
                ", messageCount=" + messageCount +
                ", lastIntent='" + lastIntent + '\'' +
                ", lastProvider=" + lastProvider +
                ", ageMinutes=" + getAgeInMinutes() +
                '}';
    }
    
    // Provider type enum
    public enum ProviderType {
        BOTPRESS, RASA, GPT
    }
}
