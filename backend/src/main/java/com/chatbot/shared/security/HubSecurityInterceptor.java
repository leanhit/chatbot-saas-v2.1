package com.chatbot.shared.security;

import com.chatbot.shared.dto.TenantContext;
import com.chatbot.shared.exceptions.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class HubSecurityInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();
        String method = request.getMethod();
        
        if (isHubEndpoint(path)) {
            validateHubAccess(path, method);
        }
        
        return true;
    }

    private boolean isHubEndpoint(String path) {
        return path.startsWith("/api/") && 
               (path.contains("/identity/") ||
                path.contains("/user/") ||
                path.contains("/tenant/") ||
                path.contains("/app/") ||
                path.contains("/billing/") ||
                path.contains("/wallet/") ||
                path.contains("/config/") ||
                path.contains("/message/"));
    }

    private void validateHubAccess(String path, String method) {
        String hub = extractHubName(path);
        
        switch (hub) {
            case "identity":
                validateIdentityHub(path, method);
                break;
            case "user":
                validateUserHub(path, method);
                break;
            case "tenant":
                validateTenantHub(path, method);
                break;
            case "app":
                validateAppHub(path, method);
                break;
            case "billing":
                validateBillingHub(path, method);
                break;
            case "wallet":
                validateWalletHub(path, method);
                break;
            case "config":
                validateConfigHub(path, method);
                break;
            case "message":
                validateMessageHub(path, method);
                break;
            default:
                throw new UnauthorizedException("Unknown hub: " + hub);
        }
    }

    private String extractHubName(String path) {
        String[] parts = path.split("/");
        for (int i = 0; i < parts.length; i++) {
            if ("api".equals(parts[i]) && i + 1 < parts.length) {
                return parts[i + 1];
            }
        }
        return "unknown";
    }

    private void validateIdentityHub(String path, String method) {
        if (path.contains("/admin/") && !SecurityUtils.hasRole("ADMIN")) {
            throw new UnauthorizedException("Admin access required");
        }
    }

    private void validateUserHub(String path, String method) {
        if (path.contains("/profile/") && !isOwnerOrAdmin(path)) {
            throw new UnauthorizedException("Access denied to user profile");
        }
    }

    private void validateTenantHub(String path, String method) {
        if (method.equals("POST") || method.equals("PUT") || method.equals("DELETE")) {
            if (!SecurityUtils.hasRole("TENANT_ADMIN")) {
                throw new UnauthorizedException("Tenant admin access required");
            }
        }
    }

    private void validateAppHub(String path, String method) {
        if (path.contains("/registry/") && method.equals("POST")) {
            if (!SecurityUtils.hasRole("SYSTEM_ADMIN")) {
                throw new UnauthorizedException("System admin access required for app registration");
            }
        }
    }

    private void validateBillingHub(String path, String method) {
        if (!method.equals("GET")) {
            throw new UnauthorizedException("Billing hub is read-only");
        }
    }

    private void validateWalletHub(String path, String method) {
        if (path.contains("/transaction/") && 
            (method.equals("POST") || method.equals("PUT"))) {
            if (!SecurityUtils.isAuthenticated()) {
                throw new UnauthorizedException("Authentication required for wallet operations");
            }
        }
    }

    private void validateConfigHub(String path, String method) {
        if (path.contains("/runtime/") && method.equals("POST")) {
            if (!SecurityUtils.hasRole("CONFIG_ADMIN")) {
                throw new UnauthorizedException("Config admin access required");
            }
        }
    }

    private void validateMessageHub(String path, String method) {
        if (path.contains("/router/") && method.equals("POST")) {
            if (!SecurityUtils.hasRole("MESSAGE_ADMIN")) {
                throw new UnauthorizedException("Message admin access required");
            }
        }
    }

    private boolean isOwnerOrAdmin(String path) {
        return SecurityUtils.hasRole("ADMIN") || SecurityUtils.hasRole("USER");
    }
}
