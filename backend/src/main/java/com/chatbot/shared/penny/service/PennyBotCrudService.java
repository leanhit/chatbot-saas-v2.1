package com.chatbot.shared.penny.service;

import com.chatbot.shared.penny.context.ContextManager;
import com.chatbot.shared.penny.analytics.AnalyticsCollector;
import com.chatbot.shared.penny.model.PennyBot;
import com.chatbot.shared.penny.model.PennyBotType;
import com.chatbot.shared.penny.repository.PennyBotRepository;
import com.chatbot.core.tenant.infra.TenantContext;
import com.chatbot.core.tenant.service.PackageLimitValidationService;
import com.chatbot.spokes.facebook.connection.model.FacebookConnection;
import com.chatbot.spokes.facebook.connection.repository.FacebookConnectionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Penny Bot CRUD Service - Handles Bot lifecycle, CRUD operations, and message processing
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PennyBotCrudService {

    private final PennyBotRepository pennyBotRepository;
    private final ContextManager contextManager;
    private final AnalyticsCollector analyticsCollector;
    private final PackageLimitValidationService limitValidationService;
    private final com.chatbot.shared.penny.core.PennyMiddlewareEngine pennyMiddlewareEngine;
    private final com.chatbot.core.message.store.repository.ConversationRepository conversationRepository;
    private final com.chatbot.shared.penny.security.InputSanitizer inputSanitizer;
    private final FacebookConnectionRepository facebookConnectionRepository;

    /**
     * Create Penny Bot with tenant validation and type selection
     */
    @Transactional
    public PennyBot createBot(String ownerId, String botName, PennyBotType botType, String description) {
        log.info("🤖 Creating Penny bot: {} of type: {} for owner: {}", botName, botType, ownerId);
        
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new IllegalStateException("Tenant context not found");
        }
        
        log.info("🔍 Checking chatbot limit for tenant {} before creating bot", tenantId);
        limitValidationService.validateChatbotCreation(tenantId);
        log.info("✅ Chatbot limit validation passed for tenant {}", tenantId);
        
        if (pennyBotRepository.existsByTenantIdAndBotTypeAndIsActiveTrue(tenantId, botType)) {
            throw new IllegalStateException(
                "Tenant " + tenantId + " already has an active " + botType.getDisplayName() + " bot"
            );
        }
        
        PennyBot newBot = PennyBot.builder()
            .id(UUID.randomUUID())
            .botName(botName)
            .botType(botType)
            .tenantId(tenantId)
            .ownerId(ownerId)
            .pennyBotId(botType.getPennyBotId())
            .description(description)
            .isActive(true)
            .isEnabled(true)
            .build();
        
        PennyBot savedBot = pennyBotRepository.save(newBot);
        
        try {
            contextManager.initializeBotContext(savedBot.getId().toString(), tenantId, botType);
            log.info("🧠 Initialized Penny context for bot: {}", savedBot.getId());
        } catch (Exception e) {
            log.error("❌ Failed to initialize context for bot {}: {}", savedBot.getId(), e.getMessage());
        }
        
        try {
            analyticsCollector.configureBotAnalytics(savedBot.getId().toString());
            log.info("📊 Configured analytics for bot: {}", savedBot.getId());
        } catch (Exception e) {
            log.error("❌ Failed to configure analytics for bot {}: {}", savedBot.getId(), e.getMessage());
        }
        
        log.info("✅ Penny bot created successfully: {} (ID: {})", savedBot.getBotName(), savedBot.getId());
        return savedBot;
    }

    /**
     * Auto-create bot for Facebook connection based on tenant needs
     */
    @Transactional
    public PennyBot autoCreateBotForConnection(String ownerId, String pageId) {
        log.info("🤖 Auto-creating Penny bot for connection: pageId={}, owner={}", pageId, ownerId);
        
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new IllegalStateException("Tenant context not found for auto-creation");
        }
        
        PennyBotType botType = determineBestBotType(tenantId);
        
        String botName = "Auto-Bot-" + pageId + "-" + botType.name().toLowerCase();
        String description = "Auto-generated " + botType.getDisplayName() + " bot for Facebook page: " + pageId;
        
        return createBot(ownerId, botName, botType, description);
    }

    /**
     * Update bot information
     */
    @Transactional
    public PennyBot updateBot(UUID botId, String botName, String description, Boolean isEnabled) {
        log.info("🔄 Updating Penny bot: {}", botId);
        
        PennyBot bot = pennyBotRepository.findById(botId)
            .orElseThrow(() -> new IllegalArgumentException("Bot not found: " + botId));
        
        if (botName != null && !botName.isBlank()) {
            bot.setBotName(botName);
        }
        
        if (description != null) {
            bot.setDescription(description);
        }
        
        if (isEnabled != null) {
            bot.setEnabled(isEnabled);
        }
        
        PennyBot updatedBot = pennyBotRepository.save(bot);
        log.info("✅ Penny bot updated successfully: {}", updatedBot.getId());
        
        return updatedBot;
    }

    /**
     * Update bot information with Map payload
     */
    @Transactional
    public PennyBot updateBot(UUID botId, Map<String, Object> updates, String ownerId) {
        log.info("📝 Updating Penny bot: {} with updates: {}", botId, updates);
        
        PennyBot bot = getBot(botId);
        
        if (!bot.getOwnerId().equals(ownerId)) {
            throw new AccessDeniedException("Not authorized to update this bot");
        }
        
        if (updates.containsKey("botName")) {
            bot.setBotName((String) updates.get("botName"));
        }
        
        if (updates.containsKey("botType")) {
            String botTypeStr = (String) updates.get("botType");
            bot.setBotType(PennyBotType.fromString(botTypeStr));
        }
        
        if (updates.containsKey("description")) {
            bot.setDescription((String) updates.get("description"));
        }
        
        if (updates.containsKey("isActive")) {
            bot.setActive((Boolean) updates.get("isActive"));
        }
        
        if (updates.containsKey("isEnabled")) {
            bot.setEnabled((Boolean) updates.get("isEnabled"));
        }
        
        bot.setUpdatedAt(java.time.LocalDateTime.now());
        
        PennyBot savedBot = pennyBotRepository.save(bot);
        
        log.info("✅ Penny bot updated successfully: {}", savedBot.getBotName());
        return savedBot;
    }

    /**
     * Toggle bot status (active/inactive)
     */
    @Transactional
    public PennyBot toggleBotStatus(UUID botId, boolean enabled, String ownerId) {
        log.info("🔄 Toggling Penny bot: {} to {} by owner: {}", botId, enabled, ownerId);
        
        PennyBot bot = getBot(botId);
        
        if (!bot.getOwnerId().equals(ownerId)) {
            throw new AccessDeniedException("Not authorized to toggle this bot status");
        }
        
        bot.setEnabled(enabled);
        bot.setActive(enabled);
        bot.setUpdatedAt(java.time.LocalDateTime.now());
        
        PennyBot savedBot = pennyBotRepository.save(bot);
        
        log.info("✅ Penny bot status toggled successfully: {} -> active: {}, enabled: {}", 
                savedBot.getBotName(), savedBot.isActive(), savedBot.isEnabled());
        return savedBot;
    }

    /**
     * Check if bot can be deleted (active connections & recent conversations)
     */
    public void checkBotCanBeDeleted(UUID botId, String ownerId) {
        log.info("🔍 Checking if bot can be deleted: {} by owner: {}", botId, ownerId);
        
        PennyBot bot = getBot(botId);
        
        if (!bot.getOwnerId().equals(ownerId)) {
            throw new IllegalArgumentException("Bot does not belong to owner: " + ownerId);
        }
        
        if (hasActiveConnections(botId)) {
            throw new IllegalStateException("Cannot delete bot: Bot has active connections. Please disconnect them first.");
        }
        
        if (hasRecentConversations(botId)) {
            throw new IllegalStateException("Cannot delete bot: Bot has recent conversations. Please wait or archive them first.");
        }
        
        log.info("✅ Bot {} can be safely deleted", botId);
    }

    /**
     * Delete bot with basic checks
     */
    @Transactional
    public boolean deleteBot(UUID botId, String ownerId) {
        log.info("🗑️ Deleting Penny bot: {} by owner: {}", botId, ownerId);
        
        checkBotCanBeDeleted(botId, ownerId);
        
        try {
            try {
                contextManager.cleanupBotContext(botId.toString());
                log.info("✅ Cleaned up Penny context for bot: {}", botId);
            } catch (Exception e) {
                log.warn("⚠️ Failed to cleanup Penny context for bot {}: {}", botId, e.getMessage());
            }
            
            try {
                analyticsCollector.cleanupBotAnalytics(botId.toString());
                log.info("✅ Cleaned up analytics for bot: {}", botId);
            } catch (Exception e) {
                log.warn("⚠️ Failed to cleanup analytics for bot {}: {}", botId, e.getMessage());
            }
            
            pennyBotRepository.hardDeleteBot(botId);
            log.info("✅ Hard deleted bot from database: {}", botId);
            
            log.info("✅ Penny bot deleted successfully: {}", botId);
            return true;
            
        } catch (Exception e) {
            log.error("❌ Critical error deleting Penny bot {}: {}", botId, e.getMessage(), e);
            throw new RuntimeException("Failed to delete bot: " + e.getMessage(), e);
        }
    }

    /**
     * Get bot by ID with tenant context
     */
    public PennyBot getBot(UUID botId) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId != null) {
            return pennyBotRepository.findByIdAndTenantId(botId, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Bot not found for tenant " + tenantId + ": " + botId));
        }
        return pennyBotRepository.findById(botId)
            .orElseThrow(() -> new IllegalArgumentException("Bot not found: " + botId));
    }

    /**
     * Get all bots for owner
     */
    public List<PennyBot> getBotsForOwner(String ownerId) {
        return pennyBotRepository.findActiveBotsByOwner(ownerId);
    }

    /**
     * Get all bots for current tenant
     */
    public List<PennyBot> getBotsForCurrentTenant() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new IllegalStateException("Tenant context not found");
        }
        return pennyBotRepository.findByTenantIdAndIsActiveTrue(tenantId);
    }

    /**
     * Process message through Penny middleware
     */
    public String processMessage(UUID botId, String message, String userId, boolean isTestMode) {
        return processMessage(botId, message, userId, null, isTestMode);
    }

    /**
     * Process message through Penny middleware with explicit ownerId for authorization
     */
    public String processMessage(UUID botId, String message, String userId, String ownerId, boolean isTestMode) {
        log.info("💬 Processing message for bot {} by userId {} - ownerId {} - TestMode: {} - Message: {}", botId, userId, ownerId, isTestMode, message);
        
        try {
            String sanitizedMessage = inputSanitizer.sanitizeMessage(message);
            
            if (inputSanitizer.isMalicious(message)) {
                log.warn("⚠️ Potentially malicious content detected in message for bot {}", botId);
                return "Tin nhắn của bạn chứa nội dung không hợp lệ. Vui lòng thử lại.";
            }
            
            if (!inputSanitizer.isValidLength(message)) {
                log.warn("⚠️ Message too long for bot {}", botId);
                return "Tin nhắn quá dài. Vui lòng gửi tin nhắn ngắn hơn.";
            }
            
            PennyBot bot = getBot(botId);
            
            if (ownerId != null && !"public".equals(ownerId)) {
                if (!bot.getOwnerId().equals(ownerId)) {
                    throw new AccessDeniedException("Not authorized to process messages for this bot");
                }
            }
            
            if (!bot.isActive() || !bot.isEnabled()) {
                return "Bot is currently inactive. Please activate the bot first.";
            }
            
            try {
                com.chatbot.shared.penny.dto.request.MiddlewareRequest middlewareRequest = 
                    com.chatbot.shared.penny.dto.request.MiddlewareRequest.builder()
                        .userId(userId)
                        .message(sanitizedMessage)
                        .platform("facebook")
                        .botId(botId.toString())
                        .timestamp(java.time.Instant.now())
                        .build();
                
                com.chatbot.shared.penny.dto.response.MiddlewareResponse response = 
                    pennyMiddlewareEngine.processMessage(middlewareRequest);
                
                bot.setLastUsedAt(java.time.LocalDateTime.now());
                pennyBotRepository.save(bot);
                
                return response.getResponse();
                
            } catch (Exception e) {
                log.error("❌ Error in PennyMiddlewareEngine processing: {}", e.getMessage(), e);
                return "Xin chào! Tôi đã nhận được tin nhắn của bạn. Hiện tại hệ thống đang gặp sự cố kỹ thuật, vui lòng thử lại sau.";
            }
            
        } catch (AccessDeniedException e) {
            log.error("❌ Access denied for bot {}: {}", botId, e.getMessage());
            return "Bạn không có quyền truy cập vào bot này.";
        } catch (Exception e) {
            log.error("❌ Error processing message for bot {}: {}", botId, e.getMessage(), e);
            return "Sorry, I encountered an error while processing your message. Please try again.";
        }
    }

    private boolean hasActiveConnections(UUID botId) {
        try {
            Long tenantId = TenantContext.getTenantId();
            List<FacebookConnection> activeConnections;
            if (tenantId != null) {
                activeConnections = facebookConnectionRepository.findAllByBotIdAndTenantIdAndIsActiveTrue(botId.toString(), tenantId);
            } else {
                activeConnections = facebookConnectionRepository.findAllByBotIdAndIsActiveTrue(botId.toString());
            }
            return !activeConnections.isEmpty();
        } catch (Exception e) {
            log.warn("Error checking active connections for bot {}: {}", botId, e.getMessage());
            return false;
        }
    }

    private boolean hasRecentConversations(UUID botId) {
        try {
            Long tenantId = TenantContext.getTenantId();
            List<FacebookConnection> connections;
            if (tenantId != null) {
                connections = facebookConnectionRepository.findAllByBotIdAndTenantIdAndIsActiveTrue(botId.toString(), tenantId);
            } else {
                connections = facebookConnectionRepository.findAllByBotIdAndIsActiveTrue(botId.toString());
            }
            
            if (connections.isEmpty()) {
                return false;
            }
            
            List<UUID> connectionIds = connections.stream()
                .map(FacebookConnection::getId)
                .toList();
                
            java.time.LocalDateTime sevenDaysAgo = java.time.LocalDateTime.now().minusDays(7);
            return conversationRepository.existsByConnectionIdInAndUpdatedAtAfter(connectionIds, sevenDaysAgo);
        } catch (Exception e) {
            log.warn("Error checking recent conversations for bot {}: {}", botId, e.getMessage());
            return false;
        }
    }

    private PennyBotType determineBestBotType(Long tenantId) {
        List<PennyBot> existingBots = pennyBotRepository.findByTenantIdAndIsActiveTrue(tenantId);
        
        if (existingBots.isEmpty()) {
            return PennyBotType.SUPPORT;
        }
        
        for (PennyBotType type : PennyBotType.values()) {
            boolean hasType = existingBots.stream()
                .anyMatch(bot -> bot.getBotType() == type);
            
            if (!hasType) {
                return type;
            }
        }
        
        return PennyBotType.GENERAL;
    }
}
