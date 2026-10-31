package com.chatbot.core.routing.dto;

import com.chatbot.core.routing.model.ConversationRouting;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationRoutingDTO {
    private Long id;
    private Long conversationId;
    private ConversationRouting.RoutingStatus routingStatus;
    private Long assignedAgentId;
    private Long assignedTeamId;
    private Long routingRuleId;
    private Integer priority;
    private Integer queuePosition;
    private LocalDateTime assignedAt;
    private LocalDateTime escalatedAt;
    private Long escalationRuleId;
    private Long escalatedFromAgentId;
    private Long escalatedToAgentId;
    private String escalationReason;
    private Integer waitTimeSeconds;
    private Integer handlingTimeSeconds;
    private Boolean isEscalated;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
