package com.chatbot.spokes.botpress.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class BotpressApiService {

    @Value("${app.integrations.botpress.api-url}")
    private String botpressApiUrl;

    @Value("${app.integrations.botpress.admin-token}")
    private String botpressAdminToken;

    private final RestTemplate restTemplate;
    private final ObjectMapper mapper = new ObjectMapper();

    private static final String WORKSPACE_ID = "default";

    public BotpressApiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(botpressAdminToken);
        headers.add("X-BP-Workspace", WORKSPACE_ID);
        return headers;
    }

    /** DTO nhỏ gọn để trả về danh sách bot */
    public static class BotInfo {
        private String id;
        private String name;

        public BotInfo(String id, String name) {
            this.id = id;
            this.name = name;
        }
        public String getId() { return id; }
        public String getName() { return name; }
    }

    // ======================== MESSAGING ========================

    // Gửi tin nhắn đến Botpress
    public Map<String, Object> sendMessage(String botId, String senderId, String message) {
        String url = String.format("%s/api/v1/admin/bots/%s/converse", botpressApiUrl, botId);
        log.info("📦 [DEBUG] Send message API invoked: " + url);

        try {
            JsonNode jsonNode = mapper.readTree(String.format("{\"type\":\"text\",\"text\":\"%s\",\"channel\":\"web\",\"target\":\"%s\"}", message, senderId));
            ResponseEntity<JsonNode> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    new HttpEntity<>(jsonNode, createHeaders()),
                    JsonNode.class
            );
            
            // Convert JsonNode to Map
            ObjectMapper mapper = new ObjectMapper();
            return mapper.convertValue(response.getBody(), Map.class);
        } catch (Exception e) {
            log.info(" [DEBUG] Error: " + e);
            return null;
        }
    }

    // Gửi event đến Botpress
    public Map<String, Object> sendEvent(String botId, String senderId, String eventType, Map<String, Object> payload) {
        String url = String.format("%s/api/v1/admin/bots/%s/converse", botpressApiUrl, botId);
        log.info("📦 [DEBUG] Send event API invoked: " + url);

        try {
            // Create event payload
            Map<String, Object> eventPayload = new HashMap<>();
            eventPayload.put("type", eventType);
            eventPayload.put("channel", "web");
            eventPayload.put("target", senderId);
            eventPayload.putAll(payload);

            JsonNode jsonNode = mapper.valueToTree(eventPayload);
            ResponseEntity<JsonNode> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    new HttpEntity<>(jsonNode, createHeaders()),
                    JsonNode.class
            );
            
            // Convert JsonNode to Map
            ObjectMapper mapper = new ObjectMapper();
            return mapper.convertValue(response.getBody(), Map.class);
        } catch (Exception e) {
            log.info("📦 [DEBUG] Error: " + e);
            return null;
        }
    }
}
