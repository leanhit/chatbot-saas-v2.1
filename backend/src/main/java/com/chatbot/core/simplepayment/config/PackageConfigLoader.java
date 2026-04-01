package com.chatbot.core.simplepayment.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

@Component
@Slf4j
public class PackageConfigLoader {

    private PackageConfig packageConfig;
    private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());

    @PostConstruct
    public void loadConfiguration() {
        try {
            log.info("📦 Loading package configuration from YAML...");
            
            // Load YAML configuration
            ClassPathResource resource = new ClassPathResource("packages-config.yml");
            if (!resource.exists()) {
                log.warn("⚠️ packages-config.yml not found, using default configuration");
                packageConfig = new PackageConfig();
                return;
            }

            try (InputStream inputStream = resource.getInputStream()) {
                packageConfig = yamlMapper.readValue(inputStream, PackageConfig.class);
                log.info("✅ Package configuration loaded successfully");
                log.info("📦 Found {} package definitions", 
                    packageConfig.getPackages() != null ? packageConfig.getPackages().size() : 0);
                
                // Log package summaries
                if (packageConfig.getPackages() != null) {
                    packageConfig.getPackages().forEach((packageId, packageData) -> {
                        log.info("  - {}: {} ({} VND)", packageId, packageData.getName(), packageData.getPrice());
                    });
                }
            }

        } catch (IOException e) {
            log.error("❌ Failed to load package configuration: {}", e.getMessage(), e);
            packageConfig = new PackageConfig(); // Fallback to empty config
        }
    }

    /**
     * Get package configuration as map
     */
    public Map<String, PackageConfig.PackageDefinition> getPackages() {
        return packageConfig != null ? packageConfig.getPackages() : null;
    }

    /**
     * Get limits configuration
     */
    public PackageConfig.LimitsConfig getLimits() {
        return packageConfig != null ? packageConfig.getLimits() : null;
    }

    /**
     * Get specific package configuration
     */
    public PackageConfig.PackageDefinition getPackage(String packageId) {
        Map<String, PackageConfig.PackageDefinition> packages = getPackages();
        if (packages == null) {
            throw new RuntimeException("Package configuration not loaded");
        }
        return packages.get(packageId);
    }

    /**
     * Check if package exists in configuration
     */
    public boolean hasPackage(String packageId) {
        Map<String, PackageConfig.PackageDefinition> packages = getPackages();
        return packages != null && packages.containsKey(packageId);
    }
}
