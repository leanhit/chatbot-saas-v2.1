package com.chatbot.core.analytics.service;

import com.chatbot.core.analytics.model.CustomReport;
import com.chatbot.core.analytics.model.ReportSchedule;
import com.chatbot.core.analytics.repository.CustomReportRepository;
import com.chatbot.core.analytics.repository.ReportScheduleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportScheduleServiceTest {

    @Mock private ReportScheduleRepository scheduleRepository;
    @Mock private CustomReportRepository reportRepository;

    @InjectMocks
    private ReportScheduleService service;

    private ReportSchedule schedule(Long id, Long reportId, String cron) {
        ReportSchedule s = new ReportSchedule();
        s.setId(id);
        s.setReportId(reportId);
        s.setCronExpression(cron);
        s.setTenantId(1L);
        s.setIsDeleted(false);
        return s;
    }

    private void stubReportFound(Long reportId, Long tenantId) {
        CustomReport r = new CustomReport();
        r.setId(reportId);
        when(reportRepository.findByIdAndTenantIdAndIsDeletedFalse(reportId, tenantId))
            .thenReturn(Optional.of(r));
    }

    @Test
    void listSchedulesDelegates() {
        ReportSchedule s = schedule(1L, 10L, "0 0 8 * * *");
        when(scheduleRepository.findByTenantIdAndIsDeletedFalse(1L)).thenReturn(List.of(s));
        assertEquals(1, service.listSchedules(1L).size());
    }

    @Test
    void createScheduleComputesNextRunAndDefaultsEnabled() {
        stubReportFound(10L, 1L);
        ReportSchedule s = schedule(null, 10L, "0 0 8 * * *");
        s.setEnabled(null);
        when(scheduleRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ReportSchedule saved = service.createSchedule(s, 1L);

        assertEquals(1L, saved.getTenantId());
        assertEquals(Boolean.TRUE, saved.getEnabled());
        assertNotNull(saved.getNextRunAt());
        assertTrue(saved.getNextRunAt().isAfter(java.time.LocalDateTime.now().minusDays(1)));
    }

    @Test
    void createScheduleRejectsReportFromOtherTenant() {
        when(reportRepository.findByIdAndTenantIdAndIsDeletedFalse(10L, 2L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class,
            () -> service.createSchedule(schedule(null, 10L, "0 0 8 * * *"), 2L));
    }

    @Test
    void createScheduleRejectsInvalidCron() {
        stubReportFound(10L, 1L);
        assertThrows(IllegalArgumentException.class,
            () -> service.createSchedule(schedule(null, 10L, "not a cron"), 1L));
    }

    @Test
    void updateScheduleRecomputesNextRunOnCronChange() {
        ReportSchedule existing = schedule(5L, 10L, "0 0 8 * * *");
        existing.setNextRunAt(java.time.LocalDateTime.now().plusDays(1));
        when(scheduleRepository.findByIdAndTenantIdAndIsDeletedFalse(5L, 1L))
            .thenReturn(Optional.of(existing));
        when(scheduleRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ReportSchedule update = new ReportSchedule();
        update.setCronExpression("0 0 18 * * *");
        update.setEnabled(false);

        ReportSchedule saved = service.updateSchedule(5L, update, 1L);

        assertEquals("0 0 18 * * *", saved.getCronExpression());
        assertEquals(Boolean.FALSE, saved.getEnabled());
        assertNotNull(saved.getNextRunAt());
    }

    @Test
    void updateScheduleKeepsCronWhenNull() {
        ReportSchedule existing = schedule(5L, 10L, "0 0 8 * * *");
        when(scheduleRepository.findByIdAndTenantIdAndIsDeletedFalse(5L, 1L))
            .thenReturn(Optional.of(existing));
        when(scheduleRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ReportSchedule update = new ReportSchedule();
        update.setEnabled(false);

        ReportSchedule saved = service.updateSchedule(5L, update, 1L);

        assertEquals("0 0 8 * * *", saved.getCronExpression());
        assertEquals(Boolean.FALSE, saved.getEnabled());
    }

    @Test
    void updateScheduleRejectsOtherTenant() {
        when(scheduleRepository.findByIdAndTenantIdAndIsDeletedFalse(5L, 2L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class,
            () -> service.updateSchedule(5L, new ReportSchedule(), 2L));
    }

    @Test
    void deleteScheduleSoftDeletes() {
        ReportSchedule existing = schedule(5L, 10L, "0 0 8 * * *");
        when(scheduleRepository.findByIdAndTenantIdAndIsDeletedFalse(5L, 1L))
            .thenReturn(Optional.of(existing));
        when(scheduleRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.deleteSchedule(5L, 1L);

        assertEquals(Boolean.TRUE, existing.getIsDeleted());
    }
}
