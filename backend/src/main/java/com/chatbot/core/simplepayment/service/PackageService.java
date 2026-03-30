package com.chatbot.core.simplepayment.service;

import com.chatbot.core.simplepayment.model.Package;
import com.chatbot.core.simplepayment.repository.PackageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PackageService {

    private final PackageRepository packageRepository;

    /**
     * Get all active packages ordered by sort order
     */
    @Transactional(readOnly = true)
    public List<Package> getActivePackages() {
        log.debug("Fetching all active packages");
        return packageRepository.findActivePackagesOrdered();
    }

    /**
     * Get all packages (including inactive) for admin
     */
    @Transactional(readOnly = true)
    public List<Package> getAllPackages() {
        log.debug("Fetching all packages for admin");
        return packageRepository.findAllByOrderBySortOrderAsc();
    }

    /**
     * Get package by ID
     */
    @Transactional(readOnly = true)
    public Optional<Package> getPackageById(Long id) {
        log.debug("Fetching package by ID: {}", id);
        return packageRepository.findById(id);
    }

    /**
     * Get package by package ID (e.g., 'free', 'pro')
     */
    @Transactional(readOnly = true)
    public Optional<Package> getPackageByPackageId(String packageId) {
        log.debug("Fetching package by package ID: {}", packageId);
        return packageRepository.findByPackageId(packageId);
    }

    /**
     * Create new package
     */
    @Transactional
    public Package createPackage(Package packageData) {
        log.info("Creating new package: {}", packageData.getPackageId());
        
        // Check if package ID already exists
        if (packageRepository.existsByPackageId(packageData.getPackageId())) {
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
        log.info("✅ Created package: {}", savedPackage.getPackageId());
        return savedPackage;
    }

    /**
     * Update existing package
     */
    @Transactional
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
        log.info("✅ Updated package: {}", updatedPackage.getPackageId());
        return updatedPackage;
    }

    /**
     * Delete package (soft delete by setting inactive)
     */
    @Transactional
    public void deletePackage(Long id) {
        log.info("Deleting package ID: {}", id);
        
        Package packageData = packageRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Package not found: " + id));

        packageData.setIsActive(false);
        packageRepository.save(packageData);
        
        log.info("✅ Soft deleted package: {}", packageData.getPackageId());
    }

    /**
     * Permanently delete package
     */
    @Transactional
    public void permanentlyDeletePackage(Long id) {
        log.info("Permanently deleting package ID: {}", id);
        
        Package packageData = packageRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Package not found: " + id));

        packageRepository.delete(packageData);
        log.info("✅ Permanently deleted package: {}", packageData.getPackageId());
    }

    /**
     * Check if packages table is empty (for seeding)
     */
    @Transactional(readOnly = true)
    public boolean isEmpty() {
        return !packageRepository.hasAnyPackages();
    }

    /**
     * Initialize default packages if table is empty
     */
    @Transactional
    public void initializeDefaultPackages() {
        if (!isEmpty()) {
            log.info("Packages already initialized, skipping seeding");
            return;
        }

        log.info("Initializing default packages...");
        createDefaultPackages();
    }
    
    /**
     * Force reinitialize packages (delete all and recreate)
     */
    @Transactional
    public void forceReinitializePackages() {
        log.info("🔄 Force reinitializing packages - deleting existing packages...");
        packageRepository.deleteAll();
        
        log.info("Creating new packages with English text...");
        createDefaultPackages();
    }
    
    /**
     * Create default packages
     */
    @Transactional
    private void createDefaultPackages() {
        
        // Free Package
        Package freePackage = Package.builder()
                .packageId("free")
                .name("Free")
                .price(BigDecimal.ZERO)
                .currency("VND")
                .duration("1 month")
                .description("Trial package")
                .messageLimit(100)
                .chatbotLimit(1)
                .hasPrioritySupport(false)
                .hasAnalytics(false)
                .hasAdvancedAnalytics(false)
                .hasCustomIntegrations(false)
                .hasDedicatedSupport(false)
                .hasCustomFeatures(false)
                .hasSlaGuarantee(false)
                .isActive(true)
                .sortOrder(1)
                .badge(null)
                .build();

        // Pro Package
        Package proPackage = Package.builder()
                .packageId("pro")
                .name("Pro")
                .price(new BigDecimal("250000"))
                .currency("VND")
                .duration("1 month")
                .description("Professional package")
                .messageLimit(5000)
                .chatbotLimit(3)
                .hasPrioritySupport(true)
                .hasAnalytics(true)
                .hasAdvancedAnalytics(false)
                .hasCustomIntegrations(false)
                .hasDedicatedSupport(false)
                .hasCustomFeatures(false)
                .hasSlaGuarantee(false)
                .isActive(true)
                .sortOrder(2)
                .badge("POPULAR")
                .build();

        // Business Package
        Package businessPackage = Package.builder()
                .packageId("business")
                .name("Business")
                .price(new BigDecimal("500000"))
                .currency("VND")
                .duration("1 month")
                .description("Business package")
                .messageLimit(15000)
                .chatbotLimit(10)
                .hasPrioritySupport(true)
                .hasAnalytics(true)
                .hasAdvancedAnalytics(true)
                .hasCustomIntegrations(true)
                .hasDedicatedSupport(true)
                .hasCustomFeatures(false)
                .hasSlaGuarantee(false)
                .isActive(true)
                .sortOrder(3)
                .badge(null)
                .build();

        // Enterprise Package
        Package enterprisePackage = Package.builder()
                .packageId("enterprise")
                .name("Enterprise")
                .price(new BigDecimal("1000000"))
                .currency("VND")
                .duration("1 month")
                .description("Enterprise package")
                .messageLimit(Integer.MAX_VALUE)
                .chatbotLimit(Integer.MAX_VALUE)
                .hasPrioritySupport(true)
                .hasAnalytics(true)
                .hasAdvancedAnalytics(true)
                .hasCustomIntegrations(true)
                .hasDedicatedSupport(true)
                .hasCustomFeatures(true)
                .hasSlaGuarantee(true)
                .isActive(true)
                .sortOrder(4)
                .badge(null)
                .build();

        // Save all packages
        packageRepository.saveAll(List.of(freePackage, proPackage, businessPackage, enterprisePackage));
        
        log.info("✅ Initialized {} default packages", 4);
    }
}
