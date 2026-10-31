package com.chatbot.core.routing.dto;

import com.chatbot.core.routing.model.EscalationRule;
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
public class CreateEscalationRuleRequest {
    @NotBlank(message = "Name is required")
    private String name;

    private String description;

    @NotNull(message = "Trigger type is required")
    private EscalationRule.TriggerType triggerType;

    private String triggerValue;

    private Integer thresholdValue;

    private Integer timeThresholdMinutes;

    private Map<String, Object> conditions;

    @NotNull(message = "Escalation action is required")
    private EscalationRule.EscalationAction escalationAction;

    private Long escalateToAgentId;

    private Long escalateToTeamId;

    @Builder.Default
    private Boolean escalateToSupervisor = false;

    @Builder.Default
    private Boolean notifySupervisor = true;

    @NotNull(message = "Priority is required")
    private Integer priority;

    @Builder.Default
    private Boolean isActive = true;
}
