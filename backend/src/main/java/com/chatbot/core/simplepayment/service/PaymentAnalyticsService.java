package com.chatbot.core.simplepayment.service;

import com.chatbot.core.simplepayment.model.PaymentStatus;
import com.chatbot.core.simplepayment.model.SimplePayment;
import com.chatbot.core.simplepayment.repository.SimplePaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentAnalyticsService {

    private final SimplePaymentRepository paymentRepository;

    /**
     * Get revenue summary for a date range
     */
    @Transactional(readOnly = true, transactionManager = "sharedTransactionManager")
    public RevenueSummary getRevenueSummary(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("📊 Getting revenue summary from {} to {}", startDate, endDate);

        List<SimplePayment> payments = paymentRepository.findByCreatedAtBetween(startDate, endDate);

        BigDecimal totalRevenue = BigDecimal.ZERO;
        BigDecimal completedRevenue = BigDecimal.ZERO;
        long totalPayments = payments.size();
        long completedPayments = 0;
        long failedPayments = 0;
        long pendingPayments = 0;
        long expiredPayments = 0;
        long cancelledPayments = 0;
        long refundedPayments = 0;

        Map<String, Long> paymentsByPackage = new HashMap<>();
        Map<String, BigDecimal> revenueByPackage = new HashMap<>();

        for (SimplePayment payment : payments) {
            totalRevenue = totalRevenue.add(payment.getAmount());

            String packageId = payment.getTargetPackageId() != null ? payment.getTargetPackageId() : "STANDARD";
            paymentsByPackage.put(packageId, paymentsByPackage.getOrDefault(packageId, 0L) + 1);

            switch (payment.getStatus()) {
                case COMPLETED:
                    completedRevenue = completedRevenue.add(payment.getAmount());
                    completedPayments++;
                    revenueByPackage.put(packageId, revenueByPackage.getOrDefault(packageId, BigDecimal.ZERO).add(payment.getAmount()));
                    break;
                case FAILED:
                    failedPayments++;
                    break;
                case PENDING:
                    pendingPayments++;
                    break;
                case EXPIRED:
                    expiredPayments++;
                    break;
                case CANCELLED:
                    cancelledPayments++;
                    break;
                case REFUNDED:
                    refundedPayments++;
                    break;
            }
        }

        BigDecimal conversionRate = totalPayments > 0 
            ? BigDecimal.valueOf(completedPayments * 100.0 / totalPayments).setScale(2, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;

        return RevenueSummary.builder()
                .totalRevenue(totalRevenue)
                .completedRevenue(completedRevenue)
                .totalPayments(totalPayments)
                .completedPayments(completedPayments)
                .failedPayments(failedPayments)
                .pendingPayments(pendingPayments)
                .expiredPayments(expiredPayments)
                .cancelledPayments(cancelledPayments)
                .refundedPayments(refundedPayments)
                .conversionRate(conversionRate)
                .paymentsByPackage(paymentsByPackage)
                .revenueByPackage(revenueByPackage)
                .startDate(startDate)
                .endDate(endDate)
                .build();
    }

    /**
     * Get daily revenue for a month
     */
    @Transactional(readOnly = true, transactionManager = "sharedTransactionManager")
    public List<DailyRevenue> getDailyRevenue(int year, int month) {
        log.info("📊 Getting daily revenue for {}/{}", year, month);

        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDateTime startDate = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime endDate = yearMonth.atEndOfMonth().atTime(23, 59, 59);

        List<SimplePayment> payments = paymentRepository.findByCreatedAtBetween(startDate, endDate);

        Map<Integer, DailyRevenue> dailyMap = new HashMap<>();

        for (SimplePayment payment : payments) {
            int day = payment.getCreatedAt().getDayOfMonth();
            
            DailyRevenue daily = dailyMap.computeIfAbsent(day, d -> 
                DailyRevenue.builder()
                    .date(yearMonth.atDay(d))
                    .revenue(BigDecimal.ZERO)
                    .payments(0L)
                    .build()
            );

            if (payment.getStatus() == PaymentStatus.COMPLETED) {
                daily.setRevenue(daily.getRevenue().add(payment.getAmount()));
            }
            daily.setPayments(daily.getPayments() + 1);
        }

        // Fill in missing days
        List<DailyRevenue> result = new ArrayList<>();
        for (int day = 1; day <= yearMonth.lengthOfMonth(); day++) {
            result.add(dailyMap.getOrDefault(day, 
                DailyRevenue.builder()
                    .date(yearMonth.atDay(day))
                    .revenue(BigDecimal.ZERO)
                    .payments(0L)
                    .build()
            ));
        }

        return result;
    }

    /**
     * Get payment trends
     */
    @Transactional(readOnly = true, transactionManager = "sharedTransactionManager")
    public PaymentTrends getPaymentTrends(int days) {
        log.info("📊 Getting payment trends for last {} days", days);

        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(days);

        List<SimplePayment> payments = paymentRepository.findByCreatedAtBetween(startDate, endDate);

        // Group by day
        Map<String, List<SimplePayment>> paymentsByDay = payments.stream()
            .collect(Collectors.groupingBy(p -> p.getCreatedAt().toLocalDate().toString()));

        List<DailyRevenue> dailyRevenues = paymentsByDay.entrySet().stream()
            .map(entry -> {
                String date = entry.getKey();
                List<SimplePayment> dayPayments = entry.getValue();
                
                BigDecimal revenue = dayPayments.stream()
                    .filter(p -> p.getStatus() == PaymentStatus.COMPLETED)
                    .map(SimplePayment::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                
                return DailyRevenue.builder()
                    .date(java.time.LocalDate.parse(date).atStartOfDay().toLocalDate())
                    .revenue(revenue)
                    .payments((long) dayPayments.size())
                    .build();
            })
            .sorted(Comparator.comparing(DailyRevenue::getDate))
            .collect(Collectors.toList());

        // Calculate trends
        BigDecimal totalRevenue = dailyRevenues.stream()
            .map(DailyRevenue::getRevenue)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        long totalPayments = dailyRevenues.stream()
            .mapToLong(DailyRevenue::getPayments)
            .sum();

        BigDecimal averageDailyRevenue = dailyRevenues.isEmpty() ? BigDecimal.ZERO 
            : totalRevenue.divide(BigDecimal.valueOf(dailyRevenues.size()), 2, RoundingMode.HALF_UP);

        return PaymentTrends.builder()
                .startDate(startDate)
                .endDate(endDate)
                .dailyRevenues(dailyRevenues)
                .totalRevenue(totalRevenue)
                .totalPayments(totalPayments)
                .averageDailyRevenue(averageDailyRevenue)
                .build();
    }

    /**
     * Get package performance
     */
    @Transactional(readOnly = true, transactionManager = "sharedTransactionManager")
    public List<PackagePerformance> getPackagePerformance(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("📊 Getting package performance from {} to {}", startDate, endDate);

        List<SimplePayment> payments = paymentRepository.findByCreatedAtBetween(startDate, endDate);

        Map<String, PackagePerformance> performanceMap = new HashMap<>();

        for (SimplePayment payment : payments) {
            String packageId = payment.getTargetPackageId() != null ? payment.getTargetPackageId() : "STANDARD";
            
            PackagePerformance perf = performanceMap.computeIfAbsent(packageId, pid -> 
                PackagePerformance.builder()
                    .packageId(pid)
                    .totalRevenue(BigDecimal.ZERO)
                    .totalPayments(0L)
                    .completedPayments(0L)
                    .failedPayments(0L)
                    .build()
            );

            perf.setTotalRevenue(perf.getTotalRevenue().add(payment.getAmount()));
            perf.setTotalPayments(perf.getTotalPayments() + 1);

            if (payment.getStatus() == PaymentStatus.COMPLETED) {
                perf.setCompletedPayments(perf.getCompletedPayments() + 1);
            } else if (payment.getStatus() == PaymentStatus.FAILED) {
                perf.setFailedPayments(perf.getFailedPayments() + 1);
            }
        }

        // Calculate conversion rates
        List<PackagePerformance> result = performanceMap.values().stream()
            .map(perf -> {
                BigDecimal conversionRate = perf.getTotalPayments() > 0
                    ? BigDecimal.valueOf(perf.getCompletedPayments() * 100.0 / perf.getTotalPayments())
                        .setScale(2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
                perf.setConversionRate(conversionRate);
                return perf;
            })
            .sorted(Comparator.comparing(PackagePerformance::getTotalRevenue).reversed())
            .collect(Collectors.toList());

        return result;
    }

    /**
     * Get top users by spending
     */
    @Transactional(readOnly = true, transactionManager = "sharedTransactionManager")
    public List<UserSpending> getTopUsersBySpending(int limit, LocalDateTime startDate, LocalDateTime endDate) {
        log.info("📊 Getting top {} users by spending from {} to {}", limit, startDate, endDate);

        List<SimplePayment> payments = paymentRepository.findByCreatedAtBetween(startDate, endDate);

        Map<Long, UserSpending> spendingMap = new HashMap<>();

        for (SimplePayment payment : payments) {
            if (payment.getStatus() != PaymentStatus.COMPLETED) {
                continue;
            }

            UserSpending spending = spendingMap.computeIfAbsent(payment.getUserId(), userId -> 
                UserSpending.builder()
                    .userId(userId)
                    .totalSpending(BigDecimal.ZERO)
                    .paymentCount(0L)
                    .build()
            );

            spending.setTotalSpending(spending.getTotalSpending().add(payment.getAmount()));
            spending.setPaymentCount(spending.getPaymentCount() + 1);
        }

        return spendingMap.values().stream()
            .sorted(Comparator.comparing(UserSpending::getTotalSpending).reversed())
            .limit(limit)
            .collect(Collectors.toList());
    }

    // DTO classes
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class RevenueSummary {
        private BigDecimal totalRevenue;
        private BigDecimal completedRevenue;
        private Long totalPayments;
        private Long completedPayments;
        private Long failedPayments;
        private Long pendingPayments;
        private Long expiredPayments;
        private Long cancelledPayments;
        private Long refundedPayments;
        private BigDecimal conversionRate;
        private Map<String, Long> paymentsByPackage;
        private Map<String, BigDecimal> revenueByPackage;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class DailyRevenue {
        private java.time.LocalDate date;
        private BigDecimal revenue;
        private Long payments;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class PaymentTrends {
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private List<DailyRevenue> dailyRevenues;
        private BigDecimal totalRevenue;
        private Long totalPayments;
        private BigDecimal averageDailyRevenue;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class PackagePerformance {
        private String packageId;
        private BigDecimal totalRevenue;
        private Long totalPayments;
        private Long completedPayments;
        private Long failedPayments;
        private BigDecimal conversionRate;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class UserSpending {
        private Long userId;
        private BigDecimal totalSpending;
        private Long paymentCount;
    }
}
