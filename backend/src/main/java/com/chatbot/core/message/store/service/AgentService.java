package com.chatbot.core.message.store.service;

import com.chatbot.core.message.store.model.Agent;
import com.chatbot.core.message.store.repository.AgentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Service for managing agents
 * Implements Phase 3.1: Agent Management System
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AgentService {

    private final AgentRepository agentRepository;

    // Email validation pattern
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    /**
     * Validate agent data
     */
    private void validateAgent(Agent agent) {
        if (agent.getName() == null || agent.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Agent name cannot be empty");
        }
        
        if (agent.getEmail() == null || agent.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Agent email cannot be empty");
        }
        
        if (!EMAIL_PATTERN.matcher(agent.getEmail()).matches()) {
            throw new IllegalArgumentException("Invalid email format: " + agent.getEmail());
        }
        
        if (agent.getRole() == null) {
            throw new IllegalArgumentException("Agent role cannot be null");
        }
        
        if (agent.getMaxConcurrentConversations() == null || agent.getMaxConcurrentConversations() <= 0) {
            throw new IllegalArgumentException("Max concurrent conversations must be greater than 0");
        }
        
        if (agent.getCurrentLoad() == null || agent.getCurrentLoad() < 0) {
            throw new IllegalArgumentException("Current load cannot be negative");
        }
        
        if (agent.getCurrentLoad() > agent.getMaxConcurrentConversations()) {
            throw new IllegalArgumentException("Current load cannot exceed max concurrent conversations");
        }
        
        if (agent.getTenantId() == null) {
            throw new IllegalArgumentException("Tenant ID cannot be null");
        }
    }

    /**
     * Create a new agent
     */
    @Transactional
    public Agent createAgent(Agent agent) {
        validateAgent(agent);
        
        agent.setCreatedAt(LocalDateTime.now());
        agent.setUpdatedAt(LocalDateTime.now());
        agent.setLastActivityAt(LocalDateTime.now());
        
        Agent savedAgent = agentRepository.save(agent);
        log.info("Created new agent: {} for tenant {}", savedAgent.getEmail(), savedAgent.getTenantId());
        
        return savedAgent;
    }

    /**
     * Update an existing agent
     */
    @Transactional
    public Agent updateAgent(Long agentId, Agent updatedAgent) {
        validateAgent(updatedAgent);
        
        return agentRepository.findById(agentId)
            .map(existingAgent -> {
                existingAgent.setName(updatedAgent.getName());
                existingAgent.setEmail(updatedAgent.getEmail());
                existingAgent.setRole(updatedAgent.getRole());
                existingAgent.setStatus(updatedAgent.getStatus());
                existingAgent.setSkills(updatedAgent.getSkills());
                existingAgent.setAssignmentPreferences(updatedAgent.getAssignmentPreferences());
                existingAgent.setActive(updatedAgent.getActive());
                existingAgent.setBio(updatedAgent.getBio());
                existingAgent.setPhoneNumber(updatedAgent.getPhoneNumber());
                existingAgent.setAvatarUrl(updatedAgent.getAvatarUrl());
                existingAgent.setMaxConcurrentConversations(updatedAgent.getMaxConcurrentConversations());
                existingAgent.setUpdatedAt(LocalDateTime.now());
                
                return agentRepository.save(existingAgent);
            })
            .orElseThrow(() -> new RuntimeException("Agent not found: " + agentId));
    }

    /**
     * Delete an agent
     */
    @Transactional
    public void deleteAgent(Long agentId) {
        if (!agentRepository.existsById(agentId)) {
            throw new RuntimeException("Agent not found: " + agentId);
        }
        
        agentRepository.deleteById(agentId);
        log.info("Deleted agent: {}", agentId);
    }

    /**
     * Get agent by ID
     */
    public Optional<Agent> getAgentById(Long agentId) {
        return agentRepository.findById(agentId);
    }

    /**
     * Get agent by user ID
     */
    public Optional<Agent> getAgentByUserId(Long userId) {
        return agentRepository.findByUserId(userId);
    }

    /**
     * Get all agents for a tenant
     */
    public List<Agent> getAgentsByTenant(Long tenantId) {
        return agentRepository.findByTenantId(tenantId);
    }

    /**
     * Get active agents for a tenant
     */
    public List<Agent> getActiveAgentsByTenant(Long tenantId) {
        return agentRepository.findByTenantIdAndActiveOrderByCreatedAtDesc(tenantId, true);
    }

    /**
     * Get available agents (online and can accept more conversations)
     */
    public List<Agent> getAvailableAgents(Long tenantId) {
        return agentRepository.findAvailableAgentsByTenantId(tenantId);
    }

    /**
     * Get agents by status
     */
    public List<Agent> getAgentsByStatus(Long tenantId, Agent.AgentStatus status) {
        return agentRepository.findByTenantIdAndStatus(tenantId, status);
    }

    /**
     * Get agents by role
     */
    public List<Agent> getAgentsByRole(Long tenantId, Agent.AgentRole role) {
        return agentRepository.findByTenantIdAndRole(tenantId, role);
    }

    /**
     * Get agents by skill
     * Uses database-level JSON query to avoid N+1 problem
     */
    public List<Agent> getAgentsBySkill(Long tenantId, String skill) {
        return agentRepository.findAgentsBySkill(tenantId, skill);
    }

    /**
     * Update agent status
     */
    @Transactional
    public void updateAgentStatus(Long agentId, Agent.AgentStatus status) {
        agentRepository.findById(agentId).ifPresent(agent -> {
            agent.setStatus(status);
            agent.setLastActivityAt(LocalDateTime.now());
            agent.setUpdatedAt(LocalDateTime.now());
            agentRepository.save(agent);
            
            log.info("Updated agent {} status to {}", agentId, status);
        });
    }

    /**
     * Update agent availability (set online/offline)
     */
    public void setAgentOnline(Long agentId) {
        updateAgentStatus(agentId, Agent.AgentStatus.ONLINE);
    }

    public void setAgentOffline(Long agentId) {
        updateAgentStatus(agentId, Agent.AgentStatus.OFFLINE);
    }

    public void setAgentAway(Long agentId) {
        updateAgentStatus(agentId, Agent.AgentStatus.AWAY);
    }

    public void setAgentBusy(Long agentId) {
        updateAgentStatus(agentId, Agent.AgentStatus.BUSY);
    }

    /**
     * Increment agent load (when assigned a new conversation)
     */
    @Transactional
    public void incrementAgentLoad(Long agentId) {
        agentRepository.findById(agentId).ifPresent(agent -> {
            agent.incrementLoad();
            agent.setUpdatedAt(LocalDateTime.now());
            agentRepository.save(agent);
            
            log.info("Incremented load for agent {}. Current load: {}", agentId, agent.getCurrentLoad());
        });
    }

    /**
     * Decrement agent load (when conversation is released/closed)
     */
    @Transactional
    public void decrementAgentLoad(Long agentId) {
        agentRepository.findById(agentId).ifPresent(agent -> {
            agent.decrementLoad();
            agent.setUpdatedAt(LocalDateTime.now());
            agentRepository.save(agent);
            
            log.info("Decremented load for agent {}. Current load: {}", agentId, agent.getCurrentLoad());
        });
    }

    /**
     * Update agent last activity
     */
    @Transactional
    public void updateLastActivity(Long agentId) {
        agentRepository.findById(agentId).ifPresent(agent -> {
            agent.setLastActivityAt(LocalDateTime.now());
            agentRepository.save(agent);
        });
    }

    /**
     * Get agent statistics for a tenant
     */
    public AgentStats getAgentStats(Long tenantId) {
        long totalAgents = agentRepository.countByTenantId(tenantId);
        long activeAgents = agentRepository.countByTenantIdAndActive(tenantId, true);
        long onlineAgents = agentRepository.countByTenantIdAndStatus(tenantId, Agent.AgentStatus.ONLINE);
        
        List<Agent> allAgents = agentRepository.findByTenantId(tenantId);
        int totalLoad = allAgents.stream().mapToInt(Agent::getCurrentLoad).sum();
        int maxCapacity = allAgents.stream().mapToInt(Agent::getMaxConcurrentConversations).sum();
        
        return AgentStats.builder()
            .totalAgents(totalAgents)
            .activeAgents(activeAgents)
            .onlineAgents(onlineAgents)
            .totalLoad(totalLoad)
            .maxCapacity(maxCapacity)
            .utilization(maxCapacity > 0 ? (double) totalLoad / maxCapacity * 100 : 0)
            .build();
    }

    /**
     * Find best available agent for conversation assignment
     * Considers load balancing and skills
     */
    public Optional<Agent> findBestAgentForAssignment(Long tenantId, List<String> requiredSkills) {
        List<Agent> availableAgents = getAvailableAgents(tenantId);
        
        if (availableAgents.isEmpty()) {
            return Optional.empty();
        }
        
        // If no specific skills required, return agent with lowest load
        if (requiredSkills == null || requiredSkills.isEmpty()) {
            return availableAgents.stream()
                .min((a1, a2) -> Integer.compare(a1.getCurrentLoad(), a2.getCurrentLoad()));
        }
        
        // Find agents with required skills
        List<Agent> skilledAgents = availableAgents.stream()
            .filter(agent -> agent.getSkills() != null && agent.getSkills().containsAll(requiredSkills))
            .toList();
        
        if (skilledAgents.isEmpty()) {
            // No agents with required skills, return any available agent with lowest load
            return availableAgents.stream()
                .min((a1, a2) -> Integer.compare(a1.getCurrentLoad(), a2.getCurrentLoad()));
        }
        
        // Return skilled agent with lowest load
        return skilledAgents.stream()
            .min((a1, a2) -> Integer.compare(a1.getCurrentLoad(), a2.getCurrentLoad()));
    }

    /**
     * Agent statistics DTO
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class AgentStats {
        private long totalAgents;
        private long activeAgents;
        private long onlineAgents;
        private int totalLoad;
        private int maxCapacity;
        private double utilization; // percentage
    }
}
