package com.chatbot.core.billing.account.dto;

import com.chatbot.core.billing.model.BillingType;
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
public class BillingAccountResponse {

    private Long id;
    private String accountNumber;
    private BillingType billingType;
    private String accountName;
    private String companyName;
    private String taxCode;
    private String email;
    private String phone;
    private String billingAddress;
    private String currency;
    private BigDecimal creditLimit;
    private BigDecimal currentBalance;
    private BigDecimal availableCredit;
    private Boolean autoPayment;
    private Boolean isActive;
    private Boolean isSuspended;
    private LocalDateTime suspendedAt;
    private String suspensionReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long tenantId;
}
