package com.chatbot.core.simplepayment.exception;

public class InvalidPaymentAmountException extends PaymentException {
    
    public InvalidPaymentAmountException(String message) {
        super("INVALID_AMOUNT", message);
    }
}
