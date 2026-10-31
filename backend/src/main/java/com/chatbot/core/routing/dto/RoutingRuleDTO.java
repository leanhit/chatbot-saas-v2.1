package com.chatbot.core.routing.dto;

import com.chatbot.core.routing.model.RoutingRule;
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
public class RoutingRuleDTO {
    private Long id;
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
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
