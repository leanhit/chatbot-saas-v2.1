package com.chatbot.core.license.exception;

import com.chatbot.shared.exceptions.ErrorCode;

public class LicenseExpiredException extends LicenseException {
    public LicenseExpiredException(String message) {
        super(ErrorCode.LICENSE_EXPIRED, message);
    }

    public LicenseExpiredException(String message, Throwable cause) {
        super(ErrorCode.LICENSE_EXPIRED, message, cause);
    }
}
