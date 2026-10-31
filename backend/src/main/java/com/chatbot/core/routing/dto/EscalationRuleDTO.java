package com.chatbot.core.routing.dto;

import com.chatbot.core.routing.model.EscalationRule;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EscalationRuleDTO {
    private Long id;
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
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
