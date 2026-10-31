package com.chatbot.core.routing.dto;

import com.chatbot.core.routing.model.ConversationRouting;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateConversationRoutingRequest {
    @NotNull(message = "Routing status is required")
    private ConversationRouting.RoutingStatus routingStatus;

    private Long assignedAgentId;

    private Long assignedTeamId;

    private Long routingRuleId;

    private Integer priority;

    private Integer queuePosition;

    private Integer handlingTimeSeconds;
}
