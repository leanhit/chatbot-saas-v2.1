package com.chatbot.core.tenant.repository;

import com.chatbot.core.tenant.model.TenantApiKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TenantApiKeyRepository extends JpaRepository<TenantApiKey, Long> {

    Optional<TenantApiKey> findByKeyHash(String keyHash);

    List<TenantApiKey> findByTenantId(Long tenantId);

    Optional<TenantApiKey> findByIdAndTenantId(Long id, Long tenantId);
}
