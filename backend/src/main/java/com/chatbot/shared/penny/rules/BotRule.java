package com.chatbot.shared.penny.rules;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Bot Rule Entity - Định nghĩa rule tùy chỉnh cho bot
 */
@Entity
@Table(name = "penny_bot_rules")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BotRule {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "bot_id", nullable = false)
    private UUID botId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "condition", nullable = false, columnDefinition = "TEXT")
    private String condition; // JSON expression for rule condition

    @Column(name = "action", nullable = false, columnDefinition = "TEXT")
    private String action; // JSON response template

    @Column(name = "priority", nullable = false)
    @Builder.Default
    private Integer priority = 0;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "rule_type", nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private RuleType ruleType = RuleType.RESPONSE;

    @Column(name = "trigger_type", nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private TriggerType triggerType = TriggerType.INTENT;

    @Column(name = "trigger_value")
    private String triggerValue; // Intent name, keyword, etc.

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "execution_count")
    @Builder.Default
    private Long executionCount = 0L;

    @Column(name = "last_executed_at")
    private LocalDateTime lastExecutedAt;

    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata; // JSON for additional data

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Increment execution count
     */
    public void incrementExecutionCount() {
        executionCount++;
        lastExecutedAt = LocalDateTime.now();
    }

    /**
     * Check if rule matches trigger
     */
    public boolean matchesTrigger(String intent, String message, Map<String, Object> context) {
        switch (triggerType) {
            case INTENT:
                return intent != null && intent.equals(triggerValue);
            case KEYWORD:
                return message != null && message.toLowerCase().contains(triggerValue.toLowerCase());
            case REGEX:
                return message != null && message.matches(triggerValue);
            case CONDITION:
                // Evaluate custom condition (would need expression evaluator)
                return evaluateCondition(context);
            default:
                return false;
        }
    }

    /**
     * Evaluate custom condition (simplified)
     */
    private boolean evaluateCondition(Map<String, Object> context) {
        // This would be implemented with a proper expression evaluator
        // For now, return true if condition exists
        return condition != null && !condition.trim().isEmpty();
    }

    // Enums
    public enum RuleType {
        RESPONSE,     // Direct response
        REDIRECT,     // Redirect to another flow
        WEBHOOK,      // Call webhook
        SCRIPT        // Execute custom script
    }

    public enum TriggerType {
        INTENT,       // Trigger by intent
        KEYWORD,      // Trigger by keyword
        REGEX,        // Trigger by regex
        CONDITION,    // Custom condition
        ALWAYS        // Always trigger
    }
}
