package com.chatbot.core.message.store.service;

import com.chatbot.core.message.store.model.Conversation;
import com.chatbot.core.message.store.model.SLAConfiguration;
import com.chatbot.core.message.store.repository.ConversationRepository;
import com.chatbot.core.message.store.repository.SLAConfigurationRepository;
import com.chatbot.core.notification.websocket.NotificationWebSocketHandler;
import com.chatbot.core.notification.email.AgentEmailNotificationService;
import com.chatbot.core.notification.slack.SlackNotificationService;
import com.chatbot.core.tenant.model.Tenant;
import com.chatbot.core.tenant.model.TenantStatus;
import com.chatbot.core.tenant.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for monitoring SLA compliance and triggering escalations
 * Implements Phase 2.1: SLA Monitoring
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SLAMonitorService {

    private final ConversationRepository conversationRepository;
    private final NotificationWebSocketHandler notificationWebSocketHandler;
    private final TenantRepository tenantRepository;
    private final SLAConfigurationRepository slaConfigurationRepository;
    private final AgentEmailNotificationService agentEmailNotificationService;
    private final SlackNotificationService slackNotificationService;

    // Default SLA thresholds (in seconds) - used as fallback
    private static final long DEFAULT_VIP_RESPONSE_TIME = 300; // 5 minutes for VIP
    private static final long DEFAULT_STANDARD_RESPONSE_TIME = 600; // 10 minutes for standard
    private static final long DEFAULT_ENTERPRISE_RESPONSE_TIME = 180; // 3 minutes for Enterprise

    /**
     * Record first bot response time
     */
    @Transactional
    public void recordFirstBotResponse(Long conversationId) {
        conversationRepository.findById(conversationId).ifPresent(conversation -> {
            if (conversation.getFirstBotResponseTime() == null) {
                conversation.setFirstBotResponseTime(LocalDateTime.now());
                conversationRepository.save(conversation);
                log.info("Recorded first bot response time for conversation {}", conversationId);
            }
        });
    }

    /**
     * Record first agent response time
     */
    @Transactional
    public void recordFirstAgentResponse(Long conversationId) {
        conversationRepository.findById(conversationId).ifPresent(conversation -> {
            if (conversation.getFirstAgentResponseTime() == null) {
                conversation.setFirstAgentResponseTime(LocalDateTime.now());
                conversationRepository.save(conversation);
                log.info("Recorded first agent response time for conversation {}", conversationId);
            }
        });
    }

    /**
     * Get expected response time based on customer tier
     * First checks database configuration, falls back to hardcoded defaults
     */
    public long getExpectedResponseTime(Conversation conversation) {
        // Check if conversation has custom expected response time
        if (conversation.getExpectedResponseTime() != null) {
            return conversation.getExpectedResponseTime();
        }

        String tier = conversation.getCustomerTier();
        if (tier == null) {
            tier = "Standard";
        }

        // Try to get from database configuration
        try {
            SLAConfiguration config = slaConfigurationRepository
                .findByTenantIdAndCustomerTierAndActiveTrue(
                    conversation.getTenantId(),
                    tier
                )
                .orElse(null);
            
            if (config != null) {
                return config.getExpectedResponseTime();
            }
        } catch (Exception e) {
            log.warn("Failed to get SLA configuration from database, using fallback: {}", e.getMessage());
        }

        // Fallback to hardcoded defaults
        switch (tier) {
            case "VIP":
                return DEFAULT_VIP_RESPONSE_TIME;
            case "Enterprise":
                return DEFAULT_ENTERPRISE_RESPONSE_TIME;
            default:
                return DEFAULT_STANDARD_RESPONSE_TIME;
        }
    }

    /**
     * Check if conversation has breached SLA
     */
    public boolean isSLABreached(Conversation conversation) {
        if (conversation.getFirstAgentResponseTime() != null) {
            // Already responded by agent, check if response was within SLA
            long responseTime = ChronoUnit.SECONDS.between(
                conversation.getCreatedAt(),
                conversation.getFirstAgentResponseTime()
            );
            return responseTime > getExpectedResponseTime(conversation);
        } else {
            // Not yet responded by agent, check if time since creation exceeds SLA
            long timeSinceCreation = ChronoUnit.SECONDS.between(
                conversation.getCreatedAt(),
                LocalDateTime.now()
            );
            return timeSinceCreation > getExpectedResponseTime(conversation);
        }
    }

    /**
     * Check for SLA breaches and trigger escalation
     * Scheduled to run every minute
     * Loops through all active tenants to check SLA compliance
     */
    @Scheduled(fixedRate = 60000) // Every minute
    public void checkSLABreaches() {
        try {
            // Get all active tenants
            List<Tenant> activeTenants = tenantRepository.findAll();
            
            for (Tenant tenant : activeTenants) {
                if (tenant.getStatus() != TenantStatus.ACTIVE) {
                    continue; // Skip inactive tenants
                }
                
                Long tenantId = tenant.getId();
                
                try {
                    // Get all open conversations for this tenant
                    List<Conversation> openConversations = conversationRepository.findByTenantIdAndStatus(tenantId, "open");

                    for (Conversation conversation : openConversations) {
                        if (isSLABreached(conversation)) {
                            handleSLABreach(conversation);
                        }
                    }
                } catch (Exception e) {
                    log.error("Error checking SLA breaches for tenant {}", tenantId, e);
                }
            }
        } catch (Exception e) {
            log.error("Error in SLA breach check scheduled job", e);
        }
    }

    /**
     * Handle SLA breach - increment count, notify via WebSocket, email, and Slack
     */
    @Transactional
    private void handleSLABreach(Conversation conversation) {
        long expectedSeconds = getExpectedResponseTime(conversation);
        long actualSeconds = ChronoUnit.SECONDS.between(conversation.getCreatedAt(), LocalDateTime.now());

        // Increment breach count
        conversation.setSlaBreachCount(conversation.getSlaBreachCount() + 1);
        conversationRepository.save(conversation);

        log.warn("SLA breach detected for conversation {}. Breach count: {}",
            conversation.getId(), conversation.getSlaBreachCount());

        // 1. WebSocket notification
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "sla_breach");
        notification.put("title", "SLA Breach Alert");
        notification.put("message", String.format(
            "Conversation %d has breached SLA. Breach count: %d. Customer: %s",
            conversation.getId(),
            conversation.getSlaBreachCount(),
            conversation.getUserName() != null ? conversation.getUserName() : conversation.getExternalUserId()
        ));
        notification.put("conversationId", conversation.getId());
        notification.put("priority", "high");
        notification.put("timestamp", LocalDateTime.now());
        notificationWebSocketHandler.broadcastToTenant(conversation.getTenantId(), notification);

        // 2. Email notification (non-blocking)
        try {
            agentEmailNotificationService.sendSLABreachNotification(conversation, expectedSeconds, actualSeconds);
        } catch (Exception e) {
            log.warn("Failed to send SLA breach email for conversation {}: {}", conversation.getId(), e.getMessage());
        }

        // 3. Slack notification (non-blocking)
        try {
            slackNotificationService.sendSLABreachAlert(conversation, expectedSeconds, actualSeconds);
        } catch (Exception e) {
            log.warn("Failed to send SLA breach Slack alert for conversation {}: {}", conversation.getId(), e.getMessage());
        }

        // If breach count is high, escalate
        if (conversation.getSlaBreachCount() >= 3) {
            triggerEscalation(conversation);
        }
    }

    /**
     * Trigger escalation for SLA breach
     */
    private void triggerEscalation(Conversation conversation) {
        log.warn("Triggering escalation for conversation {} due to repeated SLA breaches", conversation.getId());

        Map<String, Object> escalationNotification = new HashMap<>();
        escalationNotification.put("type", "escalation_required");
        escalationNotification.put("title", "Escalation Required");
        escalationNotification.put("message", String.format(
            "Conversation %d requires immediate escalation due to %d SLA breaches",
            conversation.getId(),
            conversation.getSlaBreachCount()
        ));
        escalationNotification.put("conversationId", conversation.getId());
        escalationNotification.put("priority", "urgent");
        escalationNotification.put("timestamp", LocalDateTime.now());

        notificationWebSocketHandler.broadcastToTenant(conversation.getTenantId(), escalationNotification);
    }

    /**
     * Get SLA metrics for a tenant
     */
    public Map<String, Object> getSLAMetrics(Long tenantId) {
        List<Conversation> allConversations = conversationRepository.findByTenantId(tenantId);
        List<Conversation> openConversations = conversationRepository.findByTenantIdAndStatus(tenantId, "open");

        int totalConversations = allConversations.size();
        int openConversationsCount = openConversations.size();
        int slaBreachCount = 0;
        long totalResponseTime = 0;
        int respondedConversations = 0;

        for (Conversation conv : allConversations) {
            if (conv.getSlaBreachCount() > 0) {
                slaBreachCount += conv.getSlaBreachCount();
            }

            if (conv.getFirstAgentResponseTime() != null) {
                long responseTime = ChronoUnit.SECONDS.between(
                    conv.getCreatedAt(),
                    conv.getFirstAgentResponseTime()
                );
                totalResponseTime += responseTime;
                respondedConversations++;
            }
        }

        double averageResponseTime = respondedConversations > 0 
            ? (double) totalResponseTime / respondedConversations 
            : 0;

        double slaComplianceRate = totalConversations > 0
            ? ((double) (totalConversations - slaBreachCount) / totalConversations) * 100
            : 100;

        Map<String, Object> metrics = new HashMap<>();
        metrics.put("totalConversations", totalConversations);
        metrics.put("openConversations", openConversationsCount);
        metrics.put("slaBreachCount", slaBreachCount);
        metrics.put("averageResponseTime", averageResponseTime);
        metrics.put("slaComplianceRate", slaComplianceRate);
        metrics.put("respondedConversations", respondedConversations);

        return metrics;
    }

    /**
     * Create default SLA configurations for a tenant
     */
    @Transactional
    public void createDefaultSLAConfigurations(Long tenantId) {
        if (slaConfigurationRepository.findByTenantId(tenantId).isEmpty()) {
            // VIP Configuration
            SLAConfiguration vipConfig = SLAConfiguration.builder()
                .tenantId(tenantId)
                .customerTier("VIP")
                .expectedResponseTime(DEFAULT_VIP_RESPONSE_TIME)
                .maxBreachCount(2)
                .active(true)
                .description("VIP customers - 5 minutes response time, max 2 breaches")
                .build();

            // Enterprise Configuration
            SLAConfiguration enterpriseConfig = SLAConfiguration.builder()
                .tenantId(tenantId)
                .customerTier("Enterprise")
                .expectedResponseTime(DEFAULT_ENTERPRISE_RESPONSE_TIME)
                .maxBreachCount(3)
                .active(true)
                .description("Enterprise customers - 3 minutes response time, max 3 breaches")
                .build();

            // Standard Configuration
            SLAConfiguration standardConfig = SLAConfiguration.builder()
                .tenantId(tenantId)
                .customerTier("Standard")
                .expectedResponseTime(DEFAULT_STANDARD_RESPONSE_TIME)
                .maxBreachCount(5)
                .active(true)
                .description("Standard customers - 10 minutes response time, max 5 breaches")
                .build();

            slaConfigurationRepository.save(vipConfig);
            slaConfigurationRepository.save(enterpriseConfig);
            slaConfigurationRepository.save(standardConfig);

            log.info("Created default SLA configurations for tenant {}", tenantId);
        }
    }

    /**
     * Get SLA configuration for a tenant and tier
     */
    public SLAConfiguration getSLAConfiguration(Long tenantId, String customerTier) {
        return slaConfigurationRepository
            .findByTenantIdAndCustomerTierAndActiveTrue(tenantId, customerTier)
            .orElse(null);
    }

    /**
     * Update SLA configuration
     */
    @Transactional
    public SLAConfiguration updateSLAConfiguration(Long configId, SLAConfiguration updatedConfig) {
        return slaConfigurationRepository.findById(configId)
            .map(existingConfig -> {
                existingConfig.setExpectedResponseTime(updatedConfig.getExpectedResponseTime());
                existingConfig.setMaxBreachCount(updatedConfig.getMaxBreachCount());
                existingConfig.setActive(updatedConfig.getActive());
                existingConfig.setDescription(updatedConfig.getDescription());
                return slaConfigurationRepository.save(existingConfig);
            })
            .orElseThrow(() -> new RuntimeException("SLA configuration not found: " + configId));
    }

    /**
     * Delete SLA configuration
     */
    @Transactional
    public void deleteSLAConfiguration(Long configId) {
        slaConfigurationRepository.deleteById(configId);
    }
}
