package com.chatbot.core.routing.dto;

import com.chatbot.core.routing.model.RoutingRule;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateRoutingRuleRequest {
    @NotBlank(message = "Name is required")
    private String name;

    private String description;

    @NotNull(message = "Rule type is required")
    private RoutingRule.RoutingRuleType ruleType;

    @NotNull(message = "Priority is required")
    private Integer priority;

    @NotNull(message = "Target type is required")
    private RoutingRule.TargetType targetType;

    private Long targetAgentId;

    private Long targetTeamId;

    private Long targetSkillId;

    private Map<String, Object> conditions;

    private Integer maxConcurrentConversations;

    @Builder.Default
    private Boolean isActive = true;

    @Builder.Default
    private Boolean isDefault = false;
}
