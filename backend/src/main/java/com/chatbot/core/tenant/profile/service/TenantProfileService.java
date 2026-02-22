package com.chatbot.core.tenant.profile.service;

import com.chatbot.core.tenant.model.Tenant;
import com.chatbot.core.tenant.repository.TenantRepository;
import com.chatbot.core.tenant.profile.dto.TenantProfileRequest;
import com.chatbot.core.tenant.profile.dto.TenantProfileResponse;
import com.chatbot.core.tenant.profile.model.TenantProfile;
import com.chatbot.core.tenant.profile.repository.TenantProfileRepository;
import com.chatbot.core.tenant.infra.TenantContext;
import com.chatbot.spokes.minio.image.fileMetadata.service.FileMetadataService;
import com.chatbot.spokes.minio.image.category.service.CategoryService;
import com.chatbot.spokes.minio.image.category.model.Category;
import com.chatbot.spokes.minio.image.category.dto.CategoryRequestDTO;
import com.chatbot.spokes.minio.image.category.dto.CategoryResponseDTO;
import lombok.RequiredArgsConstructor;

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
public class TenantProfileService {

    private final TenantProfileRepository profileRepo;
    private final TenantRepository tenantRepo;
    private final FileMetadataService fileMetadataService;
    private final CategoryService categoryService;

    private String getCurrentUserEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    @Transactional(readOnly = true)
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

    @Transactional
    public TenantProfileResponse upsertProfile(
            Long tenantId,
            TenantProfileRequest req
    ) {
        Tenant tenant = tenantRepo.findById(tenantId)
                .orElseThrow(() ->
                        new IllegalStateException("Tenant not found"));

        TenantProfile profile = profileRepo.findById(tenantId)
                .orElseGet(() -> {
                    TenantProfile p = new TenantProfile();
                    p.setTenant(tenant); // MapsId
                    return p;
                });

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

        profileRepo.save(profile);
        return map(profile);
    }

    @Transactional(rollbackFor = Exception.class)
    public TenantProfileResponse updateLogo(Long tenantId, MultipartFile file) {
        try {
            // 1. Clear any existing tenant context to get global categories (like user avatar)
            TenantContext.clear();
            
            // 2. Find or create GLOBAL category for tenant logos (like avatar category)
            Category logoCategory;
            List<CategoryResponseDTO> categories = categoryService.getAllCategoriesGlobal();
            Optional<Category> existingCategory = categories.stream()
                .filter(cat -> "tenant-logo".equals(cat.getName()))
                .findFirst()
                .map(catDto -> categoryService.getCategoryById(catDto.getId()).orElse(null));

            if (existingCategory.isEmpty()) {
                // Create default GLOBAL category for tenant logos if not exists
                CategoryRequestDTO categoryRequest = new CategoryRequestDTO();
                categoryRequest.setName("tenant-logo");
                categoryRequest.setDescription("Tenant logo images");
                CategoryResponseDTO newCategoryDto = categoryService.createCategoryGlobal(categoryRequest);
                logoCategory = categoryService.getCategoryById(newCategoryDto.getId()).orElse(null);
            } else {
                logoCategory = existingCategory.get();
            }

            if (logoCategory == null) {
                throw new RuntimeException("Không thể tạo hoặc tìm category cho tenant logo");
            }

            // 3. Upload file to MinIO using FileMetadataService
            com.chatbot.spokes.minio.image.fileMetadata.dto.FileRequestDTO fileRequest = 
                new com.chatbot.spokes.minio.image.fileMetadata.dto.FileRequestDTO();
            fileRequest.setCategoryId(logoCategory.getId());
            fileRequest.setTitle("Tenant logo");
            fileRequest.setDescription("Tenant logo uploaded from profile");
            fileRequest.setTags(List.of("tenant", "logo"));
            fileRequest.setFiles(List.of(file));

            List<com.chatbot.spokes.minio.image.fileMetadata.dto.FileResponseDTO> uploadedFiles = 
                fileMetadataService.processUploadRequest(fileRequest, getCurrentUserEmail());

            if (uploadedFiles.isEmpty()) {
                throw new RuntimeException("Không thể upload tenant logo");
            }

            String logoFileId = uploadedFiles.get(0).getId().toString();
            String logoUrl = "/api/images/public/" + logoFileId + "/content";

            // 4. Update tenant profile with new logo URL
            TenantProfileRequest profileRequest = new TenantProfileRequest();
            profileRequest.setLogoUrl(logoUrl);
            
            return upsertProfile(tenantId, profileRequest);

        } catch (Exception e) {
            throw new RuntimeException("Không thể cập nhật tenant logo: " + e.getMessage(), e);
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
        
        return TenantProfileResponse.builder()
                .tenantId(p.getId()) // = tenantId
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
