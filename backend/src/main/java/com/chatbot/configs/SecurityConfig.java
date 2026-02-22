package com.chatbot.configs;

import com.chatbot.core.identity.security.JwtFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
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

        // ⚠️ Dùng allowedOriginPatterns khi allowCredentials = true
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of(
                "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
        ));
        config.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "X-Tenant-Key",
                "Cache-Control"
        ));
        config.setExposedHeaders(List.of("*"));
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
            AuthenticationProvider authenticationProvider
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
                .requestMatchers("/api/auth/**", "/error").permitAll()
                .requestMatchers("/api/penny/bots/*/chat/public").permitAll()
                .requestMatchers("/webhooks/facebook/botpress/**").permitAll()
                .requestMatchers("/api/images/public/**").permitAll()
                .requestMatchers("/ws/takeover/**").permitAll()
                
                // ================= SWAGGER UI =================
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .requestMatchers("/api/swagger-ui/**", "/api/v3/api-docs/**").permitAll()

                // ================= TENANT (MASTER LEVEL) =================
                .requestMatchers(HttpMethod.POST, "/api/tenants").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/tenants").authenticated()

                // ================= TENANT CONTEXT REQUIRED =================
                .requestMatchers("/api/tenants/**").authenticated()
                .requestMatchers("/api/v1/tenant/**").authenticated()
                .requestMatchers("/api/v1/user-info/**").authenticated()

                // ================= DEFAULT =================
                .anyRequest().authenticated()
            )

            .authenticationProvider(authenticationProvider)

            // JWT filter
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
