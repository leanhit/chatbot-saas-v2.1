package com.chatbot.shared.penny.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

/**
 * Input Sanitizer - Sanitize user input to prevent XSS and injection attacks
 */
@Service
@Slf4j
public class InputSanitizer {

    // XSS patterns to detect and remove
    private static final Pattern[] XSS_PATTERNS = {
        Pattern.compile("<script[^>]*>.*?</script>", Pattern.CASE_INSENSITIVE),
        Pattern.compile("javascript:", Pattern.CASE_INSENSITIVE),
        Pattern.compile("on\\w+\\s*=", Pattern.CASE_INSENSITIVE),
        Pattern.compile("<iframe[^>]*>.*?</iframe>", Pattern.CASE_INSENSITIVE),
        Pattern.compile("<object[^>]*>.*?</object>", Pattern.CASE_INSENSITIVE),
        Pattern.compile("<embed[^>]*>.*?</embed>", Pattern.CASE_INSENSITIVE),
        Pattern.compile("<applet[^>]*>.*?</applet>", Pattern.CASE_INSENSITIVE),
        Pattern.compile("<meta[^>]*>", Pattern.CASE_INSENSITIVE),
        Pattern.compile("<link[^>]*>", Pattern.CASE_INSENSITIVE),
        Pattern.compile("<style[^>]*>.*?</style>", Pattern.CASE_INSENSITIVE),
        Pattern.compile("<img[^>]*onerror[^>]*>", Pattern.CASE_INSENSITIVE),
        Pattern.compile("<svg[^>]*>.*?</svg>", Pattern.CASE_INSENSITIVE)
    };

    // SQL injection patterns (basic detection)
    private static final Pattern[] SQL_INJECTION_PATTERNS = {
        Pattern.compile("(?i)(\\b(SELECT|INSERT|UPDATE|DELETE|DROP|ALTER|EXEC|UNION|CREATE)\\b)"),
        Pattern.compile("(?i)(--|;|\\/\\*|\\*\\/|@@|@|xp_|sp_)"),
        Pattern.compile("(?i)(\\b(OR|AND)\\s+\\d+\\s*=\\s*\\d+)")
    };

    // Command injection patterns
    private static final Pattern[] COMMAND_INJECTION_PATTERNS = {
        Pattern.compile("[;&|`$()]", Pattern.CASE_INSENSITIVE),
        Pattern.compile("\\$\\(.*\\)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("`.*`", Pattern.CASE_INSENSITIVE)
    };

    private static final int MAX_MESSAGE_LENGTH = 10000;
    private static final int MAX_WORD_LENGTH = 200;

    /**
     * Sanitize user message input
     */
    public String sanitizeMessage(String message) {
        if (!StringUtils.hasText(message)) {
            return message;
        }

        String sanitized = message.trim();

        // Check length limits
        if (sanitized.length() > MAX_MESSAGE_LENGTH) {
            log.warn("Message too long, truncating from {} to {} characters", sanitized.length(), MAX_MESSAGE_LENGTH);
            sanitized = sanitized.substring(0, MAX_MESSAGE_LENGTH);
        }

        // Remove XSS patterns
        for (Pattern pattern : XSS_PATTERNS) {
            sanitized = pattern.matcher(sanitized).replaceAll("");
        }

        // Detect and log potential SQL injection attempts
        for (Pattern pattern : SQL_INJECTION_PATTERNS) {
            if (pattern.matcher(sanitized).find()) {
                log.warn("Potential SQL injection attempt detected in message: {}", sanitized);
                sanitized = pattern.matcher(sanitized).replaceAll("");
            }
        }

        // Detect and log potential command injection attempts
        for (Pattern pattern : COMMAND_INJECTION_PATTERNS) {
            if (pattern.matcher(sanitized).find()) {
                log.warn("Potential command injection attempt detected in message: {}", sanitized);
                sanitized = pattern.matcher(sanitized).replaceAll("");
            }
        }

        // Check for extremely long words (potential DoS)
        String[] words = sanitized.split("\\s+");
        StringBuilder sanitizedBuilder = new StringBuilder();
        for (String word : words) {
            if (word.length() > MAX_WORD_LENGTH) {
                log.warn("Word too long, truncating from {} to {} characters", word.length(), MAX_WORD_LENGTH);
                word = word.substring(0, MAX_WORD_LENGTH);
            }
            sanitizedBuilder.append(word).append(" ");
        }

        return sanitizedBuilder.toString().trim();
    }

    /**
     * Sanitize bot name
     */
    public String sanitizeBotName(String botName) {
        if (!StringUtils.hasText(botName)) {
            return botName;
        }

        String sanitized = botName.trim();
        
        // Remove special characters, keep only alphanumeric, spaces, hyphens, underscores
        sanitized = sanitized.replaceAll("[^a-zA-Z0-9\\s\\-_]", "");
        
        // Limit length
        if (sanitized.length() > 100) {
            sanitized = sanitized.substring(0, 100);
        }

        return sanitized;
    }

    /**
     * Sanitize description
     */
    public String sanitizeDescription(String description) {
        if (!StringUtils.hasText(description)) {
            return description;
        }

        String sanitized = description.trim();
        
        // Remove HTML tags
        sanitized = sanitized.replaceAll("<[^>]*>", "");
        
        // Limit length
        if (sanitized.length() > 500) {
            sanitized = sanitized.substring(0, 500);
        }

        return sanitized;
    }

    /**
     * Check if message contains potentially malicious content
     */
    public boolean isMalicious(String message) {
        if (!StringUtils.hasText(message)) {
            return false;
        }

        // Check for XSS patterns
        for (Pattern pattern : XSS_PATTERNS) {
            if (pattern.matcher(message).find()) {
                return true;
            }
        }

        // Check for SQL injection patterns
        for (Pattern pattern : SQL_INJECTION_PATTERNS) {
            if (pattern.matcher(message).find()) {
                return true;
            }
        }

        // Check for command injection patterns
        for (Pattern pattern : COMMAND_INJECTION_PATTERNS) {
            if (pattern.matcher(message).find()) {
                return true;
            }
        }

        return false;
    }

    /**
     * Validate message length
     */
    public boolean isValidLength(String message) {
        return message == null || message.length() <= MAX_MESSAGE_LENGTH;
    }
}
