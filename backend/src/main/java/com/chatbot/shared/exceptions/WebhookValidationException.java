package com.chatbot.shared.exceptions;

/**
 * Exception for webhook validation errors.
 * Used when webhook configuration or execution fails validation.
 */
public class WebhookValidationException extends BaseException {
    
    public WebhookValidationException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
    
    public WebhookValidationException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
    
    // Convenience methods for common webhook validation errors
    
    public static WebhookValidationException urlExists(String url) {
        return new WebhookValidationException(ErrorCode.WEBHOOK_URL_EXISTS, 
            "Webhook URL already exists: " + url);
    }
    
    public static WebhookValidationException invalidUrl(String url) {
        return new WebhookValidationException(ErrorCode.INVALID_WEBHOOK_URL, 
            "Invalid webhook URL: " + url);
    }
    
    public static WebhookValidationException signatureError(String message) {
        return new WebhookValidationException(ErrorCode.WEBHOOK_SIGNATURE_ERROR, 
            "Webhook signature error: " + message);
    }
    
    public static WebhookValidationException testFailed(String webhookName, String reason) {
        return new WebhookValidationException(ErrorCode.WEBHOOK_TEST_FAILED, 
            "Webhook test failed for " + webhookName + ": " + reason);
    }
}
