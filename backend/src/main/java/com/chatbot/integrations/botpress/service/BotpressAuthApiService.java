package com.chatbot.integrations.botpress.service;

import com.chatbot.integrations.botpress.dto.auth.BotpressRegisterRequest;
import com.chatbot.integrations.botpress.dto.auth.BotpressRegisterResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class BotpressAuthApiService {

    @Value("${botpress.api.url}")
    private String botpressApiUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper mapper = new ObjectMapper();

    public BotpressAuthApiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /** Gọi API /api/v1/auth/register để tạo user mới trong Botpress */
    public BotpressRegisterResponse registerUser(String email, String password) {
        String url = String.format("%s/api/v1/auth/register", botpressApiUrl);
        log.info("➡️ [DEBUG] Register user API: {}", url);

        try {
            BotpressRegisterRequest request = new BotpressRegisterRequest(email, password);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<BotpressRegisterRequest> entity = new HttpEntity<>(request, headers);

            ResponseEntity<BotpressRegisterResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    BotpressRegisterResponse.class
            );

            log.info("✅ [DEBUG] Register user success: {}", response.getBody());
            return response.getBody();

        } catch (HttpClientErrorException e) {
            log.error("❌ Botpress API error (register user): {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            // Parse lỗi thành object để tránh null pointer
            BotpressRegisterResponse errorRes = new BotpressRegisterResponse();
            errorRes.setEmail(email);
            return errorRes;
        } catch (Exception e) {
            log.error("❌ Unexpected error (register user): {}", e.getMessage(), e);
            return null;
        }
    }

    /** Gọi API /api/v2/admin/auth/login/basic/{workspace} để login user */
    public BotpressRegisterResponse loginUser(String workspace, String email, String password) {
        String url = String.format("%s/api/v2/admin/auth/login/basic/%s", botpressApiUrl, workspace);
        log.info("➡️ [DEBUG] Login user API: {}", url);

        try {
            BotpressRegisterRequest request = new BotpressRegisterRequest(email, password);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<BotpressRegisterRequest> entity = new HttpEntity<>(request, headers);

            ResponseEntity<BotpressRegisterResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    BotpressRegisterResponse.class
            );

            log.info("✅ [DEBUG] Login user success: {}", response.getBody());
            return response.getBody();

        } catch (HttpClientErrorException e) {
            log.error("❌ Botpress API error (login user): {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            BotpressRegisterResponse errorRes = new BotpressRegisterResponse();
            errorRes.setEmail(email);
            return errorRes;
        } catch (Exception e) {
            log.error("❌ Unexpected error (login user): {}", e.getMessage(), e);
            return null;
        }
    }
}
