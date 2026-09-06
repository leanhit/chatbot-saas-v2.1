package com.chatbot.core.payment.plan.dto;

import com.chatbot.core.payment.plan.model.Package;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PackageResponse {
    private Long id;
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
    private String formattedPrice;

    public static PackageResponse from(Package packageEntity) {
        return PackageResponse.builder()
                .id(packageEntity.getId())
                .packageId(packageEntity.getPackageId())
                .name(packageEntity.getName())
                .price(packageEntity.getPrice())
                .currency(packageEntity.getCurrency())
                .duration(packageEntity.getDuration())
                .description(packageEntity.getDescription())
                .messageLimit(packageEntity.getMessageLimit())
                .chatbotLimit(packageEntity.getChatbotLimit())
                .hasPrioritySupport(packageEntity.getHasPrioritySupport())
                .hasAnalytics(packageEntity.getHasAnalytics())
                .hasAdvancedAnalytics(packageEntity.getHasAdvancedAnalytics())
                .hasCustomIntegrations(packageEntity.getHasCustomIntegrations())
                .hasDedicatedSupport(packageEntity.getHasDedicatedSupport())
                .hasCustomFeatures(packageEntity.getHasCustomFeatures())
                .hasSlaGuarantee(packageEntity.getHasSlaGuarantee())
                .isActive(packageEntity.getIsActive())
                .sortOrder(packageEntity.getSortOrder())
                .badge(packageEntity.getBadge())
                .formattedPrice(packageEntity.getFormattedPrice())
                .build();
    }
}
