package com.chatbot.core.message.store.repository;

import com.chatbot.core.message.store.model.SLAConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SLAConfigurationRepository extends JpaRepository<SLAConfiguration, Long> {

    /**
     * Find SLA configuration by tenant and customer tier
     */
    Optional<SLAConfiguration> findByTenantIdAndCustomerTierAndActiveTrue(
        @Param("tenantId") Long tenantId,
        @Param("customerTier") String customerTier
    );

    /**
     * Find all active SLA configurations for a tenant
     */
    List<SLAConfiguration> findByTenantIdAndActiveOrderByCustomerTierAsc(
        @Param("tenantId") Long tenantId,
        @Param("active") Boolean active
    );

    /**
     * Find all SLA configurations for a tenant
     */
    List<SLAConfiguration> findByTenantId(@Param("tenantId") Long tenantId);

    /**
     * Check if SLA configuration exists for tenant and tier
     */
    boolean existsByTenantIdAndCustomerTier(
        @Param("tenantId") Long tenantId,
        @Param("customerTier") String customerTier
    );

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional(value = "messageTransactionManager", rollbackFor = Exception.class)
    @org.springframework.data.jpa.repository.Query("DELETE FROM SLAConfiguration s WHERE s.tenantId = :tenantId")
    void deleteByTenantId(@Param("tenantId") Long tenantId);
}
