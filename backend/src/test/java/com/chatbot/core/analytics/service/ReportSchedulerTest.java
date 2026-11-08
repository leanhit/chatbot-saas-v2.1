package com.chatbot.core.analytics.service;

import com.chatbot.core.analytics.model.CustomReport;
import com.chatbot.core.analytics.model.ReportSchedule;
import com.chatbot.core.analytics.repository.CustomReportRepository;
import com.chatbot.core.analytics.repository.ReportScheduleRepository;
import com.chatbot.core.tenant.infra.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportSchedulerTest {

    @Mock private ReportScheduleRepository scheduleRepository;
    @Mock private CustomReportRepository reportRepository;
    @Mock private ReportService reportService;

    @InjectMocks
    private ReportScheduler scheduler;

    @AfterEach
    void clearTenant() {
        TenantContext.clear();
    }

    private ReportSchedule schedule(Long id, Long tenantId, Long reportId, String cron) {
        ReportSchedule s = new ReportSchedule();
        s.setId(id);
        s.setTenantId(tenantId);
        s.setReportId(reportId);
        s.setCronExpression(cron);
        s.setEnabled(true);
        s.setNextRunAt(LocalDateTime.now().minusMinutes(5));
        return s;
    }

    private void stubSave() {
        when(scheduleRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    private void stubDue(List<ReportSchedule> due) {
        when(scheduleRepository.findByEnabledTrueAndNextRunAtBeforeAndIsDeletedFalse(any(LocalDateTime.class)))
            .thenReturn(due);
    }

    @Test
    void noDueSchedulesDoesNothing() {
        stubDue(List.of());
        scheduler.runDueSchedules();
        verifyNoInteractions(reportRepository, reportService);
        verify(scheduleRepository, never()).save(any());
    }

    @Test
    void successfulRunGeneratesReportAndUpdatesSchedule() {
        ReportSchedule s = schedule(1L, 1L, 10L, "0 0 8 * * *");
        stubDue(List.of(s));
        stubSave();

        CustomReport report = new CustomReport();
        report.setId(10L);
        when(reportRepository.findByIdAndTenantIdAndIsDeletedFalse(10L, 1L)).thenReturn(Optional.of(report));
        when(reportService.generateReport(10L, 1L)).thenReturn(java.util.Map.of());

        scheduler.runDueSchedules();

        assertEquals("SUCCESS", s.getLastRunStatus());
        assertNotNull(s.getLastRunAt());
        assertNotNull(s.getNextRunAt());
        assertTrue(s.getNextRunAt().isAfter(s.getLastRunAt()));
        verify(reportService).generateReport(10L, 1L);
        verify(scheduleRepository).save(s);
        // tenant context cleared after run
        assertNull(TenantContext.getTenantId());
    }

    @Test
    void missingReportMarksReportNotFound() {
        ReportSchedule s = schedule(1L, 1L, 99L, "0 0 8 * * *");
        stubDue(List.of(s));
        stubSave();
        when(reportRepository.findByIdAndTenantIdAndIsDeletedFalse(99L, 1L)).thenReturn(Optional.empty());

        scheduler.runDueSchedules();

        assertEquals("REPORT_NOT_FOUND", s.getLastRunStatus());
        verify(reportService, never()).generateReport(any(), any());
        verify(scheduleRepository).save(s);
    }

    @Test
    void generationFailureMarksFailedAndContinues() {
        ReportSchedule s1 = schedule(1L, 1L, 10L, "0 0 8 * * *");
        ReportSchedule s2 = schedule(2L, 2L, 20L, "0 0 9 * * *");
        stubDue(List.of(s1, s2));
        stubSave();

        CustomReport r1 = new CustomReport(); r1.setId(10L);
        CustomReport r2 = new CustomReport(); r2.setId(20L);
        when(reportRepository.findByIdAndTenantIdAndIsDeletedFalse(10L, 1L)).thenReturn(Optional.of(r1));
        when(reportRepository.findByIdAndTenantIdAndIsDeletedFalse(20L, 2L)).thenReturn(Optional.of(r2));
        when(reportService.generateReport(10L, 1L)).thenThrow(new RuntimeException("boom"));
        when(reportService.generateReport(20L, 2L)).thenReturn(java.util.Map.of());

        scheduler.runDueSchedules();

        assertTrue(s1.getLastRunStatus().startsWith("FAILED"));
        assertEquals("SUCCESS", s2.getLastRunStatus()); // second schedule still processed
        verify(scheduleRepository, times(2)).save(any());
    }

    @Test
    void invalidCronDefaultsNextRunToPlusOneDay() {
        ReportSchedule s = schedule(1L, 1L, 10L, "not-a-cron");
        stubDue(List.of(s));
        stubSave();
        when(reportRepository.findByIdAndTenantIdAndIsDeletedFalse(10L, 1L)).thenReturn(Optional.empty());

        LocalDateTime before = LocalDateTime.now();
        scheduler.runDueSchedules();

        // invalid cron -> nextRunAt = lastRunAt + 1 day
        assertEquals(s.getLastRunAt().plusDays(1), s.getNextRunAt());
        assertTrue(s.getNextRunAt().isAfter(before.plusHours(23)));
    }

    @Test
    void tenantContextSetPerSchedule() {
        ReportSchedule s = schedule(1L, 7L, 10L, "0 0 8 * * *");
        stubDue(List.of(s));
        stubSave();
        when(reportRepository.findByIdAndTenantIdAndIsDeletedFalse(10L, 7L)).thenReturn(Optional.empty());

        scheduler.runDueSchedules();

        // context cleared in finally — verify no leak
        assertNull(TenantContext.getTenantId());
    }
}
