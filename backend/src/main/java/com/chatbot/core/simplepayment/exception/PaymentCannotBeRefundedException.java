package com.chatbot.core.simplepayment.exception;

import com.chatbot.shared.exceptions.ErrorCode;

public class PaymentCannotBeRefundedException extends PaymentException {
    
    public PaymentCannotBeRefundedException(String reason) {
        super(ErrorCode.PAYMENT_ERROR, "Payment cannot be refunded: " + reason);
    }
}
