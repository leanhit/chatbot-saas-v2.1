package com.chatbot.core.user.model;

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
 * JPA Entity Listener for the User entity.
 * Automatically invalidates userSessions cache when user data changes (e.g. balance updates, roles, passwords).
 */
@Component
@Slf4j
public class UserEntityListener implements ApplicationContextAware {

    private static ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        context = applicationContext;
    }

    @PostUpdate
    @PostPersist
    @PostRemove
    public void onUserChange(User user) {
        if (context == null) {
            return;
        }
        try {
            CacheManager cacheManager = context.getBean(CacheManager.class);
            Cache sessionsCache = cacheManager.getCache("userSessions");
            if (sessionsCache != null && user.getEmail() != null) {
                sessionsCache.evict(user.getEmail());
                log.info("🧹 [UserEntityListener] Evicted userSessions cache for email: {}", user.getEmail());
            }
            Cache usersCache = cacheManager.getCache("users");
            if (usersCache != null && user.getId() != null) {
                usersCache.evict(user.getId());
                log.info("🧹 [UserEntityListener] Evicted users cache for ID: {}", user.getId());
            }
        } catch (Exception e) {
            log.error("❌ [UserEntityListener] Failed to evict user caches: {}", e.getMessage());
        }
    }
}
