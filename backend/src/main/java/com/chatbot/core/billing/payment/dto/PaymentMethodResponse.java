package com.chatbot.core.billing.payment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Builder;
import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "Payment method response")
public class PaymentMethodResponse {
    
    @Schema(description = "Payment method ID", example = "123")
    private Long id;
    
    @Schema(description = "Tenant ID", example = "456")
    private Long tenantId;
    
    @Schema(description = "Payment type", example = "CREDIT_CARD")
    private String paymentType;
    
    @Schema(description = "Payment provider", example = "STRIPE")
    private String provider;
    
    @Schema(description = "Display name for the payment method", example = "Visa ending in 4242")
    private String methodName;
    
    @Schema(description = "External payment method ID from provider", example = "pm_1234567890")
    private String externalId;
    
    @Schema(description = "Card last 4 digits", example = "4242")
    private String last4;
    
    @Schema(description = "Card expiry month", example = "12")
    private String expiryMonth;
    
    @Schema(description = "Card expiry year", example = "2025")
    private String expiryYear;
    
    @Schema(description = "Card brand", example = "VISA")
    private String brand;
    
    @Schema(description = "Bank account number (masked)", example = "****7890")
    private String bankAccountNumber;
    
    @Schema(description = "Bank routing number", example = "021000021")
    private String routingNumber;
    
    @Schema(description = "Bank name", example = "Chase Bank")
    private String bankName;
    
    @Schema(description = "Whether this is the default payment method", example = "false")
    private Boolean isDefault;
    
    @Schema(description = "Whether the payment method is active", example = "true")
    private Boolean isActive;
    
    @Schema(description = "Payment method status", example = "ACTIVE")
    private String status;
    
    @Schema(description = "Billing address ID", example = "123")
    private Long billingAddressId;
    
    @Schema(description = "Additional metadata for the payment method")
    private java.util.Map<String, Object> metadata;
    
    @Schema(description = "Creation timestamp", example = "2024-01-01T10:00:00")
    private LocalDateTime createdAt;
    
    @Schema(description = "Last update timestamp", example = "2024-01-01T10:00:00")
    private LocalDateTime updatedAt;
    
    @Schema(description = "Created by user ID", example = "789")
    private Long createdBy;
    
    @Schema(description = "Last updated by user ID", example = "789")
    private Long updatedBy;
}
