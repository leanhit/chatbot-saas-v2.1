package com.chatbot.configs;

import com.chatbot.core.identity.security.JwtFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
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
import org.springframework.web.filter.OncePerRequestFilter;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@EnableWebMvc
public class SecurityConfig {

    // ===================== PASSWORD =====================
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ===================== AUTH PROVIDER =====================
    @Bean
    public AuthenticationProvider authenticationProvider(UserDetailsService userDetailsService) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

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

        // ✅ Restrict to specific domains for production
        config.setAllowedOriginPatterns(List.of(
            "http://localhost:*",
            "https://*.truyenthongviet.vn",
            "https://truyenthongviet.vn",
            "https://*.yourdomain.com",
            "https://yourdomain.com"
        ));
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
            JwtFilter jwtFilter,
            AuthenticationProvider authenticationProvider,
            OncePerRequestFilter securityHeadersFilter
    ) throws Exception {

        http
            // ❌ Không dùng CSRF cho API stateless
            .csrf(AbstractHttpConfigurer::disable)

            // ✅ Enable CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            .authorizeHttpRequests(auth -> auth
                // =================================================
                // ⭐⭐⭐ BẮT BUỘC: CHO PHÉP PREFLIGHT ⭐⭐⭐
                // =================================================
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // ================= PUBLIC APIs =================
                // Most specific paths first
                .requestMatchers("/auth/**", "/api/auth/**", "/error").permitAll()
                .requestMatchers("/penny/bots/*/chat/public").permitAll()
                .requestMatchers("/webhooks/facebook/botpress/**").permitAll()
                .requestMatchers("/images/public/**").permitAll()
                .requestMatchers("/ws/takeover/**").permitAll()
                
                // ================= SWAGGER UI =================
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .requestMatchers("/api/swagger-ui/**", "/api/v3/api-docs/**").permitAll()

                // ================= TENANT ENDPOINTS (SPECIFIC FIRST) =================
                .requestMatchers(HttpMethod.GET, "/tenants/me").authenticated()
                .requestMatchers(HttpMethod.POST, "/tenants").authenticated()
                .requestMatchers(HttpMethod.GET, "/tenants").authenticated()

                // ================= TENANT CONTEXT REQUIRED =================
                .requestMatchers("/tenants/create").authenticated()
                .requestMatchers("/tenants/update").authenticated()
                .requestMatchers("/tenants/delete").authenticated()
                .requestMatchers("/v1/tenant/**").authenticated()
                .requestMatchers("/v1/user-info/**").authenticated()

                // ================= DEFAULT =================
                .anyRequest().authenticated()
            )

            .authenticationProvider(authenticationProvider);

        // JWT filter
        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        
        // Security headers filter
        http.addFilterBefore(securityHeadersFilter, JwtFilter.class);

        return http.build();
    }
}
