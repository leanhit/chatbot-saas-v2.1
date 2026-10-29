package com.chatbot.core.message.store.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for Agent Conversation Statistics
 * Contains KPI metrics and chart data for individual agent performance
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentConversationStatsDTO {
    
    private Long agentId;
    private String agentName;
    private String period; // "7d" | "30d" | "90d"
    
    // KPI metrics
    private Long totalConversations;
    private Long resolvedCount;
    private Long unresolvedCount;
    private Long pendingCount;
    private Double resolutionRate; // 0–100
    private Double avgFirstResponseSeconds; // seconds
    private Double csatScore; // 1.0–5.0
    private Long slaBreachCount;
    private Integer activeConversations; // currentLoad
    
    // Chart data
    private List<DailyStatPoint> dailyStats; // Bar chart data
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyStatPoint {
        private String date; // "2026-10-22"
        private Long total;
        private Long resolved;
    }
}
