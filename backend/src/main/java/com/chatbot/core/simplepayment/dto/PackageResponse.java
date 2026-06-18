package com.chatbot.core.simplepayment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public static PackageResponse fromEntity(com.chatbot.core.simplepayment.model.Package entity) {
        if (entity == null) {
            return null;
        }
        return PackageResponse.builder()
                .id(entity.getId())
                .packageId(entity.getPackageId())
                .name(entity.getName())
                .price(entity.getPrice())
                .currency(entity.getCurrency())
                .duration(entity.getDuration())
                .description(entity.getDescription())
                .messageLimit(entity.getMessageLimit())
                .chatbotLimit(entity.getChatbotLimit())
                .hasPrioritySupport(entity.getHasPrioritySupport())
                .hasAnalytics(entity.getHasAnalytics())
                .hasAdvancedAnalytics(entity.getHasAdvancedAnalytics())
                .hasCustomIntegrations(entity.getHasCustomIntegrations())
                .hasDedicatedSupport(entity.getHasDedicatedSupport())
                .hasCustomFeatures(entity.getHasCustomFeatures())
                .hasSlaGuarantee(entity.getHasSlaGuarantee())
                .isActive(entity.getIsActive())
                .sortOrder(entity.getSortOrder())
                .badge(entity.getBadge())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
