package com.chatbot.modules.penny.service;

import com.chatbot.modules.penny.context.ContextManager;
import com.chatbot.modules.penny.analytics.AnalyticsCollector;
import com.chatbot.modules.penny.model.PennyBot;
import com.chatbot.modules.penny.model.PennyBotType;
import com.chatbot.modules.penny.repository.PennyBotRepository;
import com.chatbot.modules.tenant.infra.TenantContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Penny Bot Manager - Enhanced bot management with intelligent features
 * Manages CRUD operations directly on middleware layer
 */
@Service
@Slf4j
public class PennyBotManager {
    
    private final PennyBotRepository pennyBotRepository;
    private final ContextManager contextManager;
    private final AnalyticsCollector analyticsCollector;
    
    public PennyBotManager(PennyBotRepository pennyBotRepository,
                         ContextManager contextManager,
                         AnalyticsCollector analyticsCollector) {
        this.pennyBotRepository = pennyBotRepository;
        this.contextManager = contextManager;
        this.analyticsCollector = analyticsCollector;
    }
    
    /**
     * Create Penny Bot with tenant validation and type selection
     */
    @Transactional
    public PennyBot createBot(String ownerId, String botName, PennyBotType botType, String description) {
        log.info("🤖 Creating Penny bot: {} of type: {} for owner: {}", botName, botType, ownerId);
        
        // 1. Get current tenant context
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new IllegalStateException("Tenant context not found");
        }
        
        // 2. Check if tenant already has bot of this type
        if (pennyBotRepository.existsByTenantIdAndBotTypeAndIsActiveTrue(tenantId, botType)) {
            throw new IllegalStateException(
                "Tenant " + tenantId + " already has an active " + botType.getDisplayName() + " bot"
            );
        }
        
        // 3. Create Penny Bot entity
        PennyBot newBot = PennyBot.builder()
            .id(UUID.randomUUID())
            .botName(botName)
            .botType(botType)
            .tenantId(tenantId)
            .ownerId(ownerId)
            .botpressBotId(botType.getBotpressBotId()) // Get from enum mapping
            .description(description)
            .isActive(true)
            .isEnabled(true)
            .build();
        
        // 4. Save to database
        PennyBot savedBot = pennyBotRepository.save(newBot);
        
        // 5. Initialize Penny context for bot
        try {
            contextManager.initializeBotContext(savedBot.getId().toString(), tenantId, botType);
            log.info("🧠 Initialized Penny context for bot: {}", savedBot.getId());
        } catch (Exception e) {
            log.error("❌ Failed to initialize context for bot {}: {}", savedBot.getId(), e.getMessage());
            // Continue anyway - bot is created, context can be fixed later
        }
        
