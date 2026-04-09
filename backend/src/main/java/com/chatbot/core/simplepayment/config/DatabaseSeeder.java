package com.chatbot.core.simplepayment.config;

import com.chatbot.core.simplepayment.service.PackageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessException;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DatabaseSeeder {

    private final PackageService packageService;
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final long RETRY_DELAY_MS = 2000; // 2 seconds

    @Bean
    @Order(1) // Run this first
    public ApplicationRunner initializePackages() {
        return args -> {
            log.info("🌱 Checking if packages need to be initialized...");
            
            int attempt = 0;
            boolean success = false;
            
            while (attempt < MAX_RETRY_ATTEMPTS && !success) {
                attempt++;
                try {
                    log.info("📦 Attempt {} to initialize packages...", attempt);
                    
                    if (packageService.isEmpty()) {
                        log.info("📦 Packages table is empty, initializing default packages...");
                        packageService.initializeDefaultPackages();
                        log.info("✅ Default packages initialized successfully!");
                        success = true;
                    } else {
                        log.info("📦 Packages already exist, skipping initialization");
                        success = true;
                    }
                    
                } catch (DataAccessException e) {
                    log.error("❌ Database error on attempt {}: {}", attempt, e.getMessage(), e);
                    if (attempt < MAX_RETRY_ATTEMPTS) {
                        log.info("⏳ Waiting {}ms before retry...", RETRY_DELAY_MS);
                        Thread.sleep(RETRY_DELAY_MS);
                    }
                } catch (Exception e) {
                    log.error("❌ Unexpected error on attempt {}: {}", attempt, e.getMessage(), e);
                    if (attempt < MAX_RETRY_ATTEMPTS) {
                        log.info("⏳ Waiting {}ms before retry...", RETRY_DELAY_MS);
                        Thread.sleep(RETRY_DELAY_MS);
                    }
                }
            }
            
            if (!success) {
                log.error("❌ Failed to initialize packages after {} attempts", MAX_RETRY_ATTEMPTS);
                log.warn("⚠️ Application will continue but packages may not be available. Manual initialization may be required.");
                log.info("💡 To manually initialize packages, use the admin endpoint: POST /api/v1/packages/initialize");
            } else {
                log.info("🎉 Package initialization completed successfully on attempt {}", attempt);
            }
        };
    }

    /**
     * Force clean reinitialize - remove all packages and recreate from SimplePayment config
     */
    @Bean
    @Order(2) // Run after initialization
    public ApplicationRunner forceCleanReinitialize() {
        return args -> {
            // Only run if specifically requested via environment variable
            String forceReinit = System.getProperty("force.reinitialize.packages", "false");
            if (!"true".equals(forceReinit)) {
                return;
            }

            log.info(" forcing clean reinitialize of packages...");
            try {
                packageService.forceReinitializePackages();
                log.info(" Clean reinitialize completed successfully");
            } catch (Exception e) {
                log.error("Failed to force reinitialize packages: {}", e.getMessage(), e);
            }
        };
    }
}
