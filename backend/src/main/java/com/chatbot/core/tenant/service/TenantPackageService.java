package com.chatbot.core.tenant.service;

import com.chatbot.core.tenant.model.Tenant;
import com.chatbot.core.tenant.repository.TenantRepository;
import com.chatbot.core.simplepayment.service.PackageService;
import com.chatbot.core.simplepayment.model.Package;
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

    /**
     * Assign default free package to a new tenant
     */
    @Transactional
    public void assignDefaultPackageToTenant(Tenant tenant) {
        log.info("🎁 [TenantPackageService] Assigning default package to tenant: {}", tenant.getTenantKey());
        
        try {
            // Get free package
            Package freePackage = packageService.getPackageByPackageId("free")
                    .orElseThrow(() -> new RuntimeException("Free package not found in database"));
            
            // Assign free package to tenant
            tenant.setCurrentPackageId("free");
            tenant.setPackageActivatedAt(LocalDateTime.now());
            
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
        log.info("⬆️ [TenantPackageService] Upgrading tenant {} to package: {}", tenantId, packageId);
        
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found: " + tenantId));
        
        Package newPackage = packageService.getPackageByPackageId(packageId)
                .orElseThrow(() -> new RuntimeException("Package not found: " + packageId));
        
        String oldPackageId = tenant.getCurrentPackageId();
        
        log.info("🔄 [TenantPackageService] Current package: {}, New package: {}", oldPackageId, packageId);
        
        // Update tenant package
        tenant.setCurrentPackageId(packageId);
        tenant.setPackageActivatedAt(LocalDateTime.now());
        
        log.info("💾 [TenantPackageService] Saving tenant with new package...");
        tenantRepository.save(tenant);
        
        log.info("✅ [TenantPackageService] Tenant {} upgraded from {} to {} at {}", 
                tenant.getTenantKey(), oldPackageId, packageId, tenant.getPackageActivatedAt());
    }

    /**
     * Get current package for tenant
     */
    @Transactional(readOnly = true)
    public Package getCurrentTenantPackage(Long tenantId) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found: " + tenantId));
        
        if (tenant.getCurrentPackageId() == null) {
            log.warn("⚠️ [TenantPackageService] Tenant {} has no package assigned", tenant.getTenantKey());
            return null;
        }
        
        return packageService.getPackageByPackageId(tenant.getCurrentPackageId())
                .orElse(null);
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
