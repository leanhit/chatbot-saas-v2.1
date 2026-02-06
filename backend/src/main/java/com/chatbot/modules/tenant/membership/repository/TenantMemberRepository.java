package com.chatbot.modules.tenant.membership.repository;

import com.chatbot.modules.tenant.membership.model.TenantMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Tenant member repository for v0.1
 * Simplified member data access
 */
public interface TenantMemberRepository extends JpaRepository<TenantMember, UUID> {

    /**
     * Find member by tenant and user IDs
     */
    Optional<TenantMember> findByTenantIdAndUserId(UUID tenantId, UUID userId);

    /**
     * Find all members of a tenant
     */
    List<TenantMember> findByTenantId(UUID tenantId);

    /**
     * Check if user is member of tenant
     */
    boolean existsByTenantIdAndUserId(UUID tenantId, UUID userId);

    /**
     * Count members by role in tenant
     */
    @Query("SELECT COUNT(tm) FROM TenantMember tm " +
           "WHERE tm.tenant.id = :tenantId AND tm.role = :role")
    long countByTenantIdAndRole(@Param("tenantId") UUID tenantId, @Param("role") String role);

    /**
     * Find members by user ID
     */
    List<TenantMember> findByUserId(UUID userId);
}
