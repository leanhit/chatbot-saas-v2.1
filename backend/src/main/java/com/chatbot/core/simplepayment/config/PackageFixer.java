package com.chatbot.core.simplepayment.config;

import com.chatbot.core.simplepayment.repository.PackageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class PackageFixer {

    private final JdbcTemplate jdbcTemplate;

    @Bean
    @Order(0) // Run before DatabaseSeeder
    public ApplicationRunner fixPackageIsActive() {
        return args -> {
            try {
                log.info("Checking package isActive status...");
                
                // Check if any packages are inactive
                int inactiveCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM packages WHERE isactive = false", 
                    Integer.class
                );
                
                if (inactiveCount > 0) {
                    log.info("Found {} inactive packages, fixing...", inactiveCount);
                    
                    // Update all packages to active
                    int updated = jdbcTemplate.update(
                        "UPDATE packages SET isactive = true WHERE isactive = false"
                    );
                    
                    log.info("Updated {} packages to active status", updated);
                } else {
                    log.info("All packages are already active");
                }
                
            } catch (Exception e) {
                log.error("Error fixing package isActive: {}", e.getMessage(), e);
            }
        };
    }
}
