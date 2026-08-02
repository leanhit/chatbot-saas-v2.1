package com.chatbot.spokes.minio.adapter;

import com.chatbot.shared.exceptions.BaseException;
import com.chatbot.shared.exceptions.ErrorCode;
import com.chatbot.core.tenant.infra.TenantContext;
import com.chatbot.shared.storage.StorageService;
import com.chatbot.spokes.minio.image.category.dto.CategoryRequestDTO;
import com.chatbot.spokes.minio.image.category.dto.CategoryResponseDTO;
import com.chatbot.spokes.minio.image.category.model.Category;
import com.chatbot.spokes.minio.image.category.service.CategoryService;
import com.chatbot.spokes.minio.image.fileMetadata.dto.FileRequestDTO;
import com.chatbot.spokes.minio.image.fileMetadata.dto.FileResponseDTO;
import com.chatbot.spokes.minio.image.fileMetadata.service.FileMetadataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MinioStorageAdapter implements StorageService {

    private final FileMetadataService fileMetadataService;
    private final CategoryService categoryService;

    @Override
    public String uploadUserAvatar(Long userId, String userEmail, MultipartFile file) {
        try {
            log.info("🔄 [MinIO Adapter] Uploading user avatar for userId: {}", userId);
            
            Category avatarCategory = getOrCreateCategory("avatar", "User avatar images");

            FileRequestDTO fileRequest = new FileRequestDTO();
            fileRequest.setCategoryId(avatarCategory.getId());
            fileRequest.setTitle("Avatar for user " + userId);
            fileRequest.setDescription("User avatar uploaded from profile");
            fileRequest.setTags(List.of("avatar", "user"));
            fileRequest.setFiles(List.of(file));

            List<FileResponseDTO> uploadedFiles = fileMetadataService.processUploadRequest(fileRequest, userEmail);
            if (uploadedFiles.isEmpty()) {
                throw new BaseException(ErrorCode.CANNOT_UPLOAD_AVATAR, "Cannot upload avatar");
            }

            return uploadedFiles.get(0).getFileUrl();
        } catch (Exception e) {
            log.error("❌ [MinIO Adapter] Failed to upload avatar for userId: {}", userId, e);
            throw new RuntimeException("Failed to upload avatar: " + e.getMessage(), e);
        }
    }

    @Override
    public String uploadTenantLogo(Long tenantId, String userEmail, MultipartFile file) {
        try {
            log.info("🔄 [MinIO Adapter] Uploading tenant logo for tenantId: {}", tenantId);
            
            // Clear tenant context to fetch/create global category
            TenantContext.clear();
            Category logoCategory = getOrCreateCategory("tenant-logo", "Tenant logo images");

            // Set tenant context for file upload
            TenantContext.setTenantId(tenantId);

            FileRequestDTO fileRequest = new FileRequestDTO();
            fileRequest.setCategoryId(logoCategory.getId());
            fileRequest.setTitle("Tenant logo");
            fileRequest.setDescription("Tenant logo uploaded from profile");
            fileRequest.setTags(List.of("tenant", "logo"));
            fileRequest.setFiles(List.of(file));

            List<FileResponseDTO> uploadedFiles = fileMetadataService.processUploadRequest(fileRequest, userEmail);
            if (uploadedFiles.isEmpty()) {
                throw new BaseException(ErrorCode.CANNOT_UPLOAD_LOGO, "Cannot upload tenant logo");
            }

            return uploadedFiles.get(0).getFileUrl();
        } catch (Exception e) {
            log.error("❌ [MinIO Adapter] Failed to upload logo for tenantId: {}", tenantId, e);
            throw new BaseException(ErrorCode.CANNOT_UPDATE_LOGO, "Cannot update tenant logo: " + e.getMessage(), e);
        }
    }

    private Category getOrCreateCategory(String name, String description) {
        List<CategoryResponseDTO> categories = categoryService.getAllCategoriesGlobal();
        Optional<Category> existingCategory = categories.stream()
                .filter(cat -> name.equals(cat.getName()))
                .findFirst()
                .map(catDto -> categoryService.getCategoryById(catDto.getId()).orElse(null));

        if (existingCategory.isPresent() && existingCategory.get() != null) {
            return existingCategory.get();
        }

        CategoryRequestDTO categoryRequest = new CategoryRequestDTO();
        categoryRequest.setName(name);
        categoryRequest.setDescription(description);
        CategoryResponseDTO newCategoryDto = categoryService.createCategoryGlobal(categoryRequest);
        return categoryService.getCategoryById(newCategoryDto.getId())
                .orElseThrow(() -> new BaseException(ErrorCode.CANNOT_CREATE_AVATAR_CATEGORY, "Cannot create category: " + name));
    }
}
