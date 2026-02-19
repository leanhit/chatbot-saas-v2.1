package com.chatbot.configs;

import com.chatbot.core.message.decision.websocket.TakeoverWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class TakeoverWebSocketConfig implements WebSocketConfigurer {

    private final TakeoverWebSocketHandler handler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(handler, "/ws/takeover")
                .setAllowedOriginPatterns("*");
                //.withSockJS();
    }
}