package com.chatbot.core.simplepayment.exception;

import com.chatbot.shared.exceptions.BaseException;
import com.chatbot.shared.exceptions.ErrorCode;

/**
 * Exception for package not found errors.
 */
public class PackageNotFoundException extends BaseException {
    
    public PackageNotFoundException(String packageId) {
        super(ErrorCode.RESOURCE_NOT_FOUND, "Package not found: " + packageId);
    }
    
    public PackageNotFoundException(Long id) {
        super(ErrorCode.RESOURCE_NOT_FOUND, "Package not found with ID: " + id);
    }
}
