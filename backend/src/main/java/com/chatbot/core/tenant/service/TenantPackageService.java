package com.chatbot.core.tenant.service;

import com.chatbot.core.tenant.dto.TenantPackageInfo;
import com.chatbot.core.tenant.model.Tenant;
import com.chatbot.core.tenant.repository.TenantRepository;
import com.chatbot.core.simplepayment.service.PackageService;
import com.chatbot.core.simplepayment.model.Package;
import com.chatbot.core.simplepayment.repository.PackageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class TenantPackageService {

    private final TenantRepository tenantRepository;
    private final PackageService packageService;
    private final PackageRepository packageRepository;

    /**
     * Assign default free package to a new tenant
     */
    @Transactional
    public void assignDefaultPackageToTenant(Tenant tenant) {
        log.info("🎁 [TenantPackageService] Assigning default package to tenant: {}", tenant.getTenantKey());
        
        try {
            // Get free package
            Package freePackage = packageRepository.findByPackageId("free")
                    .orElseThrow(() -> new RuntimeException("Free package not found in database"));
            
            // Assign free package to tenant
            tenant.setCurrentPackageId("free");
            tenant.setPackageActivatedAt(LocalDateTime.now());
            tenant.setExpiresAt(null); // Free packages have no expiration
            
            tenantRepository.save(tenant);
            
            log.info("✅ [TenantPackageService] Assigned free package to tenant: {} at {}", 
                    tenant.getTenantKey(), tenant.getPackageActivatedAt());
                    
        } catch (Exception e) {
            log.error("❌ [TenantPackageService] Failed to assign default package to tenant {}: {}", 
                    tenant.getTenantKey(), e.getMessage(), e);
            throw new RuntimeException("Failed to assign default package: " + e.getMessage(), e);
        }
    }

    /**
     * Upgrade tenant to a specific package
     */
    @Transactional
    public void upgradeTenantPackage(Long tenantId, String packageId) {
        log.info(" [TenantPackageService] Upgrading tenant {} to package: {}", tenantId, packageId);
        
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found: " + tenantId));
        
        Package newPackage = packageRepository.findByPackageId(packageId)
                .orElseThrow(() -> new RuntimeException("Package not found: " + packageId));
        
        String oldPackageId = tenant.getCurrentPackageId();
        
        log.info(" [TenantPackageService] Current package: {}, New package: {}", oldPackageId, packageId);
        
        // Update tenant package
        tenant.setCurrentPackageId(packageId);
        tenant.setPackageActivatedAt(LocalDateTime.now());
        
        // Calculate and set expiration date
        if (newPackage.isFree()) {
            // Free packages have no expiration
            tenant.setExpiresAt(null);
            log.info(" [TenantPackageService] Free package assigned - no expiration date");
        } else {
            // Calculate expiration for paid packages with time accumulation
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime currentExpiration = tenant.getExpiresAt();
            LocalDateTime baseDate;
            
            // Check if tenant has valid existing expiration
            if (currentExpiration != null && currentExpiration.isAfter(now)) {
                // Extend from existing expiration (accumulate time)
                baseDate = currentExpiration;
                log.info(" [TenantPackageService] Extending existing expiration from: {}", baseDate);
            } else {
                // Start new expiration from now
                baseDate = now;
                log.info(" [TenantPackageService] Starting new expiration from: {}", baseDate);
            }
            
            LocalDateTime expirationDate = calculateExpirationDate(baseDate, newPackage.getDuration());
            
            // Optional: Prevent abuse by limiting maximum duration to 2 years
            LocalDateTime maxExpiration = now.plusYears(2);
            if (expirationDate.isAfter(maxExpiration)) {
                expirationDate = maxExpiration;
                log.warn(" [TenantPackageService] Expiration capped at maximum 2 years: {}", expirationDate);
            }
            
            tenant.setExpiresAt(expirationDate);
            log.info(" [TenantPackageService] Paid package assigned - new expires at: {} (accumulated from: {})", 
                    expirationDate, baseDate);
        }
        
        log.info(" [TenantPackageService] Saving tenant with new package...");
        tenantRepository.save(tenant);
        
        log.info(" [TenantPackageService] Tenant {} upgraded from {} to {} at {}", 
                tenant.getTenantKey(), oldPackageId, packageId, tenant.getPackageActivatedAt());
    }

    /**
     * Calculate expiration date from package duration string
     */
    private LocalDateTime calculateExpirationDate(LocalDateTime startDate, String duration) {
        if (startDate == null || duration == null) {
            return null;
        }
        
        // Parse duration string from simplepayment packages
        // Examples: "1 month", "3 months", "6 months", "12 months"
        String lowerDuration = duration.toLowerCase();
        if (lowerDuration.contains("month")) {
            String[] parts = lowerDuration.split(" ");
            int months = Integer.parseInt(parts[0]);
            return startDate.plusMonths(months);
        } else if (lowerDuration.contains("year")) {
            String[] parts = lowerDuration.split(" ");
            int years = Integer.parseInt(parts[0]);
            return startDate.plusYears(years);
        }
        
        return null;
    }

    /**
     * Test method to verify time accumulation logic (for debugging)
     */
    public void testTimeAccumulation(Long tenantId) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found: " + tenantId));
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime currentExpiration = tenant.getExpiresAt();
        
        log.info("=== Time Accumulation Test for Tenant {} ===", tenantId);
        log.info("Current time: {}", now);
        log.info("Current expiration: {}", currentExpiration);
        
        if (currentExpiration != null && currentExpiration.isAfter(now)) {
            long daysRemaining = java.time.Duration.between(now, currentExpiration).toDays();
            log.info("Days remaining: {}", daysRemaining);
            
            // Simulate adding 3 months
            LocalDateTime simulatedExpiration = calculateExpirationDate(currentExpiration, "3 months");
            log.info("After adding 3 months: {}", simulatedExpiration);
            
            long newDaysRemaining = java.time.Duration.between(now, simulatedExpiration).toDays();
            log.info("New days remaining: {}", newDaysRemaining);
            log.info("Additional days added: {}", newDaysRemaining - daysRemaining);
        } else {
            log.info("No valid expiration - would start fresh from now");
            LocalDateTime freshExpiration = calculateExpirationDate(now, "3 months");
            log.info("Fresh 3-month expiration: {}", freshExpiration);
        }
        log.info("=== End Test ===");
    }

    /**
     * Get current package info with expiration details for tenant
     */
    @Transactional(readOnly = true)
    public TenantPackageInfo getCurrentTenantPackageInfo(Long tenantId) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found: " + tenantId));
        
        Package currentPackage = null;
        if (tenant.getCurrentPackageId() != null) {
            currentPackage = packageService.getPackageByPackageId(tenant.getCurrentPackageId())
                    .orElse(null);
        }
        
        return TenantPackageInfo.from(tenantId, currentPackage, tenant.getPackageActivatedAt(), tenant.getExpiresAt());
    }
    
    /**
     * Get current package info with expiration details by tenant key
     */
    @Transactional(readOnly = true)
    public TenantPackageInfo getCurrentTenantPackageInfoByKey(String tenantKey) {
        Tenant tenant = tenantRepository.findByTenantKey(tenantKey)
                .orElseThrow(() -> new RuntimeException("Tenant not found: " + tenantKey));
        
        Package currentPackage = null;
        if (tenant.getCurrentPackageId() != null) {
            currentPackage = packageService.getPackageByPackageId(tenant.getCurrentPackageId())
                    .orElse(null);
        }
        
        return TenantPackageInfo.from(tenant.getId(), currentPackage, tenant.getPackageActivatedAt(), tenant.getExpiresAt());
    }

    /**
     * Get current package for tenant
     */
    @Transactional(readOnly = true)
    public Package getCurrentTenantPackage(Long tenantId) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found: " + tenantId));
        
        if (tenant.getCurrentPackageId() == null) {
            log.warn(" [TenantPackageService] Tenant {} has no package assigned", tenant.getTenantKey());
            return null;
        }
        
        try {
            return packageService.getPackageByPackageId(tenant.getCurrentPackageId())
                    .orElse(null);
        } catch (Exception e) {
            log.error("Error getting package by packageId {}, trying direct repository access: {}", 
                    tenant.getCurrentPackageId(), e.getMessage());
            
            // Fallback: direct repository access to avoid caching issues
            return packageRepository.findByPackageId(tenant.getCurrentPackageId())
                    .orElse(null);
        }
    }

    /**
     * Get current package for tenant by tenant key
     */
    @Transactional(readOnly = true)
    public Package getCurrentTenantPackageByKey(String tenantKey) {
        Tenant tenant = tenantRepository.findByTenantKey(tenantKey)
                .orElseThrow(() -> new RuntimeException("Tenant not found: " + tenantKey));
        
        if (tenant.getCurrentPackageId() == null) {
            log.warn("⚠️ [TenantPackageService] Tenant {} has no package assigned", tenantKey);
            return null;
        }
        
        return packageService.getPackageByPackageId(tenant.getCurrentPackageId())
                .orElse(null);
    }

    /**
     * Check if tenant has specific package
     */
    @Transactional(readOnly = true)
    public boolean hasTenantPackage(Long tenantId, String packageId) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found: " + tenantId));
        
        return packageId.equals(tenant.getCurrentPackageId());
    }

    /**
     * Initialize all existing tenants without packages to free package
     */
    @Transactional
    public void initializeExistingTenants() {
        log.info("🔄 [TenantPackageService] Initializing existing tenants without packages");
        
        int updatedCount = 0;
        var tenantsWithoutPackage = tenantRepository.findByCurrentPackageIdIsNull();
        
        for (Tenant tenant : tenantsWithoutPackage) {
            try {
                assignDefaultPackageToTenant(tenant);
                updatedCount++;
            } catch (Exception e) {
                log.error("❌ [TenantPackageService] Failed to initialize tenant {}: {}", 
                        tenant.getTenantKey(), e.getMessage());
            }
        }
        
        log.info("✅ [TenantPackageService] Initialized {} tenants with free package", updatedCount);
    }

    /**
     * Custom query to find tenants without packages
     */
    private java.util.List<Tenant> findByCurrentPackageIdIsNull() {
        return tenantRepository.findAll().stream()
                .filter(t -> t.getCurrentPackageId() == null)
                .toList();
    }
}
