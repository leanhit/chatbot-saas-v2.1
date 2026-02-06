package com.chatbot.modules.billing.core.repository;

import com.chatbot.modules.billing.core.model.TenantPlan;
import com.chatbot.modules.billing.core.model.PlanCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * TenantPlan repository for v0.1
 * Plan assignment data access
 */
public interface TenantPlanRepository extends JpaRepository<TenantPlan, UUID> {

    /**
     * Find tenant plan by tenant ID
     */
    Optional<TenantPlan> findByTenantId(UUID tenantId);

    /**
     * Check if tenant has plan assigned
     */
    boolean existsByTenantId(UUID tenantId);
}
