package com.chatbot.core.tenant.profile.service;

import com.chatbot.core.tenant.repository.TenantRepository;
import com.chatbot.core.tenant.profile.dto.TenantProfileRequest;
import com.chatbot.core.tenant.profile.dto.TenantProfileResponse;
import com.chatbot.core.tenant.profile.model.TenantProfile;
import com.chatbot.core.tenant.profile.repository.TenantProfileRepository;
import com.chatbot.shared.storage.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.stream.Collectors;
import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class TenantProfileService {

    private final TenantProfileRepository profileRepo;
    private final TenantRepository tenantRepo;
    private final StorageService storageService;

    private String getCurrentUserEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    @Cacheable(value = "tenant-profiles", key = "#tenantId", unless = "#result == null")
    @Transactional(readOnly = true, transactionManager = "tenantTransactionManager")
    public TenantProfileResponse getProfile(Long tenantId) {
        // Check if tenant exists
        if (!tenantRepo.existsById(tenantId)) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Không tìm thấy tenant với ID: " + tenantId
            );
        }

        // Get profile or throw 404 if not found
        return profileRepo.findById(tenantId)
            .map(this::map)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Chưa có thông tin hồ sơ cho tenant này. Vui lòng tạo hồ sơ trước."
            ));
    }

    @Caching(evict = {
        @CacheEvict(value = "tenant-profiles", key = "#tenantId"),
        @CacheEvict(value = "tenant-profiles-batch", allEntries = true)
    })
    @Transactional(transactionManager = "tenantTransactionManager")
    public TenantProfileResponse upsertProfile(
            Long tenantId,
            TenantProfileRequest req
    ) {
        log.info("[TenantProfileService] ===== START upsertProfile: tenantId={}", tenantId);
        log.info("[TenantProfileService] Request: description={}, primaryColor={}", req.getDescription(), req.getPrimaryColor());

        // Check if tenant exists
        if (!tenantRepo.existsById(tenantId)) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Không tìm thấy tenant với ID: " + tenantId
            );
        }

        TenantProfile profile = profileRepo.findById(tenantId)
                .orElseGet(() -> {
                    log.info("[TenantProfileService] Creating new profile for tenant: {}", tenantId);
                    // Insert directly using native query to avoid detached entity issues with @MapsId
                    profileRepo.insertProfile(tenantId);
                    // Now fetch the newly created profile
                    return profileRepo.findById(tenantId).orElseThrow();
                });

        log.info("[TenantProfileService] Profile: id={}, existing={}", profile.getId(), profile.getId() != null);

        // update fields only if not null (preserve existing values)
        if (req.getDescription() != null) {
            profile.setDescription(req.getDescription());
        }
        if (req.getIndustry() != null) {
            profile.setIndustry(req.getIndustry());
        }
        if (req.getPlan() != null) {
            profile.setPlan(req.getPlan());
        }
        if (req.getCompanySize() != null) {
            profile.setCompanySize(req.getCompanySize());
        }
        if (req.getLegalName() != null) {
            profile.setLegalName(req.getLegalName());
        }
        if (req.getTaxCode() != null) {
            profile.setTaxCode(req.getTaxCode());
        }
        if (req.getContactEmail() != null) {
            profile.setContactEmail(req.getContactEmail());
        }
        if (req.getContactPhone() != null) {
            profile.setContactPhone(req.getContactPhone());
        }
        if (req.getLogoUrl() != null) {
            profile.setLogoUrl(req.getLogoUrl());
        }
        if (req.getFaviconUrl() != null) {
            profile.setFaviconUrl(req.getFaviconUrl());
        }
        if (req.getPrimaryColor() != null) {
            profile.setPrimaryColor(req.getPrimaryColor());
        }

        log.info("[TenantProfileService] Saving profile...");
        // Use repository save method - the profile is now managed after fetch
        TenantProfile savedProfile = profileRepo.save(profile);
        log.info("[TenantProfileService] Profile saved successfully");

        TenantProfileResponse response = map(savedProfile);
        log.info("[TenantProfileService] ===== END upsertProfile successfully");
        return response;
    }

    @Caching(evict = {
        @CacheEvict(value = "tenant-profiles", key = "#tenantId"),
        @CacheEvict(value = "tenant-profiles-batch", allEntries = true)
    })
    @Transactional(rollbackFor = Exception.class)
    public TenantProfileResponse updateLogo(Long tenantId, MultipartFile file) {
        try {
            log.info("🔄 [LOGO START] Starting logo update for tenantId: {}, fileName: {}, fileSize: {}", 
                    tenantId, file.getOriginalFilename(), file.getSize());

            String logoUrl = storageService.uploadTenantLogo(tenantId, getCurrentUserEmail(), file);
            log.info("✅ [UPLOAD] File uploaded successfully to: {}", logoUrl);

            TenantProfileRequest profileRequest = new TenantProfileRequest();
            profileRequest.setLogoUrl(logoUrl);
            
            TenantProfileResponse response = upsertProfile(tenantId, profileRequest);
            log.info("✅ [LOGO SUCCESS] Logo updated successfully for tenantId: {}, logoUrl: {}", tenantId, logoUrl);
            
            return response;

        } catch (Exception e) {
            log.error("❌ [LOGO ERROR] Failed to update tenant logo for tenantId {}: {}", tenantId, e.getMessage(), e);
            throw new com.chatbot.shared.exceptions.BaseException(com.chatbot.shared.exceptions.ErrorCode.CANNOT_UPDATE_LOGO, "Cannot update tenant logo: " + e.getMessage(), e);
        }
    }

    private TenantProfileResponse map(TenantProfile p) {
        String logoUrl = p.getLogoUrl();

        // Convert existing private URLs to public URLs if needed
        if (logoUrl != null && !logoUrl.startsWith("/api/images/public/")) {
            // If it's an old private URL, we need to extract file ID and convert to public URL
            // This is a fallback - ideally existing logos should be migrated
            logoUrl = logoUrl; // Keep existing URL for now, migration needed
        }

        // With @MapsId, the profile ID is the same as tenant ID
        Long tenantId = p.getId();

        return TenantProfileResponse.builder()
                .tenantId(tenantId)
                .description(p.getDescription())
                .industry(p.getIndustry())
                .plan(p.getPlan())
                .companySize(p.getCompanySize())
                .legalName(p.getLegalName())
                .taxCode(p.getTaxCode())
                .contactEmail(p.getContactEmail())
                .contactPhone(p.getContactPhone())
                .logoUrl(logoUrl)
                .faviconUrl(p.getFaviconUrl())
                .primaryColor(p.getPrimaryColor())
                .build();
    }

    @Cacheable(value = "tenant-profiles-batch", key = "#tenantIds.hashCode()", unless = "#result == null || #result.isEmpty()")
    public Map<Long, TenantProfileResponse> getProfilesByTenantIds(List<Long> tenantIds) {
        if (tenantIds == null || tenantIds.isEmpty()) return Collections.emptyMap();
        
        return profileRepo.findByTenantIdIn(tenantIds)
                .stream()
                .map(this::map) // Sử dụng hàm map đã có trong class này
                .collect(Collectors.toMap(
                    TenantProfileResponse::getTenantId, 
                    profile -> profile,
                    (existing, replacement) -> existing 
                ));
    }
}
