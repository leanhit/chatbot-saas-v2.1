package com.chatbot.core.simplepayment.exception;

import com.chatbot.shared.exceptions.BaseException;
import com.chatbot.shared.exceptions.ErrorCode;

/**
 * Exception for discount code already exists errors.
 */
public class DiscountCodeExistsException extends BaseException {
    
    public DiscountCodeExistsException(String code) {
        super(ErrorCode.DISCOUNT_CODE_EXISTS, "Discount code already exists: " + code);
    }
}
