package com.chatbot.core.simplepayment.exception;

public class PaymentExpiredException extends PaymentException {
    
    public PaymentExpiredException(String referenceCode) {
        super("PAYMENT_EXPIRED", "Payment has expired: " + referenceCode);
    }
}
