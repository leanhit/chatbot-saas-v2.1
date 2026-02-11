package com.chatbot.configs;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import com.chatbot.modules.tenant.core.guard.TenantStatusInterceptor;
import com.chatbot.modules.tenant.infra.TenantHibernateInterceptor;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final TenantHibernateInterceptor hibernateInterceptor;
    private final TenantStatusInterceptor statusInterceptor;

    public WebConfig(
            TenantHibernateInterceptor hibernateInterceptor,
            TenantStatusInterceptor statusInterceptor
    ) {
        this.hibernateInterceptor = hibernateInterceptor;
        this.statusInterceptor = statusInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        registry.addInterceptor(hibernateInterceptor)
                .addPathPatterns("/api/**");

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
