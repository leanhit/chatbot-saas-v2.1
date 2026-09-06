package com.chatbot.core.payment.transaction.exception;

import com.chatbot.shared.exceptions.ErrorCode;

public class PaymentExpiredException extends PaymentException {
    
    public PaymentExpiredException(String referenceCode) {
        super(ErrorCode.PAYMENT_EXPIRED, "Payment has expired: " + referenceCode);
    }
}
