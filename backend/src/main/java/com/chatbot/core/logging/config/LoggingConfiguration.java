package com.chatbot.core.logging.config;

import com.chatbot.core.logging.filter.CorrelationIdFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for logging-related beans and filters.
 */
@Configuration
public class LoggingConfiguration {

    /**
     * Register the CorrelationIdFilter to add correlation IDs to all requests.
     */
    @Bean
    public FilterRegistrationBean<CorrelationIdFilter> correlationIdFilterRegistration() {
        FilterRegistrationBean<CorrelationIdFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new CorrelationIdFilter());
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(1); // High priority to run early
        return registrationBean;
    }
}
