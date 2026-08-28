package com.chatbot.config;

import com.chatbot.core.identity.security.JwtFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@EnableWebMvc
public class SecurityConfig {

    @org.springframework.beans.factory.annotation.Value("${ALLOWED_ORIGINS:http://localhost,http://localhost:8080,https://*.truyenthongviet.vn,https://truyenthongviet.vn,http://103.149.99.7,http://103.149.99.7:8080,https://103.149.99.7}")
    private String rawAllowedOrigins;

    // ===================== PASSWORD =====================
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ===================== AUTH PROVIDER =====================
    // Spring sẽ tự động configure AuthenticationManager với UserDetailsService
    // Không cần custom AuthenticationProvider khi chỉ dùng JWT + UserDetailsService

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config
    ) throws Exception {
        return config.getAuthenticationManager();
    }

    // ===================== CORS =====================
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        List<String> origins = java.util.Arrays.stream(rawAllowedOrigins.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(java.util.stream.Collectors.toList());

        // Always ensure localhost, server IP and domain patterns are allowed
        if (!origins.contains("http://localhost:*")) origins.add("http://localhost:*");
        if (!origins.contains("http://103.149.99.7:*")) origins.add("http://103.149.99.7:*");
        if (!origins.contains("https://103.149.99.7:*")) origins.add("https://103.149.99.7:*");
        if (!origins.contains("https://*.truyenthongviet.vn")) origins.add("https://*.truyenthongviet.vn");

        config.setAllowedOriginPatterns(origins);
        config.setAllowedMethods(List.of(
                "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
        ));
        config.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "X-Tenant-Key",
                "Cache-Control"
        ));
        config.setExposedHeaders(List.of("X-Tenant-Key", "X-Total-Count"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    // ===================== SECURITY FILTER CHAIN =====================
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtFilter jwtFilter
    ) throws Exception {

        http
            // ❌ Không dùng CSRF cho API stateless
            .csrf(AbstractHttpConfigurer::disable)

            // ✅ Enable CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            .headers(headers -> headers
                .frameOptions(frameOptions -> frameOptions.sameOrigin())
                .httpStrictTransportSecurity(hsts -> hsts
                    .maxAgeInSeconds(31536000)
                    .includeSubDomains(true)
                )
            )

            .authorizeHttpRequests(auth -> auth
                // =================================================
                // ⭐⭐⭐ BẮT BUỘC: CHO PHÉP PREFLIGHT ⭐⭐⭐
                // =================================================
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // ================= PUBLIC APIs =================
                // Most specific paths first
                .requestMatchers("/auth/**", "/api/auth/**", "/error").permitAll()
                .requestMatchers("/actuator/**", "/api/actuator/**", "/health").permitAll()
                .requestMatchers("/penny/bots/*/chat/public").permitAll()
                .requestMatchers("/webhooks/**", "/api/webhooks/**").permitAll()
                .requestMatchers("/webhooks/facebook/botpress/**").permitAll()
                .requestMatchers("/webhooks/facebook/pennybot/**").permitAll()
                .requestMatchers("/api/v1/facebook/webhook/**").permitAll()
                .requestMatchers("/images/public/**", "/api/images/public/**").permitAll()
                .requestMatchers("/ws/**", "/ws/takeover/**", "/ws/presence/**", "/ws/notifications/**").permitAll()
                .requestMatchers("/api/takeover/**").authenticated()
                
                // ================= LOCATION APIs (PUBLIC) =================
                .requestMatchers("/api/locations/**").permitAll()
                
                // ================= SIMPLE PAYMENT APIs (PUBLIC) =================
                .requestMatchers("/api/public/simple-payment/**").permitAll()
                .requestMatchers("/api/simple-payment/public/**").permitAll()
                .requestMatchers("/api/simple-payment/bank-info").permitAll()
                .requestMatchers("/api/simple-payment/health").permitAll()
                // Allow deposit endpoint without authentication for external clients
                .requestMatchers(HttpMethod.POST, "/api/simple-payment/deposit").permitAll()
                
                // ================= PACKAGES APIs (PUBLIC) =================
                .requestMatchers("/api/v1/packages/active").permitAll()
                .requestMatchers("/api/v1/packages/by-package-id/**").permitAll()
                
                // ================= SWAGGER UI =================
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .requestMatchers("/api/swagger-ui/**", "/api/v3/api-docs/**").permitAll()

                // ================= LICENSE ENDPOINTS (SPECIFIC FIRST) =================
                .requestMatchers(HttpMethod.GET, "/api/license/me").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/license/check/feature/**").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/license/check/module/**").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/license/check/limit/**").authenticated()
                
                // ================= TENANT ENDPOINTS (SPECIFIC FIRST) =================
                .requestMatchers(HttpMethod.GET, "/api/tenants/me").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/tenants").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/tenants").authenticated()

                // ================= TENANT CONTEXT REQUIRED =================
                .requestMatchers("/tenants/create").authenticated()
                .requestMatchers("/tenants/update").authenticated()
                .requestMatchers("/tenants/delete").authenticated()
                .requestMatchers("/v1/tenant/**").authenticated()
                .requestMatchers("/v1/user-info/**").authenticated()

                // ================= DEFAULT =================
                .anyRequest().authenticated()
            );

        // JWT filter
        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
