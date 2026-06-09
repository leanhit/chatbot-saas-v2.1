package com.chatbot.core.simplepayment.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicLong;
import jakarta.annotation.PostConstruct;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentMetricsService {

    private final MeterRegistry meterRegistry;
    
    // Counters
    private Counter paymentCreatedCounter;
    private Counter paymentCompletedCounter;
    private Counter paymentFailedCounter;
    private Counter paymentExpiredCounter;
    private Counter paymentCancelledCounter;
    private Counter paymentRefundedCounter;
    private Counter webhookSentCounter;
    private Counter webhookFailedCounter;
    
    // Timers
    private Timer paymentProcessingTimer;
    private Timer bankApiCallTimer;
    private Timer qrCodeGenerationTimer;
    
    // Gauges
    private AtomicLong pendingPaymentsGauge;
    private AtomicLong totalRevenueGauge;
    
    @PostConstruct
    public void init() {
        // Initialize counters
        paymentCreatedCounter = Counter.builder("payment.created.total")
                .description("Total number of payments created")
                .tag("type", "simplepayment")
                .register(meterRegistry);
                
        paymentCompletedCounter = Counter.builder("payment.completed.total")
                .description("Total number of payments completed")
                .tag("type", "simplepayment")
                .register(meterRegistry);
                
        paymentFailedCounter = Counter.builder("payment.failed.total")
                .description("Total number of payments failed")
                .tag("type", "simplepayment")
                .register(meterRegistry);
                
        paymentExpiredCounter = Counter.builder("payment.expired.total")
                .description("Total number of payments expired")
                .tag("type", "simplepayment")
                .register(meterRegistry);
                
        paymentCancelledCounter = Counter.builder("payment.cancelled.total")
                .description("Total number of payments cancelled")
                .tag("type", "simplepayment")
                .register(meterRegistry);
                
        paymentRefundedCounter = Counter.builder("payment.refunded.total")
                .description("Total number of payments refunded")
                .tag("type", "simplepayment")
                .register(meterRegistry);
                
        webhookSentCounter = Counter.builder("webhook.sent.total")
                .description("Total number of webhooks sent")
                .tag("type", "simplepayment")
                .register(meterRegistry);
                
        webhookFailedCounter = Counter.builder("webhook.failed.total")
                .description("Total number of webhooks failed")
                .tag("type", "simplepayment")
                .register(meterRegistry);
        
        // Initialize timers
        paymentProcessingTimer = Timer.builder("payment.processing.duration")
                .description("Time taken to process payments")
                .tag("type", "simplepayment")
                .register(meterRegistry);
                
        bankApiCallTimer = Timer.builder("bank.api.call.duration")
                .description("Time taken for bank API calls")
                .tag("type", "simplepayment")
                .register(meterRegistry);
                
        qrCodeGenerationTimer = Timer.builder("qr.code.generation.duration")
                .description("Time taken to generate QR codes")
                .tag("type", "simplepayment")
                .register(meterRegistry);
        
        // Initialize gauges
        pendingPaymentsGauge = new AtomicLong(0);
        Gauge.builder("payment.pending.count", pendingPaymentsGauge, AtomicLong::get)
                .description("Current number of pending payments")
                .tag("type", "simplepayment")
                .register(meterRegistry);
                
        totalRevenueGauge = new AtomicLong(0);
        Gauge.builder("payment.revenue.total", totalRevenueGauge, AtomicLong::get)
                .description("Total revenue from completed payments")
                .tag("type", "simplepayment")
                .register(meterRegistry);
                
        log.info("Payment metrics service initialized");
    }
    
    public void incrementPaymentCreated() {
        paymentCreatedCounter.increment();
    }
    
    public void incrementPaymentCompleted() {
        paymentCompletedCounter.increment();
    }
    
    public void incrementPaymentFailed() {
        paymentFailedCounter.increment();
    }
    
    public void incrementPaymentExpired() {
        paymentExpiredCounter.increment();
    }
    
    public void incrementPaymentCancelled() {
        paymentCancelledCounter.increment();
    }
    
    public void incrementPaymentRefunded() {
        paymentRefundedCounter.increment();
    }
    
    public void incrementWebhookSent() {
        webhookSentCounter.increment();
    }
    
    public void incrementWebhookFailed() {
        webhookFailedCounter.increment();
    }
    
    public Timer.Sample startPaymentProcessingTimer() {
        return Timer.start(meterRegistry);
    }
    
    public void stopPaymentProcessingTimer(Timer.Sample sample) {
        sample.stop(paymentProcessingTimer);
    }
    
    public Timer.Sample startBankApiCallTimer() {
        return Timer.start(meterRegistry);
    }
    
    public void stopBankApiCallTimer(Timer.Sample sample) {
        sample.stop(bankApiCallTimer);
    }
    
    public Timer.Sample startQrCodeGenerationTimer() {
        return Timer.start(meterRegistry);
    }
    
    public void stopQrCodeGenerationTimer(Timer.Sample sample) {
        sample.stop(qrCodeGenerationTimer);
    }
    
    public void updatePendingPaymentsGauge(long count) {
        pendingPaymentsGauge.set(count);
    }
    
    public void updateTotalRevenueGauge(BigDecimal revenue) {
        totalRevenueGauge.set(revenue.longValue());
    }
    
    public void recordPaymentAmount(BigDecimal amount) {
        meterRegistry.counter("payment.amount", "type", "simplepayment")
                .increment(amount.doubleValue());
    }
}
