package com.chatbot.core.analytics.service;

import com.chatbot.core.analytics.model.CustomReport;
import com.chatbot.core.analytics.model.ReportSchedule;
import com.chatbot.core.analytics.repository.CustomReportRepository;
import com.chatbot.core.analytics.repository.ReportScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

/**
 * CRUD for report schedules. nextRunAt is computed from the cron expression
 * at creation/update time; ReportScheduler picks up due schedules.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReportScheduleService {

    private final ReportScheduleRepository scheduleRepository;
    private final CustomReportRepository reportRepository;

    @Transactional(readOnly = true)
    public List<ReportSchedule> listSchedules(Long tenantId) {
        return scheduleRepository.findByTenantIdAndIsDeletedFalse(tenantId);
    }

    @Transactional
    public ReportSchedule createSchedule(ReportSchedule schedule, Long tenantId) {
        // Report must belong to same tenant
        CustomReport report = reportRepository
            .findByIdAndTenantIdAndIsDeletedFalse(schedule.getReportId(), tenantId)
            .orElseThrow(() -> new IllegalArgumentException("Report not found"));
        validateCron(schedule.getCronExpression());
        schedule.setTenantId(tenantId);
        if (schedule.getEnabled() == null) schedule.setEnabled(true);
        schedule.setNextRunAt(computeNextRun(schedule.getCronExpression(), LocalDateTime.now()));
        return scheduleRepository.save(schedule);
    }

    @Transactional
    public ReportSchedule updateSchedule(Long id, ReportSchedule update, Long tenantId) {
        ReportSchedule schedule = scheduleRepository.findByIdAndTenantIdAndIsDeletedFalse(id, tenantId)
            .orElseThrow(() -> new IllegalArgumentException("Schedule not found"));
        if (update.getCronExpression() != null) {
            validateCron(update.getCronExpression());
            schedule.setCronExpression(update.getCronExpression());
            schedule.setNextRunAt(computeNextRun(update.getCronExpression(), LocalDateTime.now()));
        }
        if (update.getEnabled() != null) schedule.setEnabled(update.getEnabled());
        return scheduleRepository.save(schedule);
    }

    @Transactional
    public void deleteSchedule(Long id, Long tenantId) {
        ReportSchedule schedule = scheduleRepository.findByIdAndTenantIdAndIsDeletedFalse(id, tenantId)
            .orElseThrow(() -> new IllegalArgumentException("Schedule not found"));
        schedule.setIsDeleted(true);
        scheduleRepository.save(schedule);
    }

    private void validateCron(String cron) {
        try {
            CronExpression.parse(cron);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid cron expression: " + cron);
        }
    }

    private LocalDateTime computeNextRun(String cron, LocalDateTime after) {
        return CronExpression.parse(cron)
            .next(after.atZone(ZoneId.systemDefault()).toInstant())
            .atZone(ZoneId.systemDefault()).toLocalDateTime();
    }
}
