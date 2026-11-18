package com.chatbot.core.penny.escalation.dto;

import lombok.Data;

@Data
public class EscalationTicketRequest {
    private String userId;
    private String conversationId;
    private String reason;
    private String status;
    private String priority;
    private String assignedAgentId;
    private Double confidenceScore;
    private String metadata;
    private String resolutionNotes;
}
