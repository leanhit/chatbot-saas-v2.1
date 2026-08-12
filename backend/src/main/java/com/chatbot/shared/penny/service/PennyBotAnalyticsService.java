package com.chatbot.shared.penny.service;

import com.chatbot.core.tenant.infra.TenantContext;
import com.chatbot.shared.penny.model.PennyBot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Penny Bot Analytics Service - Handles analytics aggregation and performance metrics calculation
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PennyBotAnalyticsService {

    private final PennyBotCrudService pennyBotCrudService;
    private final com.chatbot.shared.penny.core.PennyMiddlewareEngine pennyMiddlewareEngine;
    private final com.chatbot.core.message.store.repository.ConversationRepository conversationRepository;

    /**
     * Get bot analytics
     */
    public Map<String, Object> getBotAnalytics(UUID botId, String timeRange, String ownerId) {
        log.info("📊 Getting analytics for bot: {} with range: {}", botId, timeRange);
        
        PennyBot bot = pennyBotCrudService.getBot(botId);
        
        if (!bot.getOwnerId().equals(ownerId)) {
            throw new AccessDeniedException("Not authorized to view analytics for this bot");
        }
        
        Map<String, Object> analytics = new HashMap<>();
        
        com.chatbot.shared.penny.core.PennyMiddlewareEngine.EngineMetrics metrics = pennyMiddlewareEngine.getEngineMetrics();
        
        Map<String, Object> collectorAnalytics = new HashMap<>();
        collectorAnalytics.put("totalConversations", metrics.getTotalProcessed());
        collectorAnalytics.put("totalMessages", metrics.getTotalProcessed());
        collectorAnalytics.put("averageResponseTime", metrics.getAverageProcessingTime());
        collectorAnalytics.put("errorRate", metrics.getErrorRate() * 100);
        
        Long tenantId = TenantContext.getTenantId();
        Double averageSatisfactionRating = null;
        if (tenantId != null) {
            try {
                averageSatisfactionRating = conversationRepository.getAverageSatisfactionRating(tenantId);
            } catch (Exception e) {
                log.warn("Failed to get average satisfaction rating for tenant {}: {}", tenantId, e.getMessage());
            }
        }
        double satisfactionRate = (averageSatisfactionRating != null) ? (averageSatisfactionRating / 5.0) * 100 : 0.0;
        collectorAnalytics.put("satisfactionRate", satisfactionRate);
        
        long resolvedCount = 0;
        long unresolvedCount = 0;
        long totalCount = 0;
        if (tenantId != null) {
            try {
                resolvedCount = conversationRepository.countResolvedConversations(tenantId);
                unresolvedCount = conversationRepository.countUnresolvedConversations(tenantId);
                totalCount = conversationRepository.countTotalConversationsForResolution(tenantId);
            } catch (Exception e) {
                log.warn("Failed to get resolution counts for tenant {}: {}", tenantId, e.getMessage());
            }
        }
        double resolutionRate = (totalCount > 0) ? ((double) resolvedCount / totalCount) * 100 : 0.0;
        collectorAnalytics.put("resolutionRate", resolutionRate);
        collectorAnalytics.put("resolvedCount", resolvedCount);
        collectorAnalytics.put("unresolvedCount", unresolvedCount);
        
        collectorAnalytics.put("uptime", 99.9);
        
        analytics.put("botId", botId.toString());
        analytics.put("botName", bot.getBotName());
        analytics.put("botType", bot.getBotType().name());
        analytics.put("timeRange", timeRange);
        analytics.put("isActive", bot.isActive());
        analytics.put("isEnabled", bot.isEnabled());
        analytics.put("createdAt", bot.getCreatedAt().toString());
        analytics.put("lastUsedAt", bot.getLastUsedAt() != null ? bot.getLastUsedAt().toString() : null);
        
        analytics.putAll(collectorAnalytics);
        
        analytics.put("totalConversations", collectorAnalytics.getOrDefault("totalConversations", 0));
        analytics.put("totalMessages", collectorAnalytics.getOrDefault("totalMessages", 0));
        analytics.put("averageResponseTime", collectorAnalytics.getOrDefault("averageResponseTime", 0));
        analytics.put("satisfactionRate", collectorAnalytics.getOrDefault("satisfactionRate", 0));
        analytics.put("resolutionRate", collectorAnalytics.getOrDefault("resolutionRate", 0));
        
        return analytics;
    }
}
