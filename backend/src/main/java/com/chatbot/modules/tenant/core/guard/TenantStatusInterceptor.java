package com.chatbot.modules.tenant.core.guard;

import com.chatbot.modules.tenant.core.model.Tenant;
import com.chatbot.modules.tenant.core.model.TenantStatus;
import com.chatbot.modules.tenant.core.service.TenantService;
import com.chatbot.modules.tenant.infra.TenantContext;
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
