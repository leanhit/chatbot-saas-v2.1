package com.chatbot.core.notification.email;

import com.chatbot.core.message.store.model.Conversation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Service for sending email notifications to agents
 * Implements Phase 1.2: Direct Notifications — Email channel
 *
 * Email is disabled by default. Set notification.email.enabled=true
 * and configure spring.mail.* to enable.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AgentEmailNotificationService {

    private final JavaMailSender mailSender;

    @Value("${notification.email.enabled:false}")
    private boolean emailEnabled;

    @Value("${notification.email.from:noreply@chatbot.com}")
    private String fromEmail;

    // -------------------------------------------------------
    // Public API
    // -------------------------------------------------------

    /**
     * Send escalation notification email to tenant admins / team leads.
     *
     * @param conversation  The escalated conversation
     * @param tierLevel     Escalation tier level (1/2/3)
     * @param tierName      Escalation tier name (e.g. "Team Lead")
     */
    public void sendEscalationNotification(Conversation conversation, int tierLevel, String tierName) {
        if (!emailEnabled) {
            log.debug("Email notifications disabled. Skipping escalation email for conversation {}",
                conversation.getId());
            return;
        }

        String subject = String.format("[ESCALATION] Conversation #%d escalated to %s (Tier %d)",
            conversation.getId(), tierName, tierLevel);

        String body = String.format(
            "A conversation has been escalated.\n\n" +
            "Conversation ID : %d\n" +
            "Customer        : %s\n" +
            "Customer Tier   : %s\n" +
            "Escalated To    : Tier %d — %s\n\n" +
            "Please review and take action as soon as possible.\n",
            conversation.getId(),
            conversation.getUserName() != null ? conversation.getUserName() : conversation.getExternalUserId(),
            conversation.getCustomerTier(),
            tierLevel,
            tierName
        );

        sendEmail(subject, body);
    }

    /**
     * Send SLA breach notification email.
     *
     * @param conversation       The conversation that breached SLA
     * @param expectedSeconds    Expected response time in seconds
     * @param actualSeconds      Actual elapsed time in seconds
     */
    public void sendSLABreachNotification(Conversation conversation, long expectedSeconds, long actualSeconds) {
        if (!emailEnabled) {
            log.debug("Email notifications disabled. Skipping SLA breach email for conversation {}",
                conversation.getId());
            return;
        }

        String subject = String.format("[SLA BREACH] Conversation #%d exceeded response time",
            conversation.getId());

        String body = String.format(
            "A conversation has breached its SLA.\n\n" +
            "Conversation ID  : %d\n" +
            "Customer         : %s\n" +
            "Customer Tier    : %s\n" +
            "Expected Response: %d minutes\n" +
            "Elapsed Time     : %d minutes\n" +
            "SLA Breach Count : %d\n\n" +
            "Please take immediate action.\n",
            conversation.getId(),
            conversation.getUserName() != null ? conversation.getUserName() : conversation.getExternalUserId(),
            conversation.getCustomerTier(),
            expectedSeconds / 60,
            actualSeconds / 60,
            conversation.getSlaBreachCount()
        );

        sendEmail(subject, body);
    }

    /**
     * Send notification when a conversation is assigned to an agent.
     *
     * @param conversation The conversation that was assigned
     * @param agentEmail   The email of the assigned agent
     */
    public void sendConversationAssignedNotification(Conversation conversation, String agentEmail) {
        if (!emailEnabled) {
            log.debug("Email notifications disabled. Skipping assignment email for conversation {}",
                conversation.getId());
            return;
        }

        String subject = String.format("[NEW ASSIGNMENT] Conversation #%d assigned to you",
            conversation.getId());

        String body = String.format(
            "A conversation has been assigned to you.\n\n" +
            "Conversation ID : %d\n" +
            "Customer        : %s\n" +
            "Customer Tier   : %s\n" +
            "Channel         : %s\n\n" +
            "Please login and respond to the customer.\n",
            conversation.getId(),
            conversation.getUserName() != null ? conversation.getUserName() : conversation.getExternalUserId(),
            conversation.getCustomerTier(),
            conversation.getChannel()
        );

        sendEmail(agentEmail, subject, body);
    }

    // -------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------

    /**
     * Send email to a specific recipient.
     */
    private void sendEmail(String toEmail, String subject, String body) {
        if (!emailEnabled) return;
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("Sent email notification to {}: {}", toEmail, subject);
        } catch (Exception e) {
            log.warn("Failed to send email to {}: {}", toEmail, e.getMessage());
        }
    }

    /**
     * Send email to the configured default admin/notification address.
     * Used for broadcast-style alerts (escalation, SLA breach).
     */
    private void sendEmail(String subject, String body) {
        if (!emailEnabled) return;
        // Broadcast to the fromEmail as a fallback (acts as a notification inbox)
        sendEmail(fromEmail, subject, body);
    }
}
