package com.chatbot.core.tenant.service;

import com.chatbot.core.tenant.infra.TenantContext;
import com.chatbot.core.tenant.model.TenantWebhook;
import com.chatbot.core.tenant.repository.TenantWebhookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * CRUD for tenant outbound webhook endpoints (conversation events).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TenantWebhookService {

    private final TenantWebhookRepository webhookRepository;
    private final TenantAuditLogService auditLogService;
    private final TenantPermissionValidator permissionValidator;

    private String currentUserEmail() {
        try {
            return permissionValidator.getCurrentUserEmail();
        } catch (Exception e) {
            return "system";
        }
    }

    private Long requireTenantId() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new IllegalStateException("Tenant context not set");
        }
        return tenantId;
    }

    @Transactional(value = "tenantTransactionManager", readOnly = true)
    public List<TenantWebhook> listWebhooks() {
        return webhookRepository.findByTenantIdAndIsActiveTrue(requireTenantId());
    }

    @Transactional(value = "tenantTransactionManager")
    public TenantWebhook createWebhook(TenantWebhook webhook) {
        Long tenantId = requireTenantId();
        webhook.setId(null);
        webhook.setTenantId(tenantId);
        webhook.setIsActive(true);
        TenantWebhook saved = webhookRepository.save(webhook);
        auditLogService.logActionAsync(tenantId, currentUserEmail(),
                "TENANT_WEBHOOK_CREATED", "webhookId=" + saved.getId() + ", url=" + saved.getUrl());
        return saved;
    }

    @Transactional(value = "tenantTransactionManager")
    public TenantWebhook updateWebhook(Long id, TenantWebhook updated) {
        Long tenantId = requireTenantId();
        TenantWebhook existing = webhookRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException("Webhook not found: " + id));
        existing.setUrl(updated.getUrl());
        existing.setSecret(updated.getSecret());
        existing.setEvents(updated.getEvents());
        existing.setIsActive(updated.getIsActive());
        TenantWebhook saved = webhookRepository.save(existing);
        auditLogService.logActionAsync(tenantId, currentUserEmail(),
                "TENANT_WEBHOOK_UPDATED", "webhookId=" + id);
        return saved;
    }

    @Transactional(value = "tenantTransactionManager")
    public void deleteWebhook(Long id) {
        Long tenantId = requireTenantId();
        TenantWebhook webhook = webhookRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException("Webhook not found: " + id));
        webhookRepository.delete(webhook);
        auditLogService.logActionAsync(tenantId, currentUserEmail(),
                "TENANT_WEBHOOK_DELETED", "webhookId=" + id + ", url=" + webhook.getUrl());
    }
}
