package com.chatbot.modules.app.core.repository;

import com.chatbot.modules.app.core.model.TenantApp;
import com.chatbot.modules.app.core.model.AppCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * TenantApp repository for v0.1
 * App status data access per tenant
 */
public interface TenantAppRepository extends JpaRepository<TenantApp, UUID> {

    /**
     * Find tenant app by tenant ID and app code
     */
    Optional<TenantApp> findByTenantIdAndAppCode(UUID tenantId, AppCode appCode);

    /**
     * Find all apps for a tenant
     */
    List<TenantApp> findByTenantId(UUID tenantId);

    /**
     * Check if app exists for tenant
     */
    boolean existsByTenantIdAndAppCode(UUID tenantId, AppCode appCode);
}
