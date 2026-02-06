package com.chatbot.modules.penny.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Penny Bot Entity - Bot management với UUID tenant support
 * Adapted từ traloitudongV2 cho chatbot-saas-v2 architecture
 */
@Entity
@Table(name = "penny_bots")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PennyBot {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "bot_name", nullable = false)
    private String botName;

    @Column(name = "bot_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private PennyBotType botType;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "botpress_bot_id", nullable = false)
    private String botpressBotId;

    @Column(name = "description")
    private String description;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "is_enabled", nullable = false)
    @Builder.Default
    private Boolean isEnabled = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    @Column(name = "configuration")
    private String configuration; // JSON string for additional config

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        lastUsedAt = LocalDateTime.now();
    }

    /**
     * Get the Botpress bot ID based on bot type
     */
    public String getTargetBotpressBotId() {
        return botType.getBotpressBotId();
    }

    /**
     * Check if this bot can handle the given message type
     */
    public Boolean canHandle(String messageType) {
        // Logic to determine if bot can handle specific message types
        // This can be extended based on bot capabilities
        return Boolean.TRUE.equals(isActive) && Boolean.TRUE.equals(isEnabled);
    }
}
