package com.chatbot.spokes.facebook.webhook.service;

import com.chatbot.spokes.facebook.handler.FacebookErrorHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class FacebookApiGraphService {

    private final WebClient webClient;
    private final FacebookErrorHandler facebookErrorHandler;

    @Value("${facebook.autoConnect.appId}")
    private String appId;

    @Value("${facebook.autoConnect.appSecret}")
    private String appSecret;

    public FacebookApiGraphService(WebClient.Builder webClientBuilder, FacebookErrorHandler facebookErrorHandler) {
        this.webClient = webClientBuilder.baseUrl("https://graph.facebook.com/v18.0").build();
        this.facebookErrorHandler = facebookErrorHandler;
    }

    /**
     * Lấy user ID từ access token với unified error handling
     */
    public String getUserIdFromToken(String userAccessToken) {
        try {
            Map<String, Object> response = webClient.get()
                    .uri("/me?fields=id")
                    .header("Authorization", "Bearer " + userAccessToken)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .block();

            return (String) response.get("id");
        } catch (WebClientResponseException e) {
            logFacebookApiError("Failed to get user ID from token", e);
            return null;
        } catch (Exception e) {
            log.error("Unexpected error getting user ID from token: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Lấy danh sách pages của user với unified error handling
     */
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
        } catch (WebClientResponseException e) {
            logFacebookApiError("Failed to get user pages", e);
        } catch (Exception e) {
            log.error("Unexpected error getting user pages: {}", e.getMessage(), e);
        }
        return List.of();
    }

    /**
     * Convert user token short term to long term với unified error handling
     */
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
                log.info("Convert user token long term success");
                return (String) response.get("access_token");
            }
        } catch (WebClientResponseException e) {
            logFacebookApiError("Failed to convert user token to long-lived", e);
        } catch (Exception e) {
            log.error("Unexpected error converting user token: {}", e.getMessage(), e);
        }
        return null;
    }

    /**
     * Đăng ký page vào webhook
     */
    public boolean subscribePageToWebhook(String pageId, String pageAccessToken) {
        try {
            // TODO: Implement webhook subscription logic
            log.info("Đăng ký webhook cho page: {}", pageId);
            return true;
        } catch (Exception e) {
            log.error("Lỗi khi đăng ký webhook: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Hủy đăng ký page khỏi webhook với unified error handling
     */
    public boolean unsubscribePageFromWebhook(String pageId, String pageAccessToken) {
        try {
            webClient.delete()
                    .uri(uriBuilder -> uriBuilder.path("/{pageId}/subscribed_apps")
                            .queryParam("access_token", pageAccessToken)
                            .build(pageId))
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();

            log.info("✅ Đã hủy webhook cho page: {}", pageId);
            return true;
        } catch (WebClientResponseException e) {
            logFacebookApiError("Failed to unsubscribe page from webhook", e);
            return false;
        } catch (Exception e) {
            log.error("Unexpected error unsubscribing webhook for page {}: {}", pageId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Helper method for unified Facebook API error logging
     */
    private void logFacebookApiError(String operation, WebClientResponseException e) {
        if (facebookErrorHandler.isTokenError(e)) {
            log.warn("🔑 Token error during {}: {}", operation, facebookErrorHandler.extractErrorMessage(e.getResponseBodyAsString()));
        } else {
            log.error("❌ Facebook API error during {}: {}", operation, e.getResponseBodyAsString(), e);
        }
    }
}
