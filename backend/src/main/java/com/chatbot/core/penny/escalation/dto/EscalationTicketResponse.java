package com.chatbot.core.penny.escalation.dto;

import com.chatbot.core.penny.escalation.EscalationTicket;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EscalationTicketResponse {

    private UUID id;
    private UUID botId;
    private Long tenantId;
    private String userId;
    private String conversationId;
    private String reason;
    private String status;
    private String assignedAgentId;
    private String priority;
    private Double confidenceScore;
    private String metadata;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;
    private String resolutionNotes;

    public static EscalationTicketResponse from(EscalationTicket t) {
        if (t == null) return null;
        return EscalationTicketResponse.builder()
                .id(t.getId())
                .botId(t.getBotId())
                .tenantId(t.getTenantId())
                .userId(t.getUserId())
                .conversationId(t.getConversationId())
                .reason(t.getReason())
                .status(t.getStatus())
                .assignedAgentId(t.getAssignedAgentId())
                .priority(t.getPriority())
                .confidenceScore(t.getConfidenceScore())
                .metadata(t.getMetadata())
                .createdAt(t.getCreatedAt())
                .resolvedAt(t.getResolvedAt())
                .resolutionNotes(t.getResolutionNotes())
                .build();
    }
}
