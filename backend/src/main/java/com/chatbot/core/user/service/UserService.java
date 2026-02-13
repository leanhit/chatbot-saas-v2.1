package com.chatbot.core.user.service;

import com.chatbot.core.user.dto.*;
import com.chatbot.core.user.model.User;
import com.chatbot.core.user.profile.UserProfile;
import com.chatbot.core.user.repository.UserRepository;
import com.chatbot.core.user.repository.UserProfileRepository;
import com.chatbot.modules.address.service.AddressService;
import com.chatbot.modules.address.dto.AddressDetailResponseDTO;
import com.chatbot.modules.address.model.OwnerType;
import com.chatbot.integrations.image.fileMetadata.service.FileMetadataService;
import com.chatbot.integrations.image.fileMetadata.dto.FileRequestDTO;
import com.chatbot.integrations.image.category.service.CategoryService;
import com.chatbot.integrations.image.category.model.Category;
import com.chatbot.integrations.image.category.dto.CategoryRequestDTO;
import com.chatbot.integrations.image.category.dto.CategoryResponseDTO;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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
    private final AddressService addressService;
    private final FileMetadataService fileMetadataService;
    private final CategoryService categoryService;
    private final MinioClient minioClient;

    @Value("${minio.endpoint:http://localhost:9000}")
    private String minioEndpoint;

    @Value("${minio.bucket-name:chatbot-files}")
    private String minioBucketName;

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
    @Transactional
    public UserProfileResponse getProfile(Long userId) {
        // Auto-create UserProfile if not exists (migration compatibility)
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseGet(() -> {
                    log.info("Auto-creating UserProfile for user ID: {}", userId);
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
                    UserProfile newProfile = UserProfile.builder()
                            .id(userId)
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
                            .id(userId)
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
        // Auto-create UserProfile if not exists (migration compatibility)
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseGet(() -> {
                    log.info("Auto-creating UserProfile for user ID: {}", userId);
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
                    UserProfile newProfile = UserProfile.builder()
                            .id(userId)
                            .user(user)
                            .build();
                    return userProfileRepository.save(newProfile);
                });

        // Upload avatar file using FileMetadataService (same pattern as existing user avatar)
        String avatarUrl;
        try {
            // 1. Find category for avatar - use default category or create new
            Category avatarCategory;
            List<CategoryResponseDTO> categories = categoryService.getAllCategoriesGlobal();
            Optional<Category> existingCategory = categories.stream()
                .filter(cat -> "avatar".equals(cat.getName()))
                .findFirst()
                .map(catDto -> categoryService.getCategoryById(catDto.getId()).orElse(null));

            if (existingCategory.isEmpty()) {
                // Create default category for avatar if not exists
                CategoryRequestDTO categoryRequest = new CategoryRequestDTO();
                categoryRequest.setName("avatar");
                categoryRequest.setDescription("User avatar images");
                CategoryResponseDTO newCategoryDto = categoryService.createCategoryGlobal(categoryRequest);
                avatarCategory = categoryService.getCategoryById(newCategoryDto.getId()).orElse(null);
            } else {
                avatarCategory = existingCategory.get();
            }

            if (avatarCategory == null) {
                throw new RuntimeException("Không thể tạo hoặc tìm category cho avatar");
            }

            // 2. Upload file to MinIO using FileMetadataService
            FileRequestDTO fileRequest = new FileRequestDTO();
            fileRequest.setCategoryId(avatarCategory.getId());
            fileRequest.setTitle("Avatar for user " + userId);
            fileRequest.setDescription("User avatar uploaded from profile");
            fileRequest.setTags(List.of("avatar", "user"));
            fileRequest.setFiles(List.of(file));

            List<com.chatbot.integrations.image.fileMetadata.dto.FileResponseDTO> uploadedFiles = 
                fileMetadataService.processUploadRequest(fileRequest, getCurrentUserEmail(userId));

            if (uploadedFiles.isEmpty()) {
                throw new RuntimeException("Không thể upload avatar");
            }

            // 3. Get public URL from FileResponse (not manual construct)
            avatarUrl = uploadedFiles.get(0).getFileUrl();
            
            log.info("Uploaded avatar for user ID: {} to public URL: {}", userId, avatarUrl);
        } catch (Exception e) {
            log.error("Failed to upload avatar for user ID: {}", userId, e);
            throw new RuntimeException("Failed to upload avatar: " + e.getMessage());
        }
        profile.setAvatar(avatarUrl);
        
        UserProfile updatedProfile = userProfileRepository.save(profile);
        log.info("Updated avatar for user ID: {}", userId);
        
        return mapToProfileResponse(updatedProfile);
    }

    /**
     * Create empty profile for new user
     */
    @Transactional
    public void createEmptyProfile(User user) {
        UserProfile profile = UserProfile.builder()
                .user(user)
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
                            .id(userId)
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
        UserProfile profile = userProfileRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User profile not found with ID: " + userId));
        
        // Update basic info only
        profile.setFullName(request.getFullName());
        profile.setPhoneNumber(request.getPhoneNumber());
        profile.setGender(request.getGender());
        profile.setBio(request.getBio());
        
        profile = userProfileRepository.save(profile);
        log.info("Updated basic info for user: {}", userId);
        
        return mapToProfileResponse(profile);
    }

    /**
     * Update professional user information only
     */
    @Transactional
    public UserProfileResponse updateProfessionalInfo(Long userId, UserRequest request) {
        UserProfile profile = userProfileRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User profile not found with ID: " + userId));
        
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
        return userRepository.save(user);
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
