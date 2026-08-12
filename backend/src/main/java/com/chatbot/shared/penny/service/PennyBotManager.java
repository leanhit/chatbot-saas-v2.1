package com.chatbot.shared.penny.service;

import com.chatbot.shared.penny.model.PennyBot;
import com.chatbot.shared.penny.model.PennyBotType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Penny Bot Manager - Facade service delegating lifecycle, analytics, and health operations
 * to specialized domain services (PennyBotCrudService, PennyBotAnalyticsService, PennyBotHealthService).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PennyBotManager {
    
    private final PennyBotCrudService pennyBotCrudService;
    private final PennyBotAnalyticsService pennyBotAnalyticsService;
    private final PennyBotHealthService pennyBotHealthService;
    
    public PennyBot createBot(String ownerId, String botName, PennyBotType botType, String description) {
        return pennyBotCrudService.createBot(ownerId, botName, botType, description);
    }
    
    public PennyBot autoCreateBotForConnection(String ownerId, String pageId) {
        return pennyBotCrudService.autoCreateBotForConnection(ownerId, pageId);
    }
    
    public PennyBot updateBot(UUID botId, String botName, String description, Boolean isEnabled) {
        return pennyBotCrudService.updateBot(botId, botName, description, isEnabled);
    }
    
    public PennyBot updateBot(UUID botId, Map<String, Object> updates, String ownerId) {
        return pennyBotCrudService.updateBot(botId, updates, ownerId);
    }
    
    public PennyBot toggleBotStatus(UUID botId, boolean enabled, String ownerId) {
        return pennyBotCrudService.toggleBotStatus(botId, enabled, ownerId);
    }
    
    public void checkBotCanBeDeleted(UUID botId, String ownerId) {
        pennyBotCrudService.checkBotCanBeDeleted(botId, ownerId);
    }
    
    public boolean deleteBot(UUID botId, String ownerId) {
        return pennyBotCrudService.deleteBot(botId, ownerId);
    }
    
    public PennyBot getBot(UUID botId) {
        return pennyBotCrudService.getBot(botId);
    }
    
    public List<PennyBot> getBotsForOwner(String ownerId) {
        return pennyBotCrudService.getBotsForOwner(ownerId);
    }
    
    public List<PennyBot> getBotsForCurrentTenant() {
        return pennyBotCrudService.getBotsForCurrentTenant();
    }
    
    public String processMessage(UUID botId, String message, String userId, boolean isTestMode) {
        return pennyBotCrudService.processMessage(botId, message, userId, isTestMode);
    }

    public String processMessage(UUID botId, String message, String userId, String ownerId, boolean isTestMode) {
        return pennyBotCrudService.processMessage(botId, message, userId, ownerId, isTestMode);
    }
    
    public Map<String, Object> getBotHealth(UUID botId) {
        return pennyBotHealthService.getBotHealth(botId);
    }
    
    public Map<String, Object> getBotAnalytics(UUID botId, String timeRange, String ownerId) {
        return pennyBotAnalyticsService.getBotAnalytics(botId, timeRange, ownerId);
    }
}
