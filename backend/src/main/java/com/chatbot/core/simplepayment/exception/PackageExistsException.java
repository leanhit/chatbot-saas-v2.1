package com.chatbot.core.simplepayment.exception;

import com.chatbot.shared.exceptions.BaseException;
import com.chatbot.shared.exceptions.ErrorCode;

/**
 * Exception for package already exists errors.
 */
public class PackageExistsException extends BaseException {
    
    public PackageExistsException(String packageId) {
        super(ErrorCode.PACKAGE_ID_EXISTS, "Package ID already exists: " + packageId);
    }
}
