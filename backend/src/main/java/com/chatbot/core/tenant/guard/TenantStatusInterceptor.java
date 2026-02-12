package com.chatbot.core.tenant.guard;

import com.chatbot.core.tenant.model.Tenant;
import com.chatbot.core.tenant.model.TenantStatus;
import com.chatbot.core.tenant.service.TenantService;
import com.chatbot.core.tenant.infra.TenantContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class TenantStatusInterceptor implements HandlerInterceptor {

    private final TenantService tenantService;

    public TenantStatusInterceptor(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {

        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return true; // system-level API
        }

        Tenant tenant = tenantService.getTenant(tenantId);

        if (tenant.getStatus() != TenantStatus.ACTIVE) {
            throw new IllegalStateException(
                "Tenant is not active: " + tenant.getStatus()
            );
        }

        return true;
    }
}
