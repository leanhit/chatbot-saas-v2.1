package com.chatbot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Configuration
public class SecurityHeadersConfig implements WebMvcConfigurer {

    @org.springframework.beans.factory.annotation.Value("${ALLOWED_ORIGINS:http://localhost:3003,http://localhost:8080}")
    private String rawAllowedOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        List<String> origins = Arrays.stream(rawAllowedOrigins.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
        registry.addMapping("/**")
                .allowedOriginPatterns(origins.toArray(new String[0]))
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("Authorization", "Content-Type", "X-Tenant-Key", "X-API-Key", "Accept-Language", "Cache-Control")
                .exposedHeaders("X-Tenant-Key", "X-Total-Count")
                .allowCredentials(true);
    }

    @Bean
    public OncePerRequestFilter securityHeadersFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request, 
                                          HttpServletResponse response, 
                                          FilterChain filterChain) throws ServletException, IOException {
                
                // Security Headers
                response.setHeader("X-Content-Type-Options", "nosniff");
                response.setHeader("X-Frame-Options", "SAMEORIGIN");
                response.setHeader("X-XSS-Protection", "1; mode=block");
                response.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
                response.setHeader("Content-Security-Policy", 
                    "default-src 'self'; " +
                    "script-src 'self' 'unsafe-inline' 'unsafe-eval' https://connect.facebook.net https://*.facebook.com https://*.facebook.net; " +
                    "style-src 'self' 'unsafe-inline' https:; " +
                    "img-src 'self' data: https: blob: https://*.facebook.com https://*.fbcdn.net; " +
                    "font-src 'self' data: https:; " +
                    "connect-src 'self' https://*.facebook.com https://*.facebook.net https://graph.facebook.com https://truyenthongviet.vn https://*.truyenthongviet.vn wss://truyenthongviet.vn wss://*.truyenthongviet.vn https://startai.vn https://*.startai.vn wss://startai.vn wss://*.startai.vn; " +
                    "frame-src 'self' https://*.facebook.com https://*.facebook.net https://staticxx.facebook.com;");
                response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
                response.setHeader("Permissions-Policy", 
                    "geolocation=(), " +
                    "microphone=(), " +
                    "camera=(), " +
                    "payment=(), " +
                    "usb=(), " +
                    "magnetometer=(), " +
                    "gyroscope=(), " +
                    "accelerometer=()");
                
                // Remove server information
                response.setHeader("Server", "");
                
                filterChain.doFilter(request, response);
            }
        };
    }
}
