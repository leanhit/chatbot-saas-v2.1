package com.chatbot.core.message.store.dto;

import com.chatbot.core.message.store.model.EscalationTier;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EscalationTierResponse {
    private Long id;
    private Integer level;
    private String name;
    private Long timeoutSeconds;
    private Boolean active;
    private String description;
    private String requiredRole;
    private Long tenantId;

    public static EscalationTierResponse from(EscalationTier t) {
        if (t == null) return null;
        return EscalationTierResponse.builder()
                .id(t.getId()).level(t.getLevel()).name(t.getName())
                .timeoutSeconds(t.getTimeoutSeconds()).active(t.getActive())
                .description(t.getDescription()).requiredRole(t.getRequiredRole())
                .tenantId(t.getTenantId()).build();
    }
}
