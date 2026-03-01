package com.chatbot.core.config.runtime.service;

import com.chatbot.core.config.runtime.dto.ConfigRequest;
import com.chatbot.core.config.runtime.dto.ConfigResponse;
import com.chatbot.core.config.runtime.model.RuntimeConfig;
import com.chatbot.core.config.runtime.model.ConfigScope;
import com.chatbot.core.config.runtime.repository.RuntimeConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RuntimeConfigService {

    private final RuntimeConfigRepository runtimeConfigRepository;

    public ConfigResponse createConfig(ConfigRequest request) {
        log.info("Creating runtime config: {} with scope: {}", request.getConfigKey(), request.getConfigScope());

        // Check if config already exists for the same scope
        if (request.getTenantId() != null) {
            runtimeConfigRepository.findByConfigKeyAndTenantId(request.getConfigKey(), request.getTenantId())
                    .ifPresent(config -> {
                        throw new IllegalStateException("Config already exists for this tenant");
                    });
        } else if (request.getUserId() != null) {
            runtimeConfigRepository.findByConfigKeyAndTenantIdAndUserId(
                    request.getConfigKey(), request.getTenantId(), request.getUserId())
                    .ifPresent(config -> {
                        throw new IllegalStateException("Config already exists for this user");
                    });
        } else {
            runtimeConfigRepository.findByConfigKeyAndConfigScope(
                    request.getConfigKey(), request.getConfigScope())
                    .ifPresent(config -> {
                        throw new IllegalStateException("Config already exists for this scope");
                    });
        }

        RuntimeConfig config = RuntimeConfig.builder()
                .configKey(request.getConfigKey())
                .configValue(request.getConfigValue())
                .defaultValue(request.getDefaultValue())
                .configType(request.getConfigType())
                .configScope(request.getConfigScope())
                .userId(request.getUserId())
                .isEncrypted(request.getIsEncrypted())
                .isReadonly(request.getIsReadonly())
                .description(request.getDescription())
                .build();
        config.setTenantId(request.getTenantId());

        RuntimeConfig savedConfig = runtimeConfigRepository.save(config);
        log.info("Created runtime config with ID: {}", savedConfig.getId());
        
        return toConfigResponse(savedConfig);
    }

    @Transactional(readOnly = true)
    public ConfigResponse getConfig(String configKey, Long tenantId, Long userId) {
        List<RuntimeConfig> configs = runtimeConfigRepository.findEffectiveConfigByKey(configKey, tenantId, userId);
        
        if (configs.isEmpty()) {
            throw new RuntimeException("Config not found: " + configKey);
        }

        // Return the most specific config (USER > TENANT > GLOBAL)
        RuntimeConfig config = configs.get(0);
        return toConfigResponse(config);
    }

    @Transactional(readOnly = true)
    public List<ConfigResponse> getAllConfigs(Long tenantId, Long userId) {
        List<RuntimeConfig> configs = runtimeConfigRepository.findEffectiveConfigs(tenantId, userId);
        return configs.stream()
                .map(this::toConfigResponse)
                .collect(Collectors.toList());
    }

    public ConfigResponse updateConfig(Long configId, ConfigRequest request) {
        RuntimeConfig config = runtimeConfigRepository.findById(configId)
                .orElseThrow(() -> new RuntimeException("Config not found"));

        if (config.getIsReadonly()) {
            throw new IllegalStateException("Cannot update readonly config");
        }

        config.setConfigValue(request.getConfigValue());
        config.setDescription(request.getDescription());
        config.setIsEncrypted(request.getIsEncrypted());

        RuntimeConfig updatedConfig = runtimeConfigRepository.save(config);
        log.info("Updated runtime config: {}", updatedConfig.getConfigKey());
        
        return toConfigResponse(updatedConfig);
    }

    public void deleteConfig(Long configId) {
        RuntimeConfig config = runtimeConfigRepository.findById(configId)
                .orElseThrow(() -> new RuntimeException("Config not found"));

        if (config.getIsReadonly()) {
            throw new IllegalStateException("Cannot delete readonly config");
        }

        runtimeConfigRepository.delete(config);
        log.info("Deleted runtime config: {}", config.getConfigKey());
    }

    @Transactional(readOnly = true)
    public String getConfigValue(String configKey, Long tenantId, Long userId) {
        try {
            ConfigResponse config = getConfig(configKey, tenantId, userId);
            return config.getConfigValue() != null ? config.getConfigValue() : config.getDefaultValue();
        } catch (Exception e) {
            log.warn("Config not found: {}, returning null", configKey);
            return null;
        }
    }

    @Transactional(readOnly = true)
    public String getConfigValueWithDefault(String configKey, String defaultValue, Long tenantId, Long userId) {
        String value = getConfigValue(configKey, tenantId, userId);
        return value != null ? value : defaultValue;
    }

    private ConfigResponse toConfigResponse(RuntimeConfig config) {
        ConfigResponse response = new ConfigResponse();
        response.setId((Long) config.getId());
        response.setConfigKey(config.getConfigKey());
        response.setConfigValue(config.getIsEncrypted() ? "***ENCRYPTED***" : config.getConfigValue());
        response.setDefaultValue(config.getDefaultValue());
        response.setConfigType(config.getConfigType().name());
        response.setConfigScope(config.getConfigScope().name());
        response.setTenantId(config.getTenantId());
        response.setUserId(config.getUserId());
        response.setIsEncrypted(config.getIsEncrypted());
        response.setIsReadonly(config.getIsReadonly());
        response.setDescription(config.getDescription());
        response.setVersion(config.getVersion());
        response.setCreatedAt(config.getCreatedAt());
        response.setUpdatedAt(config.getUpdatedAt());
        return response;
    }
}
