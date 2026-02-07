package com.chatbot.modules.tenant.membership.repository;

import com.chatbot.modules.tenant.membership.model.MembershipStatus;
import com.chatbot.modules.tenant.membership.model.TenantJoinRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TenantJoinRequestRepository extends JpaRepository<TenantJoinRequest, Long> {
    List<TenantJoinRequest> findByUserIdAndStatus(UUID userId, MembershipStatus status);
    List<TenantJoinRequest> findByTenantIdAndStatus(UUID tenantId, MembershipStatus status);
    Optional<TenantJoinRequest> findByTenantIdAndUserId(UUID tenantId, UUID userId);
    boolean existsByTenantIdAndUserIdAndStatus(UUID tenantId, UUID userId, MembershipStatus status);
}
