package com.chatbot.core.user.service;

import com.chatbot.core.user.dto.*;
import com.chatbot.core.user.model.User;
import com.chatbot.core.user.profile.UserProfile;
import com.chatbot.core.user.repository.UserRepository;
import com.chatbot.core.user.repository.UserProfileRepository;
import com.chatbot.core.tenant.membership.model.TenantJoinRequest;
import com.chatbot.core.tenant.membership.model.MembershipStatus;
import com.chatbot.core.tenant.membership.repository.TenantJoinRequestRepository;
import com.chatbot.shared.address.service.AddressService;
import com.chatbot.shared.address.dto.AddressDetailResponseDTO;
import com.chatbot.shared.address.model.OwnerType;
import com.chatbot.spokes.minio.image.fileMetadata.service.FileMetadataService;
import com.chatbot.spokes.minio.image.fileMetadata.dto.FileRequestDTO;
import com.chatbot.spokes.minio.image.category.service.CategoryService;
import com.chatbot.spokes.minio.image.category.model.Category;
import com.chatbot.spokes.minio.image.category.dto.CategoryRequestDTO;
import com.chatbot.spokes.minio.image.category.dto.CategoryResponseDTO;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.util.List;
import java.util.Optional;

/**
 * User Service - Business logic for system user management
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final TenantJoinRequestRepository joinRequestRepository;
    private final AddressService addressService;
    private final FileMetadataService fileMetadataService;
    private final CategoryService categoryService;
    private final MinioClient minioClient;

    @Value("${app.integrations.minio.endpoint:http://localhost:9000}")
    private String minioEndpoint;

    @Value("${app.integrations.minio.bucket:chatbot-files}")
    private String minioBucketName;
    
    // Expose repository for direct access
    public UserProfileRepository getUserProfileRepository() {
        return userProfileRepository;
    }

    /**
     * Get user by ID
     */
    @Transactional(readOnly = true)
    public User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
    }

    /**
     * Get user profile by ID
     */
    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(Long userId) {
        // Auto-create UserProfile if not exists (migration compatibility)
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseGet(() -> {
                    log.info("Auto-creating UserProfile for user ID: {}", userId);
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
                    UserProfile newProfile = UserProfile.builder()
                            .user(user)
                            .build();
                    return userProfileRepository.save(newProfile);
                });
        return mapToProfileResponse(profile);
    }

    /**
     * Update user profile
     */
    @Transactional
    public UserProfileResponse updateProfile(Long userId, UserRequest request) {
        // Auto-create UserProfile if not exists (migration compatibility)
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseGet(() -> {
                    log.info("Auto-creating UserProfile for user ID: {}", userId);
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
                    UserProfile newProfile = UserProfile.builder()
                            .user(user)
                            .build();
                    return userProfileRepository.save(newProfile);
                });
        
        // Update basic info
        profile.setFullName(request.getFullName());
        profile.setPhoneNumber(request.getPhoneNumber());
        profile.setGender(request.getGender());
        profile.setBio(request.getBio());
        profile.setAvatar(request.getAvatar()); // Added from UserInfo
        
        // Update professional info
        profile.setJobTitle(request.getJobTitle());
        profile.setDepartment(request.getDepartment());
        profile.setCompany(request.getCompany());
        profile.setLinkedinUrl(request.getLinkedinUrl());
        profile.setWebsite(request.getWebsite());
        profile.setLocation(request.getLocation());
        profile.setSkills(request.getSkills());
        profile.setExperience(request.getExperience());
        profile.setEducation(request.getEducation());
        profile.setCertifications(request.getCertifications());
        profile.setLanguages(request.getLanguages());
        profile.setAvailability(request.getAvailability());
        profile.setHourlyRate(request.getHourlyRate());
        profile.setPortfolioUrl(request.getPortfolioUrl());
        
        UserProfile updatedProfile = userProfileRepository.save(profile);
        log.info("Updated profile for user ID: {}", userId);
        
        return mapToProfileResponse(updatedProfile);
    }

    /**
     * Update user avatar
     */
    @Transactional
    public UserProfileResponse updateAvatar(Long userId, MultipartFile file) {
        try {
            log.info("🔄 [AVATAR UPDATE] Starting avatar update for userId: {}, fileName: {}, fileSize: {}", 
                    userId, file.getOriginalFilename(), file.getSize());
            
            // Validate file size
            if (file.getSize() == 0) {
                log.error("❌ [AVATAR UPDATE] File is empty (size: 0) for userId: {}", userId);
                throw new IllegalArgumentException("File cannot be empty");
            }
            
            // Validate file type
            if (file.getContentType() == null || !file.getContentType().startsWith("image/")) {
                log.error("❌ [AVATAR UPDATE] Invalid file type: {} for userId: {}", file.getContentType(), userId);
                throw new IllegalArgumentException("Only image files are allowed");
            }
            
            // Validate file size (max 5MB)
            if (file.getSize() > 5 * 1024 * 1024) {
                log.error("❌ [AVATAR UPDATE] File too large: {} bytes for userId: {}", file.getSize(), userId);
                throw new IllegalArgumentException("File size cannot exceed 5MB");
            }
            
            log.info("✅ [AVATAR UPDATE] File validation passed for userId: {}", userId);
            
            // Auto-create UserProfile if not exists (migration compatibility)
            UserProfile profile = userProfileRepository.findByUserId(userId)
                    .orElseGet(() -> {
                        log.info("🆕 [AVATAR UPDATE] Auto-creating UserProfile for user ID: {}", userId);
                        User user = userRepository.findById(userId)
                                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
                        UserProfile newProfile = UserProfile.builder()
                                .user(user)
                                .build();
                        return userProfileRepository.save(newProfile);
                    });

            log.info("👤 [AVATAR UPDATE] Found/created profile for userId: {}", userId);

            // Upload avatar file using FileMetadataService (same pattern as existing user avatar)
            String avatarUrl;
            try {
                log.info("📂 [AVATAR UPDATE] Starting file upload process");
                
                // 1. Find category for avatar - use default category or create new
                Category avatarCategory;
                List<CategoryResponseDTO> categories = categoryService.getAllCategoriesGlobal();
                log.info("📂 [AVATAR UPDATE] Found {} global categories", categories.size());
                
                Optional<Category> existingCategory = categories.stream()
                    .filter(cat -> "avatar".equals(cat.getName()))
                    .findFirst()
                    .map(catDto -> {
                        log.debug("📂 [AVATAR UPDATE] Found existing avatar category: {}", catDto.getId());
                        return categoryService.getCategoryById(catDto.getId()).orElse(null);
                    });

                if (existingCategory.isEmpty()) {
                    // Create default category for avatar if not exists
                    log.info("📂 [AVATAR UPDATE] Creating new avatar category");
                    CategoryRequestDTO categoryRequest = new CategoryRequestDTO();
                    categoryRequest.setName("avatar");
                    categoryRequest.setDescription("User avatar images");
                    CategoryResponseDTO newCategoryDto = categoryService.createCategoryGlobal(categoryRequest);
                    avatarCategory = categoryService.getCategoryById(newCategoryDto.getId()).orElse(null);
                    log.info("📂 [AVATAR UPDATE] Created new category with ID: {}", newCategoryDto.getId());
                } else {
                    avatarCategory = existingCategory.get();
                    log.debug("📂 [AVATAR UPDATE] Using existing category: {}", avatarCategory.getId());
                }

                if (avatarCategory == null) {
                    log.error("❌ [AVATAR UPDATE] Failed to create or find avatar category for userId: {}", userId);
                    throw new RuntimeException("Không thể tạo hoặc tìm category cho avatar");
                }

                log.info("✅ [AVATAR UPDATE] Category ready: {} (ID: {})", avatarCategory.getName(), avatarCategory.getId());

                // 2. Upload file to MinIO using FileMetadataService
                log.info("📤 [AVATAR UPDATE] Preparing file upload to MinIO");
                FileRequestDTO fileRequest = new FileRequestDTO();
                fileRequest.setCategoryId(avatarCategory.getId());
                fileRequest.setTitle("Avatar for user " + userId);
                fileRequest.setDescription("User avatar uploaded from profile");
                fileRequest.setTags(List.of("avatar", "user"));
                fileRequest.setFiles(List.of(file));

                log.info("📤 [AVATAR UPDATE] Calling FileMetadataService.processUploadRequest");
                List<com.chatbot.spokes.minio.image.fileMetadata.dto.FileResponseDTO> uploadedFiles = 
                    fileMetadataService.processUploadRequest(fileRequest, getCurrentUserEmail(userId));

                if (uploadedFiles.isEmpty()) {
                    log.error("❌ [AVATAR UPDATE] No files returned from upload service for userId: {}", userId);
                    throw new RuntimeException("Không thể upload avatar");
                }

                // 3. Get public URL from FileResponse (not manual construct)
                avatarUrl = uploadedFiles.get(0).getFileUrl();
                log.info("✅ [AVATAR UPDATE] File uploaded successfully to: {}", avatarUrl);
                
            } catch (Exception e) {
                log.error("❌ [AVATAR UPDATE] Failed to upload avatar for user ID: {}", userId, e);
                throw new RuntimeException("Failed to upload avatar: " + e.getMessage(), e);
            }
            
            // Update profile with new avatar URL
            log.info("💾 [AVATAR UPDATE] Updating profile with new avatar URL for userId: {}", userId);
            profile.setAvatar(avatarUrl);
            
            UserProfile updatedProfile = userProfileRepository.save(profile);
            log.info("✅ [AVATAR UPDATE] Avatar updated successfully for user ID: {}, avatarUrl: {}", userId, avatarUrl);
            
            return mapToProfileResponse(updatedProfile);
            
        } catch (IllegalArgumentException e) {
            log.error("❌ [AVATAR UPDATE] Validation error for userId {}: {}", userId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("💥 [AVATAR UPDATE] Unexpected error for userId {}: {}", userId, e.getMessage(), e);
            throw new RuntimeException("Failed to update avatar: " + e.getMessage(), e);
        }
    }

    /**
     * Create empty profile for new user
     */
    @Transactional
    public void createEmptyProfile(User user) {
        // Fetch managed User entity to avoid detached entity issue
        User managedUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("User not found: " + user.getId()));
                
        UserProfile profile = UserProfile.builder()
                .user(managedUser)  // Use managed User entity
                .build();
        
        userProfileRepository.save(profile);
        log.info("Created empty profile for user ID: {}", user.getId());
    }

    /**
     * Create empty address for new user
     */
    @Transactional
    public void createEmptyAddress(Long userId) {
        try {
            addressService.createEmptyAddressForUser(userId);
            log.info("Created empty address for user ID: {}", userId);
        } catch (Exception e) {
            log.error("Failed to create empty address for user {}: {}", userId, e.getMessage());
        }
    }

    /**
     * Cancel user's own join request
     */
    @Transactional
    public void cancelJoinRequest(Long requestId, User user) {
        // Find the join request
        TenantJoinRequest request = joinRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Join request not found: " + requestId));

        // Verify that the request belongs to the user
        if (!request.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You can only cancel your own join requests");
        }

        // Verify that the request is still pending
        if (request.getStatus() != MembershipStatus.PENDING) {
            throw new RuntimeException("Can only cancel pending requests");
        }

        // Delete the request
        joinRequestRepository.delete(request);
        log.info("Cancelled join request: {} for user: {}", requestId, user.getEmail());
    }

    // ===== Mappers =====
    
    private UserProfileResponse mapToProfileResponse(UserProfile profile) {
        return UserProfileResponse.builder()
                .id(profile.getId())
                .fullName(profile.getFullName())
                .phoneNumber(profile.getPhoneNumber())
                .avatar(profile.getAvatar())
                .gender(profile.getGender())
                .bio(profile.getBio())
                .jobTitle(profile.getJobTitle())
                .department(profile.getDepartment())
                .company(profile.getCompany())
                .linkedinUrl(profile.getLinkedinUrl())
                .website(profile.getWebsite())
                .location(profile.getLocation())
                .skills(profile.getSkills())
                .experience(profile.getExperience())
                .education(profile.getEducation())
                .certifications(profile.getCertifications())
                .languages(profile.getLanguages())
                .availability(profile.getAvailability())
                .hourlyRate(profile.getHourlyRate())
                .portfolioUrl(profile.getPortfolioUrl())
                .build();
    }

    private UserFullResponse.UserProfile mapToUserFullProfile(UserProfile profile) {
        return UserFullResponse.UserProfile.builder()
                .userId(profile.getId())
                .fullName(profile.getFullName())
                .phoneNumber(profile.getPhoneNumber())
                .avatar(profile.getAvatar())
                .gender(profile.getGender())
                .bio(profile.getBio())
                .jobTitle(profile.getJobTitle())
                .department(profile.getDepartment())
                .company(profile.getCompany())
                .linkedinUrl(profile.getLinkedinUrl())
                .website(profile.getWebsite())
                .location(profile.getLocation())
                .skills(profile.getSkills())
                .experience(profile.getExperience())
                .education(profile.getEducation())
                .certifications(profile.getCertifications())
                .languages(profile.getLanguages())
                .availability(profile.getAvailability())
                .hourlyRate(profile.getHourlyRate())
                .portfolioUrl(profile.getPortfolioUrl())
                .build();
    }

    // ===== Methods from UserInfoService (Migrated) =====
    
    /**
     * Get full user profile with address information
     */
    @Transactional
    public UserFullResponse getFullProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        
        // Auto-create UserProfile if not exists (migration compatibility)
        UserProfile profile = userProfileRepository.findById(userId)
                .orElseGet(() -> {
                    log.info("Auto-creating UserProfile for user ID: {}", userId);
                    UserProfile newProfile = UserProfile.builder()
                            .user(user)
                            .build();
                    return userProfileRepository.save(newProfile);
                });
        
        // Get user address (single address) - không cần tenant
        AddressDetailResponseDTO addressDetail = null;
        try {
            addressDetail = addressService.getUserAddress(OwnerType.USER, userId);
        } catch (RuntimeException e) {
            // User chưa có địa chỉ, bỏ qua
        }
        
        return UserFullResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .systemRole(user.getSystemRole().toString())
                .isActive(user.getIsActive())
                .profile(mapToUserFullProfile(profile))
                .addresses(addressDetail != null ? List.of(addressDetail) : List.of())
                .build();
    }

    /**
     * Update basic user information only
     */
    @Transactional
    public UserProfileResponse updateBasicInfo(Long userId, UserRequest request) {
        try {
            log.info("Updating basic info for user ID: {}", userId);
            
            // First, ensure profile exists in a separate transaction
            UserProfile profile = ensureProfileExists(userId);
            
            // Then update the profile
            profile.setFullName(request.getFullName());
            profile.setPhoneNumber(request.getPhoneNumber());
            profile.setGender(request.getGender());
            profile.setBio(request.getBio());
            
            profile = userProfileRepository.save(profile);
            log.info("Successfully updated basic info for user ID: {}", userId);
            
            return mapToProfileResponse(profile);
        } catch (Exception e) {
            log.error("Error updating basic info for user {}: {}", userId, e.getMessage(), e);
            throw new RuntimeException("Failed to update basic info: " + e.getMessage(), e);
        }
    }
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public UserProfile ensureProfileExists(Long userId) {
        return userProfileRepository.findByUserId(userId)
                .orElseGet(() -> {
                    log.info("Auto-creating UserProfile for user ID: {}", userId);
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
                    UserProfile newProfile = UserProfile.builder()
                            .user(user)
                            .build();
                    return userProfileRepository.save(newProfile);
                });
    }

    /**
     * Update professional user information only
     */
    @Transactional
    public UserProfileResponse updateProfessionalInfo(Long userId, UserRequest request) {
        // Auto-create UserProfile if not exists (same as updateProfile)
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseGet(() -> {
                    log.info("Auto-creating UserProfile for user ID: {}", userId);
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
                    UserProfile newProfile = UserProfile.builder()
                            .user(user)
                            .build();
                    return userProfileRepository.save(newProfile);
                });
        
        // Update professional info only
        profile.setJobTitle(request.getJobTitle());
        profile.setDepartment(request.getDepartment());
        profile.setCompany(request.getCompany());
        profile.setLinkedinUrl(request.getLinkedinUrl());
        profile.setWebsite(request.getWebsite());
        profile.setLocation(request.getLocation());
        profile.setSkills(request.getSkills());
        profile.setExperience(request.getExperience());
        profile.setEducation(request.getEducation());
        profile.setCertifications(request.getCertifications());
        profile.setLanguages(request.getLanguages());
        profile.setAvailability(request.getAvailability());
        profile.setHourlyRate(request.getHourlyRate());
        profile.setPortfolioUrl(request.getPortfolioUrl());
        
        profile = userProfileRepository.save(profile);
        log.info("Updated professional info for user: {}", userId);
        
        return mapToProfileResponse(profile);
    }

    /**
     * Save user entity (for registration)
     */
    @Transactional
    public User save(User user) {
        return userRepository.saveAndFlush(user);
    }

    /**
     * Get current user email (for file upload)
     */
    private String getCurrentUserEmail(Long userId) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
            return user.getEmail();
        } catch (Exception e) {
            log.error("Failed to get user email for ID: {}", userId, e);
            return "system@chatbot.com"; // fallback
        }
    }
}
