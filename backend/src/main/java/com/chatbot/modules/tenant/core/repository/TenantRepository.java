package com.chatbot.modules.tenant.core.repository;

import com.chatbot.modules.tenant.core.model.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

/**
 * Tenant repository for v0.1
 * Simplified tenant data access - basic CRUD only
 */
public interface TenantRepository extends JpaRepository<Tenant, UUID> {

    /**
     * Find tenants by user ID through membership
     */
    @Query("SELECT DISTINCT t FROM Tenant t " +
           "JOIN TenantMember tm ON t.id = tm.tenant.id " +
           "WHERE tm.userId = :userId")
    List<Tenant> findByUserId(UUID userId);
}
