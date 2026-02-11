package com.chatbot.core.tenant.profile.repository;

import com.chatbot.core.tenant.profile.model.TenantProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TenantProfileRepository
        extends JpaRepository<TenantProfile, Long> {

    Optional<TenantProfile> findByTenant_Id(Long tenantId);

    // Lấy tất cả profile của danh sách tenant
    List<TenantProfile> findByTenantIdIn(List<Long> tenantIds);
}
