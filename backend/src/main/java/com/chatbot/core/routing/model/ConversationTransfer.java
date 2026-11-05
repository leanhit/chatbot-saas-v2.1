package com.chatbot.core.routing.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "conversation_transfers", indexes = {
    @Index(name = "idx_transfer_tenant", columnList = "tenant_id"),
    @Index(name = "idx_transfer_conversation", columnList = "conversation_id"),
    @Index(name = "idx_transfer_from_agent", columnList = "from_agent_id"),
    @Index(name = "idx_transfer_to_agent", columnList = "to_agent_id"),
    @Index(name = "idx_transfer_created_at", columnList = "created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationTransfer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "conversation_id", nullable = false)
    private Long conversationId;

    @Column(name = "from_agent_id", nullable = false)
    private Long fromAgentId;

    @Column(name = "to_agent_id", nullable = false)
    private Long toAgentId;

    @Column(name = "transfer_reason")
    private String transferReason;

    @Column(name = "transfer_type", nullable = false)
    private String transferType;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "transferred_by")
    private Long transferredBy;

    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;

    @Column(name = "rejected_at")
    private LocalDateTime rejectedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public enum TransferType {
        MANUAL,
        AUTO_ESCALATION,
        WORKLOAD_BALANCE,
        SKILL_MISMATCH,
        SUPERVISOR_REQUEST
    }

    public enum TransferStatus {
        PENDING,
        ACCEPTED,
        REJECTED,
        COMPLETED,
        CANCELLED
    }
}
