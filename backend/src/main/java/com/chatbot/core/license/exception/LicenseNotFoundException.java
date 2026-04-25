package com.chatbot.core.license.exception;

public class LicenseNotFoundException extends LicenseException {
    public LicenseNotFoundException(String message) {
        super(message, "LICENSE_NOT_FOUND");
    }

    public LicenseNotFoundException(String message, Throwable cause) {
        super(message, "LICENSE_NOT_FOUND", cause);
    }
}
