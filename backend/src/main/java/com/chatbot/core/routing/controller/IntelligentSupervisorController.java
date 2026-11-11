package com.chatbot.core.routing.controller;

import com.chatbot.core.routing.dto.CreateSupervisorSessionRequest;
import com.chatbot.core.routing.dto.SupervisorSessionDTO;
import com.chatbot.core.routing.dto.SupervisorTakeoverResult;
import com.chatbot.core.routing.model.SupervisorSession;
import com.chatbot.core.routing.service.IntelligentSupervisorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/routing/supervisor-sessions")
@RequiredArgsConstructor
@Slf4j
public class IntelligentSupervisorController {

    private final IntelligentSupervisorService supervisorService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SUPERVISOR')")
    public ResponseEntity<List<SupervisorSessionDTO>> getAllSessions(@RequestParam(required = false) Map<String, String> params) {
        List<SupervisorSessionDTO> sessions = supervisorService.getAllSupervisorSessions(params);
        return ResponseEntity.ok(sessions);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SUPERVISOR')")
    public ResponseEntity<SupervisorSessionDTO> startSession(@Valid @RequestBody CreateSupervisorSessionRequest request) {
        SupervisorSessionDTO session = supervisorService.startSupervisorSession(request);
        return ResponseEntity.ok(session);
    }

    @PutMapping("/{id}/end")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SUPERVISOR')")
    public ResponseEntity<SupervisorSessionDTO> endSession(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        String notes = request.containsKey("notes") ? (String) request.get("notes") : null;
        Integer rating = request.containsKey("rating") ? ((Number) request.get("rating")).intValue() : null;
        SupervisorSessionDTO session = supervisorService.endSupervisorSession(id, notes, rating);
        return ResponseEntity.ok(session);
    }

    @PutMapping("/{id}/intervention")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SUPERVISOR')")
    public ResponseEntity<SupervisorSessionDTO> recordIntervention(@PathVariable Long id) {
        SupervisorSessionDTO session = supervisorService.recordIntervention(id);
        return ResponseEntity.ok(session);
    }

    @PostMapping("/{id}/whisper")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SUPERVISOR')")
    public ResponseEntity<Void> sendWhisper(@PathVariable Long id, @RequestBody Map<String, String> request) {
        String content = request.get("content");
        supervisorService.sendWhisper(id, content);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SUPERVISOR')")
    public ResponseEntity<SupervisorSessionDTO> getSession(@PathVariable Long id) {
        SupervisorSessionDTO session = supervisorService.getSupervisorSessionById(id);
        return ResponseEntity.ok(session);
    }

    @GetMapping("/supervisor/{supervisorId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SUPERVISOR')")
    public ResponseEntity<List<SupervisorSessionDTO>> getSessionsBySupervisor(@PathVariable Long supervisorId) {
        List<SupervisorSessionDTO> sessions = supervisorService.getSupervisorSessionsBySupervisor(supervisorId);
        return ResponseEntity.ok(sessions);
    }

    @GetMapping("/conversation/{conversationId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SUPERVISOR')")
    public ResponseEntity<List<SupervisorSessionDTO>> getSessionsByConversation(@PathVariable Long conversationId) {
        List<SupervisorSessionDTO> sessions = supervisorService.getSupervisorSessionsByConversation(conversationId);
        return ResponseEntity.ok(sessions);
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SUPERVISOR')")
    public ResponseEntity<List<SupervisorSessionDTO>> getActiveSessions() {
        List<SupervisorSessionDTO> sessions = supervisorService.getActiveSupervisorSessions();
        return ResponseEntity.ok(sessions);
    }

    @GetMapping("/active/supervisor/{supervisorId}/conversation/{conversationId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SUPERVISOR')")
    public ResponseEntity<SupervisorSessionDTO> getActiveSession(
            @PathVariable Long supervisorId,
            @PathVariable Long conversationId) {
        SupervisorSessionDTO session = supervisorService.getActiveSessionForSupervisorAndConversation(supervisorId, conversationId);
        return ResponseEntity.ok(session);
    }

    @GetMapping("/active/count/{supervisorId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SUPERVISOR')")
    public ResponseEntity<Long> getActiveSessionCount(@PathVariable Long supervisorId) {
        long count = supervisorService.getActiveSessionCount(supervisorId);
        return ResponseEntity.ok(count);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Void> deleteSession(@PathVariable Long id) {
        supervisorService.deleteSupervisorSession(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/takeover")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SUPERVISOR')")
    public ResponseEntity<SupervisorTakeoverResult> takeoverConversation(
            @RequestBody Map<String, Object> request) {
        Long supervisorId = ((Number) request.get("supervisorId")).longValue();
        Long conversationId = ((Number) request.get("conversationId")).longValue();
        SupervisorSession.SessionType sessionType = request.containsKey("sessionType") 
                ? SupervisorSession.SessionType.valueOf((String) request.get("sessionType"))
                : SupervisorSession.SessionType.TAKEOVER;
        
        SupervisorTakeoverResult result = supervisorService.takeoverConversation(supervisorId, conversationId, sessionType);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/release/{sessionId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SUPERVISOR')")
    public ResponseEntity<SupervisorTakeoverResult> releaseConversation(@PathVariable Long sessionId) {
        SupervisorTakeoverResult result = supervisorService.releaseConversation(sessionId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/taken-over/{conversationId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SUPERVISOR', 'AGENT')")
    public ResponseEntity<Boolean> isConversationTakenOver(@PathVariable Long conversationId) {
        boolean takenOver = supervisorService.isConversationTakenOver(conversationId);
        return ResponseEntity.ok(takenOver);
    }

    @GetMapping("/active/conversation/{conversationId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SUPERVISOR', 'AGENT')")
    public ResponseEntity<List<SupervisorSessionDTO>> getActiveSessionsForConversation(@PathVariable Long conversationId) {
        List<SupervisorSessionDTO> sessions = supervisorService.getActiveSessionsForConversation(conversationId);
        return ResponseEntity.ok(sessions);
    }
}
