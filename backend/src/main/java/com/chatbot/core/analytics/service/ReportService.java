package com.chatbot.core.analytics.service;

import com.chatbot.core.analytics.model.AnalyticsDailySummary;
import com.chatbot.core.analytics.model.CustomReport;
import com.chatbot.core.analytics.repository.AnalyticsDailySummaryRepository;
import com.chatbot.core.analytics.repository.CustomReportRepository;
import com.chatbot.core.customer.model.Feedback;
import com.chatbot.core.customer.repository.FeedbackRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Custom report generation. Reports aggregate tenant-scoped data over a
 * configurable date range and store the latest result on the report entity.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {

    private final CustomReportRepository reportRepository;
    private final AnalyticsDailySummaryRepository summaryRepository;
    private final FeedbackRepository feedbackRepository;
    private final FunnelService funnelService;

    // ---------- CRUD ----------

    @Transactional(readOnly = true)
    public List<CustomReport> listReports(Long tenantId) {
        return reportRepository.findByTenantIdAndIsDeletedFalse(tenantId);
    }

    @Transactional
    public CustomReport createReport(CustomReport report, Long tenantId) {
        report.setTenantId(tenantId);
        return reportRepository.save(report);
    }

    @Transactional
    public CustomReport updateReport(Long id, CustomReport update, Long tenantId) {
        CustomReport report = reportRepository.findByIdAndTenantIdAndIsDeletedFalse(id, tenantId)
            .orElseThrow(() -> new IllegalArgumentException("Report not found"));
        if (update.getName() != null) report.setName(update.getName());
        if (update.getDescription() != null) report.setDescription(update.getDescription());
        if (update.getReportType() != null) report.setReportType(update.getReportType());
        if (update.getDateRangeDays() != null) report.setDateRangeDays(update.getDateRangeDays());
        if (update.getFilters() != null) report.setFilters(update.getFilters());
        return reportRepository.save(report);
    }

    @Transactional
    public void deleteReport(Long id, Long tenantId) {
        CustomReport report = reportRepository.findByIdAndTenantIdAndIsDeletedFalse(id, tenantId)
            .orElseThrow(() -> new IllegalArgumentException("Report not found"));
        report.setIsDeleted(true);
        reportRepository.save(report);
    }

    // ---------- Generation ----------

    @Transactional
    public Map<String, Object> generateReport(Long reportId, Long tenantId) {
        CustomReport report = reportRepository.findByIdAndTenantIdAndIsDeletedFalse(reportId, tenantId)
            .orElseThrow(() -> new IllegalArgumentException("Report not found"));

        Map<String, Object> data = buildReportData(report, tenantId);
        report.setLastResult(data);
        report.setLastGeneratedAt(java.time.LocalDateTime.now());
        reportRepository.save(report);
        return data;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getLatestResult(Long reportId, Long tenantId) {
        CustomReport report = reportRepository.findByIdAndTenantIdAndIsDeletedFalse(reportId, tenantId)
            .orElseThrow(() -> new IllegalArgumentException("Report not found"));
        return report.getLastResult() != null ? report.getLastResult() : Map.of();
    }

    private Map<String, Object> buildReportData(CustomReport report, Long tenantId) {
        LocalDate to = LocalDate.now();
        LocalDate from = to.minusDays(report.getDateRangeDays() != null ? report.getDateRangeDays() : 30);
        List<AnalyticsDailySummary> summaries = summaryRepository
            .findByTenantIdAndDateRange(tenantId, from, to);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("reportId", report.getId());
        data.put("reportName", report.getName());
        data.put("reportType", report.getReportType().name());
        data.put("from", from.toString());
        data.put("to", to.toString());

        switch (report.getReportType()) {
            case CONVERSATIONS -> {
                data.put("daily", summaries.stream().map(s -> Map.of(
                    "date", s.getSummaryDate().toString(),
                    "totalConversations", s.getTotalConversations(),
                    "newConversations", s.getNewConversations(),
                    "closedConversations", s.getClosedConversations(),
                    "totalMessages", s.getTotalMessages(),
                    "escalations", s.getEscalationsCount(),
                    "avgFirstResponseMinutes", s.getAvgFirstResponseMinutes() != null ? s.getAvgFirstResponseMinutes() : 0,
                    "avgResolutionMinutes", s.getAvgResolutionMinutes() != null ? s.getAvgResolutionMinutes() : 0
                )).toList());
                data.put("totals", Map.of(
                    "conversations", summaries.stream().mapToInt(AnalyticsDailySummary::getTotalConversations).sum(),
                    "messages", summaries.stream().mapToInt(AnalyticsDailySummary::getTotalMessages).sum(),
                    "escalations", summaries.stream().mapToInt(AnalyticsDailySummary::getEscalationsCount).sum()
                ));
            }
            case SENTIMENT -> {
                List<Double> scores = summaries.stream()
                    .map(AnalyticsDailySummary::getAvgSentimentScore)
                    .filter(Objects::nonNull).toList();
                data.put("avgSentimentScore", scores.isEmpty() ? 0.0 : scores.stream().mapToDouble(d -> d).average().orElse(0));
                data.put("negativeSentimentCount", summaries.stream().mapToInt(AnalyticsDailySummary::getNegativeSentimentCount).sum());
                data.put("daily", summaries.stream().map(s -> Map.of(
                    "date", s.getSummaryDate().toString(),
                    "avgSentimentScore", s.getAvgSentimentScore() != null ? s.getAvgSentimentScore() : 0,
                    "negativeCount", s.getNegativeSentimentCount()
                )).toList());
            }
            case AI_USAGE -> {
                data.put("aiSuggestions", summaries.stream().mapToInt(AnalyticsDailySummary::getAiSuggestionsCount).sum());
                data.put("accepted", summaries.stream().mapToInt(AnalyticsDailySummary::getAiSuggestionsAccepted).sum());
                data.put("rejected", summaries.stream().mapToInt(AnalyticsDailySummary::getAiSuggestionsRejected).sum());
                long total = summaries.stream().mapToInt(AnalyticsDailySummary::getAiSuggestionsCount).sum();
                long accepted = summaries.stream().mapToInt(AnalyticsDailySummary::getAiSuggestionsAccepted).sum();
                data.put("acceptanceRate", total == 0 ? 0.0 : (double) accepted / total);
            }
            case FEEDBACK -> {
                List<Feedback> feedbacks = feedbackRepository
                    .findByTenantIdAndSubmittedAtBetween(tenantId,
                        from.atStartOfDay(), to.plusDays(1).atStartOfDay());
                data.put("total", feedbacks.size());
                data.put("avgRating", feedbacks.stream()
                    .filter(f -> f.getRating() != null)
                    .mapToInt(Feedback::getRating).average().orElse(0));
                data.put("byType", feedbacks.stream()
                    .collect(Collectors.groupingBy(f -> String.valueOf(f.getType()), Collectors.counting())));
                data.put("byRating", feedbacks.stream()
                    .filter(f -> f.getRating() != null)
                    .collect(Collectors.groupingBy(Feedback::getRating, Collectors.counting())));
            }
            case FUNNEL -> data.put("stages", funnelService.computeFunnel(tenantId));
        }
        return data;
    }
}
