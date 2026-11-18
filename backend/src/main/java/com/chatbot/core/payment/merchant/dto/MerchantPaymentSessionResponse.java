package com.chatbot.core.payment.merchant.dto;

import com.chatbot.core.payment.merchant.model.MerchantPaymentSession;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MerchantPaymentSessionResponse {

    private Long id;
    private String sessionId;
    private Long merchantId;
    private Long tenantId;
    private String merchantOrderId;
    private BigDecimal amount;
    private String currency;
    private String description;
    private String returnUrl;
    private String cancelUrl;
    private String metadata;
    private String status;
    private String paymentReferenceCode;
    private String bankTransactionId;
    private LocalDateTime expiresAt;
    private LocalDateTime completedAt;
    private LocalDateTime cancelledAt;
    private LocalDateTime failedAt;
    private String failureReason;
    private LocalDateTime webhookSentAt;
    private String webhookStatus;
    private Integer webhookRetryCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static MerchantPaymentSessionResponse from(MerchantPaymentSession s) {
        if (s == null) return null;
        return MerchantPaymentSessionResponse.builder()
                .id(s.getId())
                .sessionId(s.getSessionId())
                .merchantId(s.getMerchantId())
                .tenantId(s.getTenantId())
                .merchantOrderId(s.getMerchantOrderId())
                .amount(s.getAmount())
                .currency(s.getCurrency())
                .description(s.getDescription())
                .returnUrl(s.getReturnUrl())
                .cancelUrl(s.getCancelUrl())
                .metadata(s.getMetadata())
                .status(s.getStatus() != null ? s.getStatus().name() : null)
                .paymentReferenceCode(s.getPaymentReferenceCode())
                .bankTransactionId(s.getBankTransactionId())
                .expiresAt(s.getExpiresAt())
                .completedAt(s.getCompletedAt())
                .cancelledAt(s.getCancelledAt())
                .failedAt(s.getFailedAt())
                .failureReason(s.getFailureReason())
                .webhookSentAt(s.getWebhookSentAt())
                .webhookStatus(s.getWebhookStatus())
                .webhookRetryCount(s.getWebhookRetryCount())
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt())
                .build();
    }
}
