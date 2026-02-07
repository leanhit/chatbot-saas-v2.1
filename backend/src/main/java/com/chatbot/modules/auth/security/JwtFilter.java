package com.chatbot.modules.auth.security;

import com.chatbot.modules.identity.service.IdentityUserDetailsService;
import com.chatbot.modules.tenant.infra.RequestTenantContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.util.UUID;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class JwtFilter extends OncePerRequestFilter {

    private final JwtTokenConsumer jwtTokenConsumer;
    private final IdentityUserDetailsService identityUserDetailsService;
    private final RequestTenantContext requestTenantContext;
    private final ObjectMapper objectMapper;

    public JwtFilter(JwtTokenConsumer jwtTokenConsumer, 
                     IdentityUserDetailsService identityUserDetailsService,
                     RequestTenantContext requestTenantContext) {
        this.jwtTokenConsumer = jwtTokenConsumer;
        this.identityUserDetailsService = identityUserDetailsService;
        this.requestTenantContext = requestTenantContext;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.equals("/api/identity/login") 
            || path.equals("/api/identity/refresh")
            || path.startsWith("/api/auth/")
            || path.equals("/api/identity/health")
            || path.startsWith("/webhooks/")
            || path.startsWith("/ws/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                  HttpServletResponse response, 
                                  FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        String token = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }

        if (token != null) {
            log.info("JWT Filter: Processing token for request: {}", request.getRequestURI());
            Authentication existingAuth = SecurityContextHolder.getContext().getAuthentication();
            log.info("JWT Filter: Existing authentication: {}", existingAuth != null ? existingAuth.getClass() : "null");
            
            try {
                // Validate token signature and expiration
                if (!jwtTokenConsumer.validateToken(token)) {
                    sendErrorResponse(response, "Token không hợp lệ hoặc đã hết hạn");
                    return;
                }

                // Determine token type and extract information
                boolean isIdentityHubToken = jwtTokenConsumer.isIdentityHubToken(token);
                String identifier;
                UserDetails userDetails;

                if (isIdentityHubToken) {
                    // Identity Hub token: extract userId and tenant_ids
                    UUID userId = jwtTokenConsumer.extractUserId(token);
                    List<UUID> tenantIds = jwtTokenConsumer.extractTenantIds(token);
                    
                    if (userId == null) {
                        sendErrorResponse(response, "Token không có thông tin userId");
                        return;
                    }

                    // Load user by UUID for Identity Hub tokens
                    userDetails = identityUserDetailsService.loadUserByUserId(userId);
                    identifier = userId.toString();

                    // Set tenant context from JWT claims
                    requestTenantContext.setTenantIds(request, tenantIds);
                    log.info("Identity Hub token - User: {}, Tenants: {}", userId, tenantIds);

                } else {
                    // Legacy token: extract email and load user
                    String email = jwtTokenConsumer.extractEmail(token);
                    
                    if (email == null) {
                        sendErrorResponse(response, "Token không có thông tin email");
                        return;
                    }

                    // Load user by email - IdentityUserDetailsService now handles both tables
                    userDetails = identityUserDetailsService.loadUserByUsername(email);
                    identifier = email;
                    
                    log.info("Legacy token - User: {}", email);
                }

                // Set authentication
                UsernamePasswordAuthenticationToken authenticationToken = 
                    new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                    );
                
                authenticationToken.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request)
                );
                
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                
                log.info("JWT Filter: Authentication set successfully for: {}", identifier);

            } catch (ExpiredJwtException e) {
                sendErrorResponse(response, "Token đã hết hạn");
                return;
            } catch (SignatureException | MalformedJwtException e) {
                sendErrorResponse(response, "Token không hợp lệ");
                return;
            } catch (ClassCastException e) {
                log.error("Lỗi khi ép kiểu UserDetails sang CustomUserDetails", e);
                sendErrorResponse(response, "Lỗi xác thực người dùng");
                return;
            } catch (Exception e) {
                log.error("Lỗi khi xác thực người dùng", e);
                sendErrorResponse(response, "Lỗi xác thực người dùng");
                return;
            }
        }
        
        filterChain.doFilter(request, response);
    }

    private void sendErrorResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("error", "Unauthorized");
        errorDetails.put("message", message);
        errorDetails.put("status", HttpServletResponse.SC_UNAUTHORIZED);

        objectMapper.writeValue(response.getWriter(), errorDetails);
    }
}