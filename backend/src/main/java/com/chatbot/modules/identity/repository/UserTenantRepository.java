package com.chatbot.modules.identity.repository;

import com.chatbot.modules.identity.model.UserTenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * UserTenant repository for user-tenant mapping operations
 * v0.2: Updated to use UUID for tenant_id
 * TODO: Add invitation status queries
 * TODO: Add membership statistics
 */
@Repository
public interface UserTenantRepository extends JpaRepository<UserTenant, Long> {
    
    List<UserTenant> findByUserIdAndIsActive(Long userId, Boolean isActive);
    
    List<UserTenant> findByTenantIdAndIsActive(UUID tenantId, Boolean isActive);
    
    Optional<UserTenant> findByUserIdAndTenantId(Long userId, UUID tenantId);
    
    boolean existsByUserIdAndTenantId(Long userId, UUID tenantId);
    
    @Query("SELECT ut.tenantId FROM UserTenant ut WHERE ut.user.id = :userId AND ut.isActive = true")
    List<UUID> findActiveTenantIdsByUserId(@Param("userId") Long userId);
    
    @Query("SELECT COUNT(ut) FROM UserTenant ut WHERE ut.tenantId = :tenantId AND ut.isActive = true")
    long countActiveMembersByTenantId(@Param("tenantId") UUID tenantId);
}
