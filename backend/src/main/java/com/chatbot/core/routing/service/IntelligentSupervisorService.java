package com.chatbot.core.routing.service;

import com.chatbot.core.message.store.model.ConversationStatus;
import com.chatbot.core.message.store.repository.ConversationRepository;
import com.chatbot.core.notification.websocket.NotificationWebSocketHandler;
import com.chatbot.core.routing.dto.CreateSupervisorSessionRequest;
import com.chatbot.core.routing.dto.SupervisorSessionDTO;
import com.chatbot.core.routing.dto.SupervisorTakeoverResult;
import com.chatbot.core.routing.model.SupervisorSession;
import com.chatbot.core.routing.repository.SupervisorSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class IntelligentSupervisorService {

    private final SupervisorSessionRepository supervisorSessionRepository;
    private final ConversationRepository conversationRepository;
    private final NotificationWebSocketHandler notificationWebSocketHandler;

    @Transactional
    public SupervisorSessionDTO startSupervisorSession(CreateSupervisorSessionRequest request) {
        log.info("Starting supervisor session for supervisor: {}, conversation: {}",
                request.getSupervisorId(), request.getConversationId());

        Long tenantId = com.chatbot.core.tenant.infra.TenantContext.getTenantId();
        if (tenantId == null) {
            throw new IllegalStateException("Tenant context not set");
        }
        if (request.getConversationId() != null
                && conversationRepository.findByIdAndTenantId(request.getConversationId(), tenantId).isEmpty()) {
            throw new IllegalArgumentException(
                    "Conversation " + request.getConversationId() + " not found for tenant " + tenantId);
        }

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

        SupervisorSession session = findSessionForCurrentTenant(sessionId);

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

        SupervisorSession session = findSessionForCurrentTenant(sessionId);

        session.setInterventionCount(session.getInterventionCount() + 1);
        SupervisorSession updated = supervisorSessionRepository.save(session);

        log.info("Intervention recorded. Total interventions: {}", updated.getInterventionCount());
        return mapToDTO(updated);
    }

    /**
     * Send a whisper (internal note) from supervisor to the agent handling the conversation.
     * Whisper is broadcast to the tenant via WebSocket only — it is never sent to the customer.
     * Requires an ACTIVE session with canIntervene=true.
     */
    @Transactional
    public void sendWhisper(Long sessionId, String content) {
        log.info("Supervisor whisper on session {}", sessionId);

        SupervisorSession session = findSessionForCurrentTenant(sessionId);

        if (session.getSessionStatus() != SupervisorSession.SessionStatus.ACTIVE) {
            throw new IllegalStateException("Cannot whisper on a session that is not active");
        }
        if (!Boolean.TRUE.equals(session.getCanIntervene())) {
            throw new IllegalStateException("Session does not allow intervention (silent monitoring)");
        }
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Whisper content must not be empty");
        }

        session.setInterventionCount(session.getInterventionCount() + 1);
        supervisorSessionRepository.save(session);

        try {
            Map<String, Object> data = new HashMap<>();
            data.put("conversationId", session.getConversationId());
            data.put("supervisorId", session.getSupervisorId());
            data.put("sessionId", sessionId);
            data.put("sessionType", session.getSessionType() != null ? session.getSessionType().name() : null);
            data.put("content", content);
            data.put("timestamp", System.currentTimeMillis());

            Map<String, Object> event = new HashMap<>();
            event.put("type", "SUPERVISOR_WHISPER");
            event.put("data", data);
            notificationWebSocketHandler.broadcastToTenant(session.getTenantId(), event);
        } catch (Exception e) {
            log.error("Failed to broadcast whisper for session {}: {}", sessionId, e.getMessage());
        }
    }

    @Transactional
    public void deleteSupervisorSession(Long id) {
        log.info("Deleting supervisor session with ID: {}", id);
        SupervisorSession session = findSessionForCurrentTenant(id);
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
        SupervisorSession session = findSessionForCurrentTenant(id);
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

    private void markConversationTakenOver(Long conversationId, Long tenantId) {
        conversationRepository.findByIdAndTenantId(conversationId, tenantId).ifPresent(conv -> {
            conv.setIsTakenOverByAgent(true);
            conv.setStatus(ConversationStatus.ACTIVE_AGENT);
            conversationRepository.save(conv);
            log.info("Conversation {} marked as taken over by supervisor (tenant {})", conversationId, tenantId);
        });
    }

    private void markConversationReleased(Long conversationId, Long tenantId) {
        conversationRepository.findByIdAndTenantId(conversationId, tenantId).ifPresent(conv -> {
            conv.setIsTakenOverByAgent(false);
            if (ConversationStatus.ACTIVE_AGENT.equals(conv.getStatus())) {
                conv.setStatus(ConversationStatus.OPEN);
            }
            conversationRepository.save(conv);
            log.info("Conversation {} released from supervisor takeover (tenant {})", conversationId, tenantId);
        });
    }

    private void broadcastSupervisorEvent(Long tenantId, Long conversationId, Long supervisorId,
                                          String action, Long sessionId, String sessionType) {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("conversationId", conversationId);
            data.put("supervisorId", supervisorId);
            data.put("sessionId", sessionId);
            data.put("sessionType", sessionType);
            data.put("action", action);
            data.put("timestamp", System.currentTimeMillis());

            Map<String, Object> event = new HashMap<>();
            event.put("type", "SUPERVISOR_EVENT");
            event.put("data", data);
            notificationWebSocketHandler.broadcastToTenant(tenantId, event);
        } catch (Exception e) {
            log.error("Failed to broadcast supervisor event {} for conversation {}: {}",
                    action, conversationId, e.getMessage());
        }
    }

    private SupervisorSession findSessionForCurrentTenant(Long sessionId) {
        Long tenantId = com.chatbot.core.tenant.infra.TenantContext.getTenantId();
        if (tenantId == null) {
            throw new IllegalStateException("Tenant context not set");
        }
        return supervisorSessionRepository.findByIdAndTenantId(sessionId, tenantId)
                .orElseThrow(() -> new RuntimeException("Supervisor session not found"));
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

        // TAKEOVER sessions take the conversation away from the bot/agent;
        // monitoring-type sessions only observe and must not change state.
        if (sessionType == SupervisorSession.SessionType.TAKEOVER) {
            markConversationTakenOver(conversationId, tenantId);
        }
        broadcastSupervisorEvent(tenantId, conversationId, supervisorId,
                sessionType == SupervisorSession.SessionType.TAKEOVER
                        ? "supervisor_takeover_started" : "supervisor_session_started",
                (Long) saved.getId(), sessionType.name());

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
        
        SupervisorSession session = findSessionForCurrentTenant(sessionId);

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

        // Only TAKEOVER sessions mutated the conversation state — restore it.
        if (session.getSessionType() == SupervisorSession.SessionType.TAKEOVER) {
            markConversationReleased(session.getConversationId(), session.getTenantId());
        }
        broadcastSupervisorEvent(session.getTenantId(), session.getConversationId(),
                session.getSupervisorId(), "supervisor_session_ended",
                sessionId, session.getSessionType() != null ? session.getSessionType().name() : null);

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

    // Additional methods merged from SupervisorSessionService

    @Transactional
    public SupervisorSessionDTO createSupervisorSession(Long supervisorId, Long conversationId, SupervisorSession.SessionType sessionType) {
        log.info("Creating supervisor session for supervisor {} and conversation {} with type {}", supervisorId, conversationId, sessionType);

        Long tenantId = com.chatbot.core.tenant.infra.TenantContext.getTenantId();

        // Check if active session already exists for this supervisor+conversation
        supervisorSessionRepository.findByTenantIdAndSupervisorIdAndConversationId(tenantId, supervisorId, conversationId)
                .ifPresent(existing -> {
                    if (existing.getSessionStatus() == SupervisorSession.SessionStatus.ACTIVE) {
                        throw new IllegalStateException("Active supervisor session already exists for supervisor " + supervisorId + " and conversation " + conversationId);
                    }
                });

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
        log.info("Supervisor session created successfully with ID: {}", saved.getId());

        return mapToDTO(saved);
    }

    @Transactional
    public SupervisorSessionDTO interruptSupervisorSession(Long sessionId) {
        log.info("Interrupting supervisor session with ID: {}", sessionId);

        SupervisorSession session = findSessionForCurrentTenant(sessionId);

        if (session.getSessionStatus() != SupervisorSession.SessionStatus.ACTIVE) {
            throw new IllegalStateException("Cannot interrupt a session that is not active");
        }

        session.setSessionStatus(SupervisorSession.SessionStatus.INTERRUPTED);
        session.setLeftAt(LocalDateTime.now());

        // Calculate duration
        if (session.getJoinedAt() != null) {
            long seconds = ChronoUnit.SECONDS.between(session.getJoinedAt(), session.getLeftAt());
            session.setDurationSeconds((int) seconds);
        }

        SupervisorSession updated = supervisorSessionRepository.save(session);
        log.info("Supervisor session interrupted successfully. Duration: {} seconds", updated.getDurationSeconds());

        return mapToDTO(updated);
    }

    @Transactional
    public SupervisorSessionDTO updateMonitoredAgent(Long sessionId, Long monitoredAgentId) {
        log.info("Updating monitored agent to {} for supervisor session with ID: {}", monitoredAgentId, sessionId);

        SupervisorSession session = findSessionForCurrentTenant(sessionId);

        session.setMonitoredAgentId(monitoredAgentId);
        SupervisorSession updated = supervisorSessionRepository.save(session);
        log.info("Monitored agent updated successfully");

        return mapToDTO(updated);
    }

    @Transactional(readOnly = true)
    public List<SupervisorSessionDTO> getActiveSessionsBySupervisor(Long supervisorId) {
        Long tenantId = com.chatbot.core.tenant.infra.TenantContext.getTenantId();
        List<SupervisorSession> sessions = supervisorSessionRepository.findByTenantIdAndSupervisorIdAndSessionStatus(
                tenantId, supervisorId, SupervisorSession.SessionStatus.ACTIVE);
        return sessions.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SupervisorSessionDTO> getSessionsByConversation(Long conversationId) {
        Long tenantId = com.chatbot.core.tenant.infra.TenantContext.getTenantId();
        List<SupervisorSession> sessions = supervisorSessionRepository.findByTenantIdAndConversationId(tenantId, conversationId);
        return sessions.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Long getActiveSessionCount() {
        Long tenantId = com.chatbot.core.tenant.infra.TenantContext.getTenantId();
        return supervisorSessionRepository.countByTenantIdAndSessionStatus(tenantId, SupervisorSession.SessionStatus.ACTIVE);
    }

    @Transactional(readOnly = true)
    public List<SupervisorSessionDTO> getAllActiveSessions() {
        Long tenantId = com.chatbot.core.tenant.infra.TenantContext.getTenantId();
        List<SupervisorSession> sessions = supervisorSessionRepository.findByTenantIdAndSessionStatus(
                tenantId, SupervisorSession.SessionStatus.ACTIVE);
        return sessions.stream().map(this::mapToDTO).collect(Collectors.toList());
    }
}
