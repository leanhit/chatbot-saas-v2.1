package com.chatbot.core.analytics.controller;

import com.chatbot.core.analytics.model.CustomReport;
import com.chatbot.core.analytics.model.ReportSchedule;
import com.chatbot.core.analytics.service.ReportScheduleService;
import com.chatbot.core.analytics.service.ReportService;
import com.chatbot.core.tenant.infra.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Custom reports + schedules. All operations tenant-scoped.
 */
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Slf4j
public class ReportController {

    private final ReportService reportService;
    private final ReportScheduleService scheduleService;

    // ---------- Reports ----------

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SUPERVISOR')")
    public ResponseEntity<?> listReports() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) return ResponseEntity.badRequest().body(Map.of("error", "Tenant context missing"));
        return ResponseEntity.ok(reportService.listReports(tenantId));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<?> createReport(@RequestBody CustomReport report) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) return ResponseEntity.badRequest().body(Map.of("error", "Tenant context missing"));
        return ResponseEntity.ok(reportService.createReport(report, tenantId));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<?> updateReport(@PathVariable Long id, @RequestBody CustomReport report) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) return ResponseEntity.badRequest().body(Map.of("error", "Tenant context missing"));
        return ResponseEntity.ok(reportService.updateReport(id, report, tenantId));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<?> deleteReport(@PathVariable Long id) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) return ResponseEntity.badRequest().body(Map.of("error", "Tenant context missing"));
        reportService.deleteReport(id, tenantId);
        return ResponseEntity.ok(Map.of("deleted", true));
    }

    @PostMapping("/{id}/generate")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SUPERVISOR')")
    public ResponseEntity<?> generateReport(@PathVariable Long id) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) return ResponseEntity.badRequest().body(Map.of("error", "Tenant context missing"));
        return ResponseEntity.ok(reportService.generateReport(id, tenantId));
    }

    @GetMapping("/{id}/result")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SUPERVISOR')")
    public ResponseEntity<?> getReportResult(@PathVariable Long id) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) return ResponseEntity.badRequest().body(Map.of("error", "Tenant context missing"));
        return ResponseEntity.ok(reportService.getLatestResult(id, tenantId));
    }

    // ---------- Schedules ----------

    @GetMapping("/schedules")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<?> listSchedules() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) return ResponseEntity.badRequest().body(Map.of("error", "Tenant context missing"));
        return ResponseEntity.ok(scheduleService.listSchedules(tenantId));
    }

    @PostMapping("/schedules")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<?> createSchedule(@RequestBody ReportSchedule schedule) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) return ResponseEntity.badRequest().body(Map.of("error", "Tenant context missing"));
        return ResponseEntity.ok(scheduleService.createSchedule(schedule, tenantId));
    }

    @PutMapping("/schedules/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<?> updateSchedule(@PathVariable Long id, @RequestBody ReportSchedule schedule) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) return ResponseEntity.badRequest().body(Map.of("error", "Tenant context missing"));
        return ResponseEntity.ok(scheduleService.updateSchedule(id, schedule, tenantId));
    }

    @DeleteMapping("/schedules/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<?> deleteSchedule(@PathVariable Long id) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) return ResponseEntity.badRequest().body(Map.of("error", "Tenant context missing"));
        scheduleService.deleteSchedule(id, tenantId);
        return ResponseEntity.ok(Map.of("deleted", true));
    }
}
