package com.chatbot.core.simplepayment.service;

import com.chatbot.core.simplepayment.config.PackageConfig;
import com.chatbot.core.simplepayment.config.PackageConfigLoader;
import com.chatbot.core.simplepayment.model.Package;
import com.chatbot.core.simplepayment.repository.PackageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PackageService {

    private final PackageRepository packageRepository;
    private final PackageConfigLoader packageConfigLoader;
    private final CachedPackageService cachedPackageService;

    /**
     * Get all active packages ordered by sort order
     */
    @Transactional(readOnly = true, transactionManager = "sharedTransactionManager")
    public List<Package> getActivePackages() {
        log.debug("Fetching all active packages (cached)");
        return cachedPackageService.getActivePackages();
    }

    /**
     * Get all packages (including inactive) for admin
     */
    @Transactional(readOnly = true, transactionManager = "sharedTransactionManager")
    public List<Package> getAllPackages() {
        log.debug("Fetching all packages for admin (cached)");
        return cachedPackageService.getAllPackages();
    }

    /**
     * Get package by ID
     */
    @Transactional(readOnly = true, transactionManager = "sharedTransactionManager")
    public Optional<Package> getPackageById(Long id) {
        log.debug("Fetching package by ID: {} (cached)", id);
        return cachedPackageService.getPackageById(id);
    }

    /**
     * Get package by package ID (e.g., 'free', 'pro')
     */
    @Transactional(readOnly = true, transactionManager = "sharedTransactionManager")
    public Optional<Package> getPackageByPackageId(String packageId) {
        log.debug("Fetching package by package ID: {} (cached)", packageId);
        return cachedPackageService.getPackageByPackageId(packageId);
    }

    /**
     * Create new package
     */
    @Transactional("sharedTransactionManager")
    public Package createPackage(Package packageData) {
        log.info("Creating new package: {}", packageData.getPackageId());
        
        // Check if package ID already exists
        if (cachedPackageService.existsByPackageId(packageData.getPackageId())) {
            throw new IllegalArgumentException("Package ID already exists: " + packageData.getPackageId());
        }

        // Set default values
        if (packageData.getIsActive() == null) {
            packageData.setIsActive(true);
        }
        if (packageData.getSortOrder() == null) {
            packageData.setSortOrder((int) (packageRepository.count() + 1));
        }
        if (packageData.getCurrency() == null) {
            packageData.setCurrency("VND");
        }

        Package savedPackage = packageRepository.save(packageData);
        
        // Clear caches after creation
        cachedPackageService.clearAllPackageCache();
        
        log.info("✅ Created package: {}", savedPackage.getPackageId());
        return savedPackage;
    }

    /**
     * Update existing package
     */
    @Transactional("sharedTransactionManager")
    public Package updatePackage(Long id, Package packageData) {
        log.info("Updating package ID: {}", id);
        
        Package existingPackage = packageRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Package not found: " + id));

        // Update fields
        existingPackage.setName(packageData.getName());
        existingPackage.setPrice(packageData.getPrice());
        existingPackage.setCurrency(packageData.getCurrency());
        existingPackage.setDuration(packageData.getDuration());
        existingPackage.setDescription(packageData.getDescription());
        existingPackage.setMessageLimit(packageData.getMessageLimit());
        existingPackage.setChatbotLimit(packageData.getChatbotLimit());
        existingPackage.setHasPrioritySupport(packageData.getHasPrioritySupport());
        existingPackage.setHasAnalytics(packageData.getHasAnalytics());
        existingPackage.setHasAdvancedAnalytics(packageData.getHasAdvancedAnalytics());
        existingPackage.setHasCustomIntegrations(packageData.getHasCustomIntegrations());
        existingPackage.setHasDedicatedSupport(packageData.getHasDedicatedSupport());
        existingPackage.setHasCustomFeatures(packageData.getHasCustomFeatures());
        existingPackage.setHasSlaGuarantee(packageData.getHasSlaGuarantee());
        existingPackage.setIsActive(packageData.getIsActive());
        existingPackage.setSortOrder(packageData.getSortOrder());
        existingPackage.setBadge(packageData.getBadge());

        Package updatedPackage = packageRepository.save(existingPackage);
        
        // Clear caches after update
        cachedPackageService.clearPackageCache(id);
        cachedPackageService.clearPackageCacheByPackageId(updatedPackage.getPackageId());
        
        log.info("✅ Updated package: {}", updatedPackage.getPackageId());
        return updatedPackage;
    }

    /**
     * Delete package (soft delete by setting inactive)
     */
    @Transactional("sharedTransactionManager")
    public void deletePackage(Long id) {
        log.info("Deleting package ID: {}", id);
        
        Package packageData = packageRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Package not found: " + id));

        packageData.setIsActive(false);
        packageRepository.save(packageData);
        
        // Clear caches after deletion
        cachedPackageService.clearPackageCache(id);
        cachedPackageService.clearPackageCacheByPackageId(packageData.getPackageId());
        cachedPackageService.clearActivePackagesCache();
        
        log.info("✅ Soft deleted package: {}", packageData.getPackageId());
    }

    /**
     * Permanently delete package
     */
    @Transactional("sharedTransactionManager")
    public void permanentlyDeletePackage(Long id) {
        log.info("Permanently deleting package ID: {}", id);
        
        Package packageData = packageRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Package not found: " + id));

        packageRepository.delete(packageData);
        
        // Clear all caches after permanent deletion
        cachedPackageService.clearAllPackageCache();
        
        log.info("✅ Permanently deleted package: {}", packageData.getPackageId());
    }

    /**
     * Check if packages table is empty (for seeding)
     */
    @Transactional(readOnly = true, transactionManager = "sharedTransactionManager")
    public boolean isEmpty() {
        return !packageRepository.hasAnyPackages();
    }

    /**
     * Initialize default packages if table is empty
     */
    @Transactional("sharedTransactionManager")
    public void initializeDefaultPackages() {
        if (!isEmpty()) {
            log.info("Packages already initialized, skipping seeding");
            return;
        }

        log.info("Initializing default packages...");
        createDefaultPackages();
        
        // Warm up caches after initialization
        cachedPackageService.warmUpCaches();
    }
    
    /**
     * Force reinitialize packages (delete all and recreate)
     */
    @Transactional("sharedTransactionManager")
    public void forceReinitializePackages() {
        log.info("🔄 Force reinitializing packages - deleting existing packages...");
        packageRepository.deleteAll();
        
        log.info("Creating new packages with English text...");
        createDefaultPackages();
        
        // Warm up caches after reinitialization
        cachedPackageService.warmUpCaches();
    }
    
    /**
     * Create default packages from configuration
     */
    @Transactional("sharedTransactionManager")
    private void createDefaultPackages() {
        log.info("📦 Creating packages from configuration...");
        
        Map<String, PackageConfig.PackageDefinition> packageConfigs = packageConfigLoader.getPackages();
        if (packageConfigs == null || packageConfigs.isEmpty()) {
            log.warn("⚠️ No package configuration found, skipping package creation");
            return;
        }

        List<Package> packages = packageConfigs.entrySet().stream()
            .map(entry -> createPackageFromConfig(entry.getKey(), entry.getValue()))
            .toList();

        // Save all packages
        packageRepository.saveAll(packages);
        
        log.info("✅ Created {} packages from configuration", packages.size());
    }

    /**
     * Create Package entity from configuration
     */
    private Package createPackageFromConfig(String packageId, PackageConfig.PackageDefinition config) {
        log.debug("Creating package {} from config", packageId);
        
        return Package.builder()
                .packageId(packageId)
                .name(config.getName())
                .price(BigDecimal.valueOf(config.getPrice()))
                .currency(config.getCurrency())
                .duration(config.getDuration())
                .description(config.getDescription())
                .messageLimit(config.getMessageLimit())
                .chatbotLimit(config.getChatbotLimit())
                .hasPrioritySupport(config.isHasPrioritySupport())
                .hasAnalytics(config.isHasAnalytics())
                .hasAdvancedAnalytics(config.isHasAdvancedAnalytics())
                .hasCustomIntegrations(config.isHasCustomIntegrations())
                .hasDedicatedSupport(config.isHasDedicatedSupport())
                .hasCustomFeatures(config.isHasCustomFeatures())
                .hasSlaGuarantee(config.isHasSlaGuarantee())
                .isActive(true) // Force active for all default packages
                .sortOrder(config.getSortOrder())
                .badge(config.getBadge())
                .build();
    }

    /**
     * Warm up package caches
     */
    public void warmupCache() {
        cachedPackageService.warmUpCaches();
    }

    /**
     * Clear package caches
     */
    public void clearCache() {
        cachedPackageService.clearAllPackageCache();
    }

    /**
     * Get cache statistics
     */
    public String getCacheStats() {
        return cachedPackageService.getCacheStats();
    }
}
