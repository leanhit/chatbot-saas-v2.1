package com.chatbot.core.penny.escalation.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EscalationTicketRequest {
    @NotBlank(message = "userId is required")
    private String userId;
    @NotBlank(message = "conversationId is required")
    private String conversationId;
    private String reason;
    private String status;
    private String priority;
    private String assignedAgentId;
    private Double confidenceScore;
    private String metadata;
    private String resolutionNotes;
}
