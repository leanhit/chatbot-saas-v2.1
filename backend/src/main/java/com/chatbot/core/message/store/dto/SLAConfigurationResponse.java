package com.chatbot.core.message.store.dto;

import com.chatbot.core.message.store.model.SLAConfiguration;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SLAConfigurationResponse {
    private Long id;
    private String customerTier;
    private Long expectedResponseTime;
    private Integer maxBreachCount;
    private Boolean active;
    private String description;
    private Long tenantId;

    public static SLAConfigurationResponse from(SLAConfiguration c) {
        if (c == null) return null;
        return SLAConfigurationResponse.builder()
                .id(c.getId()).customerTier(c.getCustomerTier())
                .expectedResponseTime(c.getExpectedResponseTime())
                .maxBreachCount(c.getMaxBreachCount()).active(c.getActive())
                .description(c.getDescription()).tenantId(c.getTenantId()).build();
    }
}
