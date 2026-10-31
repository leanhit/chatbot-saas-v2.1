package com.chatbot.core.routing.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EscalationResult {
    
    private Long conversationId;
    private boolean escalated;
    private Long escalatedToAgentId;
    private Long escalatedToTeamId;
    private boolean escalatedToSupervisor;
    private Long escalationRuleId;
    private String escalationAction;
    private LocalDateTime escalatedAt;
    private String reason;
}
