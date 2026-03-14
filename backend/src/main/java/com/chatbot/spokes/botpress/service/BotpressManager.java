package com.chatbot.spokes.botpress.service;

import com.chatbot.shared.penny.model.PennyBot;
import com.chatbot.shared.penny.model.PennyBotType;
import com.chatbot.shared.penny.repository.PennyBotRepository;
import com.chatbot.core.tenant.infra.TenantContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Botpress Manager - Auto-create Botpress bots for Facebook connections
 */
@Service
@Slf4j
public class BotpressManager {
    
    private final PennyBotRepository pennyBotRepository;
    private final BotpressApiService botpressApiService;
    
    public BotpressManager(PennyBotRepository pennyBotRepository, BotpressApiService botpressApiService) {
        this.pennyBotRepository = pennyBotRepository;
        this.botpressApiService = botpressApiService;
    }
    
    /**
     * Auto-create Botpress bot for Facebook connection
     */
    @Transactional
    public PennyBot autoCreateBotForConnection(String ownerId, String pageId) {
        log.info("🤖 Auto-creating Botpress bot for connection: pageId={}, owner={}", pageId, ownerId);
        
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new IllegalStateException("Tenant context not found for auto-creation");
        }
        
        // Get available Botpress bots
        List<BotpressApiService.BotInfo> availableBots = botpressApiService.getAvailableBots();
        if (availableBots.isEmpty()) {
            throw new RuntimeException("No available Botpress bots found");
        }
        
        // Use first available bot (or implement selection logic)
        BotpressApiService.BotInfo selectedBot = availableBots.get(0);
        
        String botName = "Auto-Botpress-" + pageId;
        String description = "Auto-generated Botpress bot for Facebook page: " + pageId;
        
        return createBot(ownerId, botName, selectedBot.getId(), description);
    }
    
    /**
     * Create Botpress bot
     */
    @Transactional
    public PennyBot createBot(String ownerId, String botName, String botpressBotId, String description) {
        log.info("🤖 Creating Botpress bot: {} with botpressId: {} for owner: {}", botName, botpressBotId, ownerId);
        
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new IllegalStateException("Tenant context not found");
        }
        
        // Create Penny Bot entity with Botpress integration
        PennyBot newBot = PennyBot.builder()
            .id(UUID.randomUUID())
            .botName(botName)
            .botType(PennyBotType.BOTPRESS) // Use BOTPRESS type
            .tenantId(tenantId)
            .ownerId(ownerId)
            .pennyBotId(botpressBotId) // Store Botpress bot ID
            .description(description)
            .isActive(true)
            .isEnabled(true)
            .build();
        
        // Save to database
        PennyBot savedBot = pennyBotRepository.save(newBot);
        
        log.info("✅ Botpress bot created successfully: {} (ID: {}, BotpressID: {})", 
            savedBot.getBotName(), savedBot.getId(), savedBot.getPennyBotId());
        return savedBot;
    }
}
