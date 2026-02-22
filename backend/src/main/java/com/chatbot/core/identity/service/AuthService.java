package com.chatbot.core.identity.service;

import com.chatbot.shared.address.service.AddressService;
import com.chatbot.core.identity.dto.*;
import com.chatbot.core.identity.model.SystemRole;
import com.chatbot.core.identity.repository.AuthRepository;
import com.chatbot.core.identity.security.CustomUserDetails;
import com.chatbot.core.user.model.User;
import com.chatbot.core.user.service.UserService;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService implements UserDetailsService {

    private final AuthRepository authRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AddressService addressService;
    private final UserService userService;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = authRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng với email: " + email));

        return new CustomUserDetails(user);
    }

    @Transactional(rollbackFor = Exception.class)
    public UserResponse register(RegisterRequest request) {

        if (authRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email đã tồn tại");
        }

        boolean isFirstUser = authRepository.count() == 0;

        // tạo User entity
        User userEntity = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .systemRole(isFirstUser ? SystemRole.ADMIN : SystemRole.USER)
                .build();

        // save User entity và tạo profile
        User savedUser = userService.save(userEntity);
        userService.createEmptyProfile(savedUser);

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
        User user = authRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng với email: " + request.getEmail()));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Mật khẩu không chính xác");
        }

        String token = jwtService.generateToken(user.getEmail());
        UserDto userDto = new UserDto(user.getId(), user.getEmail(), user.getSystemRole().name());
        return new UserResponse(token, userDto);
    }

    public UserResponse changePassword(String email, ChangePasswordRequest request) {
        User user = authRepository.findByEmail(email)
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
        User user = authRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với ID: " + userId));

        // 2. Cập nhật role mới
        user.setSystemRole(newRole);
        authRepository.save(user);

        log.info("Admin đã thay đổi quyền của user {} thành {}", user.getEmail(), newRole);

        // 3. Trả về thông tin user sau khi cập nhật
        return new UserDto(user.getId(), user.getEmail(), user.getSystemRole().name());
    }
}
