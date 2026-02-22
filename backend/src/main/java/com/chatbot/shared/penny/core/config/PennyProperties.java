package com.chatbot.shared.penny.core.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import lombok.Data;
import java.time.Duration;

/**
 * Penny Middleware Configuration Properties
 */
@ConfigurationProperties(prefix = "penny")
@Data
public class PennyProperties {
    
    private Middleware middleware = new Middleware();
    private Provider provider = new Provider();
    private Intent intent = new Intent();
    private Context context = new Context();
    private Analytics analytics = new Analytics();
    private Error error = new Error();
    
    @Data
    public static class Middleware {
        private boolean enabled = true;
        private Duration processingTimeout = Duration.ofSeconds(30);
        private boolean streamingEnabled = true;
        private int maxRetries = 3;
        private Duration retryDelay = Duration.ofMillis(500);
    }
    
    @Data
    public static class Provider {
        private String selectionStrategy = "hybrid";
        private Duration healthCheckInterval = Duration.ofSeconds(30);
        private boolean fallbackEnabled = true;
        private Health health = new Health();
        
        @Data
        public static class Health {
            private boolean monitorEnabled = true;
            private Duration checkInterval = Duration.ofSeconds(30);
            private int maxFailures = 3;
            private Duration recoveryTime = Duration.ofMinutes(5);
        }
    }
    
    @Data
    public static class Intent {
        private Cache cache = new Cache();
        private boolean vietnameseEnabled = true;
        private double confidenceThreshold = 0.7;
        private boolean entityExtractionEnabled = true;
        
        @Data
        public static class Cache {
            private boolean enabled = true;
            private Duration ttl = Duration.ofMinutes(30);
            private int maxSize = 1000;
        }
    }
    
    @Data
    public static class Context {
        private String storageType = "redis";
        private Duration ttl = Duration.ofHours(24);
        private boolean compressionEnabled = true;
        private Cleanup cleanup = new Cleanup();
        
        @Data
        public static class Cleanup {
            private Duration interval = Duration.ofHours(1);
            boolean autoCleanup = true;
            private Duration maxAge = Duration.ofDays(7);
        }
    }
    
    @Data
    public static class Analytics {
        private boolean enabled = true;
        private int batchSize = 100;
        private Duration flushInterval = Duration.ofSeconds(60);
        private boolean realTimeEnabled = false;
        private Metrics metrics = new Metrics();
        
        @Data
        public static class Metrics {
            private boolean processingTimeEnabled = true;
            private boolean providerUsageEnabled = true;
            private boolean intentTrackingEnabled = true;
            private boolean errorTrackingEnabled = true;
        }
    }
    
    @Data
    public static class Error {
        private boolean circuitbreakerEnabled = true;
        private boolean fallbackEnabled = true;
        private int maxRetries = 3;
        private Duration timeoutDuration = Duration.ofSeconds(30);
        private Duration recoveryDuration = Duration.ofSeconds(60);
        private int failureThreshold = 5;
    }
}
