package com.chatbot.core.tenant.membership.exception;

import com.chatbot.shared.exceptions.BaseException;
import com.chatbot.shared.exceptions.ErrorCode;

/**
 * Exception thrown when notification sending fails.
 */
public class NotificationException extends BaseException {
    
    public NotificationException(String message) {
        super(ErrorCode.NOTIFICATION_ERROR, message);
    }
    
    public NotificationException(String message, Throwable cause) {
        super(ErrorCode.NOTIFICATION_ERROR, message, cause);
    }
}
