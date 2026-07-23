package com.chatbot.shared.exceptions;

/**
 * Exception thrown when storage operations fail (e.g., MinIO S3 operations).
 * HTTP Status: 500 Internal Server Error
 */
public class StorageException extends BaseException {
    
    public StorageException(String message) {
        super(ErrorCode.INTERNAL_ERROR, message);
    }
    
    public StorageException(String message, Throwable cause) {
        super(ErrorCode.INTERNAL_ERROR, message, cause);
    }
}
