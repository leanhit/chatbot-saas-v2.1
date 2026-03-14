package com.chatbot.spokes.facebook.handler;

import com.chatbot.spokes.facebook.exception.FacebookTokenException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Centralized Facebook API Error Handler
 * Detects token-related errors and provides unified error handling
 */
@Component
@Slf4j
public class FacebookErrorHandler {
    
    // Pattern to extract error codes from Facebook API responses
    private static final Pattern ERROR_CODE_PATTERN = Pattern.compile("\"error_code\":\\s*(\\d+)");
    private static final Pattern ERROR_SUBCODE_PATTERN = Pattern.compile("\"error_subcode\":\\s*(\\d+)");
    private static final Pattern OAUTH_EXCEPTION_PATTERN = Pattern.compile("OAuthException");
    
    /**
     * Check if exception is a token-related error
     */
    public boolean isTokenError(Exception e) {
        if (e instanceof FacebookTokenException) {
            return true;
        }
        
        if (e instanceof WebClientResponseException) {
            String responseBody = ((WebClientResponseException) e).getResponseBodyAsString();
            return isTokenError(responseBody);
        }
        
        return false;
    }
    
    /**
     * Check if response body contains token error
     */
    public boolean isTokenError(String responseBody) {
        return OAUTH_EXCEPTION_PATTERN.matcher(responseBody).find() &&
               (isTokenExpiredError(responseBody) || isPermissionError(responseBody));
    }
    
    /**
     * Check if token is expired (OAuthException #190)
     */
    public boolean isTokenExpiredError(String responseBody) {
        return OAUTH_EXCEPTION_PATTERN.matcher(responseBody).find() &&
               responseBody.contains("\"code\":190");
    }
    
    /**
     * Check if permission denied (OAuthException #200)
     */
    public boolean isPermissionError(String responseBody) {
        return OAUTH_EXCEPTION_PATTERN.matcher(responseBody).find() &&
               responseBody.contains("\"code\":200");
    }
    
    /**
     * Extract error code from response body
     */
    public String extractErrorCode(String responseBody) {
        Matcher matcher = ERROR_CODE_PATTERN.matcher(responseBody);
        return matcher.find() ? matcher.group(1) : null;
    }
    
    /**
     * Extract error subcode from response body
     */
    public String extractErrorSubcode(String responseBody) {
        Matcher matcher = ERROR_SUBCODE_PATTERN.matcher(responseBody);
        return matcher.find() ? matcher.group(1) : null;
    }
    
    /**
     * Convert exception to FacebookTokenException if applicable
     */
    public FacebookTokenException convertToTokenException(Exception e, String pageId) {
        if (e instanceof FacebookTokenException) {
            return (FacebookTokenException) e;
        }
        
        if (e instanceof WebClientResponseException) {
            String responseBody = ((WebClientResponseException) e).getResponseBodyAsString();
            
            if (isTokenError(responseBody)) {
                String errorCode = extractErrorCode(responseBody);
                String errorSubcode = extractErrorSubcode(responseBody);
                
                String message = String.format("Facebook token error for page %s: %s", 
                                             pageId, extractErrorMessage(responseBody));
                
                return new FacebookTokenException(message, pageId, errorCode, errorSubcode);
            }
        }
        
        return null;
    }
    
    /**
     * Extract error message from Facebook API response
     */
    public String extractErrorMessage(String responseBody) {
        try {
            Pattern messagePattern = Pattern.compile("\"message\":\\s*\"([^\"]+)\"");
            Matcher matcher = messagePattern.matcher(responseBody);
            if (matcher.find()) {
                return matcher.group(1);
            }
        } catch (Exception e) {
            log.debug("Failed to extract error message from response", e);
        }
        return "Unknown Facebook API error";
    }
    
    /**
     * Log token error with context
     */
    public void logTokenError(FacebookTokenException e) {
        if (e.isTokenExpired()) {
            log.warn("🔑 Facebook token expired for page: {}", e.getPageId());
        } else if (e.isPermissionDenied()) {
            log.warn("🚫 Facebook permission denied for page: {}", e.getPageId());
        } else {
            log.warn("❌ Facebook token error for page {}: {}", e.getPageId(), e.getMessage());
        }
    }
    
    /**
     * Check if error is recoverable (can be fixed with token refresh)
     */
    public boolean isRecoverableTokenError(Exception e) {
        FacebookTokenException tokenException = convertToTokenException(e, "unknown");
        return tokenException != null && tokenException.isTokenExpired();
    }
}
