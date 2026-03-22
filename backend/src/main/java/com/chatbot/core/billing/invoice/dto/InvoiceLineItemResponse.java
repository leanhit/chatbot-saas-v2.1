package com.chatbot.core.billing.invoice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Builder;
import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "Invoice line item response")
public class InvoiceLineItemResponse {
    
    @Schema(description = "Line item ID", example = "123")
    private Long id;
    
    @Schema(description = "Invoice ID", example = "456")
    private Long invoiceId;
    
    @Schema(description = "Item description", example = "Monthly subscription")
    private String description;
    
    @Schema(description = "Item quantity", example = "1")
    private Integer quantity;
    
    @Schema(description = "Unit price", example = "100.00")
    private BigDecimal unitPrice;
    
    @Schema(description = "Total price", example = "100.00")
    private BigDecimal totalPrice;
    
    @Schema(description = "Item type", example = "SUBSCRIPTION")
    private String itemType;
    
    @Schema(description = "Service or product code", example = "PRO_PLAN_MONTHLY")
    private String serviceCode;
    
    @Schema(description = "Tax amount for this item", example = "10.00")
    private BigDecimal tax;
    
    @Schema(description = "Discount amount for this item", example = "5.00")
    private BigDecimal discount;
    
    @Schema(description = "Additional metadata for the item")
    private java.util.Map<String, Object> metadata;
}
