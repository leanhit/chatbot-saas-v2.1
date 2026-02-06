package com.chatbot.modules.messagehub.core.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Conversation context entity for Message Hub MVP
 * Stores minimal conversation state for decision making
 */
@Entity
@Table(name = "conversation_contexts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ConversationContext {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private final UUID id = UUID.randomUUID();

    @Column(name = "conversation_id", nullable = false, unique = true, length = 100)
    private String conversationId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "last_intent", length = 50)
    private String lastIntent;

    @Column(name = "asked_price_count", nullable = false)
    private Integer askedPriceCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "handler_type", nullable = false, length = 20)
    private HandlerType handlerType = HandlerType.BOT;

    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Handler type enumeration
     */
    public enum HandlerType {
        BOT,    // Bot handles the conversation
        HUMAN   // Human agent handles the conversation
    }
}
