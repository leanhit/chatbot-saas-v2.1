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
public class MerchantPaymentResponse {
    
    private String sessionId;
    private String merchantOrderId;
    private BigDecimal amount;
    private String currency;
    private String description;
    private String status;
    private String checkoutUrl; // URL for embeddable widget
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
    
    public static MerchantPaymentResponse from(MerchantPaymentSession session, String checkoutUrl) {
        return MerchantPaymentResponse.builder()
                .sessionId(session.getSessionId())
                .merchantOrderId(session.getMerchantOrderId())
                .amount(session.getAmount())
                .currency(session.getCurrency())
                .description(session.getDescription())
                .status(session.getStatus().name())
                .checkoutUrl(checkoutUrl)
                .expiresAt(session.getExpiresAt())
                .createdAt(session.getCreatedAt())
                .build();
    }
}
