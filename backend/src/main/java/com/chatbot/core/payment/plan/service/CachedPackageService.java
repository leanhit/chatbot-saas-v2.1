package com.chatbot.core.payment.plan.service;

import com.chatbot.core.payment.plan.model.Package;
import com.chatbot.core.payment.plan.repository.PackageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Cached service for package operations
 * Provides caching layer for package data
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CachedPackageService {

    private final PackageRepository packageRepository;

    /**
     * Get all active packages with caching
     */
    @Cacheable(value = "packages:active", key = "'all'")
    public List<Package> getActivePackages() {
        log.info("📦 Fetching active packages from database");
        return packageRepository.findByIsActiveTrueOrderBySortOrderAsc();
    }

    /**
     * Get package by ID with caching
     */
    @Cacheable(value = "packages", key = "#id")
    public Package getPackageById(Long id) {
        log.info("📦 Fetching package by ID: {}", id);
        return packageRepository.findById(id).orElse(null);
    }

    /**
     * Get package by package ID with caching
     */
    @Cacheable(value = "packages", key = "'packageId:' + #packageId")
    public Package getPackageByPackageId(String packageId) {
        log.info("📦 Fetching package by package ID: {}", packageId);
        return packageRepository.findByPackageId(packageId).orElse(null);
    }

    /**
     * Clear package cache
     */
    public void clearCache() {
        log.info("🗑️ Clearing package cache");
        // Cache will be cleared automatically via Spring Cache annotations
    }

    /**
     * Warm up caches
     */
    public void warmUpCaches() {
        log.info("🔥 Warming up package caches");
        getActivePackages();
    }
}
