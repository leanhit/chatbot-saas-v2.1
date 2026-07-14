package com.chatbot.shared.penny.escalation;

import com.chatbot.shared.utils.DateUtils;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing an escalation ticket for Penny Bot
 * 
 * Created when AI confidence is low or user explicitly requests human assistance.
 * Tracks the escalation status and resolution.
 */
@Entity
@Table(name = "penny_escalation_tickets")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EscalationTicket {

    @Id
    @Column(name = "id")
    @Builder.Default
    private UUID id = UUID.randomUUID();

    @Column(name = "bot_id")
    private UUID botId;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "user_id", nullable = false, length = 255)
    private String userId; // External user ID (e.g., Facebook PSID)

    @Column(name = "conversation_id", length = 255)
    private String conversationId; // External conversation ID

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason; // Why escalation was triggered (low confidence, keyword, etc.)

    @Column(name = "status", length = 50, nullable = false)
    @Builder.Default
    private String status = "PENDING"; // PENDING, ASSIGNED, RESOLVED, CANCELLED

    @Column(name = "assigned_agent_id", length = 255)
    private String assignedAgentId; // ID of the agent who took over

    @Column(name = "priority", length = 20)
    @Builder.Default
    private String priority = "NORMAL"; // LOW, NORMAL, HIGH, URGENT

    @Column(name = "confidence_score")
    private Double confidenceScore; // AI confidence score that triggered escalation

    @Column(name = "metadata", columnDefinition = "JSONB")
    private String metadata; // Additional context as JSON

    @JsonFormat(pattern = DateUtils.STANDARD_JSON_FORMAT, timezone = DateUtils.STANDARD_TIMEZONE)
    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @JsonFormat(pattern = DateUtils.STANDARD_JSON_FORMAT, timezone = DateUtils.STANDARD_TIMEZONE)
    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "resolution_notes", columnDefinition = "TEXT")
    private String resolutionNotes;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    /**
     * Check if ticket is pending
     */
    public boolean isPending() {
        return "PENDING".equals(status);
    }

    /**
     * Check if ticket is resolved
     */
    public boolean isResolved() {
        return "RESOLVED".equals(status);
    }

    /**
     * Check if ticket is assigned to an agent
     */
    public boolean isAssigned() {
        return "ASSIGNED".equals(status) && assignedAgentId != null;
    }

    /**
     * Mark ticket as assigned
     */
    public void markAsAssigned(String agentId) {
        this.status = "ASSIGNED";
        this.assignedAgentId = agentId;
    }

    /**
     * Mark ticket as resolved
     */
    public void markAsResolved(String notes) {
        this.status = "RESOLVED";
        this.resolvedAt = LocalDateTime.now();
        this.resolutionNotes = notes;
    }

    /**
     * Cancel ticket
     */
    public void cancel() {
        this.status = "CANCELLED";
    }
}
