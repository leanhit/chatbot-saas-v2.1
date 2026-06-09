package com.chatbot.core.simplepayment.health;

import com.chatbot.core.simplepayment.model.PaymentStatus;
import com.chatbot.core.simplepayment.repository.SimplePaymentRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/simple-payment/metrics")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Payment Metrics", description = "Payment metrics and analytics endpoints")
public class PaymentMetricsEndpoint {

    private final SimplePaymentRepository paymentRepository;

    @GetMapping("/overview")
    @Operation(
        summary = "Get payment overview metrics",
        description = "Get overall payment metrics for monitoring"
    )
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<Map<String, Object>> getPaymentOverview() {
        Map<String, Object> metrics = new HashMap<>();
        
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime todayStart = now.toLocalDate().atStartOfDay();
            LocalDateTime weekStart = now.minus(7, ChronoUnit.DAYS);
            LocalDateTime monthStart = now.minus(30, ChronoUnit.DAYS);
            
            // Total counts
            long totalPayments = paymentRepository.count();
            metrics.put("totalPayments", totalPayments);
            
            // Status breakdown
            metrics.put("pendingCount", paymentRepository.countByStatus(PaymentStatus.PENDING));
            metrics.put("completedCount", paymentRepository.countByStatus(PaymentStatus.COMPLETED));
            metrics.put("failedCount", paymentRepository.countByStatus(PaymentStatus.FAILED));
            metrics.put("expiredCount", paymentRepository.countByStatus(PaymentStatus.EXPIRED));
            metrics.put("cancelledCount", paymentRepository.countByStatus(PaymentStatus.CANCELLED));
            metrics.put("refundedCount", paymentRepository.countByStatus(PaymentStatus.REFUNDED));
            
            // Today's metrics
            List<com.chatbot.core.simplepayment.model.SimplePayment> todayPayments = 
                paymentRepository.findByCreatedAtBetween(todayStart, now);
            metrics.put("todayPayments", todayPayments.size());
            
            BigDecimal todayRevenue = todayPayments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.COMPLETED)
                .map(com.chatbot.core.simplepayment.model.SimplePayment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            metrics.put("todayRevenue", todayRevenue);
            
            // This week's metrics
            List<com.chatbot.core.simplepayment.model.SimplePayment> weekPayments = 
                paymentRepository.findByCreatedAtBetween(weekStart, now);
            metrics.put("weekPayments", weekPayments.size());
            
            BigDecimal weekRevenue = weekPayments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.COMPLETED)
                .map(com.chatbot.core.simplepayment.model.SimplePayment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            metrics.put("weekRevenue", weekRevenue);
            
            // This month's metrics
            List<com.chatbot.core.simplepayment.model.SimplePayment> monthPayments = 
                paymentRepository.findByCreatedAtBetween(monthStart, now);
            metrics.put("monthPayments", monthPayments.size());
            
            BigDecimal monthRevenue = monthPayments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.COMPLETED)
                .map(com.chatbot.core.simplepayment.model.SimplePayment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            metrics.put("monthRevenue", monthRevenue);
            
            // Success rate
            long completedPayments = paymentRepository.countByStatus(PaymentStatus.COMPLETED);
            double successRate = totalPayments > 0 ? 
                (double) completedPayments / totalPayments * 100 : 0;
            metrics.put("successRate", String.format("%.2f%%", successRate));
            
            metrics.put("timestamp", now);
            
            return ResponseEntity.ok(metrics);
            
        } catch (Exception e) {
            log.error("Failed to get payment overview metrics", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/by-date-range")
    @Operation(
        summary = "Get payment metrics by date range",
        description = "Get payment metrics for a specific date range"
    )
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<Map<String, Object>> getMetricsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        Map<String, Object> metrics = new HashMap<>();
        
        try {
            List<com.chatbot.core.simplepayment.model.SimplePayment> payments = 
                paymentRepository.findByCreatedAtBetween(startDate, endDate);
            
            metrics.put("periodStart", startDate);
            metrics.put("periodEnd", endDate);
            metrics.put("totalPayments", payments.size());
            
            BigDecimal totalRevenue = payments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.COMPLETED)
                .map(com.chatbot.core.simplepayment.model.SimplePayment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            metrics.put("totalRevenue", totalRevenue);
            
            // Status breakdown
            Map<String, Long> statusBreakdown = new HashMap<>();
            for (PaymentStatus status : PaymentStatus.values()) {
                long count = payments.stream()
                    .filter(p -> p.getStatus() == status)
                    .count();
                statusBreakdown.put(status.name(), count);
            }
            metrics.put("statusBreakdown", statusBreakdown);
            
            return ResponseEntity.ok(metrics);
            
        } catch (Exception e) {
            log.error("Failed to get metrics by date range", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/health-detailed")
    @Operation(
        summary = "Get detailed health status",
        description = "Get detailed health status of payment system"
    )
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<Map<String, Object>> getDetailedHealth() {
        Map<String, Object> health = new HashMap<>();
        
        try {
            LocalDateTime now = LocalDateTime.now();
            
            // Pending payments that are about to expire (within 1 hour)
            LocalDateTime oneHourFromNow = now.plusHours(1);
            List<com.chatbot.core.simplepayment.model.SimplePayment> expiringSoon = 
                paymentRepository.findActivePendingPayments(oneHourFromNow);
            health.put("expiringSoonCount", expiringSoon.size());
            
            // Expired payments that haven't been cleaned up
            List<com.chatbot.core.simplepayment.model.SimplePayment> expiredPayments = 
                paymentRepository.findByStatusAndExpiresAtBefore(PaymentStatus.PENDING, now);
            health.put("expiredNotCleanedCount", expiredPayments.size());
            
            // Recent failed payments (last 24 hours)
            List<com.chatbot.core.simplepayment.model.SimplePayment> recentFailed = 
                paymentRepository.findByStatusAndCreatedAtAfter(PaymentStatus.FAILED, now.minusDays(1));
            health.put("recentFailedCount", recentFailed.size());
            
            health.put("timestamp", now);
            health.put("status", "HEALTHY");
            
            return ResponseEntity.ok(health);
            
        } catch (Exception e) {
            log.error("Failed to get detailed health status", e);
            health.put("status", "UNHEALTHY");
            health.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(health);
        }
    }
}
