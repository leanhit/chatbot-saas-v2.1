package com.chatbot.core.cache;

import com.chatbot.core.tenant.repository.TenantRepository;
import com.chatbot.core.tenant.service.TenantService;
import com.chatbot.shared.penny.repository.PennyBotRepository;
import com.chatbot.shared.penny.service.PennyBotService;
import com.chatbot.core.simplepayment.service.CachedPackageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.EventListener;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Cache Warmer for Chatbot SaaS v2.1
 * Pre-loads frequently accessed data into cache
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CacheWarmer {

    private final RedisTemplate<String, Object> redisTemplate;


    private final TenantRepository tenantRepository;


    private final PennyBotRepository pennyBotRepository;


    @Lazy
    private final TenantService tenantService;


    @Lazy
    private final PennyBotService pennyBotService;


    @Lazy
    private final CachedPackageService cachedPackageService;

    /**
     * Auto-run cache warming once application is fully ready and started
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("[CACHE WARMER] Application is ready. Triggering database cache warm-up...");
        warmUpCache();
    }

    /**
     * Warm up cache with frequently accessed data
     */
    public void warmUpCache() {
        log.info("Starting cache warm-up");
        
        try {
            // Warm up package configurations
            warmUpPackages();
            
            // Warm up tenant data
            warmUpTenants();
            
            // Warm up chatbot configurations
            warmUpChatbots();
            
            log.info("Cache warm-up completed successfully");
        } catch (Exception e) {
            log.error("Error during cache warm-up", e);
        }
    }

    private void warmUpPackages() {
        log.debug("Warming up packages cache");
        try {
            cachedPackageService.warmUpCaches();
        } catch (Exception e) {
            log.error("Failed to warm up packages cache", e);
        }
    }

    private void warmUpTenants() {
        log.debug("Warming up tenants cache");
        try {
            List<com.chatbot.core.tenant.model.Tenant> activeTenants = tenantRepository.findAll();
            int count = 0;
            for (com.chatbot.core.tenant.model.Tenant tenant : activeTenants) {
                if (tenant.getStatus() == com.chatbot.core.tenant.model.TenantStatus.ACTIVE) {
                    tenantService.getTenant(tenant.getId());
                    tenantService.getTenantIdByKey(tenant.getTenantKey());
                    count++;
                }
            }
            log.info("Warmed up tenants cache for {} active tenants", count);
        } catch (Exception e) {
            log.error("Failed to warm up tenants cache", e);
        }
    }

    private void warmUpChatbots() {
        log.debug("Warming up chatbots cache");
        try {
            List<com.chatbot.shared.penny.model.PennyBot> activeBots = pennyBotRepository.findAll();
            int count = 0;
            for (com.chatbot.shared.penny.model.PennyBot bot : activeBots) {
                if (bot.isActive() && bot.isEnabled()) {
                    pennyBotService.getBotById(bot.getId());
                    count++;
                }
            }
            log.info("Warmed up chatbots cache for {} active bots", count);
        } catch (Exception e) {
            log.error("Failed to warm up chatbots cache", e);
        }
    }
}
