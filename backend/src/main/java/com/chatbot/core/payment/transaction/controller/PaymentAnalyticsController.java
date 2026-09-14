package com.chatbot.core.payment.transaction.controller;

import com.chatbot.core.payment.transaction.model.PaymentStatus;
import com.chatbot.core.payment.transaction.model.SimplePayment;
import com.chatbot.core.payment.transaction.repository.SimplePaymentRepository;
import com.chatbot.core.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping({"/api/v1/analytics/payments", "/api/analytics/payments"})
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Payment Analytics", description = "Payment analytics & metrics endpoints")
public class PaymentAnalyticsController {

    private final SimplePaymentRepository paymentRepository;
    private final UserRepository userRepository;

    /**
     * Get payment dashboard analytics summary
     */
    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get payment dashboard analytics", description = "Summary of monthly/weekly revenue and payment trends")
    public ResponseEntity<Map<String, Object>> getDashboardAnalytics() {
        log.info("📊 Fetching payment dashboard analytics");

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime monthStart = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime weekStart = now.minusDays(7);

        List<SimplePayment> allPayments = paymentRepository.findAll();

        // Monthly revenue & completed count
        List<SimplePayment> monthlyPayments = allPayments.stream()
                .filter(p -> p.getCreatedAt() != null && p.getCreatedAt().isAfter(monthStart))
                .toList();

        BigDecimal monthlyRevenue = monthlyPayments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.COMPLETED && p.getAmount() != null)
                .map(SimplePayment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long monthlyCompletedCount = monthlyPayments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.COMPLETED)
                .count();

        // Weekly revenue
        List<SimplePayment> weeklyPayments = allPayments.stream()
                .filter(p -> p.getCreatedAt() != null && p.getCreatedAt().isAfter(weekStart))
                .toList();

