package com.chatbot.core.simplepayment.exception;

import com.chatbot.shared.exceptions.ErrorCode;

public class PaymentCannotBeCancelledException extends PaymentException {
    
    public PaymentCannotBeCancelledException(String reason) {
        super(ErrorCode.PAYMENT_ERROR, "Payment cannot be cancelled: " + reason);
    }
}
