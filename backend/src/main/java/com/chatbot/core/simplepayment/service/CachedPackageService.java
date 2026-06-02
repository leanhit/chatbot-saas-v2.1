package com.chatbot.core.simplepayment.service;

import com.chatbot.core.simplepayment.model.Package;
import com.chatbot.core.simplepayment.repository.PackageRepository;
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
public class CachedPackageService {

    private final PackageRepository packageRepository;
    private static final String PACKAGE_CACHE = "packages";
    private static final String ACTIVE_PACKAGES_CACHE = "activePackages";

    /**
     * Get all active packages ordered by sort order (cached)
     */
    @Cacheable(value = ACTIVE_PACKAGES_CACHE, key = "'all'")
    @Transactional(readOnly = true, transactionManager = "sharedTransactionManager")
    public List<Package> getActivePackages() {
        log.debug("🔄 Fetching active packages from database (cache miss)");
        return packageRepository.findActivePackagesOrdered();
    }

    /**
     * Get all packages including inactive (cached)
     */
    @Cacheable(value = PACKAGE_CACHE, key = "'all'")
    @Transactional(readOnly = true, transactionManager = "sharedTransactionManager")
    public List<Package> getAllPackages() {
        log.debug("🔄 Fetching all packages from database (cache miss)");
        return packageRepository.findAllByOrderBySortOrderAsc();
    }

    /**
     * Get package by ID (cached)
     */
    @Cacheable(value = PACKAGE_CACHE, key = "#id")
    @Transactional(readOnly = true, transactionManager = "sharedTransactionManager")
    public Optional<Package> getPackageById(Long id) {
        log.debug("🔄 Fetching package by ID {} from database (cache miss)", id);
        return packageRepository.findById(id);
    }

    /**
     * Get package by package ID (cached)
     */
    @Cacheable(value = PACKAGE_CACHE, key = "'packageId:' + #packageId")
    @Transactional(readOnly = true, transactionManager = "sharedTransactionManager")
    public Optional<Package> getPackageByPackageId(String packageId) {
        log.debug("🔄 Fetching package by package ID {} from database (cache miss)", packageId);
        return packageRepository.findByPackageId(packageId);
    }

    /**
     * Check if package exists by package ID (cached)
     */
    @Cacheable(value = PACKAGE_CACHE, key = "'exists:' + #packageId")
    @Transactional(readOnly = true, transactionManager = "sharedTransactionManager")
    public boolean existsByPackageId(String packageId) {
        log.debug("🔄 Checking if package {} exists (cache miss)", packageId);
        return packageRepository.existsByPackageId(packageId);
    }

    /**
     * Clear all package caches
     */
    @CacheEvict(value = {PACKAGE_CACHE, ACTIVE_PACKAGES_CACHE}, allEntries = true)
    public void clearAllPackageCache() {
        log.info("🗑️ Cleared all package caches");
    }

    /**
     * Clear active packages cache
     */
    @CacheEvict(value = ACTIVE_PACKAGES_CACHE, allEntries = true)
    public void clearActivePackagesCache() {
        log.info("🗑️ Cleared active packages cache");
    }

    /**
     * Clear specific package cache
     */
    @CacheEvict(value = PACKAGE_CACHE, key = "#id")
    public void clearPackageCache(Long id) {
        log.info("🗑️ Cleared cache for package ID: {}", id);
    }

    /**
     * Clear package cache by package ID
     */
    @CacheEvict(value = PACKAGE_CACHE, key = "'packageId:' + #packageId")
    public void clearPackageCacheByPackageId(String packageId) {
        log.info("🗑️ Cleared cache for package ID: {}", packageId);
    }

    /**
     * Warm up package caches
     */
    @Transactional(readOnly = true, transactionManager = "sharedTransactionManager")
    public void warmUpCaches() {
        log.info("🔥 Warming up package caches...");
        
        try {
            // Warm up active packages cache
            getActivePackages();
            
            // Warm up all packages cache
            getAllPackages();
            
            log.info("✅ Package caches warmed up successfully");
        } catch (Exception e) {
            log.error("❌ Failed to warm up package caches: {}", e.getMessage(), e);
        }
    }

    /**
     * Get cache statistics
     */
    public String getCacheStats() {
        // This would require a cache manager to get actual statistics
        // For now, return a simple status
        return "Package caches are active for: packages, activePackages";
    }
}
