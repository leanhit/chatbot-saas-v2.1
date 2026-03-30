package com.chatbot.core.simplepayment.config;

import com.chatbot.core.simplepayment.service.PackageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DatabaseSeeder {

    private final PackageService packageService;

    @Bean
    @Order(1) // Run this first
    public ApplicationRunner initializePackages() {
        return args -> {
            log.info("🌱 Checking if packages need to be initialized...");
            
            try {
                if (packageService.isEmpty()) {
                    log.info("📦 Packages table is empty, initializing default packages...");
                    packageService.initializeDefaultPackages();
                    log.info("✅ Default packages initialized successfully!");
                } else {
                    log.info("📦 Packages already exist, skipping initialization");
                }
            } catch (Exception e) {
                log.error("❌ Error initializing packages: {}", e.getMessage(), e);
                // Don't throw the exception to allow the application to start
                // Packages can be initialized manually via admin endpoint
            }
        };
    }
}
