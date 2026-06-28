package com.chatbot.core.config.runtime.exception;

import com.chatbot.shared.exceptions.BaseException;
import com.chatbot.shared.exceptions.ErrorCode;

/**
 * Base exception for runtime config-related errors.
 */
public class ConfigException extends BaseException {
    
    public ConfigException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
    
    public ConfigException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
