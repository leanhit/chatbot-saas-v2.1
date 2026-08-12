package com.chatbot.shared.penny.service;

import com.chatbot.shared.penny.context.ContextManager;
import com.chatbot.shared.penny.analytics.AnalyticsCollector;
import com.chatbot.shared.penny.model.PennyBot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Penny Bot Health Service - Handles bot diagnostic checks and health monitoring
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PennyBotHealthService {

    private final PennyBotCrudService pennyBotCrudService;
    private final ContextManager contextManager;
    private final AnalyticsCollector analyticsCollector;

    /**
     * Get bot health status
     */
    public Map<String, Object> getBotHealth(UUID botId) {
        PennyBot bot = pennyBotCrudService.getBot(botId);
        
        Map<String, Object> health = new HashMap<>();
        
        // Bot status
        health.put("botStatus", bot.isActive() && bot.isEnabled() ? "healthy" : "unhealthy");
        health.put("botType", bot.getBotType().name());
        health.put("pennyBotId", bot.getPennyBotId());
        
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
}
