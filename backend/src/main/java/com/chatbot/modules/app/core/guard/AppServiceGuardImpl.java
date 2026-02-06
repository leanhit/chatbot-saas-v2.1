package com.chatbot.modules.app.core.guard;

import com.chatbot.modules.app.core.model.AppCode;
import com.chatbot.modules.tenant.core.model.Tenant;
import com.chatbot.modules.tenant.core.model.TenantStatus;
import com.chatbot.modules.tenant.core.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/**
 * Local-stub implementation of App Service Guard
 * Only supports CHATBOT app with SEND_MESSAGE action
 * Billing and wallet checks are skipped (always allow)
 * Throws exception on failure, returns context only on success
 */
@Service
@Primary
@RequiredArgsConstructor
@Slf4j
public class AppServiceGuardImpl implements AppServiceGuard {
    
    private final TenantRepository tenantRepository;
    private final ChatbotPricingResolver pricingResolver;
    
    @Override
    public GuardPassContext check(GuardRequest request) {
        log.debug("Checking guard request: {}", request);
        
        // Check if this guard supports the app
        if (!supports(request.getAppCode())) {
            throw new GuardException("App not supported by guard: " + request.getAppCode());
        }
        
        // Check if this guard supports the action
        if (!"SEND_MESSAGE".equals(request.getAction())) {
            throw new GuardException("Action not supported: " + request.getAction());
        }
        
        // LOCAL-STUB ONLY: Tenant check - ensure tenant is active
        Tenant tenant = tenantRepository.findById(request.getTenantId())
                .orElseThrow(() -> new GuardException("Tenant not found: " + request.getTenantId()));
        
        if (tenant.getStatus() != TenantStatus.ACTIVE) {
            throw new GuardException("Tenant not active: " + tenant.getStatus());
        }
        
        // LOCAL-STUB ONLY: App enabled check - for CHATBOT, always enabled in local mode
        if (!isAppEnabled(request.getAppCode())) {
            throw new GuardException("App not enabled: " + request.getAppCode());
        }
        
        // LOCAL-STUB ONLY: Billing check - SKIP (always allow)
        // LOCAL-STUB ONLY: Wallet check - SKIP (always allow)
        
        // Pricing resolution
        ChatbotPricingResolver.PricingResult pricing = pricingResolver.resolve(request);
        
        return GuardPassContext.of(
                "Access granted",
                pricing.isBillable(),
                pricing.getCost(),
                pricing.getTier()
        );
    }
    
    @Override
    public boolean supports(AppCode appCode) {
        // Only support CHATBOT app in this local stub
        return AppCode.CHATBOT.equals(appCode);
    }
    
    /**
     * Check if app is enabled for local mode
     * LOCAL-STUB ONLY: In local mode, CHATBOT is always enabled
     */
    private boolean isAppEnabled(AppCode appCode) {
        return AppCode.CHATBOT.equals(appCode);
    }
}
