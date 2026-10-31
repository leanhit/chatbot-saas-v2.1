package com.chatbot.core.routing.service;

import com.chatbot.core.routing.dto.CreateSupervisorSessionRequest;
import com.chatbot.core.routing.dto.SupervisorSessionDTO;
import com.chatbot.core.routing.dto.SupervisorTakeoverResult;
import com.chatbot.core.routing.model.SupervisorSession;
import com.chatbot.core.routing.repository.IntelligentSupervisorSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class IntelligentSupervisorService {

    private final IntelligentSupervisorSessionRepository supervisorSessionRepository;

    @Transactional
    public SupervisorSessionDTO startSupervisorSession(CreateSupervisorSessionRequest request) {
        log.info("Starting supervisor session for supervisor: {}, conversation: {}", 
                request.getSupervisorId(), request.getConversationId());

        SupervisorSession session = SupervisorSession.builder()
                .supervisorId(request.getSupervisorId())
                .conversationId(request.getConversationId())
                .sessionType(request.getSessionType())
                .sessionStatus(SupervisorSession.SessionStatus.ACTIVE)
                .monitoredAgentId(request.getMonitoredAgentId())
                .joinedAt(LocalDateTime.now())
                .isSilent(request.getIsSilent())
                .canIntervene(request.getCanIntervene())
                .interventionCount(0)
                .notes(request.getNotes())
                .build();

        session.setTenantId(com.chatbot.core.tenant.infra.TenantContext.getTenantId());
        SupervisorSession saved = supervisorSessionRepository.save(session);

        log.info("Supervisor session started successfully with ID: {}", saved.getId());
        return mapToDTO(saved);
    }

    @Transactional
    public SupervisorSessionDTO endSupervisorSession(Long sessionId, String notes, Integer rating) {
        log.info("Ending supervisor session with ID: {}", sessionId);

        SupervisorSession session = supervisorSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Supervisor session not found"));

        session.setSessionStatus(SupervisorSession.SessionStatus.ENDED);
        session.setLeftAt(LocalDateTime.now());

        if (session.getJoinedAt() != null && session.getLeftAt() != null) {
            long seconds = ChronoUnit.SECONDS.between(session.getJoinedAt(), session.getLeftAt());
            session.setDurationSeconds((int) seconds);
        }

        if (notes != null) {
            session.setNotes(notes);
        }

        if (rating != null) {
            session.setRating(rating);
        }

        SupervisorSession updated = supervisorSessionRepository.save(session);
        log.info("Supervisor session ended successfully");
        return mapToDTO(updated);
    }

    @Transactional
    public SupervisorSessionDTO recordIntervention(Long sessionId) {
        log.info("Recording intervention for supervisor session: {}", sessionId);

        SupervisorSession session = supervisorSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Supervisor session not found"));

        session.setInterventionCount(session.getInterventionCount() + 1);
        SupervisorSession updated = supervisorSessionRepository.save(session);

        log.info("Intervention recorded. Total interventions: {}", updated.getInterventionCount());
        return mapToDTO(updated);
    }

    @Transactional
    public void deleteSupervisorSession(Long id) {
        log.info("Deleting supervisor session with ID: {}", id);
        SupervisorSession session = supervisorSessionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Supervisor session not found"));
        supervisorSessionRepository.delete(session);
        log.info("Supervisor session deleted successfully");
    }

    @Transactional(readOnly = true)
    public List<SupervisorSessionDTO> getAllSupervisorSessions(Map<String, String> params) {
        Long tenantId = com.chatbot.core.tenant.infra.TenantContext.getTenantId();
        List<SupervisorSession> sessions = supervisorSessionRepository
                .findByTenantIdOrderByJoinedAtDesc(tenantId);
        return sessions.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SupervisorSessionDTO getSupervisorSessionById(Long id) {
        SupervisorSession session = supervisorSessionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Supervisor session not found"));
        return mapToDTO(session);
    }

    @Transactional(readOnly = true)
    public List<SupervisorSessionDTO> getSupervisorSessionsBySupervisor(Long supervisorId) {
        Long tenantId = com.chatbot.core.tenant.infra.TenantContext.getTenantId();
        List<SupervisorSession> sessions = supervisorSessionRepository
                .findByTenantIdAndSupervisorIdOrderByJoinedAtDesc(tenantId, supervisorId);
        return sessions.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SupervisorSessionDTO> getSupervisorSessionsByConversation(Long conversationId) {
        Long tenantId = com.chatbot.core.tenant.infra.TenantContext.getTenantId();
        List<SupervisorSession> sessions = supervisorSessionRepository
                .findByTenantIdAndConversationIdOrderByJoinedAtDesc(tenantId, conversationId);
        return sessions.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SupervisorSessionDTO> getActiveSupervisorSessions() {
        Long tenantId = com.chatbot.core.tenant.infra.TenantContext.getTenantId();
        List<SupervisorSession> sessions = supervisorSessionRepository
                .findByTenantIdAndSessionStatus(tenantId, SupervisorSession.SessionStatus.ACTIVE);
        return sessions.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SupervisorSessionDTO getActiveSessionForSupervisorAndConversation(Long supervisorId, Long conversationId) {
        Long tenantId = com.chatbot.core.tenant.infra.TenantContext.getTenantId();
        return supervisorSessionRepository
                .findFirstByTenantIdAndSupervisorIdAndConversationIdAndSessionStatusOrderByJoinedAtDesc(
                        tenantId, supervisorId, conversationId, SupervisorSession.SessionStatus.ACTIVE)
                .map(this::mapToDTO)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public long getActiveSessionCount(Long supervisorId) {
        Long tenantId = com.chatbot.core.tenant.infra.TenantContext.getTenantId();
        return supervisorSessionRepository.countByTenantIdAndSupervisorIdAndSessionStatus(
                tenantId, supervisorId, SupervisorSession.SessionStatus.ACTIVE);
    }

    private SupervisorSessionDTO mapToDTO(SupervisorSession session) {
        return SupervisorSessionDTO.builder()
                .id((Long) session.getId())
                .supervisorId(session.getSupervisorId())
                .conversationId(session.getConversationId())
                .sessionType(session.getSessionType())
                .sessionStatus(session.getSessionStatus())
                .monitoredAgentId(session.getMonitoredAgentId())
                .joinedAt(session.getJoinedAt())
                .leftAt(session.getLeftAt())
                .durationSeconds(session.getDurationSeconds())
                .isSilent(session.getIsSilent())
                .canIntervene(session.getCanIntervene())
                .interventionCount(session.getInterventionCount())
                .notes(session.getNotes())
                .rating(session.getRating())
                .createdAt(session.getCreatedAt())
                .updatedAt(session.getUpdatedAt())
                .build();
    }

    @Transactional
    public SupervisorTakeoverResult takeoverConversation(Long supervisorId, Long conversationId, SupervisorSession.SessionType sessionType) {
        log.info("Supervisor {} taking over conversation {} with session type {}", supervisorId, conversationId, sessionType);
        
        Long tenantId = com.chatbot.core.tenant.infra.TenantContext.getTenantId();
        
        // Check if there's already an active session for this supervisor and conversation
        SupervisorSession existingSession = supervisorSessionRepository
                .findFirstByTenantIdAndSupervisorIdAndConversationIdAndSessionStatusOrderByJoinedAtDesc(
                        tenantId, supervisorId, conversationId, SupervisorSession.SessionStatus.ACTIVE)
                .orElse(null);
        
        if (existingSession != null) {
            log.warn("Active session already exists for supervisor {} and conversation {}", supervisorId, conversationId);
            return SupervisorTakeoverResult.builder()
                    .conversationId(conversationId)
                    .supervisorId(supervisorId)
                    .success(false)
                    .reason("Active session already exists")
                    .sessionId((Long) existingSession.getId())
                    .build();
        }
        
        // Create new supervisor session
        SupervisorSession session = SupervisorSession.builder()
                .supervisorId(supervisorId)
                .conversationId(conversationId)
                .sessionType(sessionType)
                .sessionStatus(SupervisorSession.SessionStatus.ACTIVE)
                .joinedAt(LocalDateTime.now())
                .isSilent(sessionType == SupervisorSession.SessionType.MONITORING)
                .canIntervene(sessionType != SupervisorSession.SessionType.MONITORING)
                .interventionCount(0)
                .build();
        
        session.setTenantId(tenantId);
        SupervisorSession saved = supervisorSessionRepository.save(session);
        
        log.info("Supervisor takeover successful. Session ID: {}", saved.getId());
        
        // TODO: Mark conversation as taken over in conversation routing
        // TODO: Send WebSocket notification to agents
        // TODO: Send WebSocket notification to supervisor
        
        return SupervisorTakeoverResult.builder()
                .conversationId(conversationId)
                .supervisorId(supervisorId)
                .success(true)
                .sessionId((Long) saved.getId())
                .sessionType(sessionType.name())
                .takenOverAt(LocalDateTime.now())
                .reason("Takeover successful")
                .build();
    }
    
    @Transactional
    public SupervisorTakeoverResult releaseConversation(Long sessionId) {
        log.info("Releasing conversation from supervisor session {}", sessionId);
        
        SupervisorSession session = supervisorSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Supervisor session not found"));
        
        if (session.getSessionStatus() != SupervisorSession.SessionStatus.ACTIVE) {
            log.warn("Session {} is not active", sessionId);
            return SupervisorTakeoverResult.builder()
                    .conversationId(session.getConversationId())
                    .supervisorId(session.getSupervisorId())
                    .success(false)
                    .reason("Session is not active")
                    .build();
        }
        
        session.setSessionStatus(SupervisorSession.SessionStatus.ENDED);
        session.setLeftAt(LocalDateTime.now());
        
        if (session.getJoinedAt() != null && session.getLeftAt() != null) {
            long seconds = ChronoUnit.SECONDS.between(session.getJoinedAt(), session.getLeftAt());
            session.setDurationSeconds((int) seconds);
        }
        
        SupervisorSession updated = supervisorSessionRepository.save(session);
        
        log.info("Conversation released successfully");
        
        // TODO: Mark conversation as no longer taken over in conversation routing
        // TODO: Send WebSocket notification to agents
        // TODO: Send WebSocket notification to supervisor
        
        return SupervisorTakeoverResult.builder()
                .conversationId(session.getConversationId())
                .supervisorId(session.getSupervisorId())
                .success(true)
                .sessionId(sessionId)
                .releasedAt(LocalDateTime.now())
                .reason("Conversation released successfully")
                .build();
    }
    
    @Transactional(readOnly = true)
    public boolean isConversationTakenOver(Long conversationId) {
        Long tenantId = com.chatbot.core.tenant.infra.TenantContext.getTenantId();
        List<SupervisorSession> activeSessions = supervisorSessionRepository
                .findByTenantIdAndSessionStatus(tenantId, SupervisorSession.SessionStatus.ACTIVE);
        
        return activeSessions.stream()
                .anyMatch(session -> session.getConversationId().equals(conversationId));
    }
    
    @Transactional(readOnly = true)
    public List<SupervisorSessionDTO> getActiveSessionsForConversation(Long conversationId) {
        Long tenantId = com.chatbot.core.tenant.infra.TenantContext.getTenantId();
        List<SupervisorSession> sessions = supervisorSessionRepository
                .findByTenantIdAndConversationIdOrderByJoinedAtDesc(tenantId, conversationId);
        
        return sessions.stream()
                .filter(session -> session.getSessionStatus() == SupervisorSession.SessionStatus.ACTIVE)
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
}
