package com.chatbot.core.analytics.controller;

import com.chatbot.core.analytics.model.CustomReport;
import com.chatbot.core.analytics.model.ReportSchedule;
import com.chatbot.core.analytics.dto.CustomReportRequest;
import com.chatbot.core.analytics.dto.CustomReportResponse;
import com.chatbot.core.analytics.dto.ReportScheduleRequest;
import com.chatbot.core.analytics.dto.ReportScheduleResponse;
import com.chatbot.core.analytics.service.ReportScheduleService;
import com.chatbot.core.analytics.service.ReportService;
import com.chatbot.core.tenant.infra.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import jakarta.validation.Valid;

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
        if (tenantId == null) return ResponseEntity.badRequest().body(com.chatbot.shared.dto.ErrorResponse.fromMessage("Tenant context missing"));
        return ResponseEntity.ok(reportService.listReports(tenantId).stream()
                .map(CustomReportResponse::from).toList());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<?> createReport(@Valid @RequestBody CustomReportRequest request) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) return ResponseEntity.badRequest().body(com.chatbot.shared.dto.ErrorResponse.fromMessage("Tenant context missing"));
        CustomReport report = new CustomReport();
        report.setName(request.getName());
        report.setDescription(request.getDescription());
        report.setReportType(request.getReportType());
        report.setDateRangeDays(request.getDateRangeDays());
        report.setFilters(request.getFilters());
        return ResponseEntity.ok(CustomReportResponse.from(reportService.createReport(report, tenantId)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<?> updateReport(@PathVariable Long id, @Valid @RequestBody CustomReportRequest request) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) return ResponseEntity.badRequest().body(com.chatbot.shared.dto.ErrorResponse.fromMessage("Tenant context missing"));
        CustomReport report = new CustomReport();
        report.setName(request.getName());
        report.setDescription(request.getDescription());
        report.setReportType(request.getReportType());
        report.setDateRangeDays(request.getDateRangeDays());
        report.setFilters(request.getFilters());
        return ResponseEntity.ok(CustomReportResponse.from(reportService.updateReport(id, report, tenantId)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<?> deleteReport(@PathVariable Long id) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) return ResponseEntity.badRequest().body(com.chatbot.shared.dto.ErrorResponse.fromMessage("Tenant context missing"));
        reportService.deleteReport(id, tenantId);
        return ResponseEntity.ok(Map.of("deleted", true));
    }

    @PostMapping("/{id}/generate")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SUPERVISOR')")
    public ResponseEntity<?> generateReport(@PathVariable Long id) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) return ResponseEntity.badRequest().body(com.chatbot.shared.dto.ErrorResponse.fromMessage("Tenant context missing"));
        return ResponseEntity.ok(reportService.generateReport(id, tenantId));
    }

    @GetMapping("/{id}/result")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SUPERVISOR')")
    public ResponseEntity<?> getReportResult(@PathVariable Long id) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) return ResponseEntity.badRequest().body(com.chatbot.shared.dto.ErrorResponse.fromMessage("Tenant context missing"));
        return ResponseEntity.ok(reportService.getLatestResult(id, tenantId));
    }

    // ---------- Schedules ----------

    @GetMapping("/schedules")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<?> listSchedules() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) return ResponseEntity.badRequest().body(com.chatbot.shared.dto.ErrorResponse.fromMessage("Tenant context missing"));
        return ResponseEntity.ok(scheduleService.listSchedules(tenantId).stream()
                .map(ReportScheduleResponse::from).toList());
    }

    @PostMapping("/schedules")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<?> createSchedule(@Valid @RequestBody ReportScheduleRequest request) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) return ResponseEntity.badRequest().body(com.chatbot.shared.dto.ErrorResponse.fromMessage("Tenant context missing"));
        if (request.getReportId() == null) {
            return ResponseEntity.badRequest().body(com.chatbot.shared.dto.ErrorResponse.fromMessage("reportId is required"));
        }
        ReportSchedule schedule = new ReportSchedule();
        schedule.setReportId(request.getReportId());
        schedule.setCronExpression(request.getCronExpression());
        schedule.setEnabled(request.getEnabled());
        return ResponseEntity.ok(ReportScheduleResponse.from(scheduleService.createSchedule(schedule, tenantId)));
    }

    @PutMapping("/schedules/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<?> updateSchedule(@PathVariable Long id, @Valid @RequestBody ReportScheduleRequest request) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) return ResponseEntity.badRequest().body(com.chatbot.shared.dto.ErrorResponse.fromMessage("Tenant context missing"));
        ReportSchedule schedule = new ReportSchedule();
        schedule.setCronExpression(request.getCronExpression());
        schedule.setEnabled(request.getEnabled());
        return ResponseEntity.ok(ReportScheduleResponse.from(scheduleService.updateSchedule(id, schedule, tenantId)));
    }

    @DeleteMapping("/schedules/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<?> deleteSchedule(@PathVariable Long id) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) return ResponseEntity.badRequest().body(com.chatbot.shared.dto.ErrorResponse.fromMessage("Tenant context missing"));
        scheduleService.deleteSchedule(id, tenantId);
        return ResponseEntity.ok(Map.of("deleted", true));
    }
}
