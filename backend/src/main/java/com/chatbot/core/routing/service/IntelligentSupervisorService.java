package com.chatbot.core.routing.service;

import com.chatbot.core.routing.dto.CreateSupervisorSessionRequest;
import com.chatbot.core.routing.dto.SupervisorSessionDTO;
import com.chatbot.core.routing.model.SupervisorSession;
import com.chatbot.core.routing.repository.IntelligentSupervisorSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
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
}
