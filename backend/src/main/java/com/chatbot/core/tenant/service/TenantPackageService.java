package com.chatbot.core.tenant.service;

import com.chatbot.core.tenant.dto.TenantPackageInfo;
import com.chatbot.core.tenant.dto.TenantPackageDetailResponse;
import com.chatbot.shared.exceptions.ResourceNotFoundException;
import com.chatbot.core.tenant.exception.TenantNotFoundException;
import com.chatbot.core.tenant.exception.BusinessLogicException;
import com.chatbot.core.tenant.model.Tenant;
import com.chatbot.core.tenant.repository.TenantRepository;
import com.chatbot.core.payment.plan.model.Package;
import com.chatbot.core.payment.plan.repository.PackageRepository;
import com.chatbot.core.cache.CacheService;
import com.chatbot.shared.constants.CacheConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.chatbot.core.user.model.User;
import com.chatbot.core.user.repository.AuthRepository;
import com.chatbot.shared.security.SecurityUtils;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class TenantPackageService {

    private final TenantRepository tenantRepository;
    private final PackageRepository packageRepository;
    private final AuthRepository authRepository;
    private final TenantAuditLogService auditLogService;
    private final CacheService cacheService;

    @Value("${tenant.default.package:free}")
    private String defaultPackageId;

    @Value("${tenant.package.max-duration-years:10}")
    private int maxDurationYears;

    /**
     * Gán gói mặc định (free) cho tenant mới.
     */
    @Transactional(value = "tenantTransactionManager", rollbackFor = Exception.class)
    public void assignDefaultPackageToTenant(Tenant tenant) {
        log.info("[TenantPackageService] Assigning default package '{}' to tenant: {}",
                defaultPackageId, tenant.getTenantKey());

        Package freePackage = packageRepository.findByPackageId(defaultPackageId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Default package not found in database: " + defaultPackageId));

        tenant.setCurrentPackageId(defaultPackageId);
        tenant.setPackageActivatedAt(LocalDateTime.now());
        tenant.setExpiresAt(null); // Free package không có hạn

        tenantRepository.save(tenant);

        invalidateTenantPackageCache(tenant.getId(), tenant.getTenantKey());

        log.info("[TenantPackageService] Assigned '{}' package to tenant: {} at {}",
                defaultPackageId, tenant.getTenantKey(), tenant.getPackageActivatedAt());
    }

    /**
     * Nâng cấp tenant lên gói cụ thể.
     */
    @Transactional(value = "tenantTransactionManager", rollbackFor = Exception.class)
    public void upgradeTenantPackage(Long tenantId, String packageId) {
        log.info("[TenantPackageService] Upgrading tenant {} to package: {}", tenantId, packageId);

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new TenantNotFoundException("Tenant not found: " + tenantId));

        log.debug("📝 [DEBUG] Before upgrade - tenantId: {}, currentPackageId: {}, targetPackageId: {}",
                tenantId, tenant.getCurrentPackageId(), packageId);

        Package newPackage = packageRepository.findByPackageId(packageId)
                .orElseThrow(() -> new ResourceNotFoundException("Package not found: " + packageId));

        String oldPackageId = tenant.getCurrentPackageId();

        tenant.setCurrentPackageId(packageId);
        tenant.setPackageActivatedAt(LocalDateTime.now());

        if (newPackage.isFree()) {
            tenant.setExpiresAt(null);
        } else {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime currentExpiration = tenant.getExpiresAt();
            LocalDateTime baseDate = (currentExpiration != null && currentExpiration.isAfter(now))
                    ? currentExpiration : now;

            LocalDateTime expirationDate = calculateExpirationDate(baseDate, newPackage.getDuration());

            // Cap tối đa maxDurationYears năm
            LocalDateTime maxExpiration = now.plusYears(maxDurationYears);
            if (expirationDate != null && expirationDate.isAfter(maxExpiration)) {
                expirationDate = maxExpiration;
                log.warn("[TenantPackageService] Expiration capped at {} years: {}", maxDurationYears, expirationDate);
            }

            tenant.setExpiresAt(expirationDate);
        }

        tenantRepository.save(tenant);

        invalidateTenantPackageCache(tenantId, tenant.getTenantKey());

        log.debug("📝 [DEBUG] After save - tenantId: {}, currentPackageId: {}, expiresAt: {}",
                tenantId, tenant.getCurrentPackageId(), tenant.getExpiresAt());

        String actor = SecurityUtils.getCurrentUserId().orElse("system");
        auditLogService.logAction(tenantId, actor, "UPGRADE_PACKAGE",
            "Package changed from '" + oldPackageId + "' to '" + packageId +
            "', new expiry=" + tenant.getExpiresAt());

        log.info("[TenantPackageService] Tenant {} upgraded: {} → {} | expires={}",
                tenant.getTenantKey(), oldPackageId, packageId, tenant.getExpiresAt());
    }

    /**
     * Tính ngày hết hạn từ chuỗi duration.
     * Hỗ trợ: "1 month", "3 months", "6 months", "12 months", "1 year", "2 years".
     */
    private LocalDateTime calculateExpirationDate(LocalDateTime startDate, String duration) {
        if (startDate == null || duration == null) throw new BusinessLogicException("Invalid duration: null");

        String lower = duration.toLowerCase().trim();
        String[] parts = lower.split("\\s+");
        if (parts.length < 2) {
            log.warn("[TenantPackageService] Cannot parse duration string: '{}'", duration);
            throw new BusinessLogicException("Cannot parse duration string: " + duration);
        }

        try {
            int amount = Integer.parseInt(parts[0]);
            if (lower.contains("month")) return startDate.plusMonths(amount);
            if (lower.contains("year"))  return startDate.plusYears(amount);
        } catch (NumberFormatException e) {
            log.warn("[TenantPackageService] Invalid duration number in: '{}'", duration);
            throw new BusinessLogicException("Invalid duration number in: " + duration);
        }
        throw new BusinessLogicException("Unsupported duration format: " + duration);
    }

    /**
     * Fallback: tìm tenantId từ header hoặc từ user.
     */
    @Transactional(readOnly = true, transactionManager = "tenantTransactionManager")
    public Long extractTenantIdWithFallback(String tenantKey, String username) {
        if (tenantKey != null && !tenantKey.isBlank()) {
            try {
                return tenantRepository.findByTenantKey(tenantKey)
                        .map(Tenant::getId)
                        .orElseThrow(() -> new TenantNotFoundException("Tenant not found: " + tenantKey));
            } catch (Exception e) {
                log.warn("[TenantPackageService] Cannot resolve tenant from key {}: {}", tenantKey, e.getMessage());
            }
        }

        if (username != null) {
            try {
                User user = authRepository.findByEmail(username)
                        .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
                return tenantRepository.findByUserId(user.getId())
                        .map(Tenant::getId)
                        .orElseThrow(() -> new TenantNotFoundException("No tenant for user: " + user.getId()));
            } catch (Exception e) {
                log.warn("[TenantPackageService] Cannot resolve tenant from user {}: {}", username, e.getMessage());
            }
        }

        throw new TenantNotFoundException(com.chatbot.shared.exceptions.ErrorCode.TENANT_ID_NOT_FOUND, "Tenant ID not found in header or user context");
    }

    /**
     * Lấy thông tin gói dịch vụ hiện tại của tenant (dạng Map cho API response).
     */
    @Transactional(readOnly = true, transactionManager = "tenantTransactionManager")
    public TenantPackageDetailResponse getMyPackageInfo(Long tenantId) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new TenantNotFoundException("Tenant not found"));

        PackageExpirationResult result = resolvePackageWithExpiration(tenant.getCurrentPackageId(), tenant.getExpiresAt());
        Package currentPackage = resolvePackage(result.packageId);

        return TenantPackageDetailResponse.builder()
                .tenantId(tenantId)
                .tenantKey(tenant.getTenantKey())
                .currentPackageId(result.packageId)
                .packageActivatedAt(tenant.getPackageActivatedAt())
                .expiresAt(result.expiresAt)
                .packageName(currentPackage.getName())
                .packagePrice(currentPackage.getPrice())
                .packageCurrency(currentPackage.getCurrency())
                .packageDuration(currentPackage.getDuration())
                .chatbotLimit(currentPackage.getChatbotLimit())
                .messageLimit(currentPackage.getMessageLimit())
                .build();
    }

    /**
     * Lấy thông tin gói dịch vụ kèm ngày hết hạn — dùng trong TenantPackageInfo DTO.
     */
    @Transactional(readOnly = true, transactionManager = "tenantTransactionManager")
    public TenantPackageInfo getCurrentTenantPackageInfo(Long tenantId) {
        String cacheKey = CacheConstants.Tenant.TENANT_PACKAGE_INFO + tenantId;
        
        TenantPackageInfo cached = cacheService.get(cacheKey, TenantPackageInfo.class);
        if (cached != null) {
            log.debug("[TenantPackageService] Cache hit for tenant package info: {}", tenantId);
            return cached;
        }
        
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new TenantNotFoundException("Tenant not found: " + tenantId));
        TenantPackageInfo result = buildPackageInfoWithExpirationCheck(tenant);
        
        cacheService.set(cacheKey, result, Duration.ofSeconds(CacheConstants.TTL.TENANT_PACKAGE));
        log.debug("[TenantPackageService] Cached tenant package info: {}", tenantId);
        
        return result;
    }

    @Transactional(readOnly = true, transactionManager = "tenantTransactionManager")
    public TenantPackageInfo getCurrentTenantPackageInfoByKey(String tenantKey) {
        String cacheKey = CacheConstants.Tenant.TENANT_PACKAGE_INFO + "key:" + tenantKey;
        
        TenantPackageInfo cached = cacheService.get(cacheKey, TenantPackageInfo.class);
        if (cached != null) {
            log.debug("[TenantPackageService] Cache hit for tenant package info by key: {}", tenantKey);
            return cached;
        }
        
        Tenant tenant = tenantRepository.findByTenantKey(tenantKey)
                .orElseThrow(() -> new TenantNotFoundException("Tenant not found: " + tenantKey));
        TenantPackageInfo result = buildPackageInfoWithExpirationCheck(tenant);
        
        cacheService.set(cacheKey, result, Duration.ofSeconds(CacheConstants.TTL.TENANT_PACKAGE));
        log.debug("[TenantPackageService] Cached tenant package info by key: {}", tenantKey);
        
        return result;
    }

    @Transactional(readOnly = true, transactionManager = "tenantTransactionManager")
    public Package getCurrentTenantPackage(Long tenantId) {
        String cacheKey = CacheConstants.Tenant.TENANT_PACKAGE + tenantId;
        
        Package cached = cacheService.get(cacheKey, Package.class);
        if (cached != null) {
            log.debug("[TenantPackageService] Cache hit for tenant package: {}", tenantId);
            return cached;
        }
        
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new TenantNotFoundException("Tenant not found: " + tenantId));
        Package result = resolveCurrentPackageForTenant(tenant);
        
        cacheService.set(cacheKey, result, Duration.ofSeconds(CacheConstants.TTL.TENANT_PACKAGE));
        log.debug("[TenantPackageService] Cached tenant package: {}", tenantId);
        
        return result;
    }

    @Transactional(readOnly = true, transactionManager = "tenantTransactionManager")
    public Package getCurrentTenantPackageByKey(String tenantKey) {
        String cacheKey = CacheConstants.Tenant.TENANT_PACKAGE + "key:" + tenantKey;
        
        Package cached = cacheService.get(cacheKey, Package.class);
        if (cached != null) {
            log.debug("[TenantPackageService] Cache hit for tenant package by key: {}", tenantKey);
            return cached;
        }
        
        Tenant tenant = tenantRepository.findByTenantKey(tenantKey)
                .orElseThrow(() -> new TenantNotFoundException("Tenant not found: " + tenantKey));
        Package result = resolveCurrentPackageForTenant(tenant);
        
        cacheService.set(cacheKey, result, Duration.ofSeconds(CacheConstants.TTL.TENANT_PACKAGE));
        log.debug("[TenantPackageService] Cached tenant package by key: {}", tenantKey);
        
        return result;
    }

    @Transactional(readOnly = true, transactionManager = "tenantTransactionManager")
    public boolean hasTenantPackage(Long tenantId, String packageId) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new TenantNotFoundException("Tenant not found: " + tenantId));
        return packageId.equals(tenant.getCurrentPackageId());
    }

    /**
     * Khởi tạo gói mặc định cho tất cả tenant chưa có gói.
     */
    @Transactional("tenantTransactionManager")
    public void initializeExistingTenants() {
        log.info("[TenantPackageService] Initializing tenants without packages in batch...");
        try {
            int updatedCount = tenantRepository.initializeTenantsWithDefaultPackage(defaultPackageId, LocalDateTime.now());
            log.info("[TenantPackageService] Initialized {} tenants with default package '{}'", updatedCount, defaultPackageId);
        } catch (Exception e) {
            log.error("[TenantPackageService] Failed to batch initialize tenants: {}", e.getMessage(), e);
            throw e;
        }
    }

    /* ================= PRIVATE HELPERS ================= */

    /**
     * Invalidate tenant package cache for both ID and key lookups.
     */
    private void invalidateTenantPackageCache(Long tenantId, String tenantKey) {
        if (tenantId != null) {
            String infoKey = CacheConstants.Tenant.TENANT_PACKAGE_INFO + tenantId;
            String packageKey = CacheConstants.Tenant.TENANT_PACKAGE + tenantId;
            cacheService.delete(infoKey);
            cacheService.delete(packageKey);
            log.debug("[TenantPackageService] Invalidated cache for tenant ID: {}", tenantId);
        }
        if (tenantKey != null && !tenantKey.isBlank()) {
            String infoKey = CacheConstants.Tenant.TENANT_PACKAGE_INFO + "key:" + tenantKey;
            String packageKey = CacheConstants.Tenant.TENANT_PACKAGE + "key:" + tenantKey;
            cacheService.delete(infoKey);
            cacheService.delete(packageKey);
            log.debug("[TenantPackageService] Invalidated cache for tenant key: {}", tenantKey);
        }
    }

    /**
     * Result holder for package resolution with expiration check.
     */
    private static record PackageExpirationResult(String packageId, LocalDateTime expiresAt) {}

    /**
     * Resolve package ID and expiration date, handling expiration fallback to default.
     */
    private PackageExpirationResult resolvePackageWithExpiration(String packageId, LocalDateTime expiresAt) {
        if (expiresAt != null && expiresAt.isBefore(LocalDateTime.now())) {
            return new PackageExpirationResult(defaultPackageId, null);
        }
        return new PackageExpirationResult(packageId, expiresAt);
    }

    /**
     * Resolve the current package for a tenant, handling expiration and fallback to default.
     */
    private Package resolveCurrentPackageForTenant(Tenant tenant) {
        if (tenant.getCurrentPackageId() == null) {
            log.warn("[TenantPackageService] Tenant {} has no package assigned, falling back to default package '{}'", tenant.getTenantKey(), defaultPackageId);
            return resolvePackage(defaultPackageId);
        }
        if (tenant.getExpiresAt() != null && tenant.getExpiresAt().isBefore(LocalDateTime.now())) {
            return resolvePackage(defaultPackageId);
        }
        return resolvePackage(tenant.getCurrentPackageId());
    }

    /**
     * Build package info with expiration check for a tenant.
     */
    private TenantPackageInfo buildPackageInfoWithExpirationCheck(Tenant tenant) {
        Package currentPackage;
        LocalDateTime expiresAt = tenant.getExpiresAt();
        if (expiresAt != null && expiresAt.isBefore(LocalDateTime.now())) {
            currentPackage = resolvePackage(defaultPackageId);
            expiresAt = null;
        } else {
            currentPackage = resolvePackage(tenant.getCurrentPackageId());
        }
        return TenantPackageInfo.from(tenant.getId(), currentPackage, tenant.getPackageActivatedAt(), expiresAt);
    }

    private Package resolvePackage(String packageId) {
        if (packageId == null) {
            log.warn("[TenantPackageService] Package ID is null");
            throw new ResourceNotFoundException("Package ID cannot be null");
        }
        return packageRepository.findByPackageId(packageId)
                .orElseThrow(() -> {
                    log.warn("[TenantPackageService] Package not found: {}", packageId);
                    return new ResourceNotFoundException("Package not found: " + packageId);
                });
    }
}
