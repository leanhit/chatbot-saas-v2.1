package com.chatbot.core.analytics.service;

import com.chatbot.core.analytics.model.AnalyticsDailySummary;
import com.chatbot.core.analytics.model.CustomReport;
import com.chatbot.core.analytics.repository.AnalyticsDailySummaryRepository;
import com.chatbot.core.analytics.repository.CustomReportRepository;
import com.chatbot.core.customer.model.Feedback;
import com.chatbot.core.customer.repository.FeedbackRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock private CustomReportRepository reportRepository;
    @Mock private AnalyticsDailySummaryRepository summaryRepository;
    @Mock private FeedbackRepository feedbackRepository;
    @Mock private FunnelService funnelService;

    @InjectMocks
    private ReportService service;

    private CustomReport report(Long id, CustomReport.ReportType type) {
        CustomReport r = new CustomReport();
        r.setId(id);
        r.setName("Test report");
        r.setReportType(type);
        r.setDateRangeDays(30);
        r.setTenantId(1L);
        r.setIsDeleted(false);
        return r;
    }

    private AnalyticsDailySummary summary(LocalDate date) {
        AnalyticsDailySummary s = new AnalyticsDailySummary();
        s.setSummaryDate(date);
        s.setTotalConversations(10);
        s.setNewConversations(4);
        s.setClosedConversations(6);
        s.setTotalMessages(50);
        s.setEscalationsCount(2);
        s.setAvgFirstResponseMinutes(1.5);
        s.setAvgResolutionMinutes(30.0);
        s.setAvgSentimentScore(0.4);
        s.setNegativeSentimentCount(3);
        s.setAiSuggestionsCount(20);
        s.setAiSuggestionsAccepted(12);
        s.setAiSuggestionsRejected(8);
        return s;
    }

    @Test
    void deleteReportSoftDeletes() {
        CustomReport r = report(1L, CustomReport.ReportType.CONVERSATIONS);
        when(reportRepository.findByIdAndTenantIdAndIsDeletedFalse(1L, 1L)).thenReturn(Optional.of(r));
        when(reportRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.deleteReport(1L, 1L);

        assertEquals(Boolean.TRUE, r.getIsDeleted());
    }

    @Test
    void deleteReportRejectsOtherTenant() {
        when(reportRepository.findByIdAndTenantIdAndIsDeletedFalse(1L, 2L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.deleteReport(1L, 2L));
    }

    @Test
    void generateConversationsReportAggregatesTotals() {
        CustomReport r = report(1L, CustomReport.ReportType.CONVERSATIONS);
        when(reportRepository.findByIdAndTenantIdAndIsDeletedFalse(1L, 1L)).thenReturn(Optional.of(r));
        when(summaryRepository.findByTenantIdAndDateRange(eq(1L), any(), any()))
            .thenReturn(List.of(summary(LocalDate.now().minusDays(1)), summary(LocalDate.now().minusDays(2))));
        when(reportRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Map<String, Object> data = service.generateReport(1L, 1L);

        assertEquals("CONVERSATIONS", data.get("reportType"));
        @SuppressWarnings("unchecked")
        Map<String, Object> totals = (Map<String, Object>) data.get("totals");
        assertEquals(20, totals.get("conversations"));
        assertEquals(100, totals.get("messages"));
        assertEquals(4, totals.get("escalations"));
        assertNotNull(r.getLastResult());
        assertNotNull(r.getLastGeneratedAt());
    }

    @Test
    void generateAiUsageReportComputesAcceptanceRate() {
        CustomReport r = report(1L, CustomReport.ReportType.AI_USAGE);
        when(reportRepository.findByIdAndTenantIdAndIsDeletedFalse(1L, 1L)).thenReturn(Optional.of(r));
        when(summaryRepository.findByTenantIdAndDateRange(eq(1L), any(), any()))
            .thenReturn(List.of(summary(LocalDate.now())));
        when(reportRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Map<String, Object> data = service.generateReport(1L, 1L);

        assertEquals(20, data.get("aiSuggestions"));
        assertEquals(12, data.get("accepted"));
        assertEquals(0.6, (Double) data.get("acceptanceRate"), 0.001);
    }

    @Test
    void generateSentimentReportAveragesScores() {
        CustomReport r = report(1L, CustomReport.ReportType.SENTIMENT);
        when(reportRepository.findByIdAndTenantIdAndIsDeletedFalse(1L, 1L)).thenReturn(Optional.of(r));
        when(summaryRepository.findByTenantIdAndDateRange(eq(1L), any(), any()))
            .thenReturn(List.of(summary(LocalDate.now())));
        when(reportRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Map<String, Object> data = service.generateReport(1L, 1L);

        assertEquals(0.4, (Double) data.get("avgSentimentScore"), 0.001);
        assertEquals(3, data.get("negativeSentimentCount"));
    }

    @Test
    void generateFeedbackReportAggregatesRatings() {
        CustomReport r = report(1L, CustomReport.ReportType.FEEDBACK);
        Feedback f1 = new Feedback();
        f1.setRating(5);
        f1.setType(Feedback.FeedbackType.CONVERSATION);
        Feedback f2 = new Feedback();
        f2.setRating(3);
        f2.setType(Feedback.FeedbackType.CONVERSATION);
        Feedback f3 = new Feedback();
        f3.setRating(null);
        f3.setType(Feedback.FeedbackType.GENERAL);

        when(reportRepository.findByIdAndTenantIdAndIsDeletedFalse(1L, 1L)).thenReturn(Optional.of(r));
        when(summaryRepository.findByTenantIdAndDateRange(eq(1L), any(), any())).thenReturn(List.of());
        when(feedbackRepository.findByTenantIdAndSubmittedAtBetween(eq(1L), any(), any()))
            .thenReturn(List.of(f1, f2, f3));
        when(reportRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Map<String, Object> data = service.generateReport(1L, 1L);

        assertEquals(3, data.get("total"));
        assertEquals(4.0, (Double) data.get("avgRating"), 0.001);
    }

    @Test
    void generateFunnelReportDelegatesToFunnelService() {
        CustomReport r = report(1L, CustomReport.ReportType.FUNNEL);
        when(reportRepository.findByIdAndTenantIdAndIsDeletedFalse(1L, 1L)).thenReturn(Optional.of(r));
        when(summaryRepository.findByTenantIdAndDateRange(eq(1L), any(), any())).thenReturn(List.of());
        when(funnelService.computeFunnel(1L)).thenReturn(List.of());
        when(reportRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Map<String, Object> data = service.generateReport(1L, 1L);

        assertTrue(data.containsKey("stages"));
        verify(funnelService).computeFunnel(1L);
    }

    @Test
    void getLatestResultReturnsEmptyWhenNeverGenerated() {
        CustomReport r = report(1L, CustomReport.ReportType.CONVERSATIONS);
        when(reportRepository.findByIdAndTenantIdAndIsDeletedFalse(1L, 1L)).thenReturn(Optional.of(r));

        assertTrue(service.getLatestResult(1L, 1L).isEmpty());
    }
}
