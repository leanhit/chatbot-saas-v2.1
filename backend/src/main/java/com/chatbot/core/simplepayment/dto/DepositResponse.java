package com.chatbot.core.simplepayment.dto;

import com.chatbot.core.simplepayment.model.SimplePayment;
import com.chatbot.shared.utils.DateUtils;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class DepositResponse {
    
    private Long id;
    private String referenceCode;
    private BigDecimal amount;
    private String currency;
    private String status;
    private String qrContent;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
    private String bankAccountNumber;
    private String bankAccountName;
    private String bankName;
    
    // Formatted timestamps for frontend
    private String expiresAtFormatted;
    private String createdAtFormatted;
    
    // Helper method to set formatted dates
    public DepositResponse withFormattedDates() {
        this.expiresAtFormatted = expiresAt != null ? 
            DateUtils.formatLocalDateTime(expiresAt, "dd/MM/yyyy HH:mm:ss") : null;
        this.createdAtFormatted = createdAt != null ? 
            DateUtils.formatLocalDateTime(createdAt, "dd/MM/yyyy HH:mm:ss") : null;
        return this;
    }
    
    public static DepositResponse from(SimplePayment payment, String qrContent) {
        DepositResponse response = new DepositResponse();
        response.setId(payment.getId());
        response.setReferenceCode(payment.getReferenceCode());
        response.setAmount(payment.getAmount());
        response.setCurrency(payment.getCurrency());
        response.setStatus(payment.getStatus().name());
        response.setQrContent(qrContent);
        response.setExpiresAt(payment.getExpiresAt());
        response.setCreatedAt(payment.getCreatedAt());
        
        // Default bank info - should be configurable
        response.setBankAccountNumber("1234567890");
        response.setBankAccountName("CHATBOT SaaS");
        response.setBankName("Vietcombank");
        
        return response;
    }
}
