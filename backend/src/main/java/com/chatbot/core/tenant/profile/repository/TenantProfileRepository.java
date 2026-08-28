package com.chatbot.core.tenant.profile.repository;

import com.chatbot.core.tenant.profile.model.TenantProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface TenantProfileRepository
        extends JpaRepository<TenantProfile, Long> {

    Optional<TenantProfile> findByTenant_Id(Long tenantId);

    // Lấy tất cả profile của danh sách tenant
    List<TenantProfile> findByTenantIdIn(List<Long> tenantIds);

    @Modifying
    @Query(value = "INSERT INTO tenant_profiles (tenant_id) VALUES (:tenantId)", nativeQuery = true)
    void insertProfile(Long tenantId);

    @Modifying
    @Query("DELETE FROM TenantProfile p WHERE p.tenant.id = :tenantId")
    void deleteByTenantId(@org.springframework.data.repository.query.Param("tenantId") Long tenantId);
}
