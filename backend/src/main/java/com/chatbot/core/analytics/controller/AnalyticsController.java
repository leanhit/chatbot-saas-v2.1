package com.chatbot.core.analytics.controller;

import com.chatbot.core.analytics.dto.AnalyticsSummaryDTO;
import com.chatbot.core.analytics.model.AnalyticsDailySummary;
import com.chatbot.core.analytics.model.FunnelStage;
import com.chatbot.core.analytics.service.AnalyticsAggregationService;
import com.chatbot.core.analytics.service.CustomerJourneyService;
import com.chatbot.core.analytics.service.FunnelService;
import com.chatbot.core.tenant.infra.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Tenant-scoped analytics endpoints (Phase 5).
 * All queries are limited to the current tenant and date range.
 */
@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@Slf4j
public class AnalyticsController {

    private final AnalyticsAggregationService aggregationService;
    private final CustomerJourneyService journeyService;
    private final FunnelService funnelService;

    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SUPERVISOR')")
    public ResponseEntity<?> getSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Tenant context missing"));
        }
        List<AnalyticsSummaryDTO> summaries = aggregationService.getSummary(tenantId, from, to)
            .stream().map(AnalyticsSummaryDTO::from).toList();
        return ResponseEntity.ok(summaries);
    }

    @GetMapping("/summary/{date}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SUPERVISOR')")
    public ResponseEntity<?> getSummaryForDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Tenant context missing"));
        }
        return aggregationService.getSummaryForDate(tenantId, date)
            .map(s -> ResponseEntity.ok(AnalyticsSummaryDTO.from(s)))
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/aggregate/{date}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<?> aggregateForDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Tenant context missing"));
        }
        AnalyticsDailySummary summary = aggregationService.aggregateForDate(tenantId, date);
        return ResponseEntity.ok(AnalyticsSummaryDTO.from(summary));
    }

    // ---------- Customer journey ----------

    @GetMapping("/journey/{customerProfileId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SUPERVISOR', 'AGENT')")
    public ResponseEntity<?> getCustomerJourney(@PathVariable Long customerProfileId) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Tenant context missing"));
        }
        return ResponseEntity.ok(journeyService.getJourney(customerProfileId, tenantId));
    }

    // ---------- Funnel ----------

    @GetMapping("/funnel")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SUPERVISOR')")
    public ResponseEntity<?> getFunnel() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Tenant context missing"));
        }
        return ResponseEntity.ok(funnelService.computeFunnel(tenantId));
    }

    @GetMapping("/funnel/stages")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<?> listFunnelStages() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Tenant context missing"));
        }
        return ResponseEntity.ok(funnelService.listStages(tenantId));
    }

    @PostMapping("/funnel/stages")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<?> createFunnelStage(@RequestBody FunnelStage stage) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Tenant context missing"));
        }
        return ResponseEntity.ok(funnelService.createStage(stage, tenantId));
    }

    @PutMapping("/funnel/stages/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<?> updateFunnelStage(@PathVariable Long id, @RequestBody FunnelStage stage) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Tenant context missing"));
        }
        return ResponseEntity.ok(funnelService.updateStage(id, stage, tenantId));
    }

    @DeleteMapping("/funnel/stages/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<?> deleteFunnelStage(@PathVariable Long id) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Tenant context missing"));
        }
        funnelService.deleteStage(id, tenantId);
        return ResponseEntity.ok(Map.of("deleted", true));
    }
}
