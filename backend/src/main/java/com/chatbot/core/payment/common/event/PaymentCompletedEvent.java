package com.chatbot.core.payment.common.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Event fired when a payment is completed successfully
 * This event triggers downstream processes like package upgrades, notifications, webhooks
 */
@Getter
public class PaymentCompletedEvent extends ApplicationEvent {

    private final String referenceCode;
    private final Long userId;
    private final Long tenantId;
    private final BigDecimal amount;
    private final String currency;
    private final String bankTransactionId;
    private final String targetPackageId;
    private final LocalDateTime completedAt;
    private final String description;

    public PaymentCompletedEvent(Object source, String referenceCode, Long userId, Long tenantId,
                                   BigDecimal amount, String currency, String bankTransactionId,
                                   String targetPackageId, LocalDateTime completedAt, String description) {
        super(source);
        this.referenceCode = referenceCode;
        this.userId = userId;
        this.tenantId = tenantId;
        this.amount = amount;
        this.currency = currency;
        this.bankTransactionId = bankTransactionId;
        this.targetPackageId = targetPackageId;
        this.completedAt = completedAt;
        this.description = description;
    }
}
