package com.chatbot.configs;

import com.chatbot.core.message.decision.websocket.WebSocketAuthInterceptor;
import com.chatbot.core.presence.websocket.PresenceWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class PresenceWebSocketConfig implements WebSocketConfigurer {

    private final PresenceWebSocketHandler handler;
    private final WebSocketAuthInterceptor authInterceptor;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(handler, "/ws/presence")
                .addInterceptors(authInterceptor)
                .setAllowedOriginPatterns("*");
    }
}
