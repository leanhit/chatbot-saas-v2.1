package com.chatbot.core.tenant.grpc;

import com.chatbot.core.identity.service.AuthService;
import com.chatbot.core.identity.service.JwtService;
import com.chatbot.core.identity.security.CustomUserDetails;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.Status;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * gRPC Authentication Interceptor
 * Validates JWT tokens from gRPC metadata before processing any request.
 * 
 * Security: All gRPC mutations require valid JWT authentication.
 * Read-only operations may be allowed without auth for public endpoints.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class GrpcAuthInterceptor implements ServerInterceptor {

    private final JwtService jwtService;
    private final AuthService authService;

    private static final Metadata.Key<String> AUTHORIZATION_KEY = 
        Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER);

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {

        String token = extractToken(headers);

        if (token == null || token.trim().isEmpty()) {
            log.warn("❌ [gRPC Auth] Missing authorization token");
            call.close(Status.UNAUTHENTICATED.withDescription("Missing authorization token"), headers);
            return new ServerCall.Listener<ReqT>() {};
        }

        try {
            String email = jwtService.extractEmail(token);
            if (email != null) {
                CustomUserDetails userDetails = (CustomUserDetails) authService.loadUserByUsername(email);
                if (jwtService.validateToken(token, userDetails)) {
                    log.info("✅ [gRPC Auth] Authenticated user: {} for method: {}", email, call.getMethodDescriptor().getFullMethodName());
                    return next.startCall(call, headers);
                }
            }
        } catch (Exception e) {
            log.error("❌ [gRPC Auth] Token validation failed: {}", e.getMessage());
        }

        log.warn("❌ [gRPC Auth] Invalid token for method: {}", call.getMethodDescriptor().getFullMethodName());
        call.close(Status.UNAUTHENTICATED.withDescription("Invalid or expired token"), headers);
        return new ServerCall.Listener<ReqT>() {};
    }

    private String extractToken(Metadata headers) {
        String authHeader = headers.get(AUTHORIZATION_KEY);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return authHeader;
    }
}
