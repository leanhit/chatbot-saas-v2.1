package com.chatbot.core.tenant.model;

import jakarta.persistence.PostPersist;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * JPA Entity Listener for the Tenant entity.
 * Automatically invalidates tenants and tenant-key-to-id caches when tenant data changes (e.g. package upgrades).
 */
@Component
@Slf4j
public class TenantEntityListener implements ApplicationContextAware {

    private static ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        context = applicationContext;
    }

    @PostUpdate
    @PostPersist
    @PostRemove
    public void onTenantChange(Tenant tenant) {
        if (context == null) {
            return;
        }
        try {
            CacheManager cacheManager = context.getBean(CacheManager.class);
            
            // Evict tenants cache by tenant ID
            Cache tenantsCache = cacheManager.getCache("tenants");
            if (tenantsCache != null && tenant.getId() != null) {
                tenantsCache.evict(tenant.getId());
                log.info("🧹 [TenantEntityListener] Evicted tenants cache for ID: {}", tenant.getId());
            }
            
            // Evict tenant-key-to-id cache by tenantKey
            Cache keyCache = cacheManager.getCache("tenant-key-to-id");
            if (keyCache != null && tenant.getTenantKey() != null) {
                keyCache.evict(tenant.getTenantKey());
                log.info("🧹 [TenantEntityListener] Evicted tenant-key-to-id cache for key: {}", tenant.getTenantKey());
            }
        } catch (Exception e) {
            log.error("❌ [TenantEntityListener] Failed to evict tenant caches: {}", e.getMessage());
        }
    }
}