        BigDecimal weeklyRevenue = weeklyPayments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.COMPLETED && p.getAmount() != null)
                .map(SimplePayment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long weeklyCompletedCount = weeklyPayments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.COMPLETED)
                .count();

        // Overall trends
        long totalPayments = allPayments.size();
        long completedCount = allPayments.stream().filter(p -> p.getStatus() == PaymentStatus.COMPLETED).count();
        long failedCount = allPayments.stream().filter(p -> p.getStatus() == PaymentStatus.FAILED).count();
        long pendingCount = allPayments.stream().filter(p -> p.getStatus() == PaymentStatus.PENDING).count();

        Map<String, Object> response = new HashMap<>();
        response.put("monthlyRevenue", Map.of(
            "totalRevenue", monthlyRevenue,
            "completedCount", monthlyCompletedCount
        ));
        response.put("weeklyRevenue", Map.of(
            "totalRevenue", weeklyRevenue,
            "completedCount", weeklyCompletedCount
        ));
        response.put("trends", Map.of(
            "totalPayments", totalPayments,
            "completedCount", completedCount,
            "failedCount", failedCount,
            "pendingCount", pendingCount
        ));

        return ResponseEntity.ok(response);
    }

    /**
     * Get top users by payment expenditure
     */
    @GetMapping("/top-users")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get top users by spending", description = "Get top spending users within date range")
    public ResponseEntity<List<Map<String, Object>>> getTopUsers(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        log.info("📊 Fetching top users: limit={}, startDate={}, endDate={}", limit, startDate, endDate);

        LocalDateTime start = parseDate(startDate, LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0));
        LocalDateTime end = parseDate(endDate, LocalDateTime.now());

        List<SimplePayment> payments = paymentRepository.findAll().stream()
                .filter(p -> p.getStatus() == PaymentStatus.COMPLETED)
                .filter(p -> p.getCreatedAt() != null && !p.getCreatedAt().isBefore(start) && !p.getCreatedAt().isAfter(end))
                .toList();

        Map<Long, BigDecimal> userSpending = payments.stream()
                .collect(Collectors.groupingBy(
                        SimplePayment::getUserId,
                        Collectors.reducing(BigDecimal.ZERO, p -> p.getAmount() != null ? p.getAmount() : BigDecimal.ZERO, BigDecimal::add)
                ));

        Map<Long, Long> userPaymentCount = payments.stream()
                .collect(Collectors.groupingBy(SimplePayment::getUserId, Collectors.counting()));

        List<Map<String, Object>> result = userSpending.entrySet().stream()
                .sorted(Map.Entry.<Long, BigDecimal>comparingByValue().reversed())
                .limit(limit)
                .map(entry -> {
                    Long userId = entry.getKey();
                    BigDecimal totalSpent = entry.getValue();
                    Long count = userPaymentCount.getOrDefault(userId, 0L);

                    Map<String, Object> userMap = new HashMap<>();
                    userMap.put("userId", userId);
                    userMap.put("totalSpent", totalSpent);
                    userMap.put("paymentCount", count);

                    userRepository.findById(userId).ifPresent(u -> {
                        userMap.put("userEmail", u.getEmail());
                        userMap.put("userName", u.getProfile() != null && u.getProfile().getFullName() != null 
                                ? u.getProfile().getFullName() : u.getEmail());
                    });

                    if (!userMap.containsKey("userEmail")) {
                        userMap.put("userEmail", "User #" + userId);
                        userMap.put("userName", "User #" + userId);
                    }

                    return userMap;
                })
                .toList();

        return ResponseEntity.ok(result);
    }

    /**
     * Get revenue summary for date range
     */
    @GetMapping("/revenue-summary")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get revenue summary", description = "Revenue summary for specified date range")
    public ResponseEntity<Map<String, Object>> getRevenueSummary(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        log.info("📊 Fetching revenue summary: startDate={}, endDate={}", startDate, endDate);

        LocalDateTime start = parseDate(startDate, LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0));
        LocalDateTime end = parseDate(endDate, LocalDateTime.now());

        List<SimplePayment> payments = paymentRepository.findAll().stream()
                .filter(p -> p.getCreatedAt() != null && !p.getCreatedAt().isBefore(start) && !p.getCreatedAt().isAfter(end))
                .toList();

        BigDecimal totalRevenue = payments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.COMPLETED && p.getAmount() != null)
                .map(SimplePayment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long completedCount = payments.stream().filter(p -> p.getStatus() == PaymentStatus.COMPLETED).count();
        long pendingCount = payments.stream().filter(p -> p.getStatus() == PaymentStatus.PENDING).count();
        long failedCount = payments.stream().filter(p -> p.getStatus() == PaymentStatus.FAILED).count();

        Map<String, Object> response = new HashMap<>();
        response.put("totalRevenue", totalRevenue);
        response.put("completedCount", completedCount);
        response.put("pendingCount", pendingCount);
        response.put("failedCount", failedCount);
        response.put("startDate", start);
        response.put("endDate", end);

        return ResponseEntity.ok(response);
    }

    /**
     * Get daily revenue breakdown
     */
    @GetMapping("/daily-revenue")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get daily revenue", description = "Daily revenue breakdown for year and month")
    public ResponseEntity<List<Map<String, Object>>> getDailyRevenue(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {

        log.info("📊 Fetching daily revenue: year={}, month={}", year, month);

        int targetYear = year != null ? year : LocalDateTime.now().getYear();
        int targetMonth = month != null ? month : LocalDateTime.now().getMonthValue();

        LocalDateTime start = LocalDateTime.of(targetYear, targetMonth, 1, 0, 0);
        LocalDateTime end = start.plusMonths(1).minusNanos(1);

        List<SimplePayment> payments = paymentRepository.findAll().stream()
                .filter(p -> p.getStatus() == PaymentStatus.COMPLETED)
                .filter(p -> p.getCreatedAt() != null && !p.getCreatedAt().isBefore(start) && !p.getCreatedAt().isAfter(end))
                .toList();

        Map<Integer, BigDecimal> dailyMap = payments.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getCreatedAt().getDayOfMonth(),
                        Collectors.reducing(BigDecimal.ZERO, p -> p.getAmount() != null ? p.getAmount() : BigDecimal.ZERO, BigDecimal::add)
                ));

        List<Map<String, Object>> result = new ArrayList<>();
        int daysInMonth = start.toLocalDate().lengthOfMonth();

        for (int day = 1; day <= daysInMonth; day++) {
            Map<String, Object> dayData = new HashMap<>();
            dayData.put("day", day);
            dayData.put("date", String.format("%04d-%02d-%02d", targetYear, targetMonth, day));
            dayData.put("revenue", dailyMap.getOrDefault(day, BigDecimal.ZERO));
            result.add(dayData);
        }

        return ResponseEntity.ok(result);
    }

    /**
     * Get payment trends over N days
     */
    @GetMapping("/trends")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get payment trends", description = "Payment trends over the specified number of days")
    public ResponseEntity<Map<String, Object>> getPaymentTrends(@RequestParam(defaultValue = "30") int days) {
        log.info("📊 Fetching payment trends: days={}", days);

        LocalDateTime since = LocalDateTime.now().minusDays(days);
        List<SimplePayment> payments = paymentRepository.findAll().stream()
                .filter(p -> p.getCreatedAt() != null && p.getCreatedAt().isAfter(since))
                .toList();

        long totalPayments = payments.size();
        long completedCount = payments.stream().filter(p -> p.getStatus() == PaymentStatus.COMPLETED).count();
        long failedCount = payments.stream().filter(p -> p.getStatus() == PaymentStatus.FAILED).count();
        long pendingCount = payments.stream().filter(p -> p.getStatus() == PaymentStatus.PENDING).count();

        Map<String, Object> response = new HashMap<>();
        response.put("days", days);
        response.put("totalPayments", totalPayments);
        response.put("completedCount", completedCount);
        response.put("failedCount", failedCount);
        response.put("pendingCount", pendingCount);

        return ResponseEntity.ok(response);
    }

    /**
     * Get performance by package
     */
    @GetMapping("/package-performance")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get package performance", description = "Package performance breakdown for date range")
    public ResponseEntity<List<Map<String, Object>>> getPackagePerformance(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        log.info("📊 Fetching package performance: startDate={}, endDate={}", startDate, endDate);

        LocalDateTime start = parseDate(startDate, LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0));
        LocalDateTime end = parseDate(endDate, LocalDateTime.now());

        List<SimplePayment> payments = paymentRepository.findAll().stream()
                .filter(p -> p.getStatus() == PaymentStatus.COMPLETED)
                .filter(p -> p.getCreatedAt() != null && !p.getCreatedAt().isBefore(start) && !p.getCreatedAt().isAfter(end))
                .toList();

        Map<String, BigDecimal> packageRevenue = payments.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getTargetPackageId() != null ? p.getTargetPackageId() : "DEFAULT",
                        Collectors.reducing(BigDecimal.ZERO, p -> p.getAmount() != null ? p.getAmount() : BigDecimal.ZERO, BigDecimal::add)
                ));

        Map<String, Long> packageCount = payments.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getTargetPackageId() != null ? p.getTargetPackageId() : "DEFAULT",
                        Collectors.counting()
                ));

        List<Map<String, Object>> result = new ArrayList<>();
        packageRevenue.forEach((pkgId, revenue) -> {
            Map<String, Object> item = new HashMap<>();
            item.put("packageId", pkgId);
            item.put("revenue", revenue);
            item.put("salesCount", packageCount.getOrDefault(pkgId, 0L));
            result.add(item);
        });

        return ResponseEntity.ok(result);
    }

    private LocalDateTime parseDate(String dateStr, LocalDateTime defaultValue) {
        if (dateStr == null || dateStr.isBlank()) {
            return defaultValue;
        }
        try {
            return LocalDateTime.parse(dateStr, DateTimeFormatter.ISO_DATE_TIME);
        } catch (Exception e) {
            try {
                return LocalDateTime.parse(dateStr + "T00:00:00");
            } catch (Exception ex) {
                return defaultValue;
            }
        }
    }
}
