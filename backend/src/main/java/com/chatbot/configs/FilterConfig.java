package com.chatbot.configs;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<CorrelationIdFilter> correlationIdFilterRegistration(
            CorrelationIdFilter filter
    ) {
        FilterRegistrationBean<CorrelationIdFilter> bean =
                new FilterRegistrationBean<>();

        bean.setFilter(filter);
        bean.setOrder(1); // Highest priority - should run first
        bean.addUrlPatterns("/*");
        return bean;
    }
}
