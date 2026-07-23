package com.chatbot.shared.exceptions;

/**
 * Exception for file validation errors.
 * Used when uploaded files fail validation checks (type, size, null, empty).
 */
public class FileValidationException extends BaseException {
    
    public FileValidationException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
    
    public FileValidationException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
    
    // Convenience methods for common file validation errors
    
    public static FileValidationException fileNull() {
        return new FileValidationException(ErrorCode.FILE_NULL, "File cannot be null");
    }
    
    public static FileValidationException fileEmpty() {
        return new FileValidationException(ErrorCode.FILE_EMPTY, "File cannot be empty");
    }
    
    public static FileValidationException invalidFileType(String allowedTypes) {
        return new FileValidationException(ErrorCode.INVALID_FILE_TYPE, 
            "Invalid file type. Allowed types: " + allowedTypes);
    }
    
    public static FileValidationException fileTooLarge(long maxSizeBytes) {
        return new FileValidationException(ErrorCode.FILE_TOO_LARGE, 
            "File size cannot exceed " + maxSizeBytes + " bytes");
    }
}
