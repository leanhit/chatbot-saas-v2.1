package com.chatbot.core.license.exception;

import com.chatbot.shared.exceptions.ErrorCode;

public class LicenseNotFoundException extends LicenseException {
    public LicenseNotFoundException(String message) {
        super(ErrorCode.LICENSE_NOT_FOUND, message);
    }

    public LicenseNotFoundException(String message, Throwable cause) {
        super(ErrorCode.LICENSE_NOT_FOUND, message, cause);
    }
}
