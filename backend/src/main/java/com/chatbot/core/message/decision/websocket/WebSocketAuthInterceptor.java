package com.chatbot.core.message.decision.websocket;

import com.chatbot.core.identity.security.CustomUserDetails;
import com.chatbot.core.identity.service.AuthService;
import com.chatbot.core.identity.service.JwtService;
import com.chatbot.core.tenant.service.TenantService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * WebSocketAuthInterceptor - Validates JWT token from WebSocket handshake query parameters.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketAuthInterceptor implements HandshakeInterceptor {

    private final JwtService jwtService;
    private final AuthService authService;
    private final TenantService tenantService;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        if (request instanceof ServletServerHttpRequest) {
            ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
            HttpServletRequest httpServletRequest = servletRequest.getServletRequest();
            
            String token = httpServletRequest.getParameter("token");
            String tenantKey = httpServletRequest.getParameter("tenantKey");

            if (token == null || token.trim().isEmpty()) {
                log.warn("❌ [WebSocket Handshake] Missing token in request parameters");
                return false;
            }

            try {
                String email = jwtService.extractEmail(token);
                if (email != null) {
                    CustomUserDetails userDetails = (CustomUserDetails) authService.loadUserByUsername(email);
                    if (jwtService.validateToken(token, userDetails)) {
                        attributes.put("email", email);
                        attributes.put("userId", userDetails.getUser().getId());
                        attributes.put("fullName", userDetails.getUser().getProfile() != null ? 
                            userDetails.getUser().getProfile().getFullName() : email);
                        
                        if (tenantKey != null && !tenantKey.trim().isEmpty()) {
                            Long tenantId = tenantService.getTenantIdByKey(tenantKey);
                            if (tenantId != null) {
                                attributes.put("tenantId", tenantId);
                                log.info("✅ [WebSocket Handshake] Authenticated user {} (ID: {}) for tenant {} (ID: {})", 
                                        email, userDetails.getUser().getId(), tenantKey, tenantId);
                            } else {
                                log.warn("❌ [WebSocket Handshake] Tenant not found for key: {}", tenantKey);
                            }
                        } else {
                            log.info("✅ [WebSocket Handshake] Authenticated user {} (ID: {}) without tenant context", 
                                    email, userDetails.getUser().getId());
                        }
                        return true;
                    }
                }
            } catch (Exception e) {
                log.error("❌ [WebSocket Handshake] Authentication failed: {}", e.getMessage());
            }
        }
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // No-op
    }
}
