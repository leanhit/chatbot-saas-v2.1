package com.chatbot.core.message.store.service;

import com.chatbot.core.message.store.model.Agent;
import com.chatbot.core.message.store.model.Conversation;
import com.chatbot.core.message.store.repository.AgentRepository;
import com.chatbot.core.message.store.repository.ConversationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for skills-based agent assignment
 * Implements Phase 3.2: Skills-based Routing
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AgentAssignmentService {

    private final AgentRepository agentRepository;
    private final ConversationRepository conversationRepository;
    private final AgentService agentService;

    /**
     * Assign conversation to best matching agent based on skills
     * @param conversation The conversation to assign
     * @param requiredSkills List of required skills for this conversation
     * @return The assigned agent, or empty if no suitable agent found
     */
    public Optional<Agent> assignConversationToAgent(Conversation conversation, List<String> requiredSkills) {
        Long tenantId = conversation.getTenantId();

        // Get all available agents
        List<Agent> availableAgents = agentRepository.findAvailableAgentsByTenantId(tenantId);

        if (availableAgents.isEmpty()) {
            log.warn("No available agents for tenant {}", tenantId);
            return Optional.empty();
        }

        // If no specific skills required, use load balancing
        if (requiredSkills == null || requiredSkills.isEmpty()) {
            return assignByLoadBalancing(availableAgents);
        }

        // Filter agents with required skills
        List<Agent> skilledAgents = filterAgentsBySkills(availableAgents, requiredSkills);

        if (skilledAgents.isEmpty()) {
            log.warn("No agents with required skills: {}. Using load balancing fallback.", requiredSkills);
            return assignByLoadBalancing(availableAgents);
        }

        // Assign based on skill match score and load
        return assignBySkillMatchAndLoad(skilledAgents, requiredSkills);
    }

    /**
     * Filter agents by required skills
     */
    private List<Agent> filterAgentsBySkills(List<Agent> agents, List<String> requiredSkills) {
        return agents.stream()
            .filter(agent -> agent.getSkills() != null && agent.getSkills().containsAll(requiredSkills))
            .collect(Collectors.toList());
    }

    /**
     * Assign by load balancing (lowest load first)
     */
    private Optional<Agent> assignByLoadBalancing(List<Agent> agents) {
        return agents.stream()
            .min(Comparator.comparingInt(Agent::getCurrentLoad));
    }

    /**
     * Assign by skill match score and load
     * Considers both skill match quality and current load
     */
    private Optional<Agent> assignBySkillMatchAndLoad(List<Agent> agents, List<String> requiredSkills) {
        return agents.stream()
            .min((a1, a2) -> {
                // Calculate score for each agent
                double score1 = calculateAssignmentScore(a1, requiredSkills);
                double score2 = calculateAssignmentScore(a2, requiredSkills);
                
                // Higher score is better, but we want min for this comparison
                // So we invert the scores
                return Double.compare(score2, score1);
            });
    }

    /**
     * Calculate assignment score for an agent
     * Higher score means better match
     */
    private double calculateAssignmentScore(Agent agent, List<String> requiredSkills) {
        double score = 0;

        // Skill match bonus
        if (agent.getSkills() != null) {
            int matchedSkills = 0;
            for (String skill : requiredSkills) {
                if (agent.getSkills().contains(skill)) {
                    matchedSkills++;
                }
            }
            score += matchedSkills * 10; // Each matched skill gives 10 points
        }

        // Load penalty (lower load is better)
        double loadRatio = (double) agent.getCurrentLoad() / agent.getMaxConcurrentConversations();
        score -= loadRatio * 5; // Penalty based on load ratio

        return score;
    }

    /**
     * Auto-assign conversation based on conversation attributes
     * Uses conversation language, customer tier, and custom attributes to determine required skills
     */
    public Optional<Agent> autoAssignConversation(Conversation conversation) {
        List<String> requiredSkills = new ArrayList<>();

        // Add language skill if specified
        if (conversation.getLanguage() != null) {
            requiredSkills.add(conversation.getLanguage().toLowerCase());
        }

        // Add customer tier skill
        if (conversation.getCustomerTier() != null) {
            requiredSkills.add(conversation.getCustomerTier().toLowerCase());
        }

        // Parse custom attributes for additional skill requirements
        if (conversation.getCustomAttributes() != null) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> attributes = new com.fasterxml.jackson.databind.ObjectMapper().readValue(
                    conversation.getCustomAttributes(),
                    Map.class
                );
                
                if (attributes.containsKey("requiredSkills")) {
                    Object skillsObj = attributes.get("requiredSkills");
                    if (skillsObj instanceof List) {
                        @SuppressWarnings("unchecked")
                        List<String> skillsList = (List<String>) skillsObj;
                        requiredSkills.addAll(skillsList);
                    }
                }
            } catch (Exception e) {
                log.error("Failed to parse custom attributes for skill matching", e);
            }
        }

        return assignConversationToAgent(conversation, requiredSkills);
    }

    /**
     * Reassign conversation to different agent
     */
    public boolean reassignConversation(Long conversationId, Long newAgentId) {
        Conversation conversation = conversationRepository.findById(conversationId).orElse(null);
        if (conversation == null) {
            log.error("Conversation not found for reassignment: {}", conversationId);
            return false;
        }

        Agent newAgent = agentRepository.findById(newAgentId).orElse(null);
        if (newAgent == null) {
            log.error("Agent not found for reassignment: {}", newAgentId);
            return false;
        }

        // Check if new agent can accept more conversations
        if (!newAgent.canAcceptMoreConversations()) {
            log.warn("Agent {} cannot accept more conversations", newAgentId);
            return false;
        }

        // Decrement load from old agent if assigned
        if (conversation.getAgentAssignedId() != null) {
            agentService.decrementAgentLoad(conversation.getAgentAssignedId());
        }

        // Assign to new agent
        conversation.setAgentAssignedId(newAgentId);
        conversation.setIsTakenOverByAgent(true);
        conversationRepository.save(conversation);

        // Increment load for new agent
        agentService.incrementAgentLoad(newAgentId);

        log.info("Reassigned conversation {} to agent {}", conversationId, newAgentId);
        return true;
    }

    /**
     * Get recommended agents for a conversation
     * Returns list of agents sorted by match score (best first)
     */
    public List<Agent> getRecommendedAgents(Conversation conversation, List<String> requiredSkills, int limit) {
        Long tenantId = conversation.getTenantId();
        List<Agent> availableAgents = agentRepository.findAvailableAgentsByTenantId(tenantId);

        if (availableAgents.isEmpty()) {
            return Collections.emptyList();
        }

        // If no skills required, return by load
        if (requiredSkills == null || requiredSkills.isEmpty()) {
            return availableAgents.stream()
                .sorted(Comparator.comparingInt(Agent::getCurrentLoad))
                .limit(limit)
                .collect(Collectors.toList());
        }

        // Sort by assignment score
        return availableAgents.stream()
            .sorted((a1, a2) -> {
                double score1 = calculateAssignmentScore(a1, requiredSkills);
                double score2 = calculateAssignmentScore(a2, requiredSkills);
                return Double.compare(score2, score1); // Descending order
            })
            .limit(limit)
            .collect(Collectors.toList());
    }

    /**
     * Check if agent has specific skill
     */
    public boolean agentHasSkill(Long agentId, String skill) {
        return agentRepository.findById(agentId)
            .map(agent -> agent.getSkills() != null && agent.getSkills().contains(skill))
            .orElse(false);
    }

    /**
     * Add skill to agent
     */
    public void addSkillToAgent(Long agentId, String skill) {
        agentRepository.findById(agentId).ifPresent(agent -> {
            if (agent.getSkills() == null) {
                agent.setSkills(new HashSet<>());
            }
            agent.getSkills().add(skill);
            agentRepository.save(agent);
            log.info("Added skill {} to agent {}", skill, agentId);
        });
    }

    /**
     * Remove skill from agent
     */
    public void removeSkillFromAgent(Long agentId, String skill) {
        agentRepository.findById(agentId).ifPresent(agent -> {
            if (agent.getSkills() != null) {
                agent.getSkills().remove(skill);
                agentRepository.save(agent);
                log.info("Removed skill {} from agent {}", skill, agentId);
            }
        });
    }

    /**
     * Get skill distribution across agents
     */
    public Map<String, Integer> getSkillDistribution(Long tenantId) {
        List<Agent> agents = agentRepository.findByTenantId(tenantId);
        Map<String, Integer> distribution = new HashMap<>();

        for (Agent agent : agents) {
            if (agent.getSkills() != null) {
                for (String skill : agent.getSkills()) {
                    distribution.put(skill, distribution.getOrDefault(skill, 0) + 1);
                }
            }
        }

        return distribution;
    }
}
