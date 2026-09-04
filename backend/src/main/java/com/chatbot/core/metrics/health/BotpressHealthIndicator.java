package com.chatbot.core.metrics.health;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Custom health indicator for Botpress API.
 * Checks if Botpress API is responsive and can handle requests.
 */
@Component
@ConditionalOnProperty(name = "app.integrations.botpress.enabled", havingValue = "true", matchIfMissing = false)
public class BotpressHealthIndicator implements HealthIndicator {

    private final WebClient webClient;
    private final String botpressApiUrl;

    public BotpressHealthIndicator(
            WebClient webClient,
            @Value("${app.integrations.botpress.api-url:http://localhost:3001}") String botpressApiUrl) {
        this.webClient = webClient;
        this.botpressApiUrl = botpressApiUrl;
    }

    @Override
    public Health health() {
        try {
            String healthUrl = botpressApiUrl + "/api/v1/health";
            String response = webClient.get()
                .uri(healthUrl)
                .retrieve()
                .bodyToMono(String.class)
                .block();
            
            if (response != null && !response.isEmpty()) {
                return Health.up()
                    .withDetail("status", "Botpress API is responsive")
                    .withDetail("apiUrl", botpressApiUrl)
                    .withDetail("response", response)
                    .build();
            } else {
                return Health.down()
                    .withDetail("status", "Botpress API returned empty response")
                    .build();
            }
        } catch (Exception e) {
            return Health.down()
                .withDetail("status", "Botpress API connection failed")
                .withDetail("apiUrl", botpressApiUrl)
                .withDetail("error", e.getMessage())
                .build();
        }
    }
}
