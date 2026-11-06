package com.chatbot.core.analytics.dto;

import com.chatbot.core.analytics.model.AnalyticsDailySummary;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Builder
public record AnalyticsSummaryDTO(
    Long id,
    LocalDate summaryDate,
    Integer totalConversations,
    Integer newConversations,
    Integer closedConversations,
    Integer totalMessages,
    Integer userMessages,
    Integer botMessages,
    Integer agentMessages,
    Integer escalationsCount,
    Double avgFirstResponseMinutes,
    Double avgResolutionMinutes,
    Double avgSentimentScore,
    Integer negativeSentimentCount,
    Integer aiSuggestionsCount,
    Integer aiSuggestionsAccepted,
    Integer aiSuggestionsRejected,
    Map<String, Object> metrics,
    LocalDateTime createdAt
) {
    public static AnalyticsSummaryDTO from(AnalyticsDailySummary s) {
        return AnalyticsSummaryDTO.builder()
            .id(s.getId())
            .summaryDate(s.getSummaryDate())
            .totalConversations(s.getTotalConversations())
            .newConversations(s.getNewConversations())
            .closedConversations(s.getClosedConversations())
            .totalMessages(s.getTotalMessages())
            .userMessages(s.getUserMessages())
            .botMessages(s.getBotMessages())
            .agentMessages(s.getAgentMessages())
            .escalationsCount(s.getEscalationsCount())
            .avgFirstResponseMinutes(s.getAvgFirstResponseMinutes())
            .avgResolutionMinutes(s.getAvgResolutionMinutes())
            .avgSentimentScore(s.getAvgSentimentScore())
            .negativeSentimentCount(s.getNegativeSentimentCount())
            .aiSuggestionsCount(s.getAiSuggestionsCount())
            .aiSuggestionsAccepted(s.getAiSuggestionsAccepted())
            .aiSuggestionsRejected(s.getAiSuggestionsRejected())
            .metrics(s.getMetrics())
            .createdAt(s.getCreatedAt())
            .build();
    }
}
