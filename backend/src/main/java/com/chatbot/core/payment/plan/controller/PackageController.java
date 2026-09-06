package com.chatbot.core.payment.plan.controller;

import com.chatbot.core.payment.plan.dto.PackageResponse;
import com.chatbot.core.payment.plan.model.Package;
import com.chatbot.core.payment.plan.service.PackageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payment/packages")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Package Management", description = "Package management endpoints")
public class PackageController {

    private final PackageService packageService;

    /**
     * Get all active packages
     */
    @GetMapping("/active")
    @Operation(
        summary = "Get active packages",
        description = "Get all active packages ordered by sort order"
    )
    public ResponseEntity<List<PackageResponse>> getActivePackages() {
        log.info("📦 Fetching active packages");
        
        List<Package> packages = packageService.getActivePackages();
        List<PackageResponse> responses = packages.stream()
                .map(PackageResponse::from)
                .toList();
        
        return ResponseEntity.ok(responses);
    }

    /**
     * Get package by packageId
     */
    @GetMapping("/{packageId}")
    @Operation(
        summary = "Get package by ID",
        description = "Get a specific package by its package ID"
    )
    public ResponseEntity<PackageResponse> getPackage(@PathVariable String packageId) {
        log.info("📦 Fetching package: {}", packageId);
        
        return packageService.getPackageByPackageId(packageId)
                .map(PackageResponse::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get all packages (including inactive) - Admin only
     */
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Get all packages (Admin)",
        description = "Get all packages including inactive ones - Admin only"
    )
    public ResponseEntity<List<PackageResponse>> getAllPackages() {
        log.info("📦 Fetching all packages (Admin)");
        
        List<Package> packages = packageService.getAllPackages();
        List<PackageResponse> responses = packages.stream()
                .map(PackageResponse::from)
                .toList();
        
        return ResponseEntity.ok(responses);
    }

    /**
     * Create new package - Admin only
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Create package (Admin)",
        description = "Create a new package - Admin only"
    )
    public ResponseEntity<PackageResponse> createPackage(@RequestBody Package packageEntity) {
        log.info("📦 Creating new package: {}", packageEntity.getPackageId());
        
        try {
            Package created = packageService.createPackage(packageEntity);
            return ResponseEntity.ok(PackageResponse.from(created));
        } catch (Exception e) {
            log.error("❌ Failed to create package: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Update package - Admin only
     */
    @PutMapping("/{packageId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Update package (Admin)",
        description = "Update an existing package - Admin only"
    )
    public ResponseEntity<PackageResponse> updatePackage(
            @PathVariable String packageId,
            @RequestBody Package packageEntity) {
        log.info("📦 Updating package: {}", packageId);
        
        try {
            Package updated = packageService.updatePackage(packageId, packageEntity);
            return ResponseEntity.ok(PackageResponse.from(updated));
        } catch (Exception e) {
            log.error("❌ Failed to update package: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Delete package - Admin only
     */
    @DeleteMapping("/{packageId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Delete package (Admin)",
        description = "Delete a package - Admin only"
    )
    public ResponseEntity<Map<String, String>> deletePackage(@PathVariable String packageId) {
        log.info("🗑️ Deleting package: {}", packageId);
        
        try {
            packageService.deletePackage(packageId);
            return ResponseEntity.ok(Map.of("message", "Package deleted successfully"));
        } catch (Exception e) {
            log.error("❌ Failed to delete package: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Clear package cache - Admin only
     */
    @PostMapping("/cache/clear")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Clear package cache (Admin)",
        description = "Clear package cache - Admin only"
    )
    public ResponseEntity<Map<String, String>> clearCache() {
        log.info("🧹 Clearing package cache");
        
        packageService.clearCache();
        return ResponseEntity.ok(Map.of("message", "Cache cleared successfully"));
    }
}
