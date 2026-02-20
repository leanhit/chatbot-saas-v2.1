package com.chatbot.core.message.processor.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * Message Validator Service - Validates messages before processing
 */
@Service
@Slf4j
public class MessageValidator {
    
    // Validation patterns
    private static final Pattern VALID_CONTENT_PATTERN = Pattern.compile("^[\\w\\s\\p{Punct}]{1,1000}$");
    private static final Pattern SPAM_PATTERN = Pattern.compile("(.)\\1{4,}"); // Repeated characters
    private static final Pattern PROFANITY_PATTERN = Pattern.compile("(?i)\\b(bad|word|list)\\b");
    
    /**
     * Validate message content
     */
    public ValidationResult validateMessage(String content, Map<String, Object> context) {
        ValidationResult result = new ValidationResult();
        
        if (content == null) {
            result.setValid(false);
            result.addError("Message content cannot be null");
            return result;
        }
        
        // Basic validations
        validateBasicRules(content, result);
        validateContentRules(content, result);
        validateSecurityRules(content, result);
        validateBusinessRules(content, context, result);
        
        log.debug("Message validation result: {} (errors: {})", 
                 result.isValid(), result.getErrors().size());
        
        return result;
    }
    
    /**
     * Validate basic rules
     */
    private void validateBasicRules(String content, ValidationResult result) {
        // Length validation
        if (content.length() == 0) {
            result.addError("Message cannot be empty");
        } else if (content.length() > 1000) {
            result.addError("Message too long (max 1000 characters)");
        }
        
        // Character validation
        if (!VALID_CONTENT_PATTERN.matcher(content).matches()) {
            result.addError("Message contains invalid characters");
        }
    }
    
    /**
     * Validate content rules
     */
    private void validateContentRules(String content, ValidationResult result) {
        // Spam detection
        if (SPAM_PATTERN.matcher(content).find()) {
            result.addWarning("Message contains repeated characters (possible spam)");
        }
        
        // Profanity check
        if (PROFANITY_PATTERN.matcher(content).find()) {
            result.addWarning("Message contains potentially inappropriate content");
        }
        
        // Empty content after cleanup
        String cleaned = content.trim().replaceAll("\\s+", "");
        if (cleaned.length() < 2) {
            result.addError("Message too short after cleanup");
        }
    }
    
    /**
     * Validate security rules
     */
    private void validateSecurityRules(String content, ValidationResult result) {
        String lowerContent = content.toLowerCase();
        
        // XSS prevention
        if (containsXssPatterns(lowerContent)) {
            result.addError("Message contains potentially dangerous content");
        }
        
        // SQL injection prevention
        if (containsSqlPatterns(lowerContent)) {
            result.addError("Message contains potentially malicious SQL patterns");
        }
        
        // Command injection prevention
        if (containsCommandPatterns(lowerContent)) {
            result.addError("Message contains potentially dangerous command patterns");
        }
    }
    
    /**
     * Validate business rules
     */
    private void validateBusinessRules(String content, Map<String, Object> context, ValidationResult result) {
        String channel = (String) context.getOrDefault("channel", "unknown");
        String messageType = (String) context.getOrDefault("messageType", "text");
        
        // Channel-specific rules
        switch (channel.toLowerCase()) {
            case "facebook":
                validateFacebookRules(content, result);
                break;
            case "web":
                validateWebRules(content, result);
                break;
            case "api":
                validateApiRules(content, result);
                break;
        }
        
        // Message type specific rules
        switch (messageType.toLowerCase()) {
            case "text":
                validateTextMessage(content, result);
                break;
            case "image":
                validateImageMessage(content, result);
                break;
            case "file":
                validateFileMessage(content, result);
                break;
        }
    }
    
    /**
     * Facebook-specific validation
     */
    private void validateFacebookRules(String content, ValidationResult result) {
        // Facebook message limits
        if (content.length() > 800) {
            result.addWarning("Facebook message is long (max 800 recommended)");
        }
        
        // Facebook-specific restrictions
        if (content.contains("facebook.com") && content.contains("login")) {
            result.addError("Message contains suspicious Facebook login reference");
        }
    }
    
    /**
     * Web-specific validation
     */
    private void validateWebRules(String content, ValidationResult result) {
        // Web form validation
        if (content.contains("<script>") || content.contains("javascript:")) {
            result.addError("Web message contains script tags");
        }
    }
    
    /**
     * API-specific validation
     */
    private void validateApiRules(String content, ValidationResult result) {
        // API format validation
        try {
            // Check if content is valid JSON if it looks like JSON
            if (content.trim().startsWith("{") && content.trim().endsWith("}")) {
                // JSON validation would go here
            }
        } catch (Exception e) {
            result.addError("Invalid JSON format in API message");
        }
    }
    
    /**
     * Text message validation
     */
    private void validateTextMessage(String content, ValidationResult result) {
        // Text-specific rules
        if (content.trim().isEmpty()) {
            result.addError("Text message cannot be empty");
        }
    }
    
    /**
     * Image message validation
     */
    private void validateImageMessage(String content, ValidationResult result) {
        // Image validation would check URL or base64 content
        if (content.startsWith("http") && !content.matches("^https?://.*\\.(jpg|jpeg|png|gif)$")) {
            result.addWarning("Image URL may not be valid");
        }
    }
    
    /**
     * File message validation
     */
    private void validateFileMessage(String content, ValidationResult result) {
        // File validation would check file type and size
        if (content.length() > 10000000) { // 10MB
            result.addError("File content too large");
        }
    }
    
    /**
     * Check for XSS patterns
     */
    private boolean containsXssPatterns(String content) {
        return content.contains("<script>") ||
               content.contains("javascript:") ||
               content.contains("onerror=") ||
               content.contains("onclick=") ||
               content.contains("onload=");
    }
    
    /**
     * Check for SQL injection patterns
     */
    private boolean containsSqlPatterns(String content) {
        return content.contains("drop table") ||
               content.contains("union select") ||
               content.contains("insert into") ||
               content.contains("delete from") ||
               content.contains("update set") ||
               content.contains("exec(");
    }
    
    /**
     * Check for command injection patterns
     */
    private boolean containsCommandPatterns(String content) {
        return content.contains("&&") ||
               content.contains("||") ||
               content.contains(";") ||
               content.contains("|") ||
               content.contains(">");
    }
    
    /**
     * Validation result class
     */
    public static class ValidationResult {
        private boolean valid = true;
        private java.util.List<String> errors = new java.util.ArrayList<>();
        private java.util.List<String> warnings = new java.util.ArrayList<>();
        
        public boolean isValid() { return valid; }
        public void setValid(boolean valid) { this.valid = valid; }
        public java.util.List<String> getErrors() { return errors; }
        public java.util.List<String> getWarnings() { return warnings; }
        
        public void addError(String error) {
            this.errors.add(error);
            this.valid = false;
        }
        
        public void addWarning(String warning) {
            this.warnings.add(warning);
        }
    }
}
