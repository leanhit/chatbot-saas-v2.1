package com.chatbot.modules.tenant.infra;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class TenantHibernateInterceptor implements HandlerInterceptor {

    private final HibernateTenantFilter filter;

    public TenantHibernateInterceptor(HibernateTenantFilter filter) {
        this.filter = filter;
    }

    @Override
    public boolean preHandle(HttpServletRequest req,
                             HttpServletResponse res,
                             Object handler) {
        filter.enable();
        return true;
    }
}
