package com.chatbot.core.message.processor.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * Message Transformer Service - Transforms messages before processing
 */
@Service
@Slf4j
public class MessageTransformer {
    
    // Common patterns
    private static final Pattern URL_PATTERN = Pattern.compile("(https?://\\S+)");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\\b");
    private static final Pattern PHONE_PATTERN = Pattern.compile("\\b\\d{10,}\\b");
    
    /**
     * Transform message content
     */
    public String transformMessage(String content, Map<String, Object> context) {
        if (content == null || content.trim().isEmpty()) {
            return content;
        }
        
        String transformed = content;
        
        // Apply transformations based on context
        transformed = applyNormalization(transformed);
        transformed = applyContentFilters(transformed, context);
        transformed = applySecurityFilters(transformed);
        transformed = applyFormatting(transformed);
        
        log.debug("Transformed message: {} -> {}", content, transformed);
        return transformed;
    }
    
    /**
     * Apply basic normalization
     */
    private String applyNormalization(String content) {
        return content
                .trim()
                .replaceAll("\\s+", " ")  // Normalize whitespace
                .toLowerCase();            // Normalize case
    }
    
    /**
     * Apply content-based filters
     */
    private String applyContentFilters(String content, Map<String, Object> context) {
        String channel = (String) context.getOrDefault("channel", "unknown");
        
        // Channel-specific transformations
        switch (channel.toLowerCase()) {
            case "facebook":
                return applyFacebookFilters(content);
            case "web":
                return applyWebFilters(content);
            case "api":
                return applyApiFilters(content);
            default:
                return content;
        }
    }
    
    /**
     * Apply Facebook-specific filters
     */
    private String applyFacebookFilters(String content) {
        // Remove Facebook-specific formatting
        return content
                .replaceAll("\\[.*?\\]", "")  // Remove bracketed text
                .replaceAll("@\\w+", "")     // Remove mentions
                .replaceAll("#\\w+", "");     // Remove hashtags
    }
    
    /**
     * Apply web-specific filters
     */
    private String applyWebFilters(String content) {
        // HTML sanitization for web input
        return content
                .replaceAll("<[^>]*>", "")  // Remove HTML tags
                .replaceAll("&nbsp;", " ")    // Replace HTML spaces
                .replaceAll("&amp;", "&");    // Replace HTML entities
    }
    
    /**
     * Apply API-specific filters
     */
    private String applyApiFilters(String content) {
        // JSON sanitization for API input
        return content
                .replaceAll("[\\\\\"']", "")   // Remove quotes
                .replaceAll("[\\n\\r]", " "); // Replace newlines
    }
    
    /**
     * Apply security filters
     */
    private String applySecurityFilters(String content) {
        // Detect and flag potentially harmful content
        if (containsSuspiciousContent(content)) {
            log.warn("Suspicious content detected: {}", content);
            // Could implement quarantine logic here
        }
        
        return content;
    }
    
    /**
     * Apply formatting transformations
     */
    private String applyFormatting(String content) {
        // Apply standard formatting rules
        return content
                .replaceAll("\\burl:\\s*", "https://")  // URL shorthand
                .replaceAll("\\bemail:\\s*", "")      // Email shorthand
                .replaceAll("\\bphone:\\s*", "");     // Phone shorthand
    }
    
    /**
     * Check for suspicious content
     */
    private boolean containsSuspiciousContent(String content) {
        String lowerContent = content.toLowerCase();
        
        // Check for common attack patterns
        return lowerContent.contains("<script>") ||
               lowerContent.contains("javascript:") ||
               lowerContent.contains("onerror=") ||
               lowerContent.contains("onclick=") ||
               lowerContent.contains("eval(");
    }
    
    /**
     * Extract entities from message
     */
    public Map<String, Object> extractEntities(String content) {
        Map<String, Object> entities = new java.util.HashMap<>();
        
        // Extract URLs
        if (URL_PATTERN.matcher(content).find()) {
            entities.put("hasUrls", true);
            entities.put("urls", URL_PATTERN.matcher(content).results().map(r -> r.group()).toList());
        }
        
        // Extract emails
        if (EMAIL_PATTERN.matcher(content).find()) {
            entities.put("hasEmails", true);
            entities.put("emails", EMAIL_PATTERN.matcher(content).results().map(r -> r.group()).toList());
        }
        
        // Extract phone numbers
        if (PHONE_PATTERN.matcher(content).find()) {
            entities.put("hasPhones", true);
            entities.put("phones", PHONE_PATTERN.matcher(content).results().map(r -> r.group()).toList());
        }
        
        return entities;
    }
}
