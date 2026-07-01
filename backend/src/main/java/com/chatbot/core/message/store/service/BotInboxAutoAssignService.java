package com.chatbot.core.message.store.service;

import com.chatbot.core.message.store.model.Agent;
import com.chatbot.core.message.store.model.Conversation;
import com.chatbot.core.message.store.repository.ConversationRepository;
import com.chatbot.core.tenant.model.Tenant;
import com.chatbot.core.tenant.model.TenantStatus;
import com.chatbot.core.tenant.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service for auto-assigning bot inbox conversations to agents
 * Implements Phase 1.1: Auto-assign rules for bot inbox
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BotInboxAutoAssignService {

    private final ConversationRepository conversationRepository;
    private final AgentService agentService;
    private final AgentAssignmentService agentAssignmentService;
    private final RoutingRuleService routingRuleService;
    private final TenantRepository tenantRepository;

    /**
     * Auto-assign bot inbox conversations to available agents
     * Scheduled to run every 30 seconds
     */
    @Scheduled(fixedRate = 30000) // Every 30 seconds
    public void autoAssignBotInbox() {
        try {
            // Iterate over all active tenants (same pattern as SLAMonitorService)
            List<Tenant> activeTenants = tenantRepository.findAll();

            for (Tenant tenant : activeTenants) {
                if (tenant.getStatus() != TenantStatus.ACTIVE) {
                    continue; // Skip inactive tenants
                }

                Long tenantId = tenant.getId();
                try {
                    // Get all open bot conversations for this tenant (not taken over by agent)
                    List<Conversation> botConversations = conversationRepository
                            .findByIsTakenOverByAgentAndTenantId(false, tenantId);

                    if (botConversations.isEmpty()) {
                        continue;
                    }

                    log.info("Processing {} bot inbox conversations for tenant {} auto-assignment",
                            botConversations.size(), tenantId);

                    for (Conversation conversation : botConversations) {
                        try {
                            autoAssignConversation(conversation);
                        } catch (Exception e) {
                            log.error("Error auto-assigning conversation {}", conversation.getId(), e);
                        }
                    }
                } catch (Exception e) {
                    log.error("Error in bot inbox auto-assign for tenant {}", tenantId, e);
                }
            }
        } catch (Exception e) {
            log.error("Error in bot inbox auto-assign scheduled job", e);
        }
    }

    /**
     * Auto-assign a single conversation based on rules and agent availability
     */
    @Transactional
    public boolean autoAssignConversation(Conversation conversation) {
        // Skip if already taken over
        if (conversation.getIsTakenOverByAgent()) {
            return false;
        }

        // Skip if conversation is closed
        if ("closed".equals(conversation.getStatus()) || "blocked".equals(conversation.getStatus())) {
            return false;
        }

        // First, try routing rules
        boolean ruleApplied = routingRuleService.applyRoutingRules(conversation);
        if (ruleApplied) {
            log.info("Routing rule applied to conversation {}", conversation.getId());
            return true;
        }

        // If no rule matched, try skills-based auto-assignment
        return skillsBasedAutoAssign(conversation);
    }

    /**
     * Skills-based auto-assignment
     */
    private boolean skillsBasedAutoAssign(Conversation conversation) {
        // Extract skills from conversation attributes
        String requiredSkill = extractRequiredSkill(conversation);
        
        List<Agent> availableAgents;
        if (requiredSkill != null && !requiredSkill.isEmpty()) {
            // Find agents with the required skill
            availableAgents = agentService.getAgentsBySkill(conversation.getTenantId(), requiredSkill);
        } else {
            // Get all available agents
            availableAgents = agentService.getAvailableAgents(conversation.getTenantId());
        }

        if (availableAgents.isEmpty()) {
            log.debug("No available agents for conversation {}", conversation.getId());
            return false;
        }

        // Find the best agent based on load and skills
        Optional<Agent> bestAgent = findBestAgent(availableAgents, conversation);
        
        if (bestAgent.isPresent()) {
            boolean assigned = agentAssignmentService.reassignConversation(conversation.getId(), bestAgent.get().getId());
            if (assigned) {
                log.info("Auto-assigned conversation {} to agent {} based on skills", 
                    conversation.getId(), bestAgent.get().getId());
                return true;
            }
        }

        return false;
    }

    /**
     * Extract required skill from conversation attributes
     */
    private String extractRequiredSkill(Conversation conversation) {
        // Check custom attributes for skill requirements
        if (conversation.getCustomAttributes() != null) {
            try {
                java.util.Map<String, Object> attrs = new com.fasterxml.jackson.databind.ObjectMapper().readValue(
                    conversation.getCustomAttributes(),
                    java.util.Map.class
                );
                Object skill = attrs.get("requiredSkill");
                if (skill != null) {
                    return skill.toString();
                }
            } catch (Exception e) {
                log.error("Error parsing custom attributes for skill extraction", e);
            }
        }

        // Infer skill from customer tier
        String tier = conversation.getCustomerTier();
        if ("VIP".equals(tier)) {
            return "vip_support";
        }
        if ("Enterprise".equals(tier)) {
            return "enterprise_support";
        }

        return null;
    }

    /**
     * Find the best agent for assignment based on load and skills
     */
    private Optional<Agent> findBestAgent(List<Agent> agents, Conversation conversation) {
        return agents.stream()
            .filter(agent -> agent.getStatus() == Agent.AgentStatus.ONLINE)
            .filter(agent -> agent.getActive())
            .filter(agent -> agent.canAcceptMoreConversations())
            .min((a1, a2) -> {
                // Prefer agents with lower current load
                int loadCompare = a1.getCurrentLoad().compareTo(a2.getCurrentLoad());
                if (loadCompare != 0) {
                    return loadCompare;
                }
                // If load is equal, prefer agents with higher max capacity
                return a2.getMaxConcurrentConversations().compareTo(a1.getMaxConcurrentConversations());
            });
    }

    /**
     * Manually trigger auto-assignment for a specific conversation
     */
    @Transactional
    public boolean triggerAutoAssign(Long conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId).orElse(null);
        if (conversation == null) {
            log.error("Conversation not found for auto-assign: {}", conversationId);
            return false;
        }

        log.info("Manually triggering auto-assign for conversation {}", conversationId);
        return autoAssignConversation(conversation);
    }

    /**
     * Get statistics about auto-assignment
     */
    public java.util.Map<String, Object> getAutoAssignStats(Long tenantId) {
        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        
        List<Conversation> botConversations = conversationRepository.findByIsTakenOverByAgentAndTenantId(false, tenantId);
        List<Agent> availableAgents = agentService.getAvailableAgents(tenantId);
        
        stats.put("botInboxCount", botConversations.size());
        stats.put("availableAgentsCount", availableAgents.size());
        stats.put("autoAssignEnabled", true);
        stats.put("lastAutoAssignTime", LocalDateTime.now());
        
        return stats;
    }

    /**
     * Configure auto-assignment settings for a tenant
     */
    @Transactional
    public void configureAutoAssign(Long tenantId, boolean enabled, int intervalSeconds) {
        // TODO: Store configuration in database
        log.info("Configured auto-assign for tenant {}: enabled={}, interval={}s", 
            tenantId, enabled, intervalSeconds);
    }
}
