package com.chatbot.core.payment.plan.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PackageRequest {
    private String packageId;
    private String name;
    private BigDecimal price;
    private String currency;
    private String duration;
    private String description;
    private Integer messageLimit;
    private Integer chatbotLimit;
    private Boolean hasPrioritySupport;
    private Boolean hasAnalytics;
    private Boolean hasAdvancedAnalytics;
    private Boolean hasCustomIntegrations;
    private Boolean hasDedicatedSupport;
    private Boolean hasCustomFeatures;
    private Boolean hasSlaGuarantee;
    private Boolean isActive;
    private Integer sortOrder;
    private String badge;
}
