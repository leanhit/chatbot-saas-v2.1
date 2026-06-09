package com.chatbot.core.simplepayment.exception;

public class BankApiException extends PaymentException {
    
    public BankApiException(String message) {
        super("BANK_API_ERROR", message);
    }
    
    public BankApiException(String message, Throwable cause) {
        super("BANK_API_ERROR", message, cause);
    }
}
