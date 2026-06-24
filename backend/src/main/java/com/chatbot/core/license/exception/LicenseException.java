package com.chatbot.core.license.exception;

import com.chatbot.shared.exceptions.BaseException;
import com.chatbot.shared.exceptions.ErrorCode;

/**
 * Base exception for license-related errors.
 * All license exceptions should extend this class.
 */
public class LicenseException extends BaseException {
    
    public LicenseException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
    
    public LicenseException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
