package com.chatbot.configs;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import com.chatbot.core.tenant.core.guard.TenantStatusInterceptor;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final TenantStatusInterceptor statusInterceptor;

    public WebConfig(
            TenantStatusInterceptor statusInterceptor
    ) {
        this.statusInterceptor = statusInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Only add TenantStatusInterceptor (TenantHibernateInterceptor is for Hibernate, not Web MVC)
        registry.addInterceptor(statusInterceptor)
                .addPathPatterns("/api/**");
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
