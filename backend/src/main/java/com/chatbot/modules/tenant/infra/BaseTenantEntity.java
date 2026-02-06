package com.chatbot.modules.tenant.infra;

import jakarta.persistence.*;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@MappedSuperclass
@FilterDef(
        name = "tenantFilter",
        parameters = @ParamDef(name = "tenantId", type = UUID.class)
)
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
@Slf4j
public abstract class BaseTenantEntity {

    @Column(name = "tenant_id", nullable = false, updatable = false)
    private UUID tenantId;

    @PrePersist
    protected void assignTenantId() {
        if (tenantId == null) {
            UUID tenantIdFromContext = getCurrentTenantId();
            if (tenantIdFromContext == null) {
                throw new IllegalStateException("Tenant ID is missing");
            }
            this.tenantId = tenantIdFromContext;
            log.debug("Assigned tenant ID {} to entity {}", tenantIdFromContext, this.getClass().getSimpleName());
        }
    }

    /**
     * Get current tenant ID from context
     * This method provides a way to override tenant resolution for testing
     */
    protected UUID getCurrentTenantId() {
        // Try to get from TenantContextProvider if available
        try {
            TenantContextProvider provider = TenantContextProviderInstance.getProvider();
            if (provider != null) {
                return provider.getCurrentTenantId();
            }
        } catch (Exception e) {
            log.debug("Could not get tenant from TenantContextProvider, falling back to ThreadLocal", e);
        }
        
        // Fallback to ThreadLocal for backward compatibility
        return TenantContext.getTenantId();
    }

    public UUID getTenantId() {
        return tenantId;
    }

    /**
     * Static holder for TenantContextProvider instance
     * This avoids circular dependency issues
     */
    @Component
    public static class TenantContextProviderInstance {
        private static TenantContextProvider provider;

        @Autowired
        public void setProvider(TenantContextProvider provider) {
            TenantContextProviderInstance.provider = provider;
        }

        public static TenantContextProvider getProvider() {
            return provider;
        }
    }
}
