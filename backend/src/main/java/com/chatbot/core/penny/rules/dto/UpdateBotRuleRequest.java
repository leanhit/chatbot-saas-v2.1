package com.chatbot.core.penny.rules.dto;

import com.chatbot.core.penny.rules.BotRule;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating an existing bot rule
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateBotRuleRequest {
    
    private String ruleName;
    
    private String description;
    
    private String condition;
    
    private String action;
    
    @Min(value = 0, message = "Priority must be at least 0")
    @Max(value = 100, message = "Priority must be at most 100")
    private Integer priority;
    
    private Boolean isActive;
}
