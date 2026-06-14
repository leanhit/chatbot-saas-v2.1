package com.chatbot.core.simplepayment.service;

import com.chatbot.core.simplepayment.model.SystemConfig;
import com.chatbot.core.simplepayment.repository.SystemConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SystemConfigService {

    private final SystemConfigRepository systemConfigRepository;
    private final PaymentAuditService paymentAuditService;

    /**
     * Get config value by key
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "systemConfig", key = "#configKey")
    public Optional<String> getConfigValue(String configKey) {
        return systemConfigRepository.findByConfigKey(configKey)
                .map(SystemConfig::getConfigValue);
    }

    /**
     * Get config by key
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "systemConfig", key = "#configKey")
    public Optional<SystemConfig> getConfig(String configKey) {
        return systemConfigRepository.findByConfigKey(configKey);
    }

    /**
     * Get all configs by category
     */
    @Transactional(readOnly = true)
    public List<SystemConfig> getConfigsByCategory(String category) {
        return systemConfigRepository.findByConfigCategory(category);
    }

    /**
     * Get all non-sensitive configs by category
     */
    @Transactional(readOnly = true)
    public List<SystemConfig> getNonSensitiveConfigsByCategory(String category) {
        return systemConfigRepository.findNonSensitiveByCategory(category);
    }

    /**
     * Create or update config
     */
    @Transactional("sharedTransactionManager")
    @CacheEvict(value = "systemConfig", key = "#config.configKey")
    public SystemConfig saveConfig(SystemConfig config, String updatedBy) {
        log.info("💾 Saving config: {} = {}", config.getConfigKey(), 
                config.getIsSensitive() ? "***" : config.getConfigValue());

        Optional<SystemConfig> existing = systemConfigRepository.findByConfigKey(config.getConfigKey());
        
        if (existing.isPresent()) {
            SystemConfig existingConfig = existing.get();
            
            // Log audit for sensitive config changes
            if (config.getIsSensitive()) {
                paymentAuditService.logConfigChange(
                    config.getConfigKey(),
                    existingConfig.getConfigValue(),
                    config.getConfigValue(),
                    updatedBy
                );
            }
            
            // Update existing config
            existingConfig.setConfigValue(config.getConfigValue());
            existingConfig.setDescription(config.getDescription());
            existingConfig.setConfigType(config.getConfigType());
            existingConfig.setIsSensitive(config.getIsSensitive());
            existingConfig.setIsEncrypted(config.getIsEncrypted());
            existingConfig.setUpdatedBy(updatedBy);
            
            return systemConfigRepository.save(existingConfig);
        } else {
            // Create new config
            config.setUpdatedBy(updatedBy);
            SystemConfig saved = systemConfigRepository.save(config);
            
            // Log audit for new sensitive config
            if (config.getIsSensitive()) {
                paymentAuditService.logConfigChange(
                    config.getConfigKey(),
                    null,
                    config.getConfigValue(),
                    updatedBy
                );
            }
            
            return saved;
        }
    }

    /**
     * Delete config by key
     */
    @Transactional("sharedTransactionManager")
    @CacheEvict(value = "systemConfig", key = "#configKey")
    public void deleteConfig(String configKey, String updatedBy) {
        log.info("🗑️ Deleting config: {}", configKey);
        
        Optional<SystemConfig> config = systemConfigRepository.findByConfigKey(configKey);
        if (config.isPresent()) {
            // Log audit before deletion
            if (config.get().getIsSensitive()) {
                paymentAuditService.logConfigChange(
                    configKey,
                    config.get().getConfigValue(),
                    null,
                    updatedBy
                );
            }
            
            systemConfigRepository.delete(config.get());
        }
    }

    /**
     * Get bank configuration as map
     */
    @Transactional(readOnly = true)
    public Map<String, String> getBankConfig() {
        List<SystemConfig> configs = getConfigsByCategory("BANK");
        
        return configs.stream()
                .collect(java.util.stream.Collectors.toMap(
                    SystemConfig::getConfigKey,
                    SystemConfig::getConfigValue
                ));
    }

    /**
     * Save bank configuration from BankInfo DTO
     */
    @Transactional("sharedTransactionManager")
    public void saveBankConfig(QRCodeService.BankInfo bankInfo, String updatedBy) {
        log.info("💾 Saving bank configuration by: {}", updatedBy);

        // Save bank account info
        saveConfig(SystemConfig.builder()
                .configKey("payment.bank.name")
                .configValue(bankInfo.getBankName())
                .configCategory("BANK")
                .configType("STRING")
                .description("Bank name for payment")
                .isSensitive(false)
                .build(), updatedBy);

        saveConfig(SystemConfig.builder()
                .configKey("payment.bank.account-number")
                .configValue(bankInfo.getAccountNumber())
                .configCategory("BANK")
                .configType("STRING")
                .description("Bank account number")
                .isSensitive(false)
                .build(), updatedBy);

        saveConfig(SystemConfig.builder()
                .configKey("payment.bank.account-name")
                .configValue(bankInfo.getAccountName())
                .configCategory("BANK")
                .configType("STRING")
                .description("Bank account name")
                .isSensitive(false)
                .build(), updatedBy);

        // Save bank API config
        if (bankInfo.getProvider() != null) {
            saveConfig(SystemConfig.builder()
                    .configKey("payment.bank-api.provider")
                    .configValue(bankInfo.getProvider())
                    .configCategory("BANK")
                    .configType("STRING")
                    .description("Bank API provider (mock, vietqr, etc.)")
                    .isSensitive(false)
                    .build(), updatedBy);
        }

        if (bankInfo.getApiUrl() != null) {
            saveConfig(SystemConfig.builder()
                    .configKey("payment.bank-api.api-url")
                    .configValue(bankInfo.getApiUrl())
                    .configCategory("BANK")
                    .configType("STRING")
                    .description("Bank API URL")
                    .isSensitive(false)
                    .build(), updatedBy);
        }

        if (bankInfo.getApiKey() != null) {
            saveConfig(SystemConfig.builder()
                    .configKey("payment.bank-api.api-key")
                    .configValue(bankInfo.getApiKey())
                    .configCategory("BANK")
                    .configType("STRING")
                    .description("Bank API key")
                    .isSensitive(true)
                    .isEncrypted(true)
                    .build(), updatedBy);
        }

        if (bankInfo.getTimeout() != null) {
            saveConfig(SystemConfig.builder()
                    .configKey("payment.bank-api.timeout")
                    .configValue(bankInfo.getTimeout().toString())
                    .configCategory("BANK")
                    .configType("INTEGER")
                    .description("Bank API timeout in milliseconds")
                    .isSensitive(false)
                    .build(), updatedBy);
        }

        if (bankInfo.getRetryAttempts() != null) {
            saveConfig(SystemConfig.builder()
                    .configKey("payment.bank-api.retry-attempts")
                    .configValue(bankInfo.getRetryAttempts().toString())
                    .configCategory("BANK")
                    .configType("INTEGER")
                    .description("Bank API retry attempts")
                    .isSensitive(false)
                    .build(), updatedBy);
        }

        if (bankInfo.getRetryDelay() != null) {
            saveConfig(SystemConfig.builder()
                    .configKey("payment.bank-api.retry-delay")
                    .configValue(bankInfo.getRetryDelay().toString())
                    .configCategory("BANK")
                    .configType("INTEGER")
                    .description("Bank API retry delay in milliseconds")
                    .isSensitive(false)
                    .build(), updatedBy);
        }

        log.info("✅ Bank configuration saved successfully");
    }

    /**
     * Get bank configuration as BankInfo DTO
     */
    @Transactional(readOnly = true)
    public QRCodeService.BankInfo getBankInfo() {
        Map<String, String> config = getBankConfig();
        
        QRCodeService.BankInfo bankInfo = new QRCodeService.BankInfo();
        bankInfo.setBankName(config.getOrDefault("payment.bank.name", "Vietcombank"));
        bankInfo.setAccountNumber(config.getOrDefault("payment.bank.account-number", "1234567890"));
        bankInfo.setAccountName(config.getOrDefault("payment.bank.account-name", "CHATBOT SaaS"));
        
        // Bank API config
        bankInfo.setProvider(config.get("payment.bank-api.provider"));
        bankInfo.setApiUrl(config.get("payment.bank-api.api-url"));
        bankInfo.setApiKey(config.get("payment.bank-api.api-key"));
        
        if (config.containsKey("payment.bank-api.timeout")) {
            try {
                bankInfo.setTimeout(Integer.parseInt(config.get("payment.bank-api.timeout")));
            } catch (NumberFormatException e) {
                bankInfo.setTimeout(30000);
            }
        }
        
        if (config.containsKey("payment.bank-api.retry-attempts")) {
            try {
                bankInfo.setRetryAttempts(Integer.parseInt(config.get("payment.bank-api.retry-attempts")));
            } catch (NumberFormatException e) {
                bankInfo.setRetryAttempts(3);
            }
        }
        
        if (config.containsKey("payment.bank-api.retry-delay")) {
            try {
                bankInfo.setRetryDelay(Integer.parseInt(config.get("payment.bank-api.retry-delay")));
            } catch (NumberFormatException e) {
                bankInfo.setRetryDelay(1000);
            }
        }
        
        return bankInfo;
    }

    /**
     * Initialize default bank configuration if not exists
     */
    @Transactional("sharedTransactionManager")
    public void initializeDefaultBankConfig() {
        log.info("🔧 Initializing default bank configuration");
        
        if (!systemConfigRepository.existsByConfigKey("payment.bank.name")) {
            saveConfig(SystemConfig.builder()
                    .configKey("payment.bank.name")
                    .configValue("Vietcombank")
                    .configCategory("BANK")
                    .configType("STRING")
                    .description("Default bank name")
                    .isSensitive(false)
                    .build(), "SYSTEM");
        }
        
        if (!systemConfigRepository.existsByConfigKey("payment.bank.account-number")) {
            saveConfig(SystemConfig.builder()
                    .configKey("payment.bank.account-number")
                    .configValue("1234567890")
                    .configCategory("BANK")
                    .configType("STRING")
                    .description("Default bank account number")
                    .isSensitive(false)
                    .build(), "SYSTEM");
        }
        
        if (!systemConfigRepository.existsByConfigKey("payment.bank.account-name")) {
            saveConfig(SystemConfig.builder()
                    .configKey("payment.bank.account-name")
                    .configValue("CHATBOT SaaS")
                    .configCategory("BANK")
                    .configType("STRING")
                    .description("Default bank account name")
                    .isSensitive(false)
                    .build(), "SYSTEM");
        }
        
        // Bank API defaults
        if (!systemConfigRepository.existsByConfigKey("payment.bank-api.provider")) {
            saveConfig(SystemConfig.builder()
                    .configKey("payment.bank-api.provider")
                    .configValue("mock")
                    .configCategory("BANK")
                    .configType("STRING")
                    .description("Default bank API provider")
                    .isSensitive(false)
                    .build(), "SYSTEM");
        }
        
        if (!systemConfigRepository.existsByConfigKey("payment.bank-api.api-url")) {
            saveConfig(SystemConfig.builder()
                    .configKey("payment.bank-api.api-url")
                    .configValue("http://localhost:3000/mock-bank")
                    .configCategory("BANK")
                    .configType("STRING")
                    .description("Default bank API URL")
                    .isSensitive(false)
                    .build(), "SYSTEM");
        }
        
        if (!systemConfigRepository.existsByConfigKey("payment.bank-api.api-key")) {
            saveConfig(SystemConfig.builder()
                    .configKey("payment.bank-api.api-key")
                    .configValue("dev-mock-key-12345")
                    .configCategory("BANK")
                    .configType("STRING")
                    .description("Default bank API key")
                    .isSensitive(true)
                    .isEncrypted(true)
                    .build(), "SYSTEM");
        }
        
        if (!systemConfigRepository.existsByConfigKey("payment.bank-api.timeout")) {
            saveConfig(SystemConfig.builder()
                    .configKey("payment.bank-api.timeout")
                    .configValue("30000")
                    .configCategory("BANK")
                    .configType("INTEGER")
                    .description("Default bank API timeout")
                    .isSensitive(false)
                    .build(), "SYSTEM");
        }
        
        if (!systemConfigRepository.existsByConfigKey("payment.bank-api.retry-attempts")) {
            saveConfig(SystemConfig.builder()
                    .configKey("payment.bank-api.retry-attempts")
                    .configValue("3")
                    .configCategory("BANK")
                    .configType("INTEGER")
                    .description("Default bank API retry attempts")
                    .isSensitive(false)
                    .build(), "SYSTEM");
        }
        
        if (!systemConfigRepository.existsByConfigKey("payment.bank-api.retry-delay")) {
            saveConfig(SystemConfig.builder()
                    .configKey("payment.bank-api.retry-delay")
                    .configValue("1000")
                    .configCategory("BANK")
                    .configType("INTEGER")
                    .description("Default bank API retry delay")
                    .isSensitive(false)
                    .build(), "SYSTEM");
        }
        
        log.info("✅ Default bank configuration initialized");
    }
}
