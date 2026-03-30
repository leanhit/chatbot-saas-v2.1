package com.chatbot.core.billing.account.dto;

import com.chatbot.core.billing.model.BillingType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BillingAccountRequest {

    @NotBlank(message = "Account name is required")
    private String accountName;

    private String companyName;

    private String taxCode;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    private String phone;

    private String billingAddress;

    @NotBlank(message = "Currency is required")
    private String currency = "USD";

    private BigDecimal creditLimit;

    private Boolean autoPayment = false;

    private String paymentMethodToken;

    @NotNull(message = "Billing type is required")
    private BillingType billingType;
}
