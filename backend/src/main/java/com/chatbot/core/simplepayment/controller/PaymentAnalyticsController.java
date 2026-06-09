package com.chatbot.core.simplepayment.controller;

import com.chatbot.core.simplepayment.service.PaymentAnalyticsService;
import com.chatbot.shared.constants.ApiConstants;
import com.chatbot.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(ApiConstants.BASE_PATH + "/analytics/payments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Payment Analytics", description = "API for payment analytics and reporting")
public class PaymentAnalyticsController {

    private final PaymentAnalyticsService analyticsService;

    /**
     * Get revenue summary for date range (admin only)
     */
    @GetMapping("/revenue-summary")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "Get revenue summary", description = "Get revenue summary for a date range (Admin only)")
    public ResponseEntity<ApiResponse<PaymentAnalyticsService.RevenueSummary>> getRevenueSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        log.info("📊 Getting revenue summary from {} to {}", startDate, endDate);
        try {
            PaymentAnalyticsService.RevenueSummary summary = analyticsService.getRevenueSummary(startDate, endDate);
            return ResponseEntity.ok(ApiResponse.success(summary, "Revenue summary retrieved successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Get daily revenue for a month (admin only)
     */
    @GetMapping("/daily-revenue")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "Get daily revenue", description = "Get daily revenue for a specific month (Admin only)")
    public ResponseEntity<ApiResponse<List<PaymentAnalyticsService.DailyRevenue>>> getDailyRevenue(
            @RequestParam int year,
            @RequestParam int month) {
        log.info("📊 Getting daily revenue for {}/{}", year, month);
        try {
            List<PaymentAnalyticsService.DailyRevenue> dailyRevenue = analyticsService.getDailyRevenue(year, month);
            return ResponseEntity.ok(ApiResponse.success(dailyRevenue, "Daily revenue retrieved successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Get payment trends (admin only)
     */
    @GetMapping("/trends")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "Get payment trends", description = "Get payment trends for the last N days (Admin only)")
    public ResponseEntity<ApiResponse<PaymentAnalyticsService.PaymentTrends>> getPaymentTrends(
            @RequestParam(defaultValue = "30") int days) {
        log.info("📊 Getting payment trends for last {} days", days);
        try {
            PaymentAnalyticsService.PaymentTrends trends = analyticsService.getPaymentTrends(days);
            return ResponseEntity.ok(ApiResponse.success(trends, "Payment trends retrieved successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Get package performance (admin only)
     */
    @GetMapping("/package-performance")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "Get package performance", description = "Get package performance metrics (Admin only)")
    public ResponseEntity<ApiResponse<List<PaymentAnalyticsService.PackagePerformance>>> getPackagePerformance(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        log.info("📊 Getting package performance from {} to {}", startDate, endDate);
        try {
            List<PaymentAnalyticsService.PackagePerformance> performance = analyticsService.getPackagePerformance(startDate, endDate);
            return ResponseEntity.ok(ApiResponse.success(performance, "Package performance retrieved successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Get top users by spending (admin only)
     */
    @GetMapping("/top-users")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "Get top users by spending", description = "Get top users by total spending (Admin only)")
    public ResponseEntity<ApiResponse<List<PaymentAnalyticsService.UserSpending>>> getTopUsersBySpending(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        log.info("📊 Getting top {} users by spending from {} to {}", limit, startDate, endDate);
        try {
            List<PaymentAnalyticsService.UserSpending> topUsers = analyticsService.getTopUsersBySpending(limit, startDate, endDate);
            return ResponseEntity.ok(ApiResponse.success(topUsers, "Top users retrieved successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Get quick analytics for dashboard (admin only)
     */
    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "Get dashboard analytics", description = "Get quick analytics for admin dashboard (Admin only)")
    public ResponseEntity<ApiResponse<Object>> getDashboardAnalytics() {
        log.info("📊 Getting dashboard analytics");
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime startOfMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
            LocalDateTime startOfWeek = now.minusDays(7);
            
            // Get monthly revenue
            PaymentAnalyticsService.RevenueSummary monthlySummary = analyticsService.getRevenueSummary(startOfMonth, now);
            
            // Get weekly revenue
            PaymentAnalyticsService.RevenueSummary weeklySummary = analyticsService.getRevenueSummary(startOfWeek, now);
            
            // Get payment trends
            PaymentAnalyticsService.PaymentTrends trends = analyticsService.getPaymentTrends(30);
            
            Map<String, Object> dashboard = new java.util.HashMap<>();
            dashboard.put("monthlyRevenue", monthlySummary);
            dashboard.put("weeklyRevenue", weeklySummary);
            dashboard.put("trends", trends);
            dashboard.put("generatedAt", now);
            
            return ResponseEntity.ok(ApiResponse.success(dashboard, "Dashboard analytics retrieved successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
