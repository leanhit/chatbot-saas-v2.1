package com.chatbot.core.simplepayment.exception;

import com.chatbot.shared.exceptions.ErrorCode;

public class InvalidPaymentAmountException extends PaymentException {
    
    public InvalidPaymentAmountException(String message) {
        super(ErrorCode.INVALID_PAYMENT_AMOUNT, message);
    }
}
