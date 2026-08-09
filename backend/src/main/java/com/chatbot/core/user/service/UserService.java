package com.chatbot.core.user.service;

import com.chatbot.core.user.dto.*;
import com.chatbot.core.user.model.User;
import com.chatbot.core.user.profile.UserProfile;
import com.chatbot.core.user.repository.UserRepository;
import com.chatbot.core.user.repository.UserProfileRepository;
import com.chatbot.core.tenant.membership.model.TenantJoinRequest;
import com.chatbot.core.tenant.membership.model.MembershipStatus;
import com.chatbot.core.tenant.membership.repository.TenantJoinRequestRepository;
import com.chatbot.core.identity.exception.UserNotFoundException;
import com.chatbot.shared.address.service.AddressService;
import com.chatbot.shared.address.dto.AddressDetailResponseDTO;
import com.chatbot.shared.address.model.OwnerType;
import com.chatbot.shared.storage.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
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
    private final StorageService storageService;
    
    // Expose repository for direct access
    public UserProfileRepository getUserProfileRepository() {
        return userProfileRepository;
    }

    /**
     * Get user by ID
     */
    @Cacheable(value = "users", key = "#userId", unless = "#result == null")
    @Transactional(value = "userTransactionManager", readOnly = true, rollbackFor = Exception.class)
    public User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));
    }

    /**
     * Get all users
     */
    @Cacheable(value = "apiResponses", key = "'allUsers'", unless = "#result == null || #result.isEmpty()")
    @Transactional(value = "userTransactionManager", readOnly = true, rollbackFor = Exception.class)
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> UserDto.builder()
                        .id(user.getId())
                        .email(user.getEmail())
                        .systemRole(user.getSystemRole().name())
                        .isActive(user.getIsActive())
                        .createdAt(user.getCreatedAt())
                        .build())
                .toList();
    }

    /**
     * Search users with pagination
     */
    @Transactional(value = "userTransactionManager", readOnly = true, rollbackFor = Exception.class)
    public org.springframework.data.domain.Page<UserDto> searchUsers(String keyword, com.chatbot.core.identity.model.SystemRole role, org.springframework.data.domain.Pageable pageable) {
        return userRepository.searchUsers(keyword, role, pageable)
                .map(user -> UserDto.builder()
                        .id(user.getId())
                        .email(user.getEmail())
                        .systemRole(user.getSystemRole().name())
                        .isActive(user.getIsActive())
                        .createdAt(user.getCreatedAt())
                        .build());
    }

    /**
     * Update user active status
     */
    @Transactional(value = "userTransactionManager", rollbackFor = Exception.class)
    public void updateUserStatus(Long userId, boolean isActive) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));
        user.setIsActive(isActive);
        userRepository.save(user);
        log.info("Updated status for user {}: isActive={}", userId, isActive);
    }

    /**
     * Get user profile by ID
     */
    @Transactional(transactionManager = "userTransactionManager", readOnly = true, rollbackFor = Exception.class)
    public UserProfileResponse getProfile(Long userId) {
        // Auto-create UserProfile if not exists (migration compatibility)
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseGet(() -> {
                    log.info("Auto-creating UserProfile for user ID: {}", userId);
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));
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
    @Transactional(transactionManager = "userTransactionManager", rollbackFor = Exception.class)
    public UserProfileResponse updateProfile(Long userId, UserRequest request) {
        // Auto-create UserProfile if not exists (migration compatibility)
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseGet(() -> {
                    log.info("Auto-creating UserProfile for user ID: {}", userId);
                    // Insert directly using native query to avoid detached entity issues
                    userProfileRepository.insertProfile(userId);
                    // Now fetch the newly created profile
                    return userProfileRepository.findByUserId(userId).orElseThrow();
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
    @Transactional(transactionManager = "userTransactionManager", rollbackFor = Exception.class)
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
            
            // Validate file size (max 10MB)
            if (file.getSize() > 10 * 1024 * 1024) {
                log.error("❌ [AVATAR UPDATE] File too large: {} bytes for userId: {}", file.getSize(), userId);
                throw new IllegalArgumentException("File size cannot exceed 10MB");
            }
            
            log.info("✅ [AVATAR UPDATE] File validation passed for userId: {}", userId);
            
            // Auto-create UserProfile if not exists (migration compatibility)
            UserProfile profile = userProfileRepository.findByUserId(userId)
                    .orElseGet(() -> {
                        log.info("🆕 [AVATAR UPDATE] Auto-creating UserProfile for user ID: {}", userId);
                        // Insert directly using native query to avoid detached entity issues
                        userProfileRepository.insertProfile(userId);
                        // Now fetch the newly created profile
                        return userProfileRepository.findByUserId(userId).orElseThrow();
                    });

            log.info("👤 [AVATAR UPDATE] Found/created profile for userId: {}", userId);

            // Upload avatar file using StorageService abstraction
            String avatarUrl = storageService.uploadUserAvatar(userId, getCurrentUserEmail(userId), file);
            
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
    @Transactional(transactionManager = "userTransactionManager", rollbackFor = Exception.class)
    public void createEmptyProfile(User user) {
        // Insert directly using native query to avoid detached entity issues
        userProfileRepository.insertProfile(user.getId());
        log.info("Created empty profile for user ID: {}", user.getId());
    }

    /**
     * Create empty address for new user
     */
    @Transactional(transactionManager = "userTransactionManager", rollbackFor = Exception.class)
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
    @Transactional(transactionManager = "userTransactionManager", rollbackFor = Exception.class)
    public void cancelJoinRequest(Long requestId, User user) {
        // Find the join request
        TenantJoinRequest request = joinRequestRepository.findById(requestId)
                .orElseThrow(() -> new UserNotFoundException("Join request not found: " + requestId));

        // Verify that the request belongs to the user
        if (!request.getUserId().equals(user.getId())) {
            throw new com.chatbot.shared.exceptions.UnauthorizedException("You can only cancel your own join requests");
        }

        // Verify that the request is still pending
        if (request.getStatus() != MembershipStatus.PENDING) {
            throw new IllegalStateException("Can only cancel pending requests");
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
    @Transactional(transactionManager = "userTransactionManager", rollbackFor = Exception.class)
    public UserFullResponse getFullProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));
        
        // Auto-create UserProfile if not exists (migration compatibility)
        UserProfile profile = userProfileRepository.findById(userId)
                .orElseGet(() -> {
                    log.info("Auto-creating UserProfile for user ID: {}", userId);
                    // Insert directly using native query to avoid detached entity issues
                    userProfileRepository.insertProfile(userId);
                    // Now fetch the newly created profile
                    return userProfileRepository.findById(userId).orElseThrow();
                });
        
        // Get user address (single address) - không cần tenant
        AddressDetailResponseDTO addressDetail = null;
        try {
            addressDetail = addressService.getUserAddress(OwnerType.USER, userId);
        } catch (RuntimeException e) {
            log.debug("ℹ️ User address not found or not initialized yet for user ID: {}. Message: {}", userId, e.getMessage());
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
    @Transactional(transactionManager = "userTransactionManager", rollbackFor = Exception.class)
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
    
    @Transactional(transactionManager = "userTransactionManager", propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public UserProfile ensureProfileExists(Long userId) {
        return userProfileRepository.findByUserId(userId)
                .orElseGet(() -> {
                    log.info("Auto-creating UserProfile for user ID: {}", userId);
                    // Insert directly using native query to avoid detached entity issues
                    userProfileRepository.insertProfile(userId);
                    // Now fetch the newly created profile
                    return userProfileRepository.findByUserId(userId).orElseThrow();
                });
    }

    /**
     * Update professional user information only
     */
    @Transactional(transactionManager = "userTransactionManager", rollbackFor = Exception.class)
    public UserProfileResponse updateProfessionalInfo(Long userId, UserRequest request) {
        // Auto-create UserProfile if not exists (same as updateProfile)
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseGet(() -> {
                    log.info("Auto-creating UserProfile for user ID: {}", userId);
                    // Insert directly using native query to avoid detached entity issues
                    userProfileRepository.insertProfile(userId);
                    // Now fetch the newly created profile
                    return userProfileRepository.findByUserId(userId).orElseThrow();
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
    @Transactional(transactionManager = "userTransactionManager", rollbackFor = Exception.class)
    public User save(User user) {
        return userRepository.saveAndFlush(user);
    }

    /**
     * Get current user email (for file upload)
     */
    private String getCurrentUserEmail(Long userId) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));
            return user.getEmail();
        } catch (Exception e) {
            log.error("Failed to get user email for ID: {}", userId, e);
            return "system@chatbot.com"; // fallback
        }
    }
}
