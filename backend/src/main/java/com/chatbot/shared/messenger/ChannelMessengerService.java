package com.chatbot.shared.messenger;

import java.util.UUID;

/**
 * Generic Channel Messenger Service interface for Core message workflows.
 */
public interface ChannelMessengerService {
    
    /**
     * Send message to an external end-user via a connection (Facebook, Zalo, etc.)
     *
     * @param connectionId Connection UUID (e.g. Facebook Connection ID)
     * @param recipientExternalId External platform user ID (e.g. PSID)
     * @param content Message text content
     * @return true if successfully dispatched
     */
    boolean sendMessage(UUID connectionId, String recipientExternalId, String content);

    /**
     * Count active channel connections for a given tenant
     */
    long countActiveConnections(Long tenantId);

    /**
     * Get owner ID associated with a connection
     */
    String getOwnerIdForConnection(UUID connectionId);

    /**
     * Fetch user profile info from external channel platform
     */
    ChannelUserInfo getUserInfo(UUID connectionId, String externalUserId);
}
