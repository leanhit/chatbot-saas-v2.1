package com.chatbot.core.payment.invoice.dto;

import com.chatbot.core.payment.invoice.model.Invoice;
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
public class InvoiceResponse {

    private Long id;
    private String invoiceNumber;
    private Long paymentId;
    private Long userId;
    private Long tenantId;
    private String userEmail;
    private String userName;
    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private String currency;
    private String discountCode;
    private String packageId;
    private String packageName;
    private String status;
    private String notes;
    private String paymentMethod;
    private String paymentReference;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime dueDate;
    private LocalDateTime paidAt;

    public static InvoiceResponse from(Invoice inv) {
        if (inv == null) return null;
        return InvoiceResponse.builder()
                .id(inv.getId())
                .invoiceNumber(inv.getInvoiceNumber())
                .paymentId(inv.getPaymentId())
                .userId(inv.getUserId())
                .tenantId(inv.getTenantId())
                .userEmail(inv.getUserEmail())
                .userName(inv.getUserName())
                .subtotal(inv.getSubtotal())
                .discountAmount(inv.getDiscountAmount())
                .taxAmount(inv.getTaxAmount())
                .totalAmount(inv.getTotalAmount())
                .currency(inv.getCurrency())
                .discountCode(inv.getDiscountCode())
                .packageId(inv.getPackageId())
                .packageName(inv.getPackageName())
                .status(inv.getStatus() != null ? inv.getStatus().name() : null)
                .notes(inv.getNotes())
                .paymentMethod(inv.getPaymentMethod())
                .paymentReference(inv.getPaymentReference())
                .createdAt(inv.getCreatedAt())
                .updatedAt(inv.getUpdatedAt())
                .dueDate(inv.getDueDate())
                .paidAt(inv.getPaidAt())
                .build();
    }
}
