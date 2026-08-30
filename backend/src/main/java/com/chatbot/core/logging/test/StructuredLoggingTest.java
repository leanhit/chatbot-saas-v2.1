package com.chatbot.core.logging.test;

import com.chatbot.core.logging.util.ConditionalLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Test controller to verify structured logging configuration.
 * Access: GET /api/logging/test
 */
@RestController
@RequestMapping("/api/logging")
public class StructuredLoggingTest {

    private static final Logger log = LoggerFactory.getLogger(StructuredLoggingTest.class);

    @GetMapping("/test")
    public String testStructuredLogging() {
        // Test standard logging
        log.info("Testing structured logging - INFO level");
        log.warn("Testing structured logging - WARN level");
        
        // Test conditional logging (should not execute string concatenation if debug disabled)
        ConditionalLogger.debug(log, () -> "Debug message with lazy evaluation: " + expensiveOperation());
        ConditionalLogger.debug(log, "Debug message with params: {}", "test value");
        
        // Test structured arguments
        log.info("User action: userId={}, action={}, timestamp={}", 
            "user123", "login", System.currentTimeMillis());
        
        // Test error logging
        try {
            throw new RuntimeException("Test exception for logging");
        } catch (Exception e) {
            log.error("Error occurred during logging test", e);
        }
        
        return "Structured logging test completed. Check logs for JSON output.";
    }

    private String expensiveOperation() {
        // Simulate expensive operation that should only execute if debug is enabled
        return "Expensive computation result";
    }
}
