package com.chatbot.core.identity.service;

import com.chatbot.modules.address.service.AddressService;
import com.chatbot.core.identity.dto.*;
import com.chatbot.core.identity.model.Auth;
import com.chatbot.core.identity.model.SystemRole;
import com.chatbot.core.identity.repository.AuthRepository;
import com.chatbot.core.identity.security.CustomUserDetails;
import com.chatbot.modules.userInfo.model.UserInfo;
import com.chatbot.modules.userInfo.service.UserInfoService;
import com.chatbot.integrations.image.fileMetadata.service.FileMetadataService;
import com.chatbot.integrations.image.category.service.CategoryService;
import com.chatbot.integrations.image.category.model.Category;
import com.chatbot.integrations.image.category.dto.CategoryRequestDTO;
import com.chatbot.integrations.image.category.dto.CategoryResponseDTO;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService implements UserDetailsService {

    private final AuthRepository authRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AddressService addressService;
    private final UserInfoService userInfoService;
    private final FileMetadataService fileMetadataService;
    private final CategoryService categoryService;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Auth user = authRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng với email: " + email));

        return new CustomUserDetails(user);
    }

    @Transactional(rollbackFor = Exception.class)
    public UserResponse register(RegisterRequest request) {

        if (authRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email đã tồn tại");
        }

        boolean isFirstUser = authRepository.count() == 0;

        Auth user = Auth.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .systemRole(isFirstUser ? SystemRole.ADMIN : SystemRole.USER)
                .build();

        // tạo UserInfo đúng chuẩn MapsId
        UserInfo userInfo = new UserInfo();
        userInfo.setAuth(user);
        user.setUserInfo(userInfo);

        // CHỈ save Auth – UserInfo sẽ được cascade
        Auth savedUser = authRepository.save(user);

        // tạo address (không ảnh hưởng transaction chính)
        try {
            addressService.createEmptyAddressForUser(savedUser.getId());
        } catch (Exception e) {
            log.error("Không thể tạo địa chỉ trống cho user {}: {}", savedUser.getId(), e.getMessage());
        }

        String token = jwtService.generateToken(savedUser.getEmail());

        UserDto userDto = new UserDto(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getSystemRole().name()
        );

        return new UserResponse(token, userDto);
    }

    public UserResponse login(LoginRequest request) {
        Auth user = authRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng với email: " + request.getEmail()));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Mật khẩu không chính xác");
        }

        String token = jwtService.generateToken(user.getEmail());
        UserDto userDto = new UserDto(user.getId(), user.getEmail(), user.getSystemRole().name());
        return new UserResponse(token, userDto);
    }

    public UserResponse changePassword(String email, ChangePasswordRequest request) {
        Auth user = authRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng"));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("Mật khẩu cũ không chính xác");
        }

        // Cập nhật mật khẩu mới
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        authRepository.save(user);

        // --- TẠO LẠI TOKEN MỚI VÀ TRẢ VỀ GIỐNG LOGIN ---
        String newToken = jwtService.generateToken(user.getEmail());
        UserDto userDto = new UserDto(user.getId(), user.getEmail(), user.getSystemRole().name());
        
        return new UserResponse(newToken, userDto);
    }

    public UserDto changeRole(Long userId, SystemRole newRole) {
        // 1. Tìm người dùng cần đổi role
        Auth user = authRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với ID: " + userId));

        // 2. Cập nhật role mới
        user.setSystemRole(newRole);
        authRepository.save(user);

        log.info("Admin đã thay đổi quyền của user {} thành {}", user.getEmail(), newRole);

        // 3. Trả về thông tin user sau khi cập nhật
        return new UserDto(user.getId(), user.getEmail(), user.getSystemRole().name());
    }

    @Transactional(rollbackFor = Exception.class)
    public UserResponse updateAvatar(String email, MultipartFile file) {
        // 1. Tìm user
        Auth user = authRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng với email: " + email));

        try {
            // 2. Tìm category cho avatar - sử dụng category mặc định hoặc tạo mới
            Category avatarCategory;
            List<CategoryResponseDTO> categories = categoryService.getAllCategories();
            Optional<Category> existingCategory = categories.stream()
                .filter(cat -> "avatar".equals(cat.getName()))
                .findFirst()
                .map(catDto -> categoryService.getCategoryById(catDto.getId()).orElse(null));

            if (existingCategory.isEmpty()) {
                // Tạo category mặc định cho avatar nếu chưa có
                CategoryRequestDTO categoryRequest = new CategoryRequestDTO();
                categoryRequest.setName("avatar");
                categoryRequest.setDescription("User avatar images");
                CategoryResponseDTO newCategoryDto = categoryService.createCategory(categoryRequest);
                avatarCategory = categoryService.getCategoryById(newCategoryDto.getId()).orElse(null);
            } else {
                avatarCategory = existingCategory.get();
            }

            if (avatarCategory == null) {
                throw new RuntimeException("Không thể tạo hoặc tìm category cho avatar");
            }

            // 3. Upload file lên MinIO sử dụng FileMetadataService
            com.chatbot.integrations.image.fileMetadata.dto.FileRequestDTO fileRequest = 
                new com.chatbot.integrations.image.fileMetadata.dto.FileRequestDTO();
            fileRequest.setCategoryId(avatarCategory.getId());
            fileRequest.setTitle("Avatar for user " + user.getId());
            fileRequest.setDescription("User avatar uploaded from profile");
            fileRequest.setTags(List.of("avatar", "user"));
            fileRequest.setFiles(List.of(file));

            List<com.chatbot.integrations.image.fileMetadata.dto.FileResponseDTO> uploadedFiles = 
                fileMetadataService.processUploadRequest(fileRequest, email);

            if (uploadedFiles.isEmpty()) {
                throw new RuntimeException("Không thể upload avatar");
            }

            String avatarUrl = uploadedFiles.get(0).getFileUrl();

            // 4. Cập nhật avatar URL trong UserInfo sử dụng UserInfoService
            com.chatbot.modules.userInfo.dto.UserInfoRequest userInfoRequest = 
                new com.chatbot.modules.userInfo.dto.UserInfoRequest();
            userInfoRequest.setAvatar(avatarUrl);
            userInfoService.updateProfile(user.getId(), userInfoRequest);

            // 6. Tạo token mới và trả về response
            String newToken = jwtService.generateToken(user.getEmail());
            UserDto userDto = new UserDto(user.getId(), user.getEmail(), user.getSystemRole().name());
            
            log.info("Avatar updated successfully for user: {}, URL: {}", email, avatarUrl);
            return new UserResponse(newToken, userDto);

        } catch (Exception e) {
            log.error("Error updating avatar for user {}: {}", email, e.getMessage(), e);
            throw new RuntimeException("Không thể cập nhật avatar: " + e.getMessage(), e);
        }
    }
}
