package com.chatbot.core.message.store.dto;

import com.chatbot.core.message.store.model.RoutingRule;
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
public class RoutingRuleResponse {
    private Long id;
    private String name;
    private String description;
    private Integer priority;
    private Map<String, Object> conditions;
    private Map<String, Object> action;
    private Boolean active;
    private String ruleType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long tenantId;

    public static RoutingRuleResponse from(RoutingRule r) {
        if (r == null) return null;
        return RoutingRuleResponse.builder()
                .id(r.getId()).name(r.getName()).description(r.getDescription())
                .priority(r.getPriority()).conditions(r.getConditions()).action(r.getAction())
                .active(r.getActive()).ruleType(r.getRuleType() != null ? r.getRuleType().name() : null)
                .createdAt(r.getCreatedAt()).updatedAt(r.getUpdatedAt()).tenantId(r.getTenantId())
                .build();
    }
}
