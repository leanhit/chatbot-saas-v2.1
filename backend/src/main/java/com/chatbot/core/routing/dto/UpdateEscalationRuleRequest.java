package com.chatbot.core.routing.dto;

import com.chatbot.core.routing.model.EscalationRule;
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
public class UpdateEscalationRuleRequest {
    @NotBlank(message = "Name is required")
    private String name;

    private String description;

    private EscalationRule.TriggerType triggerType;

    private String triggerValue;

    private Integer thresholdValue;

    private Integer timeThresholdMinutes;

    private Map<String, Object> conditions;

    private EscalationRule.EscalationAction escalationAction;

    private Long escalateToAgentId;

    private Long escalateToTeamId;

    private Boolean escalateToSupervisor;

    private Boolean notifySupervisor;

    private Integer priority;

    private Boolean isActive;
}
