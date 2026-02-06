package com.chatbot.modules.identity.service;

/**
 * Bridge service for creating Identity Hub users from legacy auth system
 * This service handles the integration between the legacy authentication system
 * and the new Identity Hub system.
 */
public interface IdentityBridgeService {
    
    /**
     * Creates a user in Identity Hub system based on legacy auth data
     * @param email User email from legacy system
     * @param password User password from legacy system
     * @return true if user creation was successful, false otherwise
     */
    boolean createIdentityUser(String email, String password);
    
    /**
     * Checks if a user exists in Identity Hub system
     * @param email User email to check
     * @return true if user exists, false otherwise
     */
    boolean identityUserExists(String email);
    
    /**
     * Synchronizes user data between legacy and Identity Hub systems
     * @param email User email to synchronize
     * @return true if synchronization was successful, false otherwise
     */
    boolean synchronizeUserData(String email);
}
