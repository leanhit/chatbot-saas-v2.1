package com.chatbot.modules.billing.core.service;

import com.chatbot.modules.app.core.model.AppCode;
import com.chatbot.modules.billing.core.dto.EntitlementResponse;
import com.chatbot.modules.billing.core.dto.TenantPlanResponse;
import com.chatbot.modules.billing.core.model.PlanCode;
import com.chatbot.modules.billing.core.model.PlanRegistry;
import com.chatbot.modules.billing.core.model.TenantPlan;
import com.chatbot.modules.billing.core.repository.TenantPlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Billing service for v0.1
 * READ-ONLY policy service for entitlement checking
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BillingService {

    private final TenantPlanRepository tenantPlanRepository;

    /**
     * Get tenant plan information
     * Returns FREE plan if no plan assigned (default behavior)
     * TODO: Validate tenant is ACTIVE through Tenant Hub
     */
    @Transactional(readOnly = true)
    public TenantPlanResponse getTenantPlan(UUID tenantId) {
        // TODO: Validate tenant is ACTIVE via Tenant Hub API
        // TODO: Validate user has access to tenant
        
        TenantPlan tenantPlan = tenantPlanRepository
                .findByTenantId(tenantId)
                .orElseGet(() -> createDefaultPlan(tenantId));
        
        return mapToResponse(tenantPlan);
    }

    /**
     * Check if tenant is entitled to use specific app
     * Combines App Hub (enabled) and Billing Hub (plan allows) checks
     * TODO: Integrate with App Hub to check if app is enabled
     */
    @Transactional(readOnly = true)
    public EntitlementResponse checkEntitlement(UUID tenantId, AppCode appCode) {
        // TODO: Validate tenant is ACTIVE via Tenant Hub API
        // TODO: Validate user has access to tenant
        // TODO: Check if app is enabled in App Hub
        
        TenantPlan tenantPlan = tenantPlanRepository
                .findByTenantId(tenantId)
                .orElseGet(() -> createDefaultPlan(tenantId));
        
        boolean allowed = PlanRegistry.planAllowsApp(tenantPlan.getPlanCode(), appCode);
        String reason = allowed ? 
                "App allowed by plan: " + tenantPlan.getPlanCode().getDisplayName() :
                "App not allowed by plan: " + tenantPlan.getPlanCode().getDisplayName();
        
        return EntitlementResponse.builder()
                .tenantId(tenantId)
                .appCode(appCode.name())
                .allowed(allowed)
                .reason(reason)
                .build();
    }

    /**
     * Create default FREE plan for tenant
     * This is used when no plan is explicitly assigned
     */
    private TenantPlan createDefaultPlan(UUID tenantId) {
        TenantPlan defaultPlan = TenantPlan.builder()
                .tenantId(tenantId)
                .planCode(PlanCode.FREE)
                .assignedAt(java.time.LocalDateTime.now())
                .build();
        
        return tenantPlanRepository.save(defaultPlan);
    }

    private TenantPlanResponse mapToResponse(TenantPlan tenantPlan) {
        return TenantPlanResponse.builder()
                .id(tenantPlan.getId())
                .tenantId(tenantPlan.getTenantId())
                .planCode(tenantPlan.getPlanCode())
                .planDisplayName(tenantPlan.getPlanCode().getDisplayName())
                .planDescription(tenantPlan.getPlanCode().getDescription())
                .assignedAt(tenantPlan.getAssignedAt())
                .createdAt(tenantPlan.getCreatedAt())
                .updatedAt(tenantPlan.getUpdatedAt())
                .build();
    }
}
