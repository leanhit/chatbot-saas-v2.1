package com.chatbot.core.penny.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Persistent storage for Penny conversation contexts.
 * Identity: tenantId + botId + userId + platform (via contextKey).
 */
@Entity
@Table(name = "penny_conversation_contexts",
    indexes = {
        @Index(name = "idx_penny_ctx_tenant_bot", columnList = "tenant_id, bot_id"),
        @Index(name = "idx_penny_ctx_tenant_user_platform", columnList = "tenant_id, user_id, platform"),
        @Index(name = "idx_penny_ctx_expires_at", columnList = "expires_at")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PennyConversationContext {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "bot_id", nullable = false)
    private String botId;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "platform")
    private String platform;

    @Column(name = "context_key", nullable = false, unique = true)
    private String contextKey;

    @Column(name = "status")
    private String status;

    /**
     * Serialized ConversationContext JSON
     */
    @Column(name = "context_data", columnDefinition = "TEXT")
    private String contextData;

    @Column(name = "message_count")
    private Integer messageCount;

    @Column(name = "last_intent")
    private String lastIntent;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "last_updated", nullable = false)
    private Instant lastUpdated;

    @Column(name = "expires_at")
    private Instant expiresAt;
}
