package com.chatbot.modules.auth.infra;

import lombok.extern.slf4j.Slf4j;

/**
 * Request context for holding locale information
 * Thread-local storage for current request's locale
 */
@Slf4j
public final class RequestContext {
    
    private static final ThreadLocal<String> LOCALE = new ThreadLocal<>();
    
    private RequestContext() {
        // Utility class
    }
    
    /**
     * Set the current locale for this request
     */
    public static void setLocale(String locale) {
        LOCALE.set(locale != null ? locale : "vi");
        log.debug("Set request locale: {}", locale);
    }
    
    /**
     * Get the current locale for this request
     * Defaults to "vi" if not set
     */
    public static String getLocale() {
        String locale = LOCALE.get();
        return locale != null ? locale : "vi";
    }
    
    /**
     * Clear the locale for this request
     */
    public static void clearLocale() {
        LOCALE.remove();
        log.debug("Cleared request locale");
    }
    
    /**
     * Clear all context data
     */
    public static void clear() {
        clearLocale();
    }
}
