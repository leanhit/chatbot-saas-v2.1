package com.chatbot.spokes.facebook.token.service;

import com.chatbot.spokes.facebook.connection.model.FacebookConnection;
import com.chatbot.spokes.facebook.connection.repository.FacebookConnectionRepository;
import com.chatbot.spokes.facebook.api.service.FacebookApiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Slf4j
public class FacebookTokenManager {

    private final FacebookConnectionRepository connectionRepository;
    private final FacebookApiService facebookApiService;

    public FacebookTokenManager(FacebookConnectionRepository connectionRepository,
                               FacebookApiService facebookApiService) {
        this.connectionRepository = connectionRepository;
        this.facebookApiService = facebookApiService;
    }

    /**
     * Lấy valid token với JIT refresh
     * @param pageId Facebook Page ID
     * @return Valid page access token
     */
    @Transactional(readOnly = true)
    public String getValidToken(String pageId) {
        log.debug("🔍 Getting valid token for page: {}", pageId);
        
        FacebookConnection connection = connectionRepository.findByPageIdForWebhook(pageId)
            .orElseThrow(() -> new RuntimeException("❌ No active connection found for page: " + pageId));
        
        // Check if token is expired or expiring soon (within 1 hour)
        if (isTokenExpiringSoon(connection)) {
            log.info("🔄 Token expiring soon for page: {}, refreshing...", pageId);
            return refreshToken(connection);
        }
        
        log.debug("✅ Token is valid for page: {}", pageId);
        return connection.getPageAccessToken();
    }

    /**
     * Check if token is expired or will expire within 1 hour
     */
    private boolean isTokenExpiringSoon(FacebookConnection connection) {
        if (connection.getTokenExpiresAt() == null) {
            log.warn("⚠️ No expiry info for page: {}, assuming valid", connection.getPageId());
            return false;
        }
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneHourFromNow = now.plusHours(1);
        
        boolean isExpiring = connection.getTokenExpiresAt().isBefore(oneHourFromNow);
        
        if (isExpiring) {
            log.info("⏰ Token expires at: {}, current time: {}", 
                connection.getTokenExpiresAt(), now);
        }
        
        return isExpiring;
    }

    /**
     * Refresh token using Facebook API
     */
    @Transactional
    public String refreshToken(FacebookConnection connection) {
        String pageId = connection.getPageId();
        
        try {
            log.info("🔄 Starting token refresh for page: {}", pageId);
            
            // Check if we have user access token for refresh
            if (connection.getUserAccessToken() == null || connection.getUserAccessToken().isEmpty()) {
                log.error("❌ No user access token available for refresh, page: {}", pageId);
                // Return old token and let API handle the error
                return connection.getPageAccessToken();
            }

            // Call Facebook API to refresh page token
            String newPageToken = facebookApiService.refreshPageAccessToken(
                connection.getUserAccessToken()
            );
            
            if (newPageToken == null || newPageToken.isEmpty()) {
                log.error("❌ Received empty token from Facebook API, page: {}", pageId);
                return connection.getPageAccessToken();
            }

            // Update connection with new token
            updateConnectionToken(connection, newPageToken);
            
            log.info("✅ Successfully refreshed token for page: {}", pageId);
            return newPageToken;
            
        } catch (Exception e) {
            log.error("❌ Failed to refresh token for page: {}", pageId, e);
            
            // Log specific error types
            if (e.getMessage() != null) {
                if (e.getMessage().contains("OAuthException")) {
                    log.error("🔐 OAuth error - user access token might be expired, page: {}", pageId);
                } else if (e.getMessage().contains("invalid_token")) {
                    log.error("🔑 Invalid token error, page: {}", pageId);
                }
            }
            
            // Return old token and let the calling service handle the error
            return connection.getPageAccessToken();
        }
    }

    /**
     * Update connection with new token info
     */
    private void updateConnectionToken(FacebookConnection connection, String newToken) {
        connection.setPageAccessToken(newToken);
        connection.setTokenUpdatedAt(LocalDateTime.now());
        
        // Calculate new expiry (Facebook page tokens typically last 60 days)
        LocalDateTime newExpiry = LocalDateTime.now().plusDays(60);
        connection.setTokenExpiresAt(newExpiry);
        
        connectionRepository.save(connection);
        
        log.info("💾 Updated token for page: {}, new expiry: {}", 
            connection.getPageId(), newExpiry);
    }

    /**
     * Manually trigger token refresh (for admin/health check)
     */
    @Transactional
    public boolean forceRefreshToken(String pageId) {
        try {
            FacebookConnection connection = connectionRepository.findByPageIdForWebhook(pageId)
                .orElseThrow(() -> new RuntimeException("No active connection found for page: " + pageId));
            
            String newToken = refreshToken(connection);
            
            // Check if refresh was successful
            return !newToken.equals(connection.getPageAccessToken());
            
        } catch (Exception e) {
            log.error("❌ Force refresh failed for page: {}", pageId, e);
            return false;
        }
    }

    /**
     * Get token status for monitoring
     */
    @Transactional(readOnly = true)
    public TokenStatus getTokenStatus(String pageId) {
        Optional<FacebookConnection> connectionOpt = connectionRepository.findByPageIdForWebhook(pageId);
        
        if (connectionOpt.isEmpty()) {
            return TokenStatus.NOT_FOUND;
        }
        
        FacebookConnection connection = connectionOpt.get();
        
        if (connection.getTokenExpiresAt() == null) {
            return TokenStatus.UNKNOWN;
        }
        
        LocalDateTime now = LocalDateTime.now();
        
        if (connection.getTokenExpiresAt().isBefore(now)) {
            return TokenStatus.EXPIRED;
        } else if (connection.getTokenExpiresAt().isBefore(now.plusHours(1))) {
            return TokenStatus.EXPIRING_SOON;
        } else {
            return TokenStatus.VALID;
        }
    }

    public enum TokenStatus {
        VALID,
        EXPIRING_SOON,
        EXPIRED,
        UNKNOWN,
        NOT_FOUND
    }
}
