package com.chatbot.configs;

import com.chatbot.shared.security.RateLimitingFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Configuration
public class RateLimitConfig {

    @Bean
    public FilterRegistrationBean<RateLimitingFilter> rateLimitFilterRegistration(RateLimitingFilter rateLimitingFilter) {
        FilterRegistrationBean<RateLimitingFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(rateLimitingFilter);
        registration.addUrlPatterns("/*"); // Apply to all endpoints, filter will decide which to rate limit
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE); // Execute before other filters
        registration.setName("rateLimitingFilter");
        return registration;
    }
}
