package com.chatbot.core.simplepayment.exception;

import com.chatbot.shared.exceptions.ErrorCode;

public class PaymentNotFoundException extends PaymentException {
    
    public PaymentNotFoundException(String referenceCode) {
        super(ErrorCode.PAYMENT_NOT_FOUND, "Payment not found: " + referenceCode);
    }
}
