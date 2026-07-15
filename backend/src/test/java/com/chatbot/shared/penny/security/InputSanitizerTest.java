package com.chatbot.shared.penny.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for InputSanitizer
 */
class InputSanitizerTest {

    private InputSanitizer inputSanitizer;

    @BeforeEach
    void setUp() {
        inputSanitizer = new InputSanitizer();
    }

    @Test
    @DisplayName("Should sanitize XSS script tags")
    void shouldSanitizeXSSScriptTags() {
        String malicious = "<script>alert('xss')</script>Hello";
        String sanitized = inputSanitizer.sanitizeMessage(malicious);
        
        assertFalse(sanitized.contains("<script>"));
        assertFalse(sanitized.contains("</script>"));
        assertTrue(sanitized.contains("Hello"));
    }

    @Test
    @DisplayName("Should sanitize javascript: protocol")
    void shouldSanitizeJavascriptProtocol() {
        String malicious = "javascript:alert('xss')";
        String sanitized = inputSanitizer.sanitizeMessage(malicious);
        
        assertFalse(sanitized.toLowerCase().contains("javascript:"));
    }

    @Test
    @DisplayName("Should sanitize iframe tags")
    void shouldSanitizeIframeTags() {
        String malicious = "<iframe src='evil.com'></iframe>Hello";
        String sanitized = inputSanitizer.sanitizeMessage(malicious);
        
        assertFalse(sanitized.contains("<iframe"));
        assertFalse(sanitized.contains("</iframe>"));
    }

    @Test
    @DisplayName("Should detect SQL injection patterns")
    void shouldDetectSQLInjection() {
        String malicious = "1' OR '1'='1";
        assertTrue(inputSanitizer.isMalicious(malicious));
        
        String malicious2 = "SELECT * FROM users";
        assertTrue(inputSanitizer.isMalicious(malicious2));
    }

    @Test
    @DisplayName("Should detect command injection patterns")
    void shouldDetectCommandInjection() {
        String malicious = "hello; rm -rf /";
        assertTrue(inputSanitizer.isMalicious(malicious));
        
        String malicious2 = "hello && cat /etc/passwd";
        assertTrue(inputSanitizer.isMalicious(malicious2));
    }

    @Test
    @DisplayName("Should truncate overly long messages")
    void shouldTruncateLongMessages() {
        String longMessage = "a".repeat(15000);
        String sanitized = inputSanitizer.sanitizeMessage(longMessage);
        
        assertTrue(sanitized.length() <= 10000);
    }

    @Test
    @DisplayName("Should truncate overly long words")
    void shouldTruncateLongWords() {
        String longWord = "a".repeat(300);
        String sanitized = inputSanitizer.sanitizeMessage(longWord);
        
        assertTrue(sanitized.length() <= 200);
    }

    @Test
    @DisplayName("Should allow safe messages")
    void shouldAllowSafeMessages() {
        String safe = "Hello, how are you today?";
        String sanitized = inputSanitizer.sanitizeMessage(safe);
        
        assertEquals(safe, sanitized);
        assertFalse(inputSanitizer.isMalicious(safe));

        // Test normal sentences containing email addresses, semicolons, parentheses, and common verbs
        String normalWithVerbs = "Please select the option and update my email address: info@example.com (primary). Hello; how are you?";
        String sanitizedVerbs = inputSanitizer.sanitizeMessage(normalWithVerbs);
        assertEquals(normalWithVerbs, sanitizedVerbs);
        assertFalse(inputSanitizer.isMalicious(normalWithVerbs));
    }

    @Test
    @DisplayName("Should validate message length")
    void shouldValidateMessageLength() {
        assertTrue(inputSanitizer.isValidLength("Hello"));
        assertTrue(inputSanitizer.isValidLength("a".repeat(10000)));
        assertFalse(inputSanitizer.isValidLength("a".repeat(10001)));
    }

    @Test
    @DisplayName("Should handle null and empty messages")
    void shouldHandleNullAndEmptyMessages() {
        assertNull(inputSanitizer.sanitizeMessage(null));
        assertEquals("", inputSanitizer.sanitizeMessage(""));
        assertFalse(inputSanitizer.isMalicious(null));
        assertFalse(inputSanitizer.isMalicious(""));
    }

    @Test
    @DisplayName("Should sanitize bot names")
    void shouldSanitizeBotNames() {
        String botName = "My Bot <script>alert('xss')</script>";
        String sanitized = inputSanitizer.sanitizeBotName(botName);
        
        assertFalse(sanitized.contains("<script>"));
        assertFalse(sanitized.contains("</script>"));
        assertTrue(sanitized.contains("My Bot"));
    }

    @Test
    @DisplayName("Should sanitize descriptions")
    void shouldSanitizeDescriptions() {
        String description = "This is a <b>bold</b> description";
        String sanitized = inputSanitizer.sanitizeDescription(description);
        
        assertFalse(sanitized.contains("<b>"));
        assertFalse(sanitized.contains("</b>"));
        assertTrue(sanitized.contains("bold"));
    }

    @Test
    @DisplayName("Should remove special characters from bot names")
    void shouldRemoveSpecialCharactersFromBotNames() {
        String botName = "My@Bot#Name$Test";
        String sanitized = inputSanitizer.sanitizeBotName(botName);
        
        assertFalse(sanitized.contains("@"));
        assertFalse(sanitized.contains("#"));
        assertFalse(sanitized.contains("$"));
    }

    @Test
    @DisplayName("Should preserve valid characters in bot names")
    void shouldPreserveValidCharactersInBotNames() {
        String botName = "My-Bot_Name Test";
        String sanitized = inputSanitizer.sanitizeBotName(botName);
        
        assertTrue(sanitized.contains("-"));
        assertTrue(sanitized.contains("_"));
        assertTrue(sanitized.contains(" "));
    }
}
