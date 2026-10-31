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
public class RoutingAssignmentResult {
    
    private Long conversationId;
    private boolean assigned;
    private Long assignedAgentId;
    private Long assignedTeamId;
    private Long routingRuleId;
    private String routingStrategy;
    private LocalDateTime assignedAt;
    private String reason;
}
