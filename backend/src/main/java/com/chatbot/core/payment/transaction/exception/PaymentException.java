package com.chatbot.core.payment.transaction.exception;

import com.chatbot.shared.exceptions.BaseException;
import com.chatbot.shared.exceptions.ErrorCode;

/**
 * Base exception for payment-related errors.
 * All payment exceptions should extend this class.
 */
public class PaymentException extends BaseException {
    
    public PaymentException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
    
    public PaymentException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
