package com.chatbot.core.tenant.membership.service;

import com.chatbot.shared.messaging.MessagePublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Service for sending tenant-related notifications
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TenantNotificationService {

    private final MessagePublisher messagePublisher;

    /**
     * Send invitation notification
     */
    public void sendInvitationNotification(Long tenantId, String tenantName, String recipientEmail, 
                                         String role, String token, String invitedBy) {
        Map<String, Object> notification = Map.of(
            "id", UUID.randomUUID().toString(),
            "type", "TENANT_INVITATION",
            "tenantId", tenantId,
            "tenantName", tenantName,
            "recipientEmail", recipientEmail,
            "role", role,
            "token", token,
            "invitedBy", invitedBy,
            "createdAt", LocalDateTime.now(),
            "expiresAt", LocalDateTime.now().plusDays(7)
        );

        try {
            messagePublisher.publishNotification(notification);
            log.info("Invitation notification sent for tenant {} to {}", tenantName, recipientEmail);
        } catch (Exception e) {
            log.error("Failed to send invitation notification", e);
        }
    }

    /**
     * Send join request notification to tenant admins
     */
    public void sendJoinRequestNotification(Long tenantId, String tenantName, String requesterEmail, 
                                          String requesterName) {
        Map<String, Object> notification = Map.of(
            "id", UUID.randomUUID().toString(),
            "type", "TENANT_JOIN_REQUEST",
            "tenantId", tenantId,
            "tenantName", tenantName,
            "requesterEmail", requesterEmail,
            "requesterName", requesterName,
            "createdAt", LocalDateTime.now()
        );

        try {
            messagePublisher.publishNotification(notification);
            log.info("Join request notification sent for tenant {} by {}", tenantName, requesterEmail);
        } catch (Exception e) {
            log.error("Failed to send join request notification", e);
        }
    }

    /**
     * Send invitation accepted notification
     */
    public void sendInvitationAcceptedNotification(Long tenantId, String tenantName, String memberEmail, 
                                                 String role) {
        Map<String, Object> notification = Map.of(
            "id", UUID.randomUUID().toString(),
            "type", "TENANT_INVITATION_ACCEPTED",
            "tenantId", tenantId,
            "tenantName", tenantName,
            "memberEmail", memberEmail,
            "role", role,
            "createdAt", LocalDateTime.now()
        );

        try {
            messagePublisher.publishNotification(notification);
            log.info("Invitation accepted notification sent for tenant {} by {}", tenantName, memberEmail);
        } catch (Exception e) {
            log.error("Failed to send invitation accepted notification", e);
        }
    }

    /**
     * Send join request approved notification
     */
    public void sendJoinRequestApprovedNotification(Long tenantId, String tenantName, String memberEmail) {
        Map<String, Object> notification = Map.of(
            "id", UUID.randomUUID().toString(),
            "type", "TENANT_JOIN_REQUEST_APPROVED",
            "tenantId", tenantId,
            "tenantName", tenantName,
            "memberEmail", memberEmail,
            "createdAt", LocalDateTime.now()
        );

        try {
            messagePublisher.publishNotification(notification);
            log.info("Join request approved notification sent for tenant {} to {}", tenantName, memberEmail);
        } catch (Exception e) {
            log.error("Failed to send join request approved notification", e);
        }
    }
}
