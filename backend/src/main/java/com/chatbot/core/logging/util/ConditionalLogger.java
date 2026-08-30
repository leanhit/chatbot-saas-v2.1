package com.chatbot.core.logging.util;

import org.slf4j.Logger;

/**
 * Utility class for conditional logging to avoid performance overhead in production.
 * Uses lazy evaluation for log messages to prevent string concatenation when logging is disabled.
 */
public class ConditionalLogger {

    /**
     * Log debug message only if debug level is enabled.
     * Uses lambda for lazy evaluation to avoid string concatenation overhead.
     */
    public static void debug(Logger logger, java.util.function.Supplier<String> messageSupplier) {
        if (logger.isDebugEnabled()) {
            logger.debug(messageSupplier.get());
        }
    }

    /**
     * Log debug message with parameters only if debug level is enabled.
     */
    public static void debug(Logger logger, String format, Object... arguments) {
        if (logger.isDebugEnabled()) {
            logger.debug(format, arguments);
        }
    }

    /**
     * Log trace message only if trace level is enabled.
     */
    public static void trace(Logger logger, java.util.function.Supplier<String> messageSupplier) {
        if (logger.isTraceEnabled()) {
            logger.trace(messageSupplier.get());
        }
    }

    /**
     * Log trace message with parameters only if trace level is enabled.
     */
    public static void trace(Logger logger, String format, Object... arguments) {
        if (logger.isTraceEnabled()) {
            logger.trace(format, arguments);
        }
    }
}
