package com.chatbot.core.simplepayment.exception;

import com.chatbot.shared.exceptions.ErrorCode;

public class PaymentAlreadyRefundedException extends PaymentException {
    
    public PaymentAlreadyRefundedException(String referenceCode) {
        super(ErrorCode.PAYMENT_ERROR, "Payment has already been refunded: " + referenceCode);
    }
}
