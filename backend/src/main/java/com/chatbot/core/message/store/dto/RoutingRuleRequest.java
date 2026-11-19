package com.chatbot.core.message.store.dto;

import com.chatbot.core.message.store.model.RoutingRule;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Map;

@Data
public class RoutingRuleRequest {
    @NotBlank(message = "name is required")
    private String name;
    private String description;
    private Integer priority;
    private Map<String, Object> conditions;
    private Map<String, Object> action;
    private Boolean active;
    private RoutingRule.RoutingRuleType ruleType;
}
