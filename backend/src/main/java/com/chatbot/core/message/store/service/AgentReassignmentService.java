package com.chatbot.core.message.store.service;

import com.chatbot.core.message.store.model.Agent;
import com.chatbot.core.message.store.model.Conversation;
import com.chatbot.core.message.store.repository.AgentRepository;
import com.chatbot.core.message.store.repository.ConversationRepository;
import com.chatbot.core.tenant.infra.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service for auto-reassigning conversations when assigned agents go offline
 * Implements Phase 3: Auto-reassignment when agent offline
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AgentReassignmentService {

    private final ConversationRepository conversationRepository;
    private final AgentRepository agentRepository;
    private final AgentAssignmentService agentAssignmentService;
    private final AgentService agentService;

    /**
     * Scheduled task to check for conversations assigned to offline agents
     * Runs every 5 minutes
     */
    @Scheduled(fixedRate = 300000) // 5 minutes
    @Transactional(transactionManager = "messageTransactionManager", rollbackFor = Exception.class)
    public void checkAndReassignOfflineAgents() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            log.debug("No tenant context, skipping offline agent check");
            return;
        }

        log.info("🔍 [Reassignment] Checking for conversations assigned to offline agents for tenant {}", tenantId);

        // Find all conversations with assigned agents
        List<Conversation> assignedConversations = conversationRepository
            .findByTenantIdAndAgentAssignedIdIsNotNull(tenantId);

        if (assignedConversations.isEmpty()) {
            log.debug("No assigned conversations found for tenant {}", tenantId);
            return;
        }

        int reassignmentCount = 0;

        for (Conversation conversation : assignedConversations) {
            Long assignedAgentId = conversation.getAgentAssignedId();
            if (assignedAgentId == null) {
                continue;
            }

            // Check if assigned agent is offline
            Optional<Agent> agentOpt = agentRepository.findById(assignedAgentId);
            if (agentOpt.isEmpty()) {
                log.warn("Agent {} not found for conversation {}", assignedAgentId, conversation.getId());
                continue;
            }

            Agent agent = agentOpt.get();

            // If agent is offline, reassign conversation
            if (agent.getStatus() != Agent.AgentStatus.ONLINE) {
                log.info("🔄 [Reassignment] Agent {} is offline (status: {}). Reassigning conversation {}",
                    assignedAgentId, agent.getStatus(), conversation.getId());

                // Decrement load from offline agent
                agentService.decrementAgentLoad(assignedAgentId);

                // Try to find a new online agent
                Optional<Agent> newAgent = agentAssignmentService.autoAssignConversation(conversation);

                if (newAgent.isPresent()) {
                    conversation.setAgentAssignedId(newAgent.get().getId());
                    conversationRepository.save(conversation);
                    agentService.incrementAgentLoad(newAgent.get().getId());
                    reassignmentCount++;
                    log.info("✅ [Reassignment] Conversation {} reassigned from offline agent {} to online agent {}",
                        conversation.getId(), assignedAgentId, newAgent.get().getId());
                } else {
                    // No online agents available, release conversation back to bot
                    conversation.setAgentAssignedId(null);
                    conversation.setIsTakenOverByAgent(false);
                    conversation.setStatus("open");
                    conversationRepository.save(conversation);
                    reassignmentCount++;
                    log.info("🤖 [Reassignment] Conversation {} released back to bot (no online agents available)",
                        conversation.getId());
                }
            }
        }

        if (reassignmentCount > 0) {
            log.info("📊 [Reassignment] Reassigned {} conversations for tenant {}", reassignmentCount, tenantId);
        } else {
            log.debug("✅ [Reassignment] All assigned agents are online for tenant {}", tenantId);
        }
    }

    /**
     * Manual trigger for reassignment (can be called from API or admin panel)
     */
    @Transactional(transactionManager = "messageTransactionManager", rollbackFor = Exception.class)
    public int reassignConversation(Long conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId)
            .orElseThrow(() -> new RuntimeException("Conversation not found: " + conversationId));

        Long assignedAgentId = conversation.getAgentAssignedId();
        if (assignedAgentId == null) {
            log.warn("Conversation {} has no assigned agent", conversationId);
            return 0;
        }

        // Check if assigned agent is offline
        Optional<Agent> agentOpt = agentRepository.findById(assignedAgentId);
        if (agentOpt.isEmpty()) {
            log.warn("Agent {} not found for conversation {}", assignedAgentId, conversationId);
            return 0;
        }

        Agent agent = agentOpt.get();

        if (agent.getStatus() == Agent.AgentStatus.ONLINE) {
            log.warn("Agent {} is online, no reassignment needed for conversation {}", assignedAgentId, conversationId);
            return 0;
        }

        // Decrement load from offline agent
        agentService.decrementAgentLoad(assignedAgentId);

        // Try to find a new online agent
        Optional<Agent> newAgent = agentAssignmentService.autoAssignConversation(conversation);

        if (newAgent.isPresent()) {
            conversation.setAgentAssignedId(newAgent.get().getId());
            conversationRepository.save(conversation);
            agentService.incrementAgentLoad(newAgent.get().getId());
            log.info("✅ [Manual Reassignment] Conversation {} reassigned from offline agent {} to online agent {}",
                conversationId, assignedAgentId, newAgent.get().getId());
            return 1;
        } else {
            // No online agents available, release conversation back to bot
            conversation.setAgentAssignedId(null);
            conversation.setIsTakenOverByAgent(false);
            conversation.setStatus("open");
            conversationRepository.save(conversation);
            log.info("🤖 [Manual Reassignment] Conversation {} released back to bot (no online agents available)",
                conversationId);
            return 1;
        }
    }
}
