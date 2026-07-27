package com.chatbot.core.config.environment.service;

import com.chatbot.core.config.environment.model.EnvironmentConfig;
import com.chatbot.core.config.environment.repository.EnvironmentConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class EnvironmentConfigService {

    private final EnvironmentConfigRepository environmentConfigRepository;
    
    @Value("${spring.profiles.active:development}")
    private String currentEnvironment;
    
    // Cache for environment configs
    private final Map<String, String> configCache = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        loadEnvironmentConfigs();
        log.info("Loaded {} environment configs for environment: {}", configCache.size(), currentEnvironment);
    }

    @Transactional(readOnly = true)
    public String getConfig(String configKey) {
        return configCache.get(configKey);
    }

    @Transactional(readOnly = true)
    public String getConfigWithDefault(String configKey, String defaultValue) {
        return configCache.getOrDefault(configKey, defaultValue);
    }

    @Transactional(readOnly = true)
    public Integer getIntConfig(String configKey) {
        String value = getConfig(configKey);
        if (value == null) {
            return null;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            log.warn("Invalid integer config value for key: {}", configKey);
            return null;
        }
    }

    @Transactional(readOnly = true)
    public Boolean getBooleanConfig(String configKey) {
        String value = getConfig(configKey);
        if (value == null) {
            return null;
        }
        return Boolean.parseBoolean(value);
    }

    @Transactional(readOnly = true)
    public List<EnvironmentConfig> getAllConfigs() {
        return environmentConfigRepository.findActiveConfigsByEnvironment(currentEnvironment);
    }

    @Transactional
    public EnvironmentConfig createConfig(String environment, String configKey, String configValue, String description) {
        log.info("Creating environment config: {} for environment: {}", configKey, environment);

        // Check if config already exists
        environmentConfigRepository.findByEnvironmentAndConfigKeyAndIsActive(environment, configKey, true)
                .ifPresent(config -> {
                    throw new IllegalStateException("Config already exists for this environment");
                });

        EnvironmentConfig envConfig = EnvironmentConfig.builder()
                .environment(environment)
                .configKey(configKey)
                .configValue(configValue)
                .isEncrypted(false)
                .isActive(true)
                .description(description)
                .build();

        EnvironmentConfig savedConfig = environmentConfigRepository.save(envConfig);
        
        // Refresh cache if it's for current environment
        if (environment.equals(currentEnvironment)) {
            loadEnvironmentConfigs();
        }
        
        log.info("Created environment config with ID: {}", savedConfig.getId());
        return savedConfig;
    }

    @Transactional
    public EnvironmentConfig updateConfig(Long configId, String configValue, String description) {
        EnvironmentConfig config = environmentConfigRepository.findById(configId)
                .orElseThrow(() -> new RuntimeException("Environment config not found"));

        config.setConfigValue(configValue);
        config.setDescription(description);

        EnvironmentConfig updatedConfig = environmentConfigRepository.save(config);
        
        // Refresh cache if it's for current environment
        if (config.getEnvironment().equals(currentEnvironment)) {
            loadEnvironmentConfigs();
        }
        
        log.info("Updated environment config: {}", updatedConfig.getConfigKey());
        return updatedConfig;
    }

    @Transactional
    public void deactivateConfig(Long configId) {
        EnvironmentConfig config = environmentConfigRepository.findById(configId)
                .orElseThrow(() -> new RuntimeException("Environment config not found"));

        config.setIsActive(false);
        environmentConfigRepository.save(config);

        // Remove from cache if it's for current environment
        if (config.getEnvironment().equals(currentEnvironment)) {
            configCache.remove(config.getConfigKey());
        }
        
        log.info("Deactivated environment config: {}", config.getConfigKey());
    }

    @Transactional
    public void reloadConfigs() {
        loadEnvironmentConfigs();
        log.info("Reloaded environment configs for environment: {}", currentEnvironment);
    }

    private void loadEnvironmentConfigs() {
        configCache.clear();
        
        List<EnvironmentConfig> configs = environmentConfigRepository.findActiveConfigsByEnvironment(currentEnvironment);
        
        for (EnvironmentConfig config : configs) {
            String value = config.getIsEncrypted() ? "***ENCRYPTED***" : config.getConfigValue();
            configCache.put(config.getConfigKey(), value);
        }
    }

    @Transactional(readOnly = true)
    public List<String> getActiveEnvironments() {
        return environmentConfigRepository.findActiveEnvironments();
    }

    @Transactional(readOnly = true)
    public boolean hasConfig(String configKey) {
        return configCache.containsKey(configKey);
    }
}
