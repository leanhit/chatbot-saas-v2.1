package com.chatbot.modules.auth.service;

// import com.chatbot.modules.address.service.AddressService;
import com.chatbot.modules.auth.dto.*;
import com.chatbot.modules.auth.model.Auth;
import com.chatbot.modules.auth.model.SystemRole;
import com.chatbot.modules.auth.repository.AuthRepository;
import com.chatbot.modules.auth.security.CustomUserDetails;
import com.chatbot.modules.auth.security.JwtTokenConsumer;
import com.chatbot.modules.userInfo.model.UserInfo;
import com.chatbot.modules.identity.service.IdentityBridgeService;

import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService implements UserDetailsService {

    private final AuthRepository authRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenConsumer jwtTokenConsumer;
    // private final AddressService addressService;
    
    // Identity Hub bridge service for separate transaction
    private final IdentityBridgeService identityBridgeService;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Auth user = authRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng với email: " + email));

        return new CustomUserDetails(user);
    }

    /**
     * Load user by ID for Identity Hub tokens
     */
    public UserDetails loadUserById(UUID userId) throws UsernameNotFoundException {
        Auth user = authRepository.findByUserId(userId)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng với ID: " + userId));

        return new CustomUserDetails(user);
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public UserResponse register(RegisterRequest request) {

        if (authRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email đã tồn tại");
        }

        boolean isFirstUser = authRepository.count() == 0;

        Auth user = Auth.builder()
                .userId(UUID.randomUUID())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .systemRole(isFirstUser ? SystemRole.ADMIN : SystemRole.USER)
                .build();

        // tạo UserInfo đúng chuẩn MapsId
        // UserInfo userInfo = new UserInfo();
        // userInfo.setAuth(user);
        // user.setUserInfo(userInfo);

        // CHỈ save Auth – UserInfo sẽ được cascade
        Auth savedUser = authRepository.save(user);
        log.info("LEGACY REGISTER: Saved auth record with ID: {} for email: {}", savedUser.getId(), savedUser.getEmail());

        // =========================
        // IDENTITY HUB BRIDGE (COMPLETELY SEPARATE)
        // =========================
        // This runs in SEPARATE transaction and NEVER affects main registration
        try {
            boolean identityCreated = identityBridgeService.createIdentityUser(
                    savedUser.getEmail(), 
                    savedUser.getPassword()
            );
            
            if (identityCreated) {
                log.info("IDENTITY BRIDGE: SUCCESS - Identity Hub user/credential processed for email: {}", savedUser.getEmail());
            } else {
                log.warn("IDENTITY BRIDGE: FAILED - Could not create Identity Hub user/credential for email: {} - Legacy auth still functional", savedUser.getEmail());
            }
        } catch (Exception e) {
            // EXTRA SAFETY: Even if IdentityBridge somehow throws, don't fail registration
            log.error("IDENTITY BRIDGE: UNEXPECTED ERROR - Registration continuing despite identity bridge failure for email: {}. Error: {}", 
                    savedUser.getEmail(), e.getMessage(), e);
        }

        // tạo address (không ảnh hưởng transaction chính)
        try {
            // addressService.createEmptyAddressForUser(savedUser.getId());
            log.info("AddressService temporarily disabled - skipping address creation for user {}", savedUser.getId());
        } catch (Exception e) {
            log.error("Không thể tạo địa chỉ trống cho user {}: {}", savedUser.getId(), e.getMessage());
        }

        String token = jwtTokenConsumer.generateLegacyToken(savedUser.getEmail());

        UserDto userDto = new UserDto(
                savedUser.getUserId(),
                savedUser.getEmail(),
                savedUser.getSystemRole().name(),
                "vi"
        );

        return new UserResponse(token, userDto);
    }

    public UserResponse login(LoginRequest request) {
        Auth user = authRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng với email: " + request.getEmail()));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Mật khẩu không chính xác");
        }

        String token = jwtTokenConsumer.generateLegacyToken(user.getEmail());

        UserDto userDto = new UserDto(
                user.getUserId(),
                user.getEmail(),
                user.getSystemRole().name(),
                "vi"
        );
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
        String newToken = jwtTokenConsumer.generateLegacyToken(user.getEmail());
        UserDto userDto = new UserDto(
                user.getUserId(),
                user.getEmail(),
                user.getSystemRole().name(),
                "vi"
        );
        
        return new UserResponse(newToken, userDto);
    }

    public UserDto changeRole(UUID userId, SystemRole newRole) {
        // 1. Tìm người dùng cần đổi role
        Auth user = authRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với ID: " + userId));

        // 2. Cập nhật role mới
        user.setSystemRole(newRole);
        authRepository.save(user);

        log.info("Admin đã thay đổi quyền của user {} thành {}", user.getEmail(), newRole);

        // 3. Trả về thông tin user sau khi cập nhật
        return new UserDto(
                UUID.nameUUIDFromBytes(("auth_" + user.getId()).getBytes()),
                user.getEmail(),
                user.getSystemRole().name(),
                "vi"
        );
    }
}
