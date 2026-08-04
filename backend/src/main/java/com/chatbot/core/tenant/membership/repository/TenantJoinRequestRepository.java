package com.chatbot.core.tenant.membership.repository;

import com.chatbot.core.tenant.membership.model.MembershipStatus;
import com.chatbot.core.tenant.membership.model.TenantJoinRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TenantJoinRequestRepository extends JpaRepository<TenantJoinRequest, Long> {
    List<TenantJoinRequest> findByUserIdAndStatus(Long userId, MembershipStatus status);
    List<TenantJoinRequest> findByTenant_IdAndStatus(Long tenantId, MembershipStatus status);
    List<TenantJoinRequest> findByTenant_IdOrderByCreatedAtDesc(Long tenantId);
    Optional<TenantJoinRequest> findByTenant_IdAndUserId(Long tenantId, Long userId);
    boolean existsByTenant_IdAndUserIdAndStatus(Long tenantId, Long userId, MembershipStatus status);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional(value = "tenantTransactionManager", rollbackFor = Exception.class)
    @org.springframework.data.jpa.repository.Query("DELETE FROM TenantJoinRequest r WHERE r.tenant.id = :tenantId")
    void deleteByTenantId(@org.springframework.data.repository.query.Param("tenantId") Long tenantId);
}
