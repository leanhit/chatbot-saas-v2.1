package com.chatbot.core.analytics.service;

import com.chatbot.core.ai.model.AiSuggestionStatus;
import com.chatbot.core.ai.model.SentimentLabel;
import com.chatbot.core.ai.repository.AiSuggestionRepository;
import com.chatbot.core.ai.repository.ConversationSentimentRepository;
import com.chatbot.core.analytics.model.AnalyticsDailySummary;
import com.chatbot.core.analytics.repository.AnalyticsDailySummaryRepository;
import com.chatbot.core.message.store.model.Conversation;
import com.chatbot.core.message.store.model.ConversationStatus;
import com.chatbot.core.message.store.repository.ConversationRepository;
import com.chatbot.core.message.store.repository.MessageRepository;
import com.chatbot.core.tenant.infra.TenantContext;
import com.chatbot.core.tenant.model.Tenant;
import com.chatbot.core.tenant.model.TenantStatus;
import com.chatbot.core.tenant.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Tenant-scoped daily analytics aggregation (Phase 5).
 *
 * Aggregates conversations, messages, escalations, sentiment and AI suggestion
 * metrics per tenant per day into analytics_daily_summary. All source queries
 * are tenant-scoped; summaries are queryable by tenant + date range.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsAggregationService {

    private final AnalyticsDailySummaryRepository summaryRepository;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final ConversationSentimentRepository sentimentRepository;
    private final AiSuggestionRepository aiSuggestionRepository;
    private final TenantRepository tenantRepository;
    // Self-injection so scheduled job calls aggregateForDate through the
    // Spring proxy — @Transactional actually applies (no self-invocation bypass).
    private final ObjectProvider<AnalyticsAggregationService> selfProvider;

    /**
     * Nightly job: aggregate yesterday's metrics for all active tenants.
     */
    @Scheduled(cron = "0 30 1 * * *")
    @SchedulerLock(name = "AnalyticsAggregation_aggregateDaily", lockAtMostFor = "30m", lockAtLeastFor = "1m")
    public void aggregateDailyScheduled() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        try {
            List<Tenant> activeTenants = tenantRepository.findAll();
            for (Tenant tenant : activeTenants) {
                if (tenant.getStatus() != TenantStatus.ACTIVE || tenant.getId() == null) {
                    continue;
                }
                try {
                    TenantContext.executeWithTenantId(tenant.getId(), () -> {
                        selfProvider.getObject().aggregateForDate(tenant.getId(), yesterday);
                        return null;
                    });
                } catch (Exception e) {
                    log.error("[Analytics] Failed to aggregate tenant {} for {}: {}", tenant.getId(), yesterday, e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            log.error("[Analytics] Scheduled aggregation failed: {}", e.getMessage(), e);
        }
    }

    /**
     * Aggregate metrics for a tenant on a specific date. Idempotent (upsert).
     */
    @Transactional(transactionManager = "messageTransactionManager", rollbackFor = Exception.class)
    public AnalyticsDailySummary aggregateForDate(Long tenantId, LocalDate date) {
        LocalDateTime from = date.atStartOfDay();
        LocalDateTime to = date.atTime(LocalTime.MAX);

        List<Conversation> created = conversationRepository.findByTenantIdAndCreatedAtBetween(tenantId, from, to);
        List<Conversation> resolved = conversationRepository.findByTenantIdAndResolutionTimeBetween(tenantId, from, to);
        long escalatedCount = conversationRepository.countByTenantIdAndLastEscalatedAtBetween(tenantId, from, to);

        // Count messages in DB instead of loading entities into memory
        long totalMessages = messageRepository.countByTenantIdAndCreatedAtBetween(tenantId, from, to);
        long userMessages = messageRepository.countByTenantIdAndCreatedAtBetweenAndSender(tenantId, from, to, "user");
        long agentMessages = messageRepository.countByTenantIdAndCreatedAtBetweenAndSender(tenantId, from, to, "agent");
        long botMessages = totalMessages - userMessages - agentMessages;

        Double avgFirstResponse = averageMinutes(created, Conversation::getFirstAgentResponseTime);
        Double avgResolution = averageMinutes(resolved, Conversation::getResolutionTime);

        Double avgSentiment = sentimentRepository.avgScoreByTenantIdBetween(tenantId, from, to);
        long negativeSentiment = sentimentRepository.countByTenantIdAndLabelBetween(
            tenantId, SentimentLabel.NEGATIVE, from, to);

        long aiTotal = aiSuggestionRepository.countByTenantIdBetween(tenantId, from, to);
        long aiAccepted = aiSuggestionRepository.countByTenantIdAndStatusBetween(
            tenantId, AiSuggestionStatus.ACCEPTED, from, to);
        long aiRejected = aiSuggestionRepository.countByTenantIdAndStatusBetween(
            tenantId, AiSuggestionStatus.REJECTED, from, to);

        Map<String, Object> metrics = new HashMap<>();
        metrics.put("resolutionRate", created.isEmpty() ? 0.0 :
            (double) resolved.size() / created.size());
        metrics.put("escalationRate", created.isEmpty() ? 0.0 :
            (double) escalatedCount / created.size());
        metrics.put("aiAcceptanceRate", aiTotal == 0 ? 0.0 : (double) aiAccepted / aiTotal);

        AnalyticsDailySummary summary = summaryRepository
            .findByTenantIdAndSummaryDateAndIsDeletedFalse(tenantId, date)
            .orElseGet(() -> {
                AnalyticsDailySummary s = AnalyticsDailySummary.builder()
                    .summaryDate(date)
                    .build();
                s.setTenantId(tenantId);
                return s;
            });

        summary.setTotalConversations(created.size());
        summary.setNewConversations((int) created.stream()
            .filter(c -> c.getCreatedAt() != null && !c.getCreatedAt().toLocalDate().isBefore(date))
            .count());
        summary.setClosedConversations((int) resolved.stream()
            .filter(c -> ConversationStatus.CLOSED.equals(c.getStatus()))
            .count());
        summary.setTotalMessages((int) totalMessages);
        summary.setUserMessages((int) userMessages);
        summary.setBotMessages((int) botMessages);
        summary.setAgentMessages((int) agentMessages);
        summary.setEscalationsCount((int) escalatedCount);
        summary.setAvgFirstResponseMinutes(avgFirstResponse);
        summary.setAvgResolutionMinutes(avgResolution);
        summary.setAvgSentimentScore(avgSentiment);
        summary.setNegativeSentimentCount((int) negativeSentiment);
        summary.setAiSuggestionsCount((int) aiTotal);
        summary.setAiSuggestionsAccepted((int) aiAccepted);
        summary.setAiSuggestionsRejected((int) aiRejected);
        summary.setMetrics(metrics);

        return summaryRepository.save(summary);
    }

    @Transactional(readOnly = true, transactionManager = "messageTransactionManager")
    public List<AnalyticsDailySummary> getSummary(Long tenantId, LocalDate from, LocalDate to) {
        return summaryRepository.findByTenantIdAndDateRange(tenantId, from, to);
    }

    @Transactional(readOnly = true, transactionManager = "messageTransactionManager")
    public Optional<AnalyticsDailySummary> getSummaryForDate(Long tenantId, LocalDate date) {
        return summaryRepository.findByTenantIdAndSummaryDateAndIsDeletedFalse(tenantId, date);
    }

    private Double averageMinutes(List<Conversation> conversations,
                                  java.util.function.Function<Conversation, LocalDateTime> endTime) {
        double sum = 0;
        int count = 0;
        for (Conversation c : conversations) {
            LocalDateTime end = endTime.apply(c);
            if (end != null && c.getCreatedAt() != null) {
                sum += java.time.temporal.ChronoUnit.MINUTES.between(c.getCreatedAt(), end);
                count++;
            }
        }
        return count == 0 ? null : sum / count;
    }
}
