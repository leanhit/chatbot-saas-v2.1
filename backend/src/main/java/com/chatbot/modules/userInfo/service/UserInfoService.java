package com.chatbot.modules.userInfo.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.chatbot.modules.userInfo.dto.UserInfoRequest;
import com.chatbot.modules.userInfo.dto.BasicInfoRequest;
import com.chatbot.modules.userInfo.dto.ProfessionalInfoRequest;
import com.chatbot.modules.userInfo.dto.UserInfoResponse;
import com.chatbot.modules.userInfo.dto.UserFullResponse;
import com.chatbot.modules.userInfo.model.UserInfo;
import com.chatbot.modules.userInfo.repository.UserInfoRepository;
import com.chatbot.modules.address.service.AddressService;
import com.chatbot.modules.address.dto.AddressDetailResponseDTO;
import com.chatbot.modules.address.model.OwnerType;
import com.chatbot.integrations.image.fileMetadata.service.FileMetadataService;
import com.chatbot.integrations.image.category.service.CategoryService;
import com.chatbot.integrations.image.category.model.Category;
import com.chatbot.integrations.image.category.dto.CategoryRequestDTO;
import com.chatbot.integrations.image.category.dto.CategoryResponseDTO;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserInfoService {
    private final UserInfoRepository userInfoRepository;
    private final AddressService addressService;
    private final FileMetadataService fileMetadataService;
    private final CategoryService categoryService;

    public UserFullResponse getFullProfile(Long userId) {
        UserInfo profile = userInfoRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User profile not found"));
        
        // Get user address (single address) - không cần tenant
        AddressDetailResponseDTO addressDetail = null;
        try {
            addressDetail = addressService.getUserAddress(OwnerType.USER, userId);
        } catch (RuntimeException e) {
            // User chưa có địa chỉ, bỏ qua
        }
        
        return UserFullResponse.builder()
                .id(profile.getId())
                .email(profile.getAuth() != null ? profile.getAuth().getEmail() : null)
                .systemRole(profile.getAuth() != null ? profile.getAuth().getSystemRole() : null)
                .isActive(profile.getAuth() != null ? profile.getAuth().getIsActive() : false)
                .createdAt(null) // Auth entity doesn't have createdAt
                .updatedAt(null) // Auth entity doesn't have updatedAt
                .profile(UserFullResponse.UserProfile.builder()
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
                        .build())
                .addresses(addressDetail != null ? List.of(addressDetail) : List.of())
                .build();
    }

    public UserInfoResponse getProfile(Long userId) {
        UserInfo profile = userInfoRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User profile not found"));
        return mapToResponse(profile);
    }

    @Transactional
    public UserInfoResponse updateProfile(Long userId, UserInfoRequest request) {
        UserInfo profile = userInfoRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User profile not found"));
        
        profile.setFullName(request.getFullName());
        profile.setPhoneNumber(request.getPhoneNumber());
        profile.setAvatar(request.getAvatar());
        profile.setBio(request.getBio());
        profile.setGender(request.getGender());
        
        // Update professional fields
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
        
        UserInfo updatedProfile = userInfoRepository.save(profile);
        return mapToResponse(updatedProfile);
    }

    private UserInfoResponse mapToResponse(UserInfo userInfo) {
        return UserInfoResponse.builder()
                .id(userInfo.getId())
                .email(userInfo.getAuth() != null ? userInfo.getAuth().getEmail() : null)
                .fullName(userInfo.getFullName())
                .phoneNumber(userInfo.getPhoneNumber())
                .avatar(userInfo.getAvatar())
                .gender(userInfo.getGender())
                .bio(userInfo.getBio())
                .jobTitle(userInfo.getJobTitle())
                .department(userInfo.getDepartment())
                .company(userInfo.getCompany())
                .linkedinUrl(userInfo.getLinkedinUrl())
                .website(userInfo.getWebsite())
                .location(userInfo.getLocation())
                .skills(userInfo.getSkills())
                .experience(userInfo.getExperience())
                .education(userInfo.getEducation())
                .certifications(userInfo.getCertifications())
                .languages(userInfo.getLanguages())
                .availability(userInfo.getAvailability())
                .hourlyRate(userInfo.getHourlyRate())
                .portfolioUrl(userInfo.getPortfolioUrl())
                .build();
    }

    @Transactional(rollbackFor = Exception.class)
    public UserInfoResponse updateAvatar(Long userId, MultipartFile file) {
        try {
            // 1. Find user profile (no tenant context needed)
            UserInfo profile = userInfoRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User profile not found"));

            // 2. Find or create GLOBAL category for user avatars (no tenant context)
            Category avatarCategory;
            
            // Clear any existing tenant context to get global categories
            com.chatbot.core.tenant.infra.TenantContext.clear();
            
            List<CategoryResponseDTO> categories = categoryService.getAllCategoriesGlobal();
            Optional<Category> existingCategory = categories.stream()
                .filter(cat -> "avatar".equals(cat.getName()))
                .findFirst()
                .map(catDto -> categoryService.getCategoryById(catDto.getId()).orElse(null));

            if (existingCategory.isEmpty()) {
                // Create default GLOBAL category for avatars if not exists
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

            // 3. Upload file to MinIO using FileMetadataService (no tenant context needed)
            com.chatbot.integrations.image.fileMetadata.dto.FileRequestDTO fileRequest = 
                new com.chatbot.integrations.image.fileMetadata.dto.FileRequestDTO();
            fileRequest.setCategoryId(avatarCategory.getId());
            fileRequest.setTitle("Avatar for user " + userId);
            fileRequest.setDescription("User avatar uploaded from profile");
            fileRequest.setTags(List.of("avatar", "user"));
            fileRequest.setFiles(List.of(file));

            List<com.chatbot.integrations.image.fileMetadata.dto.FileResponseDTO> uploadedFiles = 
                fileMetadataService.processUploadRequest(fileRequest, profile.getAuth().getEmail());

            if (uploadedFiles.isEmpty()) {
                throw new RuntimeException("Không thể upload avatar");
            }

            String avatarFileId = uploadedFiles.get(0).getId().toString();

            // 4. Update user profile with new avatar file ID
            profile.setAvatar(avatarFileId);
            UserInfo updatedProfile = userInfoRepository.save(profile);

            return mapToResponse(updatedProfile);

        } catch (Exception e) {
            throw new RuntimeException("Không thể cập nhật avatar: " + e.getMessage(), e);
        }
    }

    // 1. Update Basic Info Only - Tách biệt không gộp chung
    @Transactional
    public UserInfoResponse updateBasicInfo(Long userId, BasicInfoRequest request) {
        UserInfo profile = userInfoRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User profile not found"));
        
        // Chỉ update basic info fields, không ảnh hưởng professional fields
        profile.setFullName(request.getFullName());
        profile.setPhoneNumber(request.getPhoneNumber());
        profile.setBio(request.getBio());
        profile.setGender(request.getGender());
        
        // Không update professional fields - giữ nguyên giá trị cũ
        
        UserInfo updatedProfile = userInfoRepository.save(profile);
        return mapToResponse(updatedProfile);
    }

    // 2. Update Professional Info Only - Tách biệt không gộp chung
    @Transactional
    public UserInfoResponse updateProfessionalInfo(Long userId, ProfessionalInfoRequest request) {
        UserInfo profile = userInfoRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User profile not found"));
        
        // Chỉ update professional fields, không ảnh hưởng basic fields
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
        
        // Không update basic fields - giữ nguyên giá trị cũ
        
        UserInfo updatedProfile = userInfoRepository.save(profile);
        return mapToResponse(updatedProfile);
    }
}