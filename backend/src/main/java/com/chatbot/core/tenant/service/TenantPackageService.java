package com.chatbot.core.tenant.service;

import com.chatbot.core.tenant.dto.TenantPackageInfo;
import com.chatbot.core.tenant.model.Tenant;
import com.chatbot.core.tenant.repository.TenantRepository;
import com.chatbot.core.simplepayment.service.PackageService;
import com.chatbot.core.simplepayment.model.Package;
import com.chatbot.core.simplepayment.repository.PackageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.chatbot.core.user.model.User;
import com.chatbot.core.identity.repository.AuthRepository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class TenantPackageService {

    private final TenantRepository tenantRepository;
    private final PackageService packageService;
    private final PackageRepository packageRepository;
    private final AuthRepository authRepository;
    private final TenantAuditLogService auditLogService;

    @Value("${tenant.default.package:free}")
    private String defaultPackageId;

    @Value("${tenant.package.max-duration-years:2}")
    private int maxDurationYears;

    /**
     * Gán gói mặc định (free) cho tenant mới.
     */
    @Transactional
    public void assignDefaultPackageToTenant(Tenant tenant) {
        log.info("[TenantPackageService] Assigning default package '{}' to tenant: {}",
                defaultPackageId, tenant.getTenantKey());

        Package freePackage = packageRepository.findByPackageId(defaultPackageId)
                .orElseThrow(() -> new RuntimeException(
                        "Default package not found in database: " + defaultPackageId));

        tenant.setCurrentPackageId(defaultPackageId);
        tenant.setPackageActivatedAt(LocalDateTime.now());
        tenant.setExpiresAt(null); // Free package không có hạn

        tenantRepository.save(tenant);

        log.info("[TenantPackageService] Assigned '{}' package to tenant: {} at {}",
                defaultPackageId, tenant.getTenantKey(), tenant.getPackageActivatedAt());
    }

    /**
     * Nâng cấp tenant lên gói cụ thể.
     */
    @Transactional
    public void upgradeTenantPackage(Long tenantId, String packageId) {
        log.info("[TenantPackageService] Upgrading tenant {} to package: {}", tenantId, packageId);

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found: " + tenantId));

        Package newPackage = packageRepository.findByPackageId(packageId)
                .orElseThrow(() -> new RuntimeException("Package not found: " + packageId));

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

        auditLogService.logAction(tenantId, "system", "UPGRADE_PACKAGE",
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
        if (startDate == null || duration == null) return null;

        String lower = duration.toLowerCase().trim();
        String[] parts = lower.split("\\s+");
        if (parts.length < 2) {
            log.warn("[TenantPackageService] Cannot parse duration string: '{}'", duration);
            return null;
        }

        try {
            int amount = Integer.parseInt(parts[0]);
            if (lower.contains("month")) return startDate.plusMonths(amount);
            if (lower.contains("year"))  return startDate.plusYears(amount);
        } catch (NumberFormatException e) {
            log.warn("[TenantPackageService] Invalid duration number in: '{}'", duration);
        }
        return null;
    }

    /**
     * Fallback: tìm tenantId từ header hoặc từ user.
     */
    @Transactional(readOnly = true)
    public Long extractTenantIdWithFallback(String tenantKey, String username) {
        if (tenantKey != null && !tenantKey.isBlank()) {
            try {
                return tenantRepository.findByTenantKey(tenantKey)
                        .map(Tenant::getId)
                        .orElseThrow(() -> new RuntimeException("Tenant not found: " + tenantKey));
            } catch (Exception e) {
                log.warn("[TenantPackageService] Cannot resolve tenant from key {}: {}", tenantKey, e.getMessage());
            }
        }

        if (username != null) {
            try {
                User user = authRepository.findByEmail(username)
                        .orElseThrow(() -> new RuntimeException("User not found: " + username));
                return tenantRepository.findByUserId(user.getId())
                        .map(Tenant::getId)
                        .orElseThrow(() -> new RuntimeException("No tenant for user: " + user.getId()));
            } catch (Exception e) {
                log.warn("[TenantPackageService] Cannot resolve tenant from user {}: {}", username, e.getMessage());
            }
        }

        throw new RuntimeException("Tenant ID không tìm thấy qua header hay user context");
    }

    /**
     * Lấy thông tin gói dịch vụ hiện tại của tenant (dạng Map cho API response).
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getMyPackageInfo(Long tenantId) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));

        Package currentPackage = null;
        if (tenant.getCurrentPackageId() != null) {
            currentPackage = packageRepository.findByPackageId(tenant.getCurrentPackageId()).orElse(null);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("tenantId", tenantId);
        response.put("tenantKey", tenant.getTenantKey());
        response.put("currentPackageId", tenant.getCurrentPackageId());
        response.put("packageActivatedAt", tenant.getPackageActivatedAt());
        response.put("expiresAt", tenant.getExpiresAt());

        if (currentPackage != null) {
            response.put("packageName", currentPackage.getName());
            response.put("packagePrice", currentPackage.getPrice());
            response.put("packageCurrency", currentPackage.getCurrency());
            response.put("packageDuration", currentPackage.getDuration());
            response.put("chatbotLimit", currentPackage.getChatbotLimit());
            response.put("messageLimit", currentPackage.getMessageLimit());
        } else {
            response.put("packageName", null);
            response.put("packagePrice", null);
            response.put("packageCurrency", null);
            response.put("packageDuration", null);
            response.put("chatbotLimit", null);
            response.put("messageLimit", null);
        }
        return response;
    }

    /**
     * Lấy thông tin gói dịch vụ kèm ngày hết hạn — dùng trong TenantPackageInfo DTO.
     */
    @Transactional(readOnly = true)
    public TenantPackageInfo getCurrentTenantPackageInfo(Long tenantId) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found: " + tenantId));

        Package currentPackage = resolvePackage(tenant.getCurrentPackageId());
        return TenantPackageInfo.from(tenantId, currentPackage, tenant.getPackageActivatedAt(), tenant.getExpiresAt());
    }

    @Transactional(readOnly = true)
    public TenantPackageInfo getCurrentTenantPackageInfoByKey(String tenantKey) {
        Tenant tenant = tenantRepository.findByTenantKey(tenantKey)
                .orElseThrow(() -> new RuntimeException("Tenant not found: " + tenantKey));

        Package currentPackage = resolvePackage(tenant.getCurrentPackageId());
        return TenantPackageInfo.from(tenant.getId(), currentPackage, tenant.getPackageActivatedAt(), tenant.getExpiresAt());
    }

    @Transactional(readOnly = true)
    public Package getCurrentTenantPackage(Long tenantId) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found: " + tenantId));

        if (tenant.getCurrentPackageId() == null) {
            log.warn("[TenantPackageService] Tenant {} has no package assigned", tenant.getTenantKey());
            return null;
        }
        return resolvePackage(tenant.getCurrentPackageId());
    }

    @Transactional(readOnly = true)
    public Package getCurrentTenantPackageByKey(String tenantKey) {
        Tenant tenant = tenantRepository.findByTenantKey(tenantKey)
                .orElseThrow(() -> new RuntimeException("Tenant not found: " + tenantKey));

        if (tenant.getCurrentPackageId() == null) {
            log.warn("[TenantPackageService] Tenant {} has no package assigned", tenantKey);
            return null;
        }
        return resolvePackage(tenant.getCurrentPackageId());
    }

    @Transactional(readOnly = true)
    public boolean hasTenantPackage(Long tenantId, String packageId) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found: " + tenantId));
        return packageId.equals(tenant.getCurrentPackageId());
    }

    /**
     * Khởi tạo gói mặc định cho tất cả tenant chưa có gói.
     */
    @Transactional
    public void initializeExistingTenants() {
        log.info("[TenantPackageService] Initializing tenants without packages...");
        int updatedCount = 0;
        for (Tenant tenant : tenantRepository.findByCurrentPackageIdIsNull()) {
            try {
                assignDefaultPackageToTenant(tenant);
                updatedCount++;
            } catch (Exception e) {
                log.error("[TenantPackageService] Failed to init tenant {}: {}",
                        tenant.getTenantKey(), e.getMessage());
            }
        }
        log.info("[TenantPackageService] Initialized {} tenants with free package", updatedCount);
    }

    /* ================= PRIVATE HELPERS ================= */

    private Package resolvePackage(String packageId) {
        if (packageId == null) return null;
        try {
            return packageService.getPackageByPackageId(packageId).orElse(null);
        } catch (Exception e) {
            log.warn("[TenantPackageService] getPackageByPackageId failed for '{}', falling back to repo: {}",
                    packageId, e.getMessage());
            return packageRepository.findByPackageId(packageId).orElse(null);
        }
    }
}
