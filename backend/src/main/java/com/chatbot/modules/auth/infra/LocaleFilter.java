package com.chatbot.modules.auth.infra;

import com.chatbot.modules.auth.security.JwtTokenConsumer;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter to extract locale from JWT token and set it in RequestContext
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class LocaleFilter extends OncePerRequestFilter {
    
    private final JwtTokenConsumer jwtTokenConsumer;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                  HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        // Extract locale from JWT token if present
        String token = extractToken(request);
        if (token != null && jwtTokenConsumer.validateToken(token)) {
            String locale = jwtTokenConsumer.extractLocale(token);
            RequestContext.setLocale(locale);
        } else {
            // Set default locale
            RequestContext.setLocale("vi");
        }
        
        try {
            filterChain.doFilter(request, response);
        } finally {
            // Clear locale after request
            RequestContext.clearLocale();
        }
    }
    
    /**
     * Extract JWT token from Authorization header
     */
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
