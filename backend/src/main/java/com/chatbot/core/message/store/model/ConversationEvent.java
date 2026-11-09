package com.chatbot.core.message.store.model;

import com.chatbot.core.tenant.infra.BaseTenantEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.chatbot.shared.utils.DateUtils;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

/**
 * Immutable event recorded on a conversation lifecycle
 * (created, assigned, transferred, escalated, takeover, closed...).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "conversation_events",
    indexes = {
        @Index(name = "idx_conv_event_tenant", columnList = "tenant_id"),
        @Index(name = "idx_conv_event_conversation", columnList = "conversation_id"),
        @Index(name = "idx_conv_event_type", columnList = "event_type")
    }
)
@EqualsAndHashCode(callSuper = true)
public class ConversationEvent extends BaseTenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "conversation_id", nullable = false)
    private Long conversationId;

    @Column(name = "event_type", nullable = false, length = 64)
    private String eventType;

    /** SYSTEM | AGENT | USER | BOT */
    @Column(name = "actor_type", length = 16)
    private String actorType;

    /** agentId / userId / email / "system" */
    @Column(name = "actor_id", length = 128)
    private String actorId;

    @Column(name = "from_value", length = 255)
    private String fromValue;

    @Column(name = "to_value", length = 255)
    private String toValue;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private String metadata;

    @Column(name = "occurred_at", nullable = false)
    @JsonFormat(pattern = DateUtils.API_DATETIME_FORMAT, timezone = DateUtils.API_TIMEZONE)
    private LocalDateTime occurredAt;
}
