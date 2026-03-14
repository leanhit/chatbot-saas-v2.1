package com.chatbot.spokes.facebook.api.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class FacebookApiService {

    private final WebClient webClient;
    
    @Value("${facebook.api.base-url:https://graph.facebook.com}")
    private String facebookApiBaseUrl;
    
    @Value("${facebook.api.version:v18.0}")
    private String apiVersion;

    @Value("${facebook.autoConnect.appId}")
    private String appId;

    @Value("${facebook.autoConnect.appSecret}")
    private String appSecret;

    public FacebookApiService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
            .baseUrl(facebookApiBaseUrl)
            .build();
    }

    // ✅ Lấy danh sách fanpage user quản lý (from traloitudongV2)
    public List<Map<String, Object>> getUserPages(String userAccessToken) {
        try {
            String longLivedToken = getLongLivedUserToken(userAccessToken);
            if (longLivedToken == null) {
                log.error("❌ Không thể convert user token dài hạn");
                return List.of();
            }

            Map<String, Object> response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/me/accounts")
                            .queryParam("access_token", longLivedToken)
                            .build())
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .block();

            if (response != null && response.get("data") instanceof List) {
                log.info("✅ Lấy danh sách fanpage thành công");
                return (List<Map<String, Object>>) response.get("data");
            }
        } catch (Exception e) {
            log.error("❌ Lỗi khi lấy fanpage từ Facebook: " + e.getMessage());
        }
        return List.of();
    }

    // ✅ Convert user token ngắn hạn thành dài hạn (from traloitudongV2)
    private String getLongLivedUserToken(String shortLivedToken) {
        try {
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
                log.info("✅ Convert user token dài hạn thành công");
                return (String) response.get("access_token");
            }
        } catch (Exception e) {
            log.error("❌ Lỗi convert user token dài hạn: " + e.getMessage());
        }
        return null;
    }

    // ✅ Lấy fbUserId từ token (from traloitudongV2)
    public String getUserIdFromToken(String userAccessToken) {
        try {
            Map<String, Object> response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/me")
                            .queryParam("fields", "id")
                            .queryParam("access_token", userAccessToken)
                            .build())
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .block();

            if (response != null && response.containsKey("id")) {
                return (String) response.get("id");
            }
        } catch (Exception e) {
            log.error("❌ Lỗi khi lấy fbUserId từ token: " + e.getMessage());
        }
        return null;
    }

    // ✅ Đăng ký webhook (from traloitudongV2)
    public void subscribePageToWebhook(String pageId, String pageAccessToken) {
        try {
            Map<String, Object> formData = Map.of(
                "subscribed_fields", "messages,messaging_postbacks"
            );

            webClient.post()
                    .uri(uriBuilder -> uriBuilder.path("/{pageId}/subscribed_apps")
                            .queryParam("access_token", pageAccessToken)
                            .build(pageId))
                    .bodyValue(formData)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();

            log.info("✅ Đã đăng ký webhook cho page: " + pageId);
        } catch (Exception e) {
            log.error("❌ Lỗi khi đăng ký webhook cho page " + pageId + ": " + e.getMessage());
        }
    }

    /**
     * Refresh page access token using user access token
     * @param userAccessToken Long-lived user access token
     * @return New page access token
     */
    public String refreshPageAccessToken(String userAccessToken) {
        log.debug("🔄 Calling Facebook API to refresh page token");
        
        try {
            Map<String, Object> response = webClient.post()
                .uri("/{version}/oauth/access_token", apiVersion)
                .bodyValue(Map.of(
                    "grant_type", "fb_exchange_token",
                    "client_id", appId,
                    "client_secret", appSecret,
                    "fb_exchange_token", userAccessToken
                ))
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(10))
                .retryWhen(Retry.backoff(2, Duration.ofSeconds(1))
                    .maxBackoff(Duration.ofSeconds(5))
                    .doBeforeRetry(retrySignal -> 
                        log.warn("⚠️ Retrying Facebook API call, attempt: {}", 
                            retrySignal.totalRetries() + 1)))
                .block();
            
            if (response != null && response.containsKey("access_token")) {
                String newToken = (String) response.get("access_token");
                Integer expiresIn = (Integer) response.get("expires_in");
                
                log.info("✅ Successfully obtained new token, expires in {} seconds", expiresIn);
                return newToken;
            } else {
                log.error("❌ Invalid response from Facebook API: {}", response);
                throw new RuntimeException("Invalid response from Facebook API");
            }
            
        } catch (WebClientResponseException e) {
            log.error("❌ Facebook API error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            
            if (e.getStatusCode().value() == 400) {
                throw new RuntimeException("OAuthException: Invalid or expired user access token", e);
            } else if (e.getStatusCode().value() == 403) {
                throw new RuntimeException("Forbidden: Insufficient permissions", e);
            } else {
                throw new RuntimeException("Facebook API error: " + e.getMessage(), e);
            }
            
        } catch (Exception e) {
            log.error("❌ Failed to refresh page access token", e);
            throw new RuntimeException("Failed to refresh page access token", e);
        }
    }

    /**
     * Validate page access token
     * @param pageAccessToken Page access token to validate
     * @return Token info if valid
     */
    public Map<String, Object> validatePageAccessToken(String pageAccessToken) {
        log.debug("🔍 Validating page access token");
        
        try {
            Map<String, Object> response = webClient.get()
                .uri("/{version}/debug_token", apiVersion)
                .header("Authorization", "Bearer " + pageAccessToken)
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(5))
                .block();
            
            if (response != null && response.containsKey("data")) {
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) response.get("data");
                
                boolean isValid = (Boolean) data.get("is_valid");
                if (isValid) {
                    log.info("✅ Page access token is valid");
                    return data;
                } else {
                    log.warn("⚠️ Page access token is invalid");
                    return data;
                }
            } else {
                log.error("❌ Invalid validation response: {}", response);
                throw new RuntimeException("Invalid validation response");
            }
            
        } catch (WebClientResponseException e) {
            log.error("❌ Token validation error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Token validation failed", e);
        } catch (Exception e) {
            log.error("❌ Failed to validate token", e);
            throw new RuntimeException("Token validation failed", e);
        }
    }

    /**
     * Get page info using access token
     * @param pageAccessToken Page access token
     * @return Page information
     */
    public Map<String, Object> getPageInfo(String pageAccessToken) {
        log.debug("📄 Getting page info");
        
        try {
            Map<String, Object> response = webClient.get()
                .uri("/{version}/me?fields=id,name,access_token", apiVersion)
                .header("Authorization", "Bearer " + pageAccessToken)
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(5))
                .block();
            
            log.info("✅ Successfully retrieved page info");
            return response;
            
        } catch (Exception e) {
            log.error("❌ Failed to get page info", e);
            throw new RuntimeException("Failed to get page info", e);
        }
    }

    // Helper methods using configuration
    private String getClientId() {
        return appId;
    }
    
    private String getClientSecret() {
        return appSecret;
    }
}
