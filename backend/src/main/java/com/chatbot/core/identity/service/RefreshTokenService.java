package com.chatbot.core.identity.service;

import com.chatbot.core.identity.exception.InvalidTokenException;
import com.chatbot.core.identity.model.RefreshToken;
import com.chatbot.core.identity.repository.RefreshTokenRepository;
import com.chatbot.core.user.model.User;
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
    
    @Transactional
    public RefreshToken createRefreshToken(User user) {
        // Delete existing refresh tokens for this user
        refreshTokenRepository.deleteByUserId(user.getId());
        
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusSeconds(refreshTokenExpirationMs))
                .build();
        
        refreshToken = refreshTokenRepository.save(refreshToken);
        log.info("Created refresh token for user: {}", user.getEmail());
        
        return refreshToken;
    }
    
    @Transactional
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }
    
    @Transactional
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.isExpired()) {
            refreshTokenRepository.delete(token);
            throw new InvalidTokenException("Refresh token đã hết hạn. Vui lòng đăng nhập lại.");
        }
        return token;
    }
    
    @Transactional
    public String refreshAccessToken(String refreshToken) {
        RefreshToken refresh = findByToken(refreshToken)
                .orElseThrow(() -> new InvalidTokenException("Refresh token không hợp lệ"));
        
        verifyExpiration(refresh);
        
        // Generate new access token
        String newAccessToken = jwtService.generateToken(refresh.getUser().getEmail());
        
        log.info("Refreshed access token for user: {}", refresh.getUser().getEmail());
        
        return newAccessToken;
    }
    
    @Transactional
    public void deleteByUserId(Long userId) {
        refreshTokenRepository.deleteByUserId(userId);
        log.info("Deleted refresh tokens for user ID: {}", userId);
    }
}
