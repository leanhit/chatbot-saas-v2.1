package com.chatbot.core.penny.rules.dto;

import com.chatbot.core.penny.rules.BotRule;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating a new bot rule
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateBotRuleRequest {
    
    @NotBlank(message = "Rule name is required")
    private String ruleName;
    
    private String description;
    
    private String condition;
    
    @NotBlank(message = "Action is required")
    private String action;
    
    @NotNull(message = "Rule type is required")
    private BotRule.RuleType ruleType;
    
    @NotNull(message = "Trigger type is required")
    private BotRule.TriggerType triggerType;
    
    private String triggerValue;
    
    @Min(value = 0, message = "Priority must be at least 0")
    @Max(value = 100, message = "Priority must be at most 100")
    private Integer priority;
}
