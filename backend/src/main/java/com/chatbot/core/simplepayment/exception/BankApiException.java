package com.chatbot.core.simplepayment.exception;

import com.chatbot.shared.exceptions.ErrorCode;

public class BankApiException extends PaymentException {
    
    public BankApiException(String message) {
        super(ErrorCode.BANK_API_ERROR, message);
    }
    
    public BankApiException(String message, Throwable cause) {
        super(ErrorCode.BANK_API_ERROR, message, cause);
    }
}
