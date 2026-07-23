package com.chatbot.core.simplepayment.exception;

import com.chatbot.shared.exceptions.ErrorCode;

public class InvoiceNotFoundException extends PaymentException {
    
    public InvoiceNotFoundException(Long invoiceId) {
        super(ErrorCode.PAYMENT_NOT_FOUND, "Invoice not found: " + invoiceId);
    }
}
