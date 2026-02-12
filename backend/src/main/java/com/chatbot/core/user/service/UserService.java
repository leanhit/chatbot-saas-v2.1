package com.chatbot.core.user.service;

import com.chatbot.core.user.dto.*;
import com.chatbot.core.user.model.User;
import com.chatbot.core.user.profile.UserProfile;
import com.chatbot.core.user.repository.UserRepository;
import com.chatbot.core.user.repository.UserProfileRepository;
import com.chatbot.modules.address.service.AddressService;
import com.chatbot.modules.address.dto.AddressDetailResponseDTO;
import com.chatbot.modules.address.model.OwnerType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User profile not found with ID: " + userId));
        return mapToProfileResponse(profile);
    }

    /**
     * Get full user information including addresses
     */
    @Transactional(readOnly = true)
    public UserFullResponse getFullProfile(Long userId) {
        User user = getUser(userId);
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User profile not found with ID: " + userId));
        
        // Get user address (single address)
        AddressDetailResponseDTO addressDetail = null;
        try {
            addressDetail = addressService.getUserAddress(OwnerType.USER, userId);
        } catch (RuntimeException e) {
            // User chưa có địa chỉ, bỏ qua
            log.debug("User {} has no address", userId);
        }
        
        return UserFullResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .systemRole(user.getSystemRole().name())
                .isActive(user.getIsActive())
                .profile(mapToUserFullProfile(profile))
                .addresses(addressDetail != null ? List.of(addressDetail) : List.of())
                .build();
    }

    /**
     * Update user profile
     */
    @Transactional
    public UserProfileResponse updateProfile(Long userId, UserRequest request) {
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User profile not found with ID: " + userId));
        
        // Update basic info
        profile.setFullName(request.getFullName());
        profile.setPhoneNumber(request.getPhoneNumber());
        profile.setGender(request.getGender());
        profile.setBio(request.getBio());
        
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
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User profile not found with ID: " + userId));

        // TODO: Implement avatar upload logic
        // For now, just update avatar URL placeholder
        String avatarUrl = "placeholder_" + System.currentTimeMillis();
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
}
