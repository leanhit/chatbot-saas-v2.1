package com.chatbot.core.customer.repository;

import com.chatbot.core.customer.model.CustomerProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerProfileRepository extends JpaRepository<CustomerProfile, Long> {

    Optional<CustomerProfile> findByExternalIdAndTenantId(String externalId, Long tenantId);

    List<CustomerProfile> findByTenantId(Long tenantId);

    List<CustomerProfile> findByTenantIdAndStatus(Long tenantId, CustomerProfile.CustomerStatus status);

    List<CustomerProfile> findByTenantIdAndTier(Long tenantId, CustomerProfile.CustomerTier tier);

    boolean existsByExternalIdAndTenantId(String externalId, Long tenantId);
}
