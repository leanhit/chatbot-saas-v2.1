package com.chatbot.core.payment.transaction.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentEvent {
    private String referenceCode;
    private Long userId;
    private Long tenantId;
    private String amount;
    private String currency;
    private String description;
    private String status;
    private String bankTransactionId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime completedAt;
    private LocalDateTime expiresAt;
    private String type; // PAYMENT_CREATED, PAYMENT_COMPLETED, PAYMENT_EXPIRED, PAYMENT_UPDATED
}
