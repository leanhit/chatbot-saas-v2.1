package com.chatbot.core.analytics.service;

import com.chatbot.core.analytics.model.CustomReport;
import com.chatbot.core.analytics.model.ReportSchedule;
import com.chatbot.core.analytics.repository.CustomReportRepository;
import com.chatbot.core.analytics.repository.ReportScheduleRepository;
import com.chatbot.core.tenant.infra.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

/**
 * Polls due report schedules and regenerates reports.
 * Runs without tenant context (system job); sets TenantContext per schedule
 * so all downstream queries stay tenant-scoped.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReportScheduler {

    private final ReportScheduleRepository scheduleRepository;
    private final CustomReportRepository reportRepository;
    private final ReportService reportService;

    @Scheduled(fixedDelay = 60_000, initialDelay = 120_000)
    @SchedulerLock(name = "reportScheduleRunner", lockAtMostFor = "10m", lockAtLeastFor = "30s")
    @Transactional
    public void runDueSchedules() {
        LocalDateTime now = LocalDateTime.now();
        List<ReportSchedule> due = scheduleRepository
            .findByEnabledTrueAndNextRunAtBeforeAndIsDeletedFalse(now);
        if (due.isEmpty()) return;

        log.info("[ReportScheduler] {} report schedule(s) due", due.size());
        for (ReportSchedule schedule : due) {
            try {
                TenantContext.setTenantId(schedule.getTenantId());
                CustomReport report = reportRepository
                    .findByIdAndTenantIdAndIsDeletedFalse(schedule.getReportId(), schedule.getTenantId())
                    .orElse(null);
                if (report == null) {
                    schedule.setLastRunStatus("REPORT_NOT_FOUND");
                } else {
                    reportService.generateReport(report.getId(), schedule.getTenantId());
                    schedule.setLastRunStatus("SUCCESS");
                    log.info("[ReportScheduler] Generated report {} for tenant {}",
                        report.getId(), schedule.getTenantId());
                }
            } catch (Exception e) {
                schedule.setLastRunStatus("FAILED: " + e.getMessage());
                log.error("[ReportScheduler] Failed schedule {}: {}", schedule.getId(), e.getMessage());
            } finally {
                schedule.setLastRunAt(now);
                schedule.setNextRunAt(computeNextRun(schedule.getCronExpression(), now));
                scheduleRepository.save(schedule);
                TenantContext.clear();
            }
        }
    }

    private LocalDateTime computeNextRun(String cron, LocalDateTime after) {
        try {
            CronExpression expr = CronExpression.parse(cron);
            return expr.next(after.atZone(ZoneId.systemDefault()).toInstant())
                .atZone(ZoneId.systemDefault()).toLocalDateTime();
        } catch (Exception e) {
            log.warn("[ReportScheduler] Invalid cron '{}', defaulting to +1 day", cron);
            return after.plusDays(1);
        }
    }
}
