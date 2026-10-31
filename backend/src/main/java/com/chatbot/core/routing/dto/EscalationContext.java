package com.chatbot.core.routing.dto;

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
public class EscalationContext {
    
    private Long conversationId;
    private Integer waitTimeMinutes;
    private LocalDateTime lastResponseTime;
    private Integer customerSatisfactionScore;
    private String customerPriority;
    private String customerTier;
    private Map<String, Object> customData;
}
