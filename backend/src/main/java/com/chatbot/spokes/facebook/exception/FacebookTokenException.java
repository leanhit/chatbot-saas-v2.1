package com.chatbot.spokes.facebook.exception;

/**
 * Exception for Facebook token-related errors
 * Used to identify token expiry and invalid token scenarios
 */
public class FacebookTokenException extends RuntimeException {
    
    private final String pageId;
    private final String errorCode;
    private final String errorSubcode;
    
    public FacebookTokenException(String message, String pageId) {
        super(message);
        this.pageId = pageId;
        this.errorCode = null;
        this.errorSubcode = null;
    }
    
    public FacebookTokenException(String message, String pageId, Throwable cause) {
        super(message, cause);
        this.pageId = pageId;
        this.errorCode = null;
        this.errorSubcode = null;
    }
    
    public FacebookTokenException(String message, String pageId, String errorCode, String errorSubcode) {
        super(message);
        this.pageId = pageId;
        this.errorCode = errorCode;
        this.errorSubcode = errorSubcode;
    }
    
    public String getPageId() {
        return pageId;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public String getErrorSubcode() {
        return errorSubcode;
    }
    
    public boolean isTokenExpired() {
        return "190".equals(errorCode);
    }
    
    public boolean isPermissionDenied() {
        return "200".equals(errorCode);
    }
    
    @Override
    public String toString() {
        return String.format("FacebookTokenException{pageId='%s', errorCode='%s', errorSubcode='%s', message='%s'}", 
                           pageId, errorCode, errorSubcode, getMessage());
    }
}
