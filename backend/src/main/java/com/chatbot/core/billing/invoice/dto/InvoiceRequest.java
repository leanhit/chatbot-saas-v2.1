package com.chatbot.core.billing.invoice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Schema(description = "Invoice request")
public class InvoiceRequest {
    
    @NotNull(message = "Tenant ID is required")
    @Schema(description = "Tenant ID", example = "123")
    private Long tenantId;
    
    @NotBlank(message = "Invoice number is required")
    @Schema(description = "Invoice number", example = "INV-2024-001")
    private String invoiceNumber;
    
    @NotNull(message = "Customer ID is required")
    @Schema(description = "Customer ID", example = "456")
    private Long customerId;
    
    @NotNull(message = "Billing account ID is required")
    @Schema(description = "Billing account ID", example = "789")
    private Long billingAccountId;
    
    @NotNull(message = "Issue date is required")
    @Schema(description = "Invoice issue date", example = "2024-01-01")
    private LocalDate issueDate;
    
    @NotNull(message = "Due date is required")
    @Schema(description = "Invoice due date", example = "2024-01-15")
    private LocalDate dueDate;
    
    @NotNull(message = "Period start date is required")
    @Schema(description = "Billing period start", example = "2024-01-01")
    private LocalDate periodStart;
    
    @NotNull(message = "Period end date is required")
    @Schema(description = "Billing period end", example = "2024-01-31")
    private LocalDate periodEnd;
    
    @NotNull(message = "Currency is required")
    @Schema(description = "Currency code", example = "USD")
    private String currency;
    
    @NotNull(message = "Subtotal is required")
    @Schema(description = "Invoice subtotal", example = "100.00")
    private BigDecimal subtotal;
    
    @Schema(description = "Tax amount", example = "10.00")
    private BigDecimal tax;
    
    @Schema(description = "Discount amount", example = "5.00")
    private BigDecimal discount;
    
    @NotNull(message = "Total amount is required")
    @Schema(description = "Invoice total", example = "105.00")
    private BigDecimal total;
    
    @Schema(description = "Invoice status", example = "PENDING", allowableValues = {"PENDING", "PAID", "OVERDUE", "CANCELLED"})
    private String status;
    
    @Schema(description = "Payment method ID", example = "123")
    private Long paymentMethodId;
    
    @Schema(description = "Invoice line items")
    private List<InvoiceLineItemRequest> lineItems;
    
    @Schema(description = "Additional metadata")
    private java.util.Map<String, Object> metadata;
}
