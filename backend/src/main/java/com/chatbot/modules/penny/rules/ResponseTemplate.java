package com.chatbot.modules.penny.rules;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Response Template Entity - Template phản hồi tùy chỉnh
 * Adapted cho UUID tenant support
 */
@Entity
@Table(name = "penny_response_templates")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseTemplate {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "bot_id", nullable = false)
    private UUID botId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "intent", nullable = false)
    private String intent; // Intent name this template responds to

    @Column(name = "template_text", nullable = false, columnDefinition = "TEXT")
    private String templateText; // Template with variables like {{variable_name}}

    @Column(name = "template_type", nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private TemplateType templateType = TemplateType.TEXT;

    @Column(name = "language", nullable = false)
    @Builder.Default
    private String language = "vi"; // Language code

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "priority", nullable = false)
    @Builder.Default
    private Integer priority = 0;

    @Column(name = "variables", columnDefinition = "TEXT")
    private String variables; // JSON definition of template variables

    @Column(name = "quick_replies", columnDefinition = "TEXT")
    private String quickReplies; // JSON array of quick replies

    @Column(name = "attachments", columnDefinition = "TEXT")
    private String attachments; // JSON array of attachments

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

    @Column(name = "usage_count")
    @Builder.Default
    private Long usageCount = 0L;

    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

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
     * Increment usage count
     */
    public void incrementUsageCount() {
        usageCount++;
        lastUsedAt = LocalDateTime.now();
    }

    /**
     * Process template with variables
     */
    public String processTemplate(Map<String, Object> variables) {
        String processed = templateText;
        
        if (variables != null) {
            for (Map.Entry<String, Object> entry : variables.entrySet()) {
                String placeholder = "{{" + entry.getKey() + "}}";
                String value = entry.getValue() != null ? entry.getValue().toString() : "";
                processed = processed.replace(placeholder, value);
            }
        }
        
        return processed;
    }

    // Template type enum
    public enum TemplateType {
        TEXT,           // Plain text
        RICH_TEXT,      // Rich text with formatting
        CARD,           // Card template
        LIST,           // List template
        CAROUSEL,       // Carousel template
        FORM,           // Form template
        CUSTOM,         // Custom template
        INTERACTIVE,     // Interactive template
        MEDIA,          // Media template
        LOCATION,        // Location template
        CONTACT,        // Contact template
        POLL,           // Poll template
        FEEDBACK,       // Feedback template
        NOTIFICATION,   // Notification template
        CONFIRMATION,   // Confirmation template
        ERROR           // Error template
    }
}
