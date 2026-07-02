package com.chatbot.core.tenant.exception;

import com.chatbot.shared.exceptions.BaseException;
import com.chatbot.shared.exceptions.ErrorCode;

/**
 * Exception thrown when gRPC integration calls fail.
 */
public class GrpcIntegrationException extends BaseException {
    
    public GrpcIntegrationException(String message) {
        super(ErrorCode.INTEGRATION_ERROR, message);
    }
    
    public GrpcIntegrationException(String message, Throwable cause) {
        super(ErrorCode.INTEGRATION_ERROR, message, cause);
    }
}
