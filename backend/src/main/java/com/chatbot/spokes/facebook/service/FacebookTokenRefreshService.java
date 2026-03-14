package com.chatbot.spokes.facebook.service;

import com.chatbot.spokes.facebook.connection.model.FacebookConnection;
import com.chatbot.spokes.facebook.connection.repository.FacebookConnectionRepository;
import com.chatbot.spokes.facebook.exception.FacebookTokenException;
import com.chatbot.spokes.facebook.handler.FacebookErrorHandler;
import com.chatbot.core.tenant.infra.TenantContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Unified Facebook Token Refresh Service
 * Handles both proactive scheduled refresh and reactive error-based refresh
 */
@Service
@Slf4j
public class FacebookTokenRefreshService {
    
    private final FacebookConnectionRepository connectionRepository;
    private final FacebookErrorHandler facebookErrorHandler;
    private final WebClient webClient;
    
    @Value("${facebook.autoConnect.appId}")
    private String appId;
    
    @Value("${facebook.autoConnect.appSecret}")
    private String appSecret;

    public FacebookTokenRefreshService(FacebookConnectionRepository connectionRepository,
                                      FacebookErrorHandler facebookErrorHandler,
                                      WebClient webClient) {
        this.connectionRepository = connectionRepository;
        this.facebookErrorHandler = facebookErrorHandler;
        this.webClient = webClient;
    }
    
    // Cache to prevent multiple refresh attempts for same page
    private final Map<String, LocalDateTime> refreshInProgress = new ConcurrentHashMap<>();
    private static final long REFRESH_COOLDOWN_MINUTES = 5;
    
    /**
     * Scheduled proactive refresh - runs daily at 2 AM
     * Refresh tokens that will expire in next 7 days
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void refreshExpiringTokensProactively() {
        log.info("🔄 Starting proactive Facebook token refresh check");
        
        try {
            LocalDateTime expiryThreshold = LocalDateTime.now().plusDays(7);
            List<FacebookConnection> expiringConnections = connectionRepository
                .findTokensExpiringBefore(expiryThreshold);
            
            if (expiringConnections.isEmpty()) {
                log.info("✅ No tokens expiring in next 7 days");
                return;
            }
            
            log.info("📊 Found {} tokens expiring soon, starting refresh", expiringConnections.size());
            
            int successCount = 0;
            int failureCount = 0;
            
            for (FacebookConnection connection : expiringConnections) {
                try {
                    if (refreshToken(connection)) {
                        successCount++;
                    } else {
                        failureCount++;
                    }
                } catch (Exception e) {
                    failureCount++;
                    log.error("❌ Failed to refresh token for page {}: {}", 
                            connection.getPageId(), e.getMessage(), e);
                }
            }
            
            log.info("🏁 Proactive refresh completed. Success: {}, Failures: {}", 
                    successCount, failureCount);
            
        } catch (Exception e) {
            log.error("❌ Error during proactive token refresh", e);
        }
    }
    
    /**
     * Reactive refresh - called when token error is detected
     */
    @Transactional
    public String refreshTokenOnError(FacebookTokenException tokenException) {
        String pageId = tokenException.getPageId();
        log.warn("🔄 Reactive token refresh triggered for page: {}", pageId);
        
        // Check if refresh is already in progress
        if (isRefreshInProgress(pageId)) {
            log.warn("⚠️ Token refresh already in progress for page: {}", pageId);
            return null;
        }
        
        try {
            Optional<FacebookConnection> connectionOpt = findConnectionByPageId(pageId);
            if (connectionOpt.isEmpty()) {
                log.error("❌ Connection not found for page: {}", pageId);
                return null;
            }
            
            FacebookConnection connection = connectionOpt.get();
            if (refreshToken(connection)) {
                log.info("✅ Reactive refresh successful for page: {}", pageId);
                return connection.getPageAccessToken();
            } else {
                log.error("❌ Reactive refresh failed for page: {}", pageId);
                return null;
            }
            
        } catch (Exception e) {
            log.error("❌ Error during reactive token refresh for page {}: {}", 
                    pageId, e.getMessage(), e);
            return null;
        } finally {
            markRefreshComplete(pageId);
        }
    }
    
