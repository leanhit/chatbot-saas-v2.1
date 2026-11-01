package com.chatbot.core.routing.service;

import com.chatbot.core.routing.dto.SupervisorSessionDTO;
import com.chatbot.core.routing.model.SupervisorSession;
import com.chatbot.core.routing.repository.SupervisorSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SupervisorSessionService {

    private final SupervisorSessionRepository supervisorSessionRepository;

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
    public SupervisorSessionDTO endSupervisorSession(Long sessionId, String notes, Integer rating) {
        log.info("Ending supervisor session with ID: {}", sessionId);

        SupervisorSession session = supervisorSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Supervisor session not found"));

        if (session.getSessionStatus() != SupervisorSession.SessionStatus.ACTIVE) {
            throw new IllegalStateException("Cannot end a session that is not active");
        }

        session.setSessionStatus(SupervisorSession.SessionStatus.ENDED);
        session.setLeftAt(LocalDateTime.now());

        // Calculate duration
        if (session.getJoinedAt() != null) {
            Duration duration = Duration.between(session.getJoinedAt(), session.getLeftAt());
            session.setDurationSeconds((int) duration.getSeconds());
        }

        if (notes != null) {
            session.setNotes(notes);
        }

        if (rating != null) {
            session.setRating(rating);
        }

        SupervisorSession updated = supervisorSessionRepository.save(session);
        log.info("Supervisor session ended successfully. Duration: {} seconds", updated.getDurationSeconds());

        return mapToDTO(updated);
    }

    @Transactional
    public SupervisorSessionDTO interruptSupervisorSession(Long sessionId) {
        log.info("Interrupting supervisor session with ID: {}", sessionId);

        SupervisorSession session = supervisorSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Supervisor session not found"));

        if (session.getSessionStatus() != SupervisorSession.SessionStatus.ACTIVE) {
            throw new IllegalStateException("Cannot interrupt a session that is not active");
        }

        session.setSessionStatus(SupervisorSession.SessionStatus.INTERRUPTED);
        session.setLeftAt(LocalDateTime.now());

        // Calculate duration
        if (session.getJoinedAt() != null) {
            Duration duration = Duration.between(session.getJoinedAt(), session.getLeftAt());
            session.setDurationSeconds((int) duration.getSeconds());
        }

        SupervisorSession updated = supervisorSessionRepository.save(session);
        log.info("Supervisor session interrupted successfully. Duration: {} seconds", updated.getDurationSeconds());

        return mapToDTO(updated);
    }

    @Transactional
    public SupervisorSessionDTO recordIntervention(Long sessionId) {
        log.info("Recording intervention for supervisor session with ID: {}", sessionId);

        SupervisorSession session = supervisorSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Supervisor session not found"));

        if (session.getSessionStatus() != SupervisorSession.SessionStatus.ACTIVE) {
            throw new IllegalStateException("Cannot record intervention for a session that is not active");
        }

        if (!session.getCanIntervene()) {
            throw new IllegalStateException("Intervention not allowed for this session type");
        }

        session.setInterventionCount(session.getInterventionCount() + 1);
        SupervisorSession updated = supervisorSessionRepository.save(session);
        log.info("Intervention recorded. Total interventions: {}", updated.getInterventionCount());

        return mapToDTO(updated);
    }

    @Transactional
    public SupervisorSessionDTO updateMonitoredAgent(Long sessionId, Long monitoredAgentId) {
        log.info("Updating monitored agent to {} for supervisor session with ID: {}", monitoredAgentId, sessionId);

        SupervisorSession session = supervisorSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Supervisor session not found"));

        session.setMonitoredAgentId(monitoredAgentId);
        SupervisorSession updated = supervisorSessionRepository.save(session);
        log.info("Monitored agent updated successfully");

        return mapToDTO(updated);
    }

    @Transactional(readOnly = true)
    public SupervisorSessionDTO getSupervisorSessionById(Long sessionId) {
        SupervisorSession session = supervisorSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Supervisor session not found"));
        return mapToDTO(session);
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

    private SupervisorSessionDTO mapToDTO(SupervisorSession session) {
        return SupervisorSessionDTO.builder()
                .id((Long) session.getId())
                .tenantId(session.getTenantId())
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
