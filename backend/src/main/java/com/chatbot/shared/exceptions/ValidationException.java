package com.chatbot.shared.exceptions;

import java.util.List;

/**
 * Exception thrown when validation fails.
 */
public class ValidationException extends BaseException {
    
    private final List<String> errors;
    
    public ValidationException(String message) {
        super(ErrorCode.VALIDATION_ERROR, message);
        this.errors = List.of(message);
    }
    
    public ValidationException(String message, Throwable cause) {
        super(ErrorCode.VALIDATION_ERROR, message, cause);
        this.errors = List.of(message);
    }
    
    public ValidationException(List<String> errors) {
        super(ErrorCode.VALIDATION_ERROR, "Validation failed: " + String.join(", ", errors));
        this.errors = errors;
    }
    
    public ValidationException(String message, List<String> errors) {
        super(ErrorCode.VALIDATION_ERROR, message);
        this.errors = errors;
    }
    
    public List<String> getErrors() {
        return errors;
    }
}
