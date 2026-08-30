package com.chatbot.core.notification.slack;

import com.chatbot.core.message.store.model.Conversation;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for sending Slack notifications via Incoming Webhooks
 * Implements Phase 1.2: Direct Notifications — Slack channel
 *
 * Uses Slack Incoming Webhooks (no SDK needed — simple HTTP POST).
 * Slack is disabled by default. Set notification.slack.enabled=true
 * and notification.slack.webhook-url=https://hooks.slack.com/... to enable.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SlackNotificationService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${notification.slack.enabled:false}")
    private boolean slackEnabled;

    @Value("${notification.slack.webhook-url:}")
    private String webhookUrl;

    @Value("${notification.slack.channel:#general}")
    private String defaultChannel;

    // -------------------------------------------------------
    // Public API
    // -------------------------------------------------------

    /**
     * Send escalation alert to Slack.
     *
     * @param conversation  The escalated conversation
     * @param tierLevel     Escalation tier level (1/2/3)
     * @param tierName      Escalation tier name (e.g. "Team Lead")
     */
    @CircuitBreaker(name = "slackApi", fallbackMethod = "fallbackEscalationAlert")
    public void sendEscalationAlert(Conversation conversation, int tierLevel, String tierName) {
        if (!slackEnabled) {
            log.debug("Slack notifications disabled. Skipping escalation alert for conversation {}",
                conversation.getId());
            return;
        }

        String emoji = tierLevel >= 3 ? "🚨" : tierLevel == 2 ? "⚠️" : "📢";
        String text = String.format(
            "%s *Conversation Escalated to Tier %d — %s*\n" +
            ">Conversation: #%d\n" +
            ">Customer: %s\n" +
            ">Customer Tier: %s\n" +
            ">Action Required: Please respond immediately.",
            emoji,
            tierLevel, tierName,
            conversation.getId(),
            conversation.getUserName() != null ? conversation.getUserName() : conversation.getExternalUserId(),
            conversation.getCustomerTier() != null ? conversation.getCustomerTier() : "Standard"
        );

        sendMessage(text, tierLevel >= 2 ? "danger" : "warning");
    }

    public void fallbackEscalationAlert(Conversation conversation, int tierLevel, String tierName, Throwable t) {
        log.warn("Slack CircuitBreaker active - Failed to send escalation alert: {}", t.getMessage());
    }

    /**
     * Send SLA breach alert to Slack.
     *
     * @param conversation       The conversation that breached SLA
     * @param expectedSeconds    Expected response time in seconds
     * @param actualSeconds      Actual elapsed time in seconds
     */
    @CircuitBreaker(name = "slackApi", fallbackMethod = "fallbackSLABreachAlert")
    public void sendSLABreachAlert(Conversation conversation, long expectedSeconds, long actualSeconds) {
        if (!slackEnabled) {
            log.debug("Slack notifications disabled. Skipping SLA breach alert for conversation {}",
                conversation.getId());
            return;
        }

        String text = String.format(
            "⏰ *SLA Breach Detected*\n" +
            ">Conversation: #%d\n" +
            ">Customer: %s\n" +
            ">Customer Tier: %s\n" +
            ">Expected: %d min | Elapsed: %d min\n" +
            ">Breach Count: %d",
            conversation.getId(),
            conversation.getUserName() != null ? conversation.getUserName() : conversation.getExternalUserId(),
            conversation.getCustomerTier() != null ? conversation.getCustomerTier() : "Standard",
            expectedSeconds / 60,
            actualSeconds / 60,
            conversation.getSlaBreachCount()
        );

        sendMessage(text, "warning");
    }

    public void fallbackSLABreachAlert(Conversation conversation, long expectedSeconds, long actualSeconds, Throwable t) {
        log.warn("Slack CircuitBreaker active - Failed to send SLA breach alert: {}", t.getMessage());
    }

    /**
     * Send conversation timeout alert to Slack.
     *
     * @param conversation The timed-out conversation
     * @param inactiveMinutes Minutes the conversation has been inactive
     */
    @CircuitBreaker(name = "slackApi", fallbackMethod = "fallbackTimeoutAlert")
    public void sendTimeoutAlert(Conversation conversation, long inactiveMinutes) {
        if (!slackEnabled) {
            log.debug("Slack notifications disabled. Skipping timeout alert for conversation {}",
                conversation.getId());
            return;
        }

        String text = String.format(
            "💤 *Conversation Timeout*\n" +
            ">Conversation: #%d\n" +
            ">Customer: %s\n" +
            ">Inactive for: %d minutes",
            conversation.getId(),
            conversation.getUserName() != null ? conversation.getUserName() : conversation.getExternalUserId(),
            inactiveMinutes
        );

        sendMessage(text, "good");
    }

    public void fallbackTimeoutAlert(Conversation conversation, long inactiveMinutes, Throwable t) {
        log.warn("Slack CircuitBreaker active - Failed to send timeout alert: {}", t.getMessage());
    }

    // -------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------

    /**
     * Send a Slack message via Incoming Webhook.
     *
     * @param text  Message text (supports Slack mrkdwn)
     * @param color Attachment color: "good" | "warning" | "danger" | hex (#RRGGBB)
     */
    private void sendMessage(String text, String color) {
        if (!slackEnabled || webhookUrl == null || webhookUrl.isBlank()) {
            log.debug("Slack webhook URL not configured. Skipping message.");
            return;
        }

        try {
            // Build Slack payload with attachment for color support
            Map<String, Object> attachment = new HashMap<>();
            attachment.put("text", text);
            attachment.put("color", color);
            attachment.put("mrkdwn_in", List.of("text"));

            Map<String, Object> payload = new HashMap<>();
            payload.put("attachments", List.of(attachment));
            payload.put("channel", defaultChannel);

            String jsonPayload = objectMapper.writeValueAsString(payload);

            String response = webClient.post()
                .uri(webhookUrl)
                .headers(h -> h.setContentType(MediaType.APPLICATION_JSON))
                .bodyValue(jsonPayload)
                .retrieve()
                .bodyToMono(String.class)
                .block();

            log.info("Sent Slack notification successfully");
        } catch (Exception e) {
            log.warn("Failed to send Slack notification: {}", e.getMessage());
            throw new RuntimeException("Failed to send Slack notification", e);
        }
    }
}
