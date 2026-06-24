package com.chatbot.core.simplepayment.validation;

import com.chatbot.core.simplepayment.dto.DepositRequest;
import com.chatbot.core.simplepayment.exception.InvalidPaymentAmountException;
import com.chatbot.core.simplepayment.exception.PaymentException;
import com.chatbot.core.simplepayment.repository.SimplePaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentValidationService {

    private final SimplePaymentRepository paymentRepository;

    public void validateDepositRequest(DepositRequest request) {
        // Validate amount
        validateAmount(request.getAmount());
        
        // Validate currency
        validateCurrency(request.getCurrency());
        
        // Validate description length
        if (request.getDescription() != null && request.getDescription().length() > 500) {
            throw new PaymentException(com.chatbot.shared.exceptions.ErrorCode.VALIDATION_ERROR, "Description must not exceed 500 characters");
        }
        
        // Validate discount code format if provided
        if (request.getDiscountCode() != null && !request.getDiscountCode().trim().isEmpty()) {
            validateDiscountCode(request.getDiscountCode());
        }
    }

    public void validateAmount(BigDecimal amount) {
        if (amount == null) {
            throw new InvalidPaymentAmountException("Amount is required");
        }

        double amountValue = amount.doubleValue();
        
        if (amountValue <= 0) {
            throw new InvalidPaymentAmountException("Amount must be positive");
        }

        if (amountValue < 10000) {
            throw new InvalidPaymentAmountException("Minimum amount is 10,000 VND");
        }

        if (amountValue > 50000000) {
            throw new InvalidPaymentAmountException("Maximum amount is 50,000,000 VND");
        }

        if (amount.scale() > 2) {
            throw new InvalidPaymentAmountException("Amount must have at most 2 decimal places");
        }
    }

    public void validateCurrency(String currency) {
        if (currency == null || currency.trim().isEmpty()) {
            throw new PaymentException(com.chatbot.shared.exceptions.ErrorCode.VALIDATION_ERROR, "Currency is required");
        }

        String normalizedCurrency = currency.toUpperCase().trim();
        
        if (!normalizedCurrency.equals("VND") && 
            !normalizedCurrency.equals("USD") && 
            !normalizedCurrency.equals("EUR")) {
            throw new PaymentException(com.chatbot.shared.exceptions.ErrorCode.VALIDATION_ERROR, 
                "Currency must be one of: VND, USD, EUR");
        }
    }

    public void validateDiscountCode(String discountCode) {
        if (discountCode == null || discountCode.trim().isEmpty()) {
            return;
        }

        String normalizedCode = discountCode.trim().toUpperCase();
        
        // Discount code should be alphanumeric, 6-20 characters
        if (!normalizedCode.matches("^[A-Z0-9]{6,20}$")) {
            throw new PaymentException(com.chatbot.shared.exceptions.ErrorCode.VALIDATION_ERROR, 
                "Discount code must be 6-20 alphanumeric characters");
        }
    }

    public void validateReferenceCode(String referenceCode) {
        if (referenceCode == null || referenceCode.trim().isEmpty()) {
            throw new PaymentException(com.chatbot.shared.exceptions.ErrorCode.VALIDATION_ERROR, "Reference code is required");
        }

        String normalizedCode = referenceCode.trim().toUpperCase();
        
        if (!normalizedCode.matches("^PAY[A-Z0-9]{12}$")) {
            throw new PaymentException(com.chatbot.shared.exceptions.ErrorCode.VALIDATION_ERROR, 
                "Invalid reference code format");
        }
    }

    public void validatePaymentExists(String referenceCode) {
        if (!paymentRepository.existsByReferenceCode(referenceCode)) {
            throw new PaymentException(com.chatbot.shared.exceptions.ErrorCode.PAYMENT_NOT_FOUND, 
                "Payment not found: " + referenceCode);
        }
    }
}
