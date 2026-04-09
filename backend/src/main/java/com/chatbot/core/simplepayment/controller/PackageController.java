package com.chatbot.core.simplepayment.controller;

import com.chatbot.core.simplepayment.model.Package;
import com.chatbot.core.simplepayment.service.PackageService;
import com.chatbot.shared.constants.ApiConstants;
import com.chatbot.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(ApiConstants.BASE_PATH + "/packages")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Package Management", description = "API for managing subscription packages")
public class PackageController {

    private final PackageService packageService;

    /**
     * Get all active packages (public endpoint)
     */
    @GetMapping("/active")
    @Operation(summary = "Get all active packages", description = "Retrieve all active packages ordered by sort order")
    public ResponseEntity<ApiResponse<List<Package>>> getActivePackages() {
        log.info("📦 Fetching active packages");
        List<Package> packages = packageService.getActivePackages();
        return ResponseEntity.ok(ApiResponse.success(packages, "Active packages retrieved successfully"));
    }

    /**
     * Get all packages (admin only)
     */
    @GetMapping
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "Get all packages", description = "Retrieve all packages including inactive ones (Admin only)")
    public ResponseEntity<ApiResponse<List<Package>>> getAllPackages() {
        log.info("🔧 Admin fetching all packages");
        List<Package> packages = packageService.getAllPackages();
        return ResponseEntity.ok(ApiResponse.success(packages, "All packages retrieved successfully"));
    }

    /**
     * Get package by ID (admin only)
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "Get package by ID", description = "Retrieve a specific package by ID (Admin only)")
    public ResponseEntity<ApiResponse<Package>> getPackageById(@PathVariable Long id) {
        log.info("🔧 Admin fetching package ID: {}", id);
        return packageService.getPackageById(id)
                .map(pkg -> ResponseEntity.ok(ApiResponse.success(pkg, "Package retrieved successfully")))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get package by package ID (public endpoint)
     */
    @GetMapping("/by-package-id/{packageId}")
    @Operation(summary = "Get package by package ID", description = "Retrieve a specific package by package ID (e.g., 'free', 'pro')")
    public ResponseEntity<ApiResponse<Package>> getPackageByPackageId(@PathVariable String packageId) {
        log.info("📦 Fetching package by package ID: {}", packageId);
        return packageService.getPackageByPackageId(packageId)
                .map(pkg -> ResponseEntity.ok(ApiResponse.success(pkg, "Package retrieved successfully")))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Create new package (admin only)
     */
    @PostMapping
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "Create new package", description = "Create a new subscription package (Admin only)")
    public ResponseEntity<ApiResponse<Package>> createPackage(@Valid @RequestBody Package packageData) {
        log.info("🔧 Admin creating new package: {}", packageData.getPackageId());
        try {
            Package createdPackage = packageService.createPackage(packageData);
            return ResponseEntity.ok(ApiResponse.success(createdPackage, "Package created successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Update existing package (admin only)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "Update package", description = "Update an existing subscription package (Admin only)")
    public ResponseEntity<ApiResponse<Package>> updatePackage(
            @PathVariable Long id, 
            @Valid @RequestBody Package packageData) {
        log.info("🔧 Admin updating package ID: {}", id);
        try {
            Package updatedPackage = packageService.updatePackage(id, packageData);
            return ResponseEntity.ok(ApiResponse.success(updatedPackage, "Package updated successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Delete package (soft delete - admin only)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "Delete package", description = "Soft delete a package (Admin only)")
    public ResponseEntity<ApiResponse<Void>> deletePackage(@PathVariable Long id) {
        log.info("🔧 Admin deleting package ID: {}", id);
        try {
            packageService.deletePackage(id);
            return ResponseEntity.ok(ApiResponse.success(null, "Package deleted successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Permanently delete package (admin only)
     */
    @DeleteMapping("/{id}/permanent")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "Permanently delete package", description = "Permanently delete a package (Admin only)")
    public ResponseEntity<ApiResponse<Void>> permanentlyDeletePackage(@PathVariable Long id) {
        log.info("🔧 Admin permanently deleting package ID: {}", id);
        try {
            packageService.permanentlyDeletePackage(id);
            return ResponseEntity.ok(ApiResponse.success(null, "Package permanently deleted successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Initialize default packages (admin only)
     */
    @PostMapping("/initialize")
    @Operation(summary = "Initialize default packages", description = "Initialize default packages if table is empty (Admin only)")
    public ResponseEntity<ApiResponse<Void>> initializeDefaultPackages() {
        log.info("🔧 Admin initializing default packages");
        packageService.initializeDefaultPackages();
        return ResponseEntity.ok(ApiResponse.success(null, "Default packages initialized successfully"));
    }

    /**
     * Force reinitialize packages (public endpoint for development)
     */
    @PostMapping("/force-reinitialize")
    @Operation(summary = "Force reinitialize packages", description = "Force reinitialize packages with English text (Development only)")
    public ResponseEntity<ApiResponse<Void>> forceReinitializePackages() {
        log.info("🔄 Force reinitializing packages with English text");
        packageService.forceReinitializePackages();
        return ResponseEntity.ok(ApiResponse.success(null, "Packages force reinitialized successfully"));
    }

    /**
     * Check if packages are initialized (admin only)
     */
    @GetMapping("/check-initialized")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "Check packages initialization", description = "Check if packages table is empty (Admin only)")
    public ResponseEntity<ApiResponse<Boolean>> checkInitialized() {
        log.info("🔧 Admin checking packages initialization");
        boolean isEmpty = packageService.isEmpty();
        return ResponseEntity.ok(ApiResponse.success(isEmpty, "Packages initialization status"));
    }

    /**
     * Warm up package caches (admin only)
     */
    @PostMapping("/warmup-cache")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "Warm up package caches", description = "Warm up package caches for performance (Admin only)")
    public ResponseEntity<ApiResponse<String>> warmupCache() {
        log.info("🔥 Admin warming up package caches");
        try {
            packageService.warmupCache();
            return ResponseEntity.ok(ApiResponse.success("Cache warmed up successfully", "Package caches warmed up"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to warm up cache: " + e.getMessage()));
        }
    }

    /**
     * Clear package caches (admin only)
     */
    @PostMapping("/clear-cache")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "Clear package caches", description = "Clear all package caches (Admin only)")
    public ResponseEntity<ApiResponse<String>> clearCache() {
        log.info("🗑️ Admin clearing package caches");
        try {
            packageService.clearCache();
            return ResponseEntity.ok(ApiResponse.success("Cache cleared successfully", "Package caches cleared"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to clear cache: " + e.getMessage()));
        }
    }

    /**
     * Get cache statistics (admin only)
     */
    @GetMapping("/cache-stats")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "Get cache statistics", description = "Get package cache statistics (Admin only)")
    public ResponseEntity<ApiResponse<String>> getCacheStats() {
        log.info("📊 Admin getting cache statistics");
        try {
            String stats = packageService.getCacheStats();
            return ResponseEntity.ok(ApiResponse.success(stats, "Cache statistics retrieved"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to get cache stats: " + e.getMessage()));
        }
    }
}
