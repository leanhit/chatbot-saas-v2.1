package com.chatbot.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class FacebookUserConfig {
    
    @Bean
    public WebClient facebookWebClient() {
        return WebClient.builder()
                .baseUrl("https://graph.facebook.com/v18.0")
                .build();
    }
}
