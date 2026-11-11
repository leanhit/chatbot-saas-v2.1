package com.chatbot.core.tenant.repository;

import com.chatbot.core.tenant.model.TenantWebhook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TenantWebhookRepository extends JpaRepository<TenantWebhook, Long> {

    List<TenantWebhook> findByTenantIdAndIsActiveTrue(Long tenantId);

    Optional<TenantWebhook> findByIdAndTenantId(Long id, Long tenantId);
}
