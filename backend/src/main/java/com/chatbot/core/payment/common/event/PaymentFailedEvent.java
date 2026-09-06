package com.chatbot.core.payment.common.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Event fired when a payment fails
 * This event triggers notification processes and cleanup
 */
@Getter
public class PaymentFailedEvent extends ApplicationEvent {

    private final String referenceCode;
    private final Long userId;
    private final Long tenantId;
    private final BigDecimal amount;
    private final String currency;
    private final String failureReason;
    private final LocalDateTime failedAt;
    private final String targetPackageId;

    public PaymentFailedEvent(Object source, String referenceCode, Long userId, Long tenantId,
                              BigDecimal amount, String currency, String failureReason,
                              LocalDateTime failedAt, String targetPackageId) {
        super(source);
        this.referenceCode = referenceCode;
        this.userId = userId;
        this.tenantId = tenantId;
        this.amount = amount;
        this.currency = currency;
        this.failureReason = failureReason;
        this.failedAt = failedAt;
        this.targetPackageId = targetPackageId;
    }
}
