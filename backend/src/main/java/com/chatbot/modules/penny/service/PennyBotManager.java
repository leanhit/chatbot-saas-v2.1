package com.chatbot.modules.penny.service;

import com.chatbot.modules.penny.model.PennyBot;
import com.chatbot.modules.penny.model.PennyBotType;
import com.chatbot.modules.penny.repository.PennyBotRepository;
import com.chatbot.modules.tenant.infra.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Penny Bot Manager - Enhanced bot management với UUID tenant support
 * Adapted từ traloitudongV2 cho chatbot-saas-v2 architecture
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PennyBotManager {
    
    private final PennyBotRepository pennyBotRepository;
    
    /**
     * Create Penny Bot với tenant validation
     */
    @Transactional
    public PennyBot createBot(Long userId, String botName, PennyBotType botType, String description) {
        log.info("🤖 Creating Penny bot: {} of type: {} for user: {}", botName, botType, userId);
        
        // 1. Get current tenant context
        UUID tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new IllegalStateException("Tenant context not found");
        }
        
        // 2. Check if bot name already exists for tenant
        if (pennyBotRepository.existsByTenantIdAndBotName(tenantId, botName)) {
            throw new IllegalArgumentException("Bot with name '" + botName + "' already exists for this tenant");
        }
        
        // 3. Create bot
        PennyBot pennyBot = PennyBot.builder()
                .id(UUID.randomUUID())
                .botName(botName)
                .botType(botType)
                .tenantId(tenantId)
                .userId(userId)
                .botpressBotId(generateBotpressBotId(botType))
                .description(description)
                .isActive(true)
                .isEnabled(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        PennyBot savedBot = pennyBotRepository.save(pennyBot);
        log.info("✅ Created Penny bot: {} with ID: {}", savedBot.getBotName(), savedBot.getId());
        
        return savedBot;
    }
    
    /**
     * Get all bots for current user
     */
    @Transactional(readOnly = true)
    public List<PennyBot> getBotsForUser(Long userId) {
        UUID tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return List.of();
        }
        
        return pennyBotRepository.findByTenantIdAndUserId(tenantId, userId);
    }
    
    /**
     * Get all bots for current tenant
     */
    @Transactional(readOnly = true)
    public List<PennyBot> getBotsForTenant() {
        UUID tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return List.of();
        }
        
        return pennyBotRepository.findByTenantId(tenantId);
    }
    
    /**
     * Get active bots for current tenant
     */
    @Transactional(readOnly = true)
    public List<PennyBot> getActiveBotsForTenant() {
        UUID tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return List.of();
        }
        
        return pennyBotRepository.findByTenantIdAndIsActiveAndIsEnabled(tenantId, true, true);
    }
    
    /**
     * Get bot by ID (with tenant security check)
     */
    @Transactional(readOnly = true)
    public PennyBot getBot(UUID botId) {
        UUID tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new IllegalStateException("Tenant context not found");
        }
        
        return pennyBotRepository.findByIdAndTenantId(botId, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Bot not found or access denied"));
    }
    
    /**
     * Update bot information
     */
    @Transactional
    public PennyBot updateBot(UUID botId, Map<String, Object> updates, Long userId) {
        log.info("📝 Updating Penny bot: {} by user: {}", botId, userId);
        
        PennyBot existingBot = getBot(botId);
        
        // Check ownership
        if (!existingBot.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Access denied: You don't own this bot");
        }
        
        // Apply updates
        updates.forEach((key, value) -> {
            switch (key) {
                case "botName" -> existingBot.setBotName((String) value);
                case "description" -> existingBot.setDescription((String) value);
                case "isActive" -> existingBot.setIsActive((Boolean) value);
                case "isEnabled" -> existingBot.setIsEnabled((Boolean) value);
                case "configuration" -> existingBot.setConfiguration((String) value);
                default -> log.warn("Unknown update field: {}", key);
            }
        });
        
        existingBot.setUpdatedAt(LocalDateTime.now());
        PennyBot updatedBot = pennyBotRepository.save(existingBot);
        
        log.info("✅ Updated Penny bot: {}", updatedBot.getBotName());
        return updatedBot;
    }
    
    /**
     * Toggle bot status (active/inactive)
     */
    @Transactional
    public PennyBot toggleBotStatus(UUID botId, Boolean enabled, Long userId) {
        log.info("🔄 Toggling Penny bot: {} to {} by user: {}", botId, enabled ? "enabled" : "disabled", userId);
        
        PennyBot existingBot = getBot(botId);
        
        // Check ownership
        if (!existingBot.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Access denied: You don't own this bot");
        }
        
        existingBot.setIsEnabled(enabled);
        existingBot.setUpdatedAt(LocalDateTime.now());
        
        PennyBot updatedBot = pennyBotRepository.save(existingBot);
        
        log.info("✅ Toggled Penny bot: {} to {}", updatedBot.getBotName(), enabled);
        return updatedBot;
    }
    
    /**
     * Delete bot (hard delete)
     */
    @Transactional
    public Boolean deleteBot(UUID botId, Long userId) {
        log.info("🗑️ Deleting Penny bot: {} by user: {}", botId, userId);
        
        try {
            PennyBot existingBot = getBot(botId);
            
            // Check ownership
            if (!existingBot.getUserId().equals(userId)) {
                throw new IllegalArgumentException("Access denied: You don't own this bot");
            }
            
            // Check if bot can be deleted
            if (existingBot.getIsActive()) {
                throw new IllegalStateException("Cannot delete active bot. Please deactivate it first.");
            }
            
            pennyBotRepository.delete(existingBot);
            
            log.info("✅ Deleted Penny bot: {}", existingBot.getBotName());
            return true;
            
        } catch (IllegalArgumentException e) {
            log.warn("⚠️ Validation error deleting bot {}: {}", botId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("❌ Runtime error deleting bot {}: {}", botId, e.getMessage(), e);
            throw new RuntimeException("Failed to delete Penny bot", e);
        }
    }
    
    /**
     * Get bot health status
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getBotHealth(UUID botId) {
        log.info("🏥 Getting health status for bot: {}", botId);
        
        try {
            PennyBot bot = getBot(botId);
            
            return Map.of(
                "botId", botId.toString(),
                "botName", bot.getBotName(),
                "botType", bot.getBotType().name(),
                "isActive", bot.getIsActive(),
                "isEnabled", bot.getIsEnabled(),
                "lastUsedAt", bot.getLastUsedAt() != null ? bot.getLastUsedAt().toString() : null,
                "createdAt", bot.getCreatedAt().toString(),
                "status", bot.getIsActive() && bot.getIsEnabled() ? "healthy" : "unhealthy",
                "message", bot.getIsActive() && bot.getIsEnabled() ? "Bot is operational" : "Bot is disabled"
            );
        } catch (Exception e) {
            return Map.of(
                "botId", botId.toString(),
                "status", "error",
                "message", "Failed to get bot health: " + e.getMessage()
            );
        }
    }
    
    /**
     * Get bot analytics
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getBotAnalytics(UUID botId, String timeRange, Long userId) {
        log.info("📊 Getting analytics for bot: {} by user: {} with range: {}", botId, userId, timeRange);
        
        try {
            PennyBot bot = getBot(botId);
            
            // TODO: Implement actual analytics logic
            // This would query message logs, user interactions, etc.
            
            return Map.of(
                "botId", botId.toString(),
                "botName", bot.getBotName(),
                "timeRange", timeRange,
                "totalMessages", 0, // Placeholder
                "activeUsers", 0, // Placeholder
                "responseTime", 0.0, // Placeholder
                "successRate", 100.0, // Placeholder
                "lastActivity", bot.getLastUsedAt() != null ? bot.getLastUsedAt().toString() : null
            );
        } catch (Exception e) {
            return Map.of(
                "botId", botId.toString(),
                "status", "error",
                "message", "Failed to get bot analytics: " + e.getMessage()
            );
        }
    }
    
    /**
     * Generate Botpress bot ID based on type
     */
    private String generateBotpressBotId(PennyBotType botType) {
        return botType.getBotpressBotId();
    }
}
