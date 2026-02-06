package com.chatbot.modules.app.core.guard;

import com.chatbot.modules.app.core.model.AppCode;

/**
 * Interface for App Service Guard
 * Validates tenant access, app availability, billing, and wallet status
 * Throws exception on failure, returns context only on success
 */
public interface AppServiceGuard {
    
    /**
     * Validate guard request and return pass context
     * Throws GuardException if validation fails
     * 
     * @param request the guard request containing tenant, user, app, and action
     * @return GuardPassContext with pricing and access info (only on success)
     * @throws GuardException if validation fails
     */
   GuardPassContext check(GuardRequest request);
    
    /**
     * Check if specific app code is supported by this guard
     * 
     * @param appCode the app code to check
     * @return true if supported, false otherwise
     */
     boolean supports(AppCode appCode);
}
