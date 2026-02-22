// src/main/java/com/chatbot/spokes/facebook/webhook/service/FacebookApiGraphService.java
package com.chatbot.spokes.facebook.webhook.service;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class FacebookApiGraphService {

    private final WebClient webClient;

    @Value("${facebook.autoConnect.appId}")
    private String appId;

    @Value("${facebook.autoConnect.appSecret}")
    private String appSecret;

    public FacebookApiGraphService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://graph.facebook.com/v18.0").build();
    }

    /**
     * Lấy user ID từ access token
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
        } catch (Exception e) {
            log.error("Lỗi khi lấy user ID từ token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Lấy danh sách pages của user
     */
    public List<Map<String, Object>> getUserPages(String userAccessToken) {
        try {
            String longLivedToken = getLongLivedUserToken(userAccessToken);
            if (longLivedToken == null) {
                log.error("❌ Không thể convert user token dài hạn");
                return null;
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
            log.error("Lỗi khi lấy danh sách pages: {}", e.getMessage());
            return null;
        }
        return null;
    }

    /**
     * Convert user token short term to long term
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
        } catch (Exception e) {
            log.error("Error converting user token long term: " + e.getMessage());
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
     * Hủy đăng ký page khỏi webhook
     */
    public boolean unsubscribePageFromWebhook(String pageId, String pageAccessToken) {
        try {
            // TODO: Implement webhook unsubscription logic
            log.info("Hủy đăng ký webhook cho page: {}", pageId);
            return true;
        } catch (Exception e) {
            log.error("Lỗi khi hủy đăng ký webhook: {}", e.getMessage());
            return false;
        }
    }
}
