package com.chatbot.core.tenant.event;

import com.chatbot.shared.address.model.OwnerType;
import com.chatbot.shared.address.service.AddressService;
import com.chatbot.core.tenant.model.Tenant;
import com.chatbot.core.tenant.repository.TenantRepository;
import com.chatbot.core.tenant.service.TenantPackageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Event listener for tenant lifecycle events.
 * Handles cross-datasource initialization tasks after Tenant creation completes.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TenantEventListener {

    private final AddressService addressService;
    private final TenantPackageService tenantPackageService;
    private final TenantRepository tenantRepository;

    /**
     * Executes AFTER the tenant creation transaction has successfully committed to Tenant Hub DB.
     * Ensures cross-datasource operations (Shared DB address, default package assignment)
     * are decoupled from the main HTTP transaction.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTenantCreated(TenantCreatedEvent event) {
        log.info("[TenantEventListener] Received TenantCreatedEvent for tenantId={}, key={}", 
                event.getTenantId(), event.getTenantKey());

        // 1. Initialize Address in Shared DB
        try {
            addressService.getOrCreateSingleAddress(event.getTenantId(), OwnerType.TENANT, event.getTenantId());
            log.info("[TenantEventListener] Successfully initialized empty address for tenantId={}", event.getTenantId());
        } catch (Exception e) {
            log.error("[TenantEventListener] Failed to initialize address for tenantId={}: {}", 
                    event.getTenantId(), e.getMessage(), e);
        }

        // 2. Assign Default Package in Tenant DB
        try {
            tenantRepository.findById(event.getTenantId()).ifPresent(tenant -> {
                tenantPackageService.assignDefaultPackageToTenant(tenant);
                log.info("[TenantEventListener] Successfully assigned default package to tenantId={}", event.getTenantId());
            });
        } catch (Exception e) {
            log.error("[TenantEventListener] Failed to assign default package to tenantId={}: {}", 
                    event.getTenantId(), e.getMessage(), e);
        }
    }
}
