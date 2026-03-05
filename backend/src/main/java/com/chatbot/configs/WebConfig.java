package com.chatbot.configs;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import com.chatbot.core.tenant.guard.TenantStatusInterceptor;
import com.chatbot.core.tenant.guard.TenantContextInterceptor;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final TenantStatusInterceptor statusInterceptor;
    private final TenantContextInterceptor tenantContextInterceptor;

    public WebConfig(
            TenantStatusInterceptor statusInterceptor,
            TenantContextInterceptor tenantContextInterceptor
    ) {
        this.statusInterceptor = statusInterceptor;
        this.tenantContextInterceptor = tenantContextInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Add TenantContextInterceptor first to set tenant context from X-Tenant-Key header
        registry.addInterceptor(tenantContextInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/**", "/api/error", "/api/actuator/**", "/api/public/**");
        
        // Add TenantStatusInterceptor to validate tenant status
        registry.addInterceptor(statusInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/**", "/api/error", "/api/actuator/**");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")  // ✅ Allow all origins
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .exposedHeaders("Authorization", "Content-Type", "X-Tenant-Key");
    }
}
