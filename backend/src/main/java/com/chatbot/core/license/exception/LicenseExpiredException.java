package com.chatbot.core.license.exception;

public class LicenseExpiredException extends LicenseException {
    public LicenseExpiredException(String message) {
        super(message, "LICENSE_EXPIRED");
    }

    public LicenseExpiredException(String message, Throwable cause) {
        super(message, "LICENSE_EXPIRED", cause);
    }
}
