package com.chatbot.core.tenant.scheduler;

import com.chatbot.core.tenant.model.Tenant;
import com.chatbot.core.tenant.repository.TenantRepository;
import com.chatbot.core.tenant.service.TenantPackageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduler for automatic handling of expired tenant packages
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TenantSubscriptionScheduler {

    private final TenantRepository tenantRepository;
    private final TenantPackageService tenantPackageService;

    /**
     * Periodically check and downgrade expired tenant subscriptions to 'free' package.
     * Runs every hour at the start of the hour.
     * Note: Transaction scope is limited to individual tenant downgrades to prevent batch rollback
     */
    @Scheduled(cron = "0 0 * * * *")
    public void downgradeExpiredTenants() {
        log.info("⏰ [TenantSubscriptionScheduler] Checking for expired tenant subscriptions...");
        try {
            LocalDateTime now = LocalDateTime.now();
            List<Tenant> expiredTenants = tenantRepository.findExpiredTenants(now);

            if (expiredTenants.isEmpty()) {
                log.debug("[TenantSubscriptionScheduler] No expired subscriptions found.");
                return;
            }

            log.info("[TenantSubscriptionScheduler] Found {} expired subscriptions to process.", expiredTenants.size());
            for (Tenant tenant : expiredTenants) {
                try {
                    log.warn("🚨 [TenantSubscriptionScheduler] Tenant '{}' (ID: {}, Key: {}) has expired (Expiry: {}). Downgrading to free package.",
                            tenant.getName(), tenant.getId(), tenant.getTenantKey(), tenant.getExpiresAt());
                    
                    // Each tenant downgrade runs in its own transaction (REQUIRES_NEW in upgradeTenantPackage)
                    tenantPackageService.upgradeTenantPackage(tenant.getId(), "free");
                } catch (Exception e) {
                    log.error("❌ [TenantSubscriptionScheduler] Failed to downgrade tenant ID {}: {}", tenant.getId(), e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            log.error("❌ [TenantSubscriptionScheduler] Error scanning expired subscriptions: {}", e.getMessage(), e);
        }
    }
}
