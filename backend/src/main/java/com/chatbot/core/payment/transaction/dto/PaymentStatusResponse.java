package com.chatbot.core.payment.transaction.dto;

import com.chatbot.shared.utils.DateUtils;
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
public class PaymentStatusResponse {
    private String referenceCode;
    private String status;
    private BigDecimal amount;
    private String currency;
    private String description;
    private String bankTransactionId;
    private String targetPackageId; // Add targetPackageId
    
    // Original timestamps
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private LocalDateTime expiresAt;
    private LocalDateTime updatedAt;
    
    // Formatted timestamps for frontend
    private String createdAtFormatted;
    private String completedAtFormatted;
    private String expiresAtFormatted;
    private String updatedAtFormatted;
    
    // Helper method to set formatted dates
    public PaymentStatusResponse withFormattedDates() {
        this.createdAtFormatted = createdAt != null ? 
            DateUtils.formatLocalDateTime(createdAt, "dd/MM/yyyy HH:mm:ss") : null;
        this.completedAtFormatted = completedAt != null ? 
            DateUtils.formatLocalDateTime(completedAt, "dd/MM/yyyy HH:mm:ss") : null;
        this.expiresAtFormatted = expiresAt != null ? 
            DateUtils.formatLocalDateTime(expiresAt, "dd/MM/yyyy HH:mm:ss") : null;
        this.updatedAtFormatted = updatedAt != null ? 
            DateUtils.formatLocalDateTime(updatedAt, "dd/MM/yyyy HH:mm:ss") : null;
        return this;
    }
}
