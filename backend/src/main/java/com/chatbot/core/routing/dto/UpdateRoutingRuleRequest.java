package com.chatbot.core.routing.dto;

import com.chatbot.core.routing.model.RoutingRule;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRoutingRuleRequest {
    @NotBlank(message = "Name is required")
    private String name;

    private String description;

    private RoutingRule.RoutingRuleType ruleType;

    private Integer priority;

    private RoutingRule.TargetType targetType;

    private Long targetAgentId;

    private Long targetTeamId;

    private Long targetSkillId;

    private Map<String, Object> conditions;

    private Integer maxConcurrentConversations;

    private Boolean isActive;

    private Boolean isDefault;
}
