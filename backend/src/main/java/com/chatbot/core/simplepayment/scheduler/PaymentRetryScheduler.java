package com.chatbot.core.simplepayment.scheduler;

import com.chatbot.core.simplepayment.model.PaymentStatus;
import com.chatbot.core.simplepayment.repository.SimplePaymentRepository;
import com.chatbot.core.simplepayment.service.RetryablePaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentRetryScheduler {

    private final SimplePaymentRepository paymentRepository;
    private final RetryablePaymentService retryablePaymentService;

    /**
     * Retry failed payments every 5 minutes
     */
    @Scheduled(fixedRate = 300000) // Every 5 minutes
    @SchedulerLock(name = "PaymentRetryScheduler_retryFailedPayments", lockAtMostFor = "6m", lockAtLeastFor = "4m")
    public void retryFailedPayments() {
        log.info("🔄 Starting retry of failed payments");
        
        try {
            // Get payments that failed in the last hour
            LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
            List<com.chatbot.core.simplepayment.model.SimplePayment> failedPayments = 
                paymentRepository.findByStatusAndCreatedAtAfter(PaymentStatus.FAILED, oneHourAgo);
            
            log.info("Found {} failed payments to retry", failedPayments.size());
            
            for (com.chatbot.core.simplepayment.model.SimplePayment payment : failedPayments) {
                try {
                    log.info("Retrying payment: {}", payment.getReferenceCode());
                    
                    // Check bank transaction again
                    String transactionId = retryablePaymentService.checkBankTransactionWithRetry(
                        payment.getReferenceCode()
                    );
                    
                    if (transactionId != null) {
                        // Transaction found, complete the payment
                        retryablePaymentService.completePaymentWithRetry(
                            payment.getReferenceCode(),
                            transactionId
                        );
                    } else {
                        log.debug("No transaction found for retry: {}", payment.getReferenceCode());
                    }
                    
                } catch (Exception e) {
                    log.error("Failed to retry payment {}: {}", payment.getReferenceCode(), e.getMessage());
                }
            }
            
            log.info("✅ Completed retry of failed payments");
            
        } catch (Exception e) {
            log.error("❌ Error during payment retry scheduler", e);
        }
    }

    /**
     * Check stuck pending payments every 10 minutes
     */
    @Scheduled(fixedRate = 600000) // Every 10 minutes
    @SchedulerLock(name = "PaymentRetryScheduler_checkStuckPendingPayments", lockAtMostFor = "11m", lockAtLeastFor = "9m")
    public void checkStuckPendingPayments() {
        log.info("🔍 Checking for stuck pending payments");
        
        try {
            // Get pending payments older than 2 hours
            LocalDateTime twoHoursAgo = LocalDateTime.now().minusHours(2);
            List<com.chatbot.core.simplepayment.model.SimplePayment> stuckPayments = 
                paymentRepository.findPendingPaymentsSince(PaymentStatus.PENDING, twoHoursAgo);
            
            log.info("Found {} potentially stuck pending payments", stuckPayments.size());
            
            for (com.chatbot.core.simplepayment.model.SimplePayment payment : stuckPayments) {
                try {
                    log.info("Checking stuck payment: {}", payment.getReferenceCode());
                    
                    // Check bank transaction with retry
                    String transactionId = retryablePaymentService.checkBankTransactionWithRetry(
                        payment.getReferenceCode()
                    );
                    
                    if (transactionId != null) {
                        retryablePaymentService.completePaymentWithRetry(
                            payment.getReferenceCode(),
                            transactionId
                        );
                    } else {
                        log.debug("Payment still pending, no transaction found: {}", payment.getReferenceCode());
                    }
                    
                } catch (Exception e) {
                    log.error("Failed to check stuck payment {}: {}", payment.getReferenceCode(), e.getMessage());
                }
            }
            
            log.info("✅ Completed check for stuck pending payments");
            
        } catch (Exception e) {
            log.error("❌ Error during stuck payment check scheduler", e);
        }
    }
}
