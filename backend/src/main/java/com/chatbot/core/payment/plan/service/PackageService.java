package com.chatbot.core.payment.plan.service;

import com.chatbot.core.payment.plan.model.Package;
import com.chatbot.core.payment.plan.repository.PackageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PackageService {

    private final PackageRepository packageRepository;

    /**
     * Get all active packages
     */
    @Transactional(readOnly = true, transactionManager = "paymentTransactionManager")
    @Cacheable(value = "packages", key = "'all-active'")
    public List<Package> getActivePackages() {
        log.info("📦 Fetching all active packages");
        return packageRepository.findByIsActiveTrueOrderBySortOrderAsc();
    }

    /**
     * Get package by packageId
     */
    @Transactional(readOnly = true, transactionManager = "paymentTransactionManager")
    @Cacheable(value = "packages", key = "#packageId")
    public Optional<Package> getPackageByPackageId(String packageId) {
        log.debug("📦 Fetching package: {}", packageId);
        return packageRepository.findByPackageId(packageId);
    }

    /**
     * Get all packages (including inactive)
     */
    @Transactional(readOnly = true, transactionManager = "paymentTransactionManager")
    public List<Package> getAllPackages() {
        return packageRepository.findAllByOrderBySortOrderAsc();
    }

    /**
     * Create new package
     */
    @Transactional(transactionManager = "paymentTransactionManager")
    @CacheEvict(value = "packages", allEntries = true)
    public Package createPackage(Package packageEntity) {
        log.info("📦 Creating new package: {}", packageEntity.getPackageId());
        
        if (packageRepository.existsByPackageId(packageEntity.getPackageId())) {
            throw new RuntimeException("Package ID already exists: " + packageEntity.getPackageId());
        }
        
        return packageRepository.save(packageEntity);
    }

    /**
     * Update package
     */
    @Transactional(transactionManager = "paymentTransactionManager")
    @CacheEvict(value = "packages", allEntries = true)
    public Package updatePackage(String packageId, Package packageEntity) {
        log.info("📦 Updating package: {}", packageId);
        
        Package existing = packageRepository.findByPackageId(packageId)
                .orElseThrow(() -> new RuntimeException("Package not found: " + packageId));
        
        // Update fields
        existing.setName(packageEntity.getName());
        existing.setPrice(packageEntity.getPrice());
        existing.setCurrency(packageEntity.getCurrency());
        existing.setDuration(packageEntity.getDuration());
        existing.setDescription(packageEntity.getDescription());
        existing.setMessageLimit(packageEntity.getMessageLimit());
        existing.setChatbotLimit(packageEntity.getChatbotLimit());
        existing.setHasPrioritySupport(packageEntity.getHasPrioritySupport());
        existing.setHasAnalytics(packageEntity.getHasAnalytics());
        existing.setHasAdvancedAnalytics(packageEntity.getHasAdvancedAnalytics());
        existing.setHasCustomIntegrations(packageEntity.getHasCustomIntegrations());
        existing.setHasDedicatedSupport(packageEntity.getHasDedicatedSupport());
        existing.setHasCustomFeatures(packageEntity.getHasCustomFeatures());
        existing.setHasSlaGuarantee(packageEntity.getHasSlaGuarantee());
        existing.setIsActive(packageEntity.getIsActive());
        existing.setSortOrder(packageEntity.getSortOrder());
        existing.setBadge(packageEntity.getBadge());
        
        return packageRepository.save(existing);
    }

    /**
     * Delete package
     */
    @Transactional(transactionManager = "paymentTransactionManager")
    @CacheEvict(value = "packages", allEntries = true)
    public void deletePackage(String packageId) {
        log.info("🗑️ Deleting package: {}", packageId);
        packageRepository.deleteByPackageId(packageId);
    }

    /**
     * Permanently delete package
     */
    @Transactional(transactionManager = "paymentTransactionManager")
    @CacheEvict(value = "packages", allEntries = true)
    public void permanentlyDeletePackage(String packageId) {
        log.info("🗑️ Permanently deleting package: {}", packageId);
        packageRepository.deleteByPackageId(packageId);
    }

    /**
     * Check if default packages are initialized
     */
    @Transactional(readOnly = true, transactionManager = "paymentTransactionManager")
    public boolean checkInitialized() {
        return packageRepository.count() > 0;
    }

    /**
     * Initialize default packages if empty
     */
    @Transactional(transactionManager = "paymentTransactionManager")
    @CacheEvict(value = "packages", allEntries = true)
    public List<Package> initializeDefaultPackages() {
        log.info("📦 Initializing default packages");
        if (packageRepository.count() > 0) {
            return packageRepository.findAllByOrderBySortOrderAsc();
        }

        List<Package> defaults = List.of(
            Package.builder()
                .packageId("free")
                .name("Free")
                .price(java.math.BigDecimal.ZERO)
                .currency("VND")
                .duration("month")
                .description("Gói miễn phí cho người mới sử dụng")
                .messageLimit(500)
                .chatbotLimit(1)
                .hasPrioritySupport(false)
                .hasAnalytics(true)
                .hasAdvancedAnalytics(false)
                .hasCustomIntegrations(false)
                .hasDedicatedSupport(false)
                .hasCustomFeatures(false)
                .hasSlaGuarantee(false)
                .isActive(true)
                .sortOrder(1)
                .build(),
            Package.builder()
                .packageId("pro")
                .name("Pro")
                .price(new java.math.BigDecimal("290000"))
                .currency("VND")
                .duration("month")
                .description("Gói chuyên nghiệp cho doanh nghiệp vừa và nhỏ")
                .messageLimit(10000)
                .chatbotLimit(5)
                .hasPrioritySupport(true)
                .hasAnalytics(true)
                .hasAdvancedAnalytics(true)
                .hasCustomIntegrations(false)
                .hasDedicatedSupport(false)
                .hasCustomFeatures(false)
                .hasSlaGuarantee(false)
                .isActive(true)
                .sortOrder(2)
                .badge("POPULAR")
                .build(),
            Package.builder()
                .packageId("business")
                .name("Business")
                .price(new java.math.BigDecimal("790000"))
                .currency("VND")
                .duration("month")
                .description("Gói doanh nghiệp đầy đủ tính năng mở rộng")
                .messageLimit(50000)
                .chatbotLimit(20)
                .hasPrioritySupport(true)
                .hasAnalytics(true)
                .hasAdvancedAnalytics(true)
                .hasCustomIntegrations(true)
                .hasDedicatedSupport(true)
                .hasCustomFeatures(false)
                .hasSlaGuarantee(true)
                .isActive(true)
                .sortOrder(3)
                .badge("RECOMMENDED")
                .build()
        );

        return packageRepository.saveAll(defaults);
    }

    /**
     * Clear cache
     */
    @CacheEvict(value = "packages", allEntries = true)
    public void clearCache() {
        log.info("🧹 Clearing package cache");
    }
}
