package com.chatbot.core.identity.security;

import com.chatbot.core.identity.security.CustomUserDetails;
import com.chatbot.core.identity.service.AuthService;
import com.chatbot.core.identity.service.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final AuthService authService;
    private final ObjectMapper objectMapper;

    public JwtFilter(JwtService jwtService, AuthService authService) {
        this.jwtService = jwtService;
        this.authService = authService;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                  HttpServletResponse response, 
                                  FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        String token = null;
        String email = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            try {
                email = jwtService.getEmailFromToken(token);
            } catch (ExpiredJwtException e) {
                sendErrorResponse(response, "Token đã hết hạn");
                return;
            } catch (SignatureException | MalformedJwtException e) {
                sendErrorResponse(response, "Token không hợp lệ");
                return;
            } catch (Exception e) {
                sendErrorResponse(response, "Lỗi khi xử lý token");
                return;
            }
        }

        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                CustomUserDetails userDetails = (CustomUserDetails) authService.loadUserByUsername(email);

                if (jwtService.validateToken(token, userDetails)) {
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
                    
                    // Log thông tin xác thực
                    logger.info("Đã xác thực user: " + email + ", roles: " + 
                              userDetails.getAuthorities());
                }
            } catch (ClassCastException e) {
                logger.error("Lỗi khi ép kiểu UserDetails sang CustomUserDetails", e);
                sendErrorResponse(response, "Lỗi xác thực người dùng");
                return;
            } catch (Exception e) {
                logger.error("Lỗi khi xác thực người dùng", e);
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