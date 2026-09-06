package com.chatbot.core.payment.transaction.exception;

import com.chatbot.shared.exceptions.ErrorCode;

public class InvalidPaymentAmountException extends PaymentException {
    
    public InvalidPaymentAmountException(String message) {
        super(ErrorCode.INVALID_PAYMENT_AMOUNT, message);
    }
}
