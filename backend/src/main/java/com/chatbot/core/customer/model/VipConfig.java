package com.chatbot.core.customer.model;

import com.chatbot.core.tenant.infra.BaseTenantEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * Per-tenant VIP tier thresholds for customer segmentation.
 * One config per tenant; when absent, CustomerSegmentationService
 * falls back to built-in defaults.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "vip_configs",
    uniqueConstraints = @UniqueConstraint(name = "uk_vip_config_tenant", columnNames = "tenant_id"),
    indexes = @Index(name = "idx_vip_config_tenant", columnList = "tenant_id"))
@EqualsAndHashCode(callSuper = true)
public class VipConfig extends BaseTenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Minimum conversations for BASIC tier */
    @Column(name = "basic_min_conversations")
    @Builder.Default
    private Integer basicMinConversations = 3;

    /** Minimum conversations for PREMIUM tier */
    @Column(name = "premium_min_conversations")
    @Builder.Default
    private Integer premiumMinConversations = 10;

    /** Minimum satisfaction score (0-5) for PREMIUM tier */
    @Column(name = "premium_min_satisfaction")
    @Builder.Default
    private Double premiumMinSatisfaction = 4.0;

    /** Minimum conversations for ENTERPRISE tier */
    @Column(name = "enterprise_min_conversations")
    @Builder.Default
    private Integer enterpriseMinConversations = 50;

    /** Whether auto tier recalculation is enabled for this tenant */
    @Column(name = "auto_recalculate_enabled", nullable = false)
    @Builder.Default
    private Boolean autoRecalculateEnabled = true;

    @Column(columnDefinition = "TEXT")
    private String description;
}
