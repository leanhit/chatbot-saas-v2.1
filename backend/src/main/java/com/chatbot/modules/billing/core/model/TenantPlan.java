package com.chatbot.modules.billing.core.model;

import com.chatbot.modules.app.core.model.AppCode;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * TenantPlan entity - Billing plan assigned to tenant
 * 
 * v0.1: Simple plan assignment model
 * - One plan per tenant
 * - Static plan assignment only
 * - No billing cycles or payment logic
 */
@Entity
@Table(
    name = "tenant_plans",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"tenant_id"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantPlan {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    @Builder.Default
    private UUID id = UUID.randomUUID();

    @Column(name = "tenant_id", nullable = false, unique = true)
    private UUID tenantId;

    @Enumerated(EnumType.STRING)
    @Column(name = "plan_code", nullable = false, length = 50)
    private PlanCode planCode;

    @Column(name = "assigned_at", nullable = false)
    private LocalDateTime assignedAt;

    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (assignedAt == null) {
            assignedAt = now;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Business logic helpers
    public boolean isFree() {
        return PlanCode.FREE.equals(this.planCode);
    }

    public boolean isPro() {
        return PlanCode.PRO.equals(this.planCode);
    }

    public boolean isEnterprise() {
        return PlanCode.ENTERPRISE.equals(this.planCode);
    }
}
