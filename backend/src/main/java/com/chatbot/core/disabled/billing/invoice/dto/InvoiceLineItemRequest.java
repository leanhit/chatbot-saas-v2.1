package com.chatbot.core.billing.invoice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "Invoice line item request")
public class InvoiceLineItemRequest {
    
    @NotBlank(message = "Description is required")
    @Schema(description = "Item description", example = "Monthly subscription")
    private String description;
    
    @NotNull(message = "Quantity is required")
    @Schema(description = "Item quantity", example = "1")
    private Integer quantity;
    
    @NotNull(message = "Unit price is required")
    @Schema(description = "Unit price", example = "100.00")
    private BigDecimal unitPrice;
    
    @NotNull(message = "Total price is required")
    @Schema(description = "Total price", example = "100.00")
    private BigDecimal totalPrice;
    
    @Schema(description = "Item type", example = "SUBSCRIPTION", allowableValues = {"SUBSCRIPTION", "USAGE", "ONE_TIME", "OVERAGE"})
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
