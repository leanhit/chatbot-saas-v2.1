package com.chatbot.core.license.exception;

public class LicenseException extends RuntimeException {
    private final String errorCode;

    public LicenseException(String message) {
        super(message);
        this.errorCode = "LICENSE_ERROR";
    }

    public LicenseException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "LICENSE_ERROR";
    }

    public LicenseException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public LicenseException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
