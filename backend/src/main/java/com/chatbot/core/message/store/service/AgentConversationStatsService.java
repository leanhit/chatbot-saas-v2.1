package com.chatbot.core.message.store.service;

import com.chatbot.core.message.store.dto.AgentConversationStatsDTO;
import com.chatbot.core.message.store.model.Agent;
import com.chatbot.core.message.store.repository.AgentRepository;
import com.chatbot.core.message.store.repository.ConversationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for calculating agent conversation statistics
 * Provides KPI metrics and chart data for individual agent performance
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AgentConversationStatsService {

    private final ConversationRepository conversationRepository;
    private final AgentRepository agentRepository;

    /**
     * Get conversation statistics for a specific agent within a period
     */
    public AgentConversationStatsDTO getAgentStats(Long agentId, Long tenantId, String period) {
        log.info("Getting stats for agent {} in tenant {} for period {}", agentId, tenantId, period);
        
        // Calculate period start date
        LocalDateTime since = calculatePeriodStart(period);
        
        // Get agent info
        Agent agent = agentRepository.findById(agentId)
                .orElseThrow(() -> new IllegalArgumentException("Agent not found: " + agentId));
        
        // Calculate KPI metrics
        Long totalConversations = conversationRepository.countByAgentAndPeriod(tenantId, agentId, since);
        Long resolvedCount = conversationRepository.countByAgentAndStatusAndPeriod(tenantId, agentId, "resolved", since);
        Long unresolvedCount = conversationRepository.countByAgentAndStatusAndPeriod(tenantId, agentId, "unresolved", since);
        Long pendingCount = conversationRepository.countByAgentAndStatusAndPeriod(tenantId, agentId, "pending", since);
        
        Double resolutionRate = totalConversations > 0 ? (resolvedCount * 100.0 / totalConversations) : 0.0;
        Double avgFirstResponseSeconds = conversationRepository.getAvgFirstResponseSecondsByAgent(tenantId, agentId, since);
        Double csatScore = conversationRepository.getAvgCsatByAgentAndPeriod(tenantId, agentId, since);
        Long slaBreachCount = conversationRepository.sumSlaBreachesByAgent(tenantId, agentId, since);
        
        // Get daily stats for chart
        List<Object[]> dailyStatsRaw = conversationRepository.getDailyStatsByAgent(tenantId, agentId, since);
        List<AgentConversationStatsDTO.DailyStatPoint> dailyStats = dailyStatsRaw.stream()
                .map(row -> AgentConversationStatsDTO.DailyStatPoint.builder()
                        .date(((java.sql.Date) row[0]).toString())
                        .total(((Number) row[1]).longValue())
                        .resolved(((Number) row[2]).longValue())
                        .build())
                .collect(Collectors.toList());
        
        return AgentConversationStatsDTO.builder()
                .agentId(agentId)
                .agentName(agent.getName())
                .period(period)
                .totalConversations(totalConversations)
                .resolvedCount(resolvedCount)
                .unresolvedCount(unresolvedCount)
                .pendingCount(pendingCount)
                .resolutionRate(resolutionRate)
                .avgFirstResponseSeconds(avgFirstResponseSeconds)
                .csatScore(csatScore)
                .slaBreachCount(slaBreachCount)
                .activeConversations(agent.getCurrentLoad())
                .dailyStats(dailyStats)
                .build();
    }

    /**
     * Get leaderboard for all agents within a period
     * Sorted by resolvedCount DESC
     */
    public List<AgentConversationStatsDTO> getLeaderboard(Long tenantId, String period) {
        log.info("Getting leaderboard for tenant {} for period {}", tenantId, period);
        
        // Calculate period start date
        LocalDateTime since = calculatePeriodStart(period);
        
        // Get all agents for tenant
        List<Agent> agents = agentRepository.findByTenantId(tenantId);
        
        // Calculate stats for each agent
        List<AgentConversationStatsDTO> leaderboard = agents.stream()
                .map(agent -> getAgentStats(agent.getId(), tenantId, period))
                .sorted((a, b) -> Long.compare(b.getResolvedCount(), a.getResolvedCount())) // Sort by resolvedCount DESC
                .collect(Collectors.toList());
        
        return leaderboard;
    }

    /**
     * Calculate period start date based on period string
     */
    private LocalDateTime calculatePeriodStart(String period) {
        LocalDateTime now = LocalDateTime.now();
        switch (period) {
            case "7d":
                return now.minusDays(7);
            case "30d":
                return now.minusDays(30);
            case "90d":
                return now.minusDays(90);
            default:
                return now.minusDays(7); // Default to 7 days
        }
    }
}
