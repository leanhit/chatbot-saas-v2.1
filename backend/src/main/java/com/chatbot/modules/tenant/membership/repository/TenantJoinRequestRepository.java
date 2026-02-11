package com.chatbot.modules.tenant.membership.repository;

import com.chatbot.modules.tenant.membership.model.MembershipStatus;
import com.chatbot.modules.tenant.membership.model.TenantJoinRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TenantJoinRequestRepository extends JpaRepository<TenantJoinRequest, Long> {
    List<TenantJoinRequest> findByUser_IdAndStatus(Long userId, MembershipStatus status);
    List<TenantJoinRequest> findByTenant_IdAndStatus(Long tenantId, MembershipStatus status);
    Optional<TenantJoinRequest> findByTenant_IdAndUser_Id(Long tenantId, Long userId);
    boolean existsByTenant_IdAndUser_IdAndStatus(Long tenantId, Long userId, MembershipStatus status);
}
