package com.chatbot.configs;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
// import com.chatbot.modules.tenant.infra.TenantResolverFilter;

// @Configuration
// public class FilterConfig {
//
//    @Bean
//    public FilterRegistrationBean<TenantResolverFilter> tenantFilter(
//            TenantResolverFilter filter
//    ) {
//        FilterRegistrationBean<TenantResolverFilter> bean =
//                new FilterRegistrationBean<>();
//
//        bean.setFilter(filter);
//        bean.setOrder(2); // JWT = 1, Tenant = 2
//        return bean;
//    }
// }