    /**
     * Core refresh logic - refreshes a single connection's token
     */
    @Transactional
    public boolean refreshToken(FacebookConnection connection) {
        String pageId = connection.getPageId();
        
        try {
            log.debug("🔄 Refreshing token for page: {}", pageId);
            
            // First, try to validate current token
            if (validateCurrentToken(connection.getPageAccessToken())) {
                // Token is still valid, just update timestamps
                connection.setTokenUpdatedAt(LocalDateTime.now());
                connection.setTokenExpiresAt(LocalDateTime.now().plusDays(60));
                connectionRepository.save(connection);
                
                log.debug("✅ Token validation successful for page: {}", pageId);
                return true;
            }
            
            // Token is invalid, try to get fresh token from stored user token
            String freshPageToken = getFreshPageToken(connection);
            if (freshPageToken != null) {
                // Update connection with fresh token
                connection.setPageAccessToken(freshPageToken);
                connection.setTokenUpdatedAt(LocalDateTime.now());
                connection.setTokenExpiresAt(LocalDateTime.now().plusDays(60));
                connectionRepository.save(connection);
                
                log.info("✅ Successfully refreshed token for page: {}", pageId);
                return true;
            }
            
            // Cannot refresh automatically, mark as needing user intervention
            connection.setTokenUpdatedAt(LocalDateTime.now());
            connection.setTokenExpiresAt(LocalDateTime.now()); // Expired
            connectionRepository.save(connection);
            
            log.warn("⚠️ Token requires user intervention for page: {}", pageId);
            return false;
            
        } catch (Exception e) {
            log.error("❌ Error refreshing token for page {}: {}", pageId, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Get fresh page token using stored user credentials
     * This would require storing user's long-lived access token
     */
    private String getFreshPageToken(FacebookConnection connection) {
        try {
            // For now, we'll implement a basic approach using Facebook API
            // In production, this would need:
            // 1. Store user's long-lived access token
            // 2. Use that token to get fresh page tokens
            // 3. Handle user re-authentication if needed
            
            log.debug("🔄 Attempting to get fresh page token for: {}", connection.getPageId());
            
            // Check if we have stored user access token (would need to be added to connection)
            String userAccessToken = getStoredUserAccessToken(connection);
            if (userAccessToken == null) {
                log.warn("⚠️ No stored user access token found for page: {}", connection.getPageId());
                return null;
            }
            
            // Get fresh page token using user's access token
            return getFreshPageTokenFromUserToken(connection.getPageId(), userAccessToken);
            
        } catch (Exception e) {
            log.error("❌ Error getting fresh page token for {}: {}", connection.getPageId(), e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Get stored user access token for the connection
     */
    private String getStoredUserAccessToken(FacebookConnection connection) {
        String userToken = connection.getUserAccessToken();
        if (userToken != null && !userToken.trim().isEmpty()) {
            log.debug("✅ Found stored user access token for page: {}", connection.getPageId());
            return userToken;
        }
        
        log.debug("⚠️ No stored user access token found for page: {}", connection.getPageId());
        return null;
    }
    
    /**
     * Get fresh page token from user's access token
     */
    private String getFreshPageTokenFromUserToken(String pageId, String userAccessToken) {
        try {
            // First convert to long-lived token if needed
            String longLivedToken = getLongLivedUserToken(userAccessToken);
            if (longLivedToken == null) {
                log.warn("⚠️ Failed to get long-lived user token");
                return null;
            }
            
            // Get user's pages with fresh tokens
            Map<String, Object> response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                    .path("/me/accounts")
                    .queryParam("access_token", longLivedToken)
                    .build())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block();
            
            if (response != null && response.get("data") instanceof List) {
                List<Map<String, Object>> pages = (List<Map<String, Object>>) response.get("data");
                
                // Find the specific page
                for (Map<String, Object> page : pages) {
                    String currentPageId = (String) page.get("id");
                    if (pageId.equals(currentPageId)) {
                        String pageToken = (String) page.get("access_token");
                        log.info("✅ Retrieved fresh page token for page: {}", pageId);
                        return pageToken;
                    }
                }
            }
            
            log.warn("⚠️ Page {} not found in user's pages list", pageId);
            return null;
            
        } catch (Exception e) {
            log.error("❌ Error getting fresh page token from user token: {}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Convert short-lived user token to long-lived token
     */
    private String getLongLivedUserToken(String shortLivedToken) {
        try {
            log.debug("🔄 Converting user token to long-lived token");
            
            Map<String, Object> response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                    .path("/oauth/access_token")
                    .queryParam("grant_type", "fb_exchange_token")
                    .queryParam("client_id", appId)
                    .queryParam("client_secret", appSecret)
                    .queryParam("fb_exchange_token", shortLivedToken)
                    .build())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block();

            if (response != null && response.get("access_token") instanceof String) {
                String longLivedToken = (String) response.get("access_token");
                log.debug("✅ Successfully converted to long-lived token");
                return longLivedToken;
            }
            
            log.warn("⚠️ No access token in response");
            return null;
            
        } catch (Exception e) {
            log.error("❌ Error converting user token to long-lived: {}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Validate current token by making a test API call
     */
    private boolean validateCurrentToken(String pageAccessToken) {
        try {
            Map<String, Object> response = webClient.get()
                .uri("/me?fields=id&access_token=" + pageAccessToken)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
            
            return response != null && response.containsKey("id");
            
        } catch (WebClientResponseException e) {
            if (facebookErrorHandler.isTokenError(e.getResponseBodyAsString())) {
                log.debug("Token validation failed: expired or invalid");
                return false;
            }
            throw e;
        } catch (Exception e) {
            log.error("Error validating token", e);
            throw e;
        }
    }
    
    /**
     * Find connection by page ID within current tenant context
     */
    private Optional<FacebookConnection> findConnectionByPageId(String pageId) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            log.error("❌ No tenant context found");
            return Optional.empty();
        }
        
        return connectionRepository.findByTenantIdAndPageId(tenantId, pageId);
    }
    
    /**
     * Check if refresh is already in progress for given page
     */
    private boolean isRefreshInProgress(String pageId) {
        LocalDateTime lastRefresh = refreshInProgress.get(pageId);
        if (lastRefresh == null) {
            return false;
        }
        
        // Check if cooldown period has passed
        return lastRefresh.isAfter(LocalDateTime.now().minusMinutes(REFRESH_COOLDOWN_MINUTES));
    }
    
    /**
     * Mark refresh as complete (remove from cooldown)
     */
    private void markRefreshComplete(String pageId) {
        refreshInProgress.put(pageId, LocalDateTime.now());
    }
    
    /**
     * Get token health status for monitoring
     */
    @Transactional(readOnly = true)
    public TokenHealthStats getTokenHealthStats() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return new TokenHealthStats(0, 0, 0);
        }
        
        List<FacebookConnection> allConnections = connectionRepository
            .findByTenantIdAndIsActiveTrue(tenantId);
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime soonExpiry = now.plusDays(7);
        
        int totalTokens = allConnections.size();
        int expiredTokens = (int) allConnections.stream()
            .filter(c -> c.getTokenExpiresAt() != null && c.getTokenExpiresAt().isBefore(now))
            .count();
        int expiringSoonTokens = (int) allConnections.stream()
            .filter(c -> c.getTokenExpiresAt() != null && 
                        c.getTokenExpiresAt().isAfter(now) && 
                        c.getTokenExpiresAt().isBefore(soonExpiry))
            .count();
        
        return new TokenHealthStats(totalTokens, expiredTokens, expiringSoonTokens);
    }
    
    /**
     * Token health statistics
     */
    public record TokenHealthStats(int totalTokens, int expiredTokens, int expiringSoonTokens) {
        public int getHealthyTokens() {
            return totalTokens - expiredTokens - expiringSoonTokens;
        }
        
        public double getHealthPercentage() {
            return totalTokens > 0 ? (double) getHealthyTokens() / totalTokens * 100 : 100;
        }
    }
}
