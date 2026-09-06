package com.chatbot.core.payment.common.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Event fired when a merchant payment is completed
 * This event triggers webhook dispatch to merchant endpoints
 */
@Getter
public class MerchantPaymentCompletedEvent extends ApplicationEvent {

    private final String paymentCode;
    private final String merchantOrderId;
    private final Long merchantId;
    private final BigDecimal amount;
    private final String currency;
    private final String bankTransactionId;
    private final LocalDateTime completedAt;
    private final String merchantWebhookUrl;

    public MerchantPaymentCompletedEvent(Object source, String paymentCode, String merchantOrderId,
                                          Long merchantId, BigDecimal amount, String currency,
                                          String bankTransactionId, LocalDateTime completedAt,
                                          String merchantWebhookUrl) {
        super(source);
        this.paymentCode = paymentCode;
        this.merchantOrderId = merchantOrderId;
        this.merchantId = merchantId;
        this.amount = amount;
        this.currency = currency;
        this.bankTransactionId = bankTransactionId;
        this.completedAt = completedAt;
        this.merchantWebhookUrl = merchantWebhookUrl;
    }
}
