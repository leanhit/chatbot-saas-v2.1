package com.chatbot.core.simplepayment.metrics;

import com.chatbot.core.simplepayment.model.PaymentStatus;
import com.chatbot.core.simplepayment.repository.SimplePaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentMetricsScheduler {

    private final PaymentMetricsService paymentMetricsService;
    private final SimplePaymentRepository paymentRepository;

    @Scheduled(fixedRate = 60000) // Every minute
    public void updatePaymentMetrics() {
        try {
            // Update pending payments gauge
            long pendingCount = paymentRepository.findActivePendingPayments(LocalDateTime.now()).size();
            paymentMetricsService.updatePendingPaymentsGauge(pendingCount);
            
            // Update total revenue gauge (completed payments)
            BigDecimal totalRevenue = paymentRepository.sumAmountByTenantIdAndStatus(0L, PaymentStatus.COMPLETED);
            if (totalRevenue == null) {
                totalRevenue = BigDecimal.ZERO;
            }
            paymentMetricsService.updateTotalRevenueGauge(totalRevenue);
            
            log.debug("Payment metrics updated: pending={}, revenue={}", pendingCount, totalRevenue);
            
        } catch (Exception e) {
            log.error("Failed to update payment metrics", e);
        }
    }
}
