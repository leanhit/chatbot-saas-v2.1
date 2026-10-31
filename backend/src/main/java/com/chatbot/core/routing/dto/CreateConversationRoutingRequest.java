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
public class CreateConversationRoutingRequest {
    @NotNull(message = "Conversation ID is required")
    private Long conversationId;

    @NotNull(message = "Routing status is required")
    private ConversationRouting.RoutingStatus routingStatus;

    private Long assignedAgentId;

    private Long assignedTeamId;

    private Long routingRuleId;

    @Builder.Default
    private Integer priority = 5;

    private Integer queuePosition;
}
