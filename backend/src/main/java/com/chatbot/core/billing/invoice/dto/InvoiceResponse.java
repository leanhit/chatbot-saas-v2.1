package com.chatbot.core.billing.invoice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Builder;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@Schema(description = "Invoice response")
public class InvoiceResponse {
    
    @Schema(description = "Invoice ID", example = "123")
    private Long id;
    
    @Schema(description = "Tenant ID", example = "456")
    private Long tenantId;
    
    @Schema(description = "Invoice number", example = "INV-2024-001")
    private String invoiceNumber;
    
    @Schema(description = "Customer ID", example = "789")
    private Long customerId;
    
    @Schema(description = "Billing account ID", example = "789")
    private Long billingAccountId;
    
    @Schema(description = "Invoice issue date", example = "2024-01-01T10:00:00")
    private LocalDateTime issueDate;
    
    @Schema(description = "Invoice due date", example = "2024-01-15T10:00:00")
    private LocalDateTime dueDate;
    
    @Schema(description = "Billing period start", example = "2024-01-01T00:00:00")
    private LocalDateTime periodStart;
    
    @Schema(description = "Billing period end", example = "2024-01-31T23:59:59")
    private LocalDateTime periodEnd;
    
    @Schema(description = "Currency code", example = "USD")
    private String currency;
    
    @Schema(description = "Invoice subtotal", example = "100.00")
    private java.math.BigDecimal subtotal;
    
    @Schema(description = "Tax amount", example = "10.00")
    private java.math.BigDecimal tax;
    
    @Schema(description = "Discount amount", example = "5.00")
    private java.math.BigDecimal discount;
    
    @Schema(description = "Invoice total", example = "105.00")
    private java.math.BigDecimal total;
    
    @Schema(description = "Paid amount", example = "105.00")
    private java.math.BigDecimal paidAmount;
    
    @Schema(description = "Outstanding amount", example = "0.00")
    private java.math.BigDecimal outstandingAmount;
    
    @Schema(description = "Invoice status", example = "PAID")
    private String status;
    
    @Schema(description = "Payment method ID", example = "123")
    private Long paymentMethodId;
    
    @Schema(description = "Payment date", example = "2024-01-10T10:00:00")
    private LocalDateTime paidAt;
    
    @Schema(description = "Invoice line items")
    private List<InvoiceLineItemResponse> lineItems;
    
    @Schema(description = "Additional metadata")
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
