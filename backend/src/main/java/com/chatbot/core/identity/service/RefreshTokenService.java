package com.chatbot.core.identity.service;

import com.chatbot.core.identity.exception.InvalidTokenException;
import com.chatbot.core.identity.model.RefreshToken;
import com.chatbot.core.identity.repository.RefreshTokenRepository;
import com.chatbot.core.user.model.User;
import com.chatbot.core.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {
    
    @Value("${app.refresh-token.expiration:2592000}") // 30 days default
    private long refreshTokenExpirationMs;
    
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final UserRepository userRepository; // Added for application-level join
    
    @Transactional(value = "identityTransactionManager", rollbackFor = Exception.class)
    public RefreshToken createRefreshToken(User user) {
        // Delete existing refresh tokens for this user
        refreshTokenRepository.deleteByUserId(user.getId());
        
        RefreshToken refreshToken = RefreshToken.builder()
                .userId(user.getId()) // Application-level join: store userId instead of User object
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusSeconds(refreshTokenExpirationMs))
                .build();
        
        refreshToken = refreshTokenRepository.save(refreshToken);
        log.info("Created refresh token for user: {}", user.getEmail());
        
        return refreshToken;
    }
    
    @Transactional(value = "identityTransactionManager", rollbackFor = Exception.class)
    public RefreshToken createRefreshToken(Long userId) {
        // Delete existing refresh tokens for this user
        refreshTokenRepository.deleteByUserId(userId);
        
        RefreshToken refreshToken = RefreshToken.builder()
                .userId(userId) // Application-level join: store userId instead of User object
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusSeconds(refreshTokenExpirationMs))
                .build();
        
        refreshToken = refreshTokenRepository.save(refreshToken);
        log.info("Created refresh token for user ID: {}", userId);
        
        return refreshToken;
    }
    
    @Transactional(value = "identityTransactionManager", rollbackFor = Exception.class)
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }
    
    @Transactional(value = "identityTransactionManager", rollbackFor = Exception.class)
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.isExpired()) {
            refreshTokenRepository.delete(token);
            throw new InvalidTokenException(com.chatbot.shared.exceptions.ErrorCode.REFRESH_TOKEN_EXPIRED, "Refresh token has expired. Please login again.");
        }
        return token;
    }
    
    @Transactional(value = "identityTransactionManager", rollbackFor = Exception.class)
    public String refreshAccessToken(String refreshToken) {
        RefreshToken refresh = findByToken(refreshToken)
                .orElseThrow(() -> new InvalidTokenException("Refresh token không hợp lệ"));
        
        verifyExpiration(refresh);
        
        // Application-level join: fetch user by userId
        User user = userRepository.findById(refresh.getUserId())
                .orElseThrow(() -> new InvalidTokenException("User not found for refresh token"));
        
        // Generate new access token
        String newAccessToken = jwtService.generateToken(user.getEmail());
        
        log.info("Refreshed access token for user: {}", user.getEmail());
        
        return newAccessToken;
    }
    
    @Transactional(value = "identityTransactionManager", rollbackFor = Exception.class)
    public void deleteByUserId(Long userId) {
        refreshTokenRepository.deleteByUserId(userId);
        log.info("Deleted refresh tokens for user ID: {}", userId);
    }
}