        // 6. Configure analytics
        try {
            analyticsCollector.configureBotAnalytics(savedBot.getId().toString());
            log.info("📊 Configured analytics for bot: {}", savedBot.getId());
        } catch (Exception e) {
            log.error("❌ Failed to configure analytics for bot {}: {}", savedBot.getId(), e.getMessage());
            // Continue anyway - bot is created, analytics can be fixed later
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
        
        // Determine best bot type based on tenant's existing bots
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
     * Kiểm tra bot có thể xóa được không (connections và conversations)
     */
    public void checkBotCanBeDeleted(UUID botId, String ownerId) {
        log.info("� Checking if bot can be deleted: {} by owner: {}", botId, ownerId);
        
        PennyBot bot = getBot(botId);
        
        // Verify ownership
        if (!bot.getOwnerId().equals(ownerId)) {
            throw new IllegalArgumentException("Bot does not belong to owner: " + ownerId);
        }
        
        // 1. Check active connections
        if (hasActiveConnections(botId)) {
            throw new IllegalStateException("Cannot delete bot: Bot has active connections. Please disconnect them first.");
        }
        
        // 2. Check recent conversations
        if (hasRecentConversations(botId)) {
            throw new IllegalStateException("Cannot delete bot: Bot has recent conversations. Please wait or archive them first.");
        }
        
        log.info("✅ Bot {} can be safely deleted", botId);
    }
    
    /**
     * Delete bot (soft delete) with basic checks
     */
    @Transactional
    public boolean deleteBot(UUID botId, String ownerId) {
        log.info("🗑️ Deleting Penny bot: {} by owner: {}", botId, ownerId);
        
        // Check if bot can be deleted
        checkBotCanBeDeleted(botId, ownerId);
        
        try {
            // 1. Cleanup Penny context (non-critical)
            try {
                contextManager.cleanupBotContext(botId.toString());
                log.info("✅ Cleaned up Penny context for bot: {}", botId);
            } catch (Exception e) {
                log.warn("⚠️ Failed to cleanup Penny context for bot {}: {}", botId, e.getMessage());
            }
            
            // 2. Cleanup analytics (non-critical)
            try {
                analyticsCollector.cleanupBotAnalytics(botId.toString());
                log.info("✅ Cleaned up analytics for bot: {}", botId);
            } catch (Exception e) {
                log.warn("⚠️ Failed to cleanup analytics for bot {}: {}", botId, e.getMessage());
            }
            
            // 3. Hard delete from database (critical)
            pennyBotRepository.hardDeleteBot(botId);
            log.info("✅ Hard deleted bot from database: {}", botId);
            
            log.info("✅ Penny bot deleted successfully: {}", botId);
            return true;
            
        } catch (Exception e) {
            log.error("❌ Critical error deleting Penny bot {}: {}", botId, e.getMessage(), e);
            throw new RuntimeException("Failed to delete bot: " + e.getMessage(), e);
        }
    }
    
    // Helper methods
    private boolean hasActiveConnections(UUID botId) {
        // TODO: Implement actual connection checking
        // Query connection tables for active connections to this bot
        // For now, return false (no connections)
        return false;
    }
    
    private boolean hasRecentConversations(UUID botId) {
        // TODO: Implement actual conversation checking  
        // Query conversation tables for recent conversations (last 7 days)
        // For now, return false (no recent conversations)
        return false;
    }
    
    /**
     * Get bot by ID
     */
    public PennyBot getBot(UUID botId) {
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
     * Get bot health status
     */
    public Map<String, Object> getBotHealth(UUID botId) {
        PennyBot bot = getBot(botId);
        
        Map<String, Object> health = new java.util.HashMap<>();
        
        // Bot status
        health.put("botStatus", bot.isActive() && bot.isEnabled() ? "healthy" : "unhealthy");
        health.put("botType", bot.getBotType().name());
        health.put("botpressBotId", bot.getBotpressBotId());
        
        // Penny context health
        boolean contextHealthy = contextManager.isBotContextHealthy(botId.toString());
        health.put("context", contextHealthy ? "healthy" : "unhealthy");
        
        // Analytics health
        boolean analyticsHealthy = analyticsCollector.isBotAnalyticsHealthy(botId.toString());
        health.put("analytics", analyticsHealthy ? "healthy" : "unhealthy");
        
        // Overall status
        boolean overallHealthy = bot.isActive() && bot.isEnabled() && contextHealthy && analyticsHealthy;
        health.put("overall", overallHealthy ? "healthy" : "unhealthy");
        
        return health;
    }
    
    /**
     * Update bot information
     */
    @Transactional
    public PennyBot updateBot(UUID botId, Map<String, Object> updates, String ownerId) {
        log.info("📝 Updating Penny bot: {} with updates: {}", botId, updates);
        
        PennyBot bot = getBot(botId);
        
        // Verify ownership
        if (!bot.getOwnerId().equals(ownerId)) {
            throw new AccessDeniedException("Not authorized to update this bot");
        }
        
        // Update fields
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
        
        // Update timestamp
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
        
        // Verify ownership
        if (!bot.getOwnerId().equals(ownerId)) {
            throw new AccessDeniedException("Not authorized to toggle this bot status");
        }
        
        // Update both isActive and isEnabled for proper functionality
        bot.setEnabled(enabled);
        bot.setActive(enabled); // Also update isActive to match frontend logic
        bot.setUpdatedAt(java.time.LocalDateTime.now());
        
        PennyBot savedBot = pennyBotRepository.save(bot);
        
        log.info("✅ Penny bot status toggled successfully: {} -> active: {}, enabled: {}", 
                savedBot.getBotName(), savedBot.isActive(), savedBot.isEnabled());
        return savedBot;
    }
    
    /**
     * Process message through Penny middleware
     */
    public String processMessage(UUID botId, String message, String ownerId) {
        log.info("💬 Processing message for bot {} by {}: {}", botId, ownerId, message);
        
        try {
            PennyBot bot = getBot(botId);
            
            // Skip ownership check for public access (ownerId = "public")
            if (!"public".equals(ownerId)) {
                // Verify ownership for authenticated access
                if (!bot.getOwnerId().equals(ownerId)) {
                    throw new AccessDeniedException("Not authorized to process messages for this bot");
                }
            }
            
            // Check if bot is active
            if (!bot.isActive() || !bot.isEnabled()) {
                return "Bot is currently inactive. Please activate the bot first.";
            }
            
            // TODO: Implement actual Penny middleware processing
            // This would involve:
            // 1. Context retrieval
            // 2. Intent recognition
            // 3. Response generation
            // 4. Analytics logging
            
            // For now, return a simple response
            String botName = bot.getBotName();
            
            // Simple keyword-based responses
            if (message.toLowerCase().contains("hello") || message.toLowerCase().contains("hi")) {
                return String.format("Hello! I'm %s. How can I help you today?", botName);
            } else if (message.toLowerCase().contains("help")) {
                return "I can help you with various tasks. What would you like to know?";
            } else if (message.toLowerCase().contains("thank")) {
                return "You're welcome! Is there anything else I can help you with?";
            } else if (message.toLowerCase().contains("bye")) {
                return "Goodbye! Have a great day!";
            } else if (message.toLowerCase().contains("how are you")) {
                return String.format("I'm %s and I'm doing great! Thanks for asking.", botName);
            } else if (message.toLowerCase().contains("what can you do")) {
                return "I can assist with customer service, answer questions, and provide information based on my training.";
            } else {
                return String.format("I received your message: \"%s\". I'm here to help!", message);
            }
            
        } catch (Exception e) {
            log.error("❌ Error processing message for bot {}: {}", botId, e.getMessage(), e);
            return "Sorry, I encountered an error while processing your message. Please try again.";
        }
    }
    
    /**
     * Get bot analytics
     */
    public Map<String, Object> getBotAnalytics(UUID botId, String timeRange, String ownerId) {
        log.info("📊 Getting analytics for bot: {} with range: {}", botId, timeRange);
        
        PennyBot bot = getBot(botId);
        
        // Verify ownership
        if (!bot.getOwnerId().equals(ownerId)) {
            throw new AccessDeniedException("Not authorized to view analytics for this bot");
        }
        
        Map<String, Object> analytics = new java.util.HashMap<>();
        
        // Get analytics from collector (mock data for now)
        // Map<String, Object> collectorAnalytics = analyticsCollector.getBotAnalytics(botId.toString(), timeRange);
        
        // Mock analytics data
        Map<String, Object> collectorAnalytics = new java.util.HashMap<>();
        collectorAnalytics.put("totalConversations", Math.floor(Math.random() * 1000) + 100);
        collectorAnalytics.put("totalMessages", Math.floor(Math.random() * 5000) + 500);
        collectorAnalytics.put("averageResponseTime", Math.floor(Math.random() * 2000) + 500);
        collectorAnalytics.put("satisfactionRate", Math.floor(Math.random() * 30) + 70);
        collectorAnalytics.put("resolutionRate", Math.floor(Math.random() * 40) + 60);
        collectorAnalytics.put("errorRate", Math.floor(Math.random() * 10) + 1);
        collectorAnalytics.put("uptime", Math.floor(Math.random() * 20) + 80);
        
        // Add bot information
        analytics.put("botId", botId.toString());
        analytics.put("botName", bot.getBotName());
        analytics.put("botType", bot.getBotType().name());
        analytics.put("timeRange", timeRange);
        analytics.put("isActive", bot.isActive());
        analytics.put("isEnabled", bot.isEnabled());
        analytics.put("createdAt", bot.getCreatedAt().toString());
        analytics.put("lastUsedAt", bot.getLastUsedAt() != null ? bot.getLastUsedAt().toString() : null);
        
        // Add analytics data
        analytics.putAll(collectorAnalytics);
        
        // Add calculated metrics
        analytics.put("totalConversations", collectorAnalytics.getOrDefault("totalConversations", 0));
        analytics.put("totalMessages", collectorAnalytics.getOrDefault("totalMessages", 0));
        analytics.put("averageResponseTime", collectorAnalytics.getOrDefault("averageResponseTime", 0));
        analytics.put("satisfactionRate", collectorAnalytics.getOrDefault("satisfactionRate", 0));
        analytics.put("resolutionRate", collectorAnalytics.getOrDefault("resolutionRate", 0));
        
        return analytics;
    }
    
    /**
     * Determine the best bot type for a tenant based on existing bots
     */
    private PennyBotType determineBestBotType(Long tenantId) {
        List<PennyBot> existingBots = pennyBotRepository.findByTenantIdAndIsActiveTrue(tenantId);
        
        // If tenant has no bots, default to CUSTOMER_SERVICE
        if (existingBots.isEmpty()) {
            return PennyBotType.CUSTOMER_SERVICE;
        }
        
        // Check what types they already have
        for (PennyBotType type : PennyBotType.values()) {
            boolean hasType = existingBots.stream()
                .anyMatch(bot -> bot.getBotType() == type);
            
            if (!hasType) {
                return type; // Return first missing type
            }
        }
        
        // If they have all types, default to GENERAL
        return PennyBotType.GENERAL;
    }
}
