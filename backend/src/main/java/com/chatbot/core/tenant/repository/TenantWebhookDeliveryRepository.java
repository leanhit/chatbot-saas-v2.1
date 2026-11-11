package com.chatbot.core.tenant.repository;

import com.chatbot.core.tenant.model.TenantWebhookDelivery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TenantWebhookDeliveryRepository extends JpaRepository<TenantWebhookDelivery, Long> {

    @Query("SELECT d FROM TenantWebhookDelivery d WHERE d.status = 'PENDING' AND d.nextRetryAt <= :now ORDER BY d.nextRetryAt ASC")
    List<TenantWebhookDelivery> findPendingDue(@Param("now") LocalDateTime now);

    List<TenantWebhookDelivery> findByTenantIdOrderByCreatedAtDesc(Long tenantId);
}
