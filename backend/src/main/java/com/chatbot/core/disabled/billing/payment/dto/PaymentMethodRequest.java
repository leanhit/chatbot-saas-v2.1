package com.chatbot.core.billing.payment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Payment method request")
public class PaymentMethodRequest {
    
    @NotBlank(message = "Payment type is required")
    @Schema(description = "Payment type", example = "CREDIT_CARD", allowableValues = {"CREDIT_CARD", "BANK_TRANSFER", "PAYPAL", "STRIPE"})
    private String paymentType;
    
    @NotBlank(message = "Provider is required")
    @Schema(description = "Payment provider", example = "STRIPE")
    private String provider;
    
    @NotBlank(message = "Method name is required")
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
    
    @Schema(description = "Bank account number (for bank transfers)", example = "1234567890")
    private String bankAccountNumber;
    
    @Schema(description = "Bank routing number", example = "021000021")
    private String routingNumber;
    
    @Schema(description = "Bank name", example = "Chase Bank")
    private String bankName;
    
    @NotNull(message = "Is default flag is required")
    @Schema(description = "Whether this is the default payment method", example = "false")
    private Boolean isDefault;
    
    @Schema(description = "Billing address ID", example = "123")
    private Long billingAddressId;
    
    @Schema(description = "Additional metadata for the payment method")
    private java.util.Map<String, Object> metadata;
}
