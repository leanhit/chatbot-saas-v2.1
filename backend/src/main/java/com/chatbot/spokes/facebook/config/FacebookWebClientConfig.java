package com.chatbot.spokes.facebook.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Facebook WebClient Configuration
 * Following traloitudongV2 pattern
 */
@Configuration
public class FacebookWebClientConfig {

    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024)); // 1MB max
    }
}
