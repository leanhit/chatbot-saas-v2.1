package com.chatbot.core.simplepayment.exception;

public class PaymentNotFoundException extends PaymentException {
    
    public PaymentNotFoundException(String referenceCode) {
        super("PAYMENT_NOT_FOUND", "Payment not found: " + referenceCode);
    }
}
