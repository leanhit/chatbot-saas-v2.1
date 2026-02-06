package com.chatbot.modules.tenant.infra;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final TenantHibernateInterceptor hibernateInterceptor;

    public WebConfig(TenantHibernateInterceptor hibernateInterceptor) {
        this.hibernateInterceptor = hibernateInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(hibernateInterceptor)
                .addPathPatterns("/api/**");
    }
}
