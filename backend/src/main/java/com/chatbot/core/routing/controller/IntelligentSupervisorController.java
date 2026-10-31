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
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/routing/supervisor-sessions")
@RequiredArgsConstructor
@Slf4j
public class IntelligentSupervisorController {

    private final IntelligentSupervisorService supervisorService;

    @PostMapping
    public ResponseEntity<SupervisorSessionDTO> startSession(@Valid @RequestBody CreateSupervisorSessionRequest request) {
        SupervisorSessionDTO session = supervisorService.startSupervisorSession(request);
        return ResponseEntity.ok(session);
    }

    @PutMapping("/{id}/end")
    public ResponseEntity<SupervisorSessionDTO> endSession(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        String notes = request.containsKey("notes") ? (String) request.get("notes") : null;
        Integer rating = request.containsKey("rating") ? ((Number) request.get("rating")).intValue() : null;
        SupervisorSessionDTO session = supervisorService.endSupervisorSession(id, notes, rating);
        return ResponseEntity.ok(session);
    }

    @PutMapping("/{id}/intervention")
    public ResponseEntity<SupervisorSessionDTO> recordIntervention(@PathVariable Long id) {
        SupervisorSessionDTO session = supervisorService.recordIntervention(id);
        return ResponseEntity.ok(session);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SupervisorSessionDTO> getSession(@PathVariable Long id) {
        SupervisorSessionDTO session = supervisorService.getSupervisorSessionById(id);
        return ResponseEntity.ok(session);
    }

    @GetMapping("/supervisor/{supervisorId}")
    public ResponseEntity<List<SupervisorSessionDTO>> getSessionsBySupervisor(@PathVariable Long supervisorId) {
        List<SupervisorSessionDTO> sessions = supervisorService.getSupervisorSessionsBySupervisor(supervisorId);
        return ResponseEntity.ok(sessions);
    }

    @GetMapping("/conversation/{conversationId}")
    public ResponseEntity<List<SupervisorSessionDTO>> getSessionsByConversation(@PathVariable Long conversationId) {
        List<SupervisorSessionDTO> sessions = supervisorService.getSupervisorSessionsByConversation(conversationId);
        return ResponseEntity.ok(sessions);
    }

    @GetMapping("/active")
    public ResponseEntity<List<SupervisorSessionDTO>> getActiveSessions() {
        List<SupervisorSessionDTO> sessions = supervisorService.getActiveSupervisorSessions();
        return ResponseEntity.ok(sessions);
    }

    @GetMapping("/active/supervisor/{supervisorId}/conversation/{conversationId}")
    public ResponseEntity<SupervisorSessionDTO> getActiveSession(
            @PathVariable Long supervisorId,
            @PathVariable Long conversationId) {
        SupervisorSessionDTO session = supervisorService.getActiveSessionForSupervisorAndConversation(supervisorId, conversationId);
        return ResponseEntity.ok(session);
    }

    @GetMapping("/active/count/{supervisorId}")
    public ResponseEntity<Long> getActiveSessionCount(@PathVariable Long supervisorId) {
        long count = supervisorService.getActiveSessionCount(supervisorId);
        return ResponseEntity.ok(count);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSession(@PathVariable Long id) {
        supervisorService.deleteSupervisorSession(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/takeover")
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
    public ResponseEntity<SupervisorTakeoverResult> releaseConversation(@PathVariable Long sessionId) {
        SupervisorTakeoverResult result = supervisorService.releaseConversation(sessionId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/taken-over/{conversationId}")
    public ResponseEntity<Boolean> isConversationTakenOver(@PathVariable Long conversationId) {
        boolean takenOver = supervisorService.isConversationTakenOver(conversationId);
        return ResponseEntity.ok(takenOver);
    }

    @GetMapping("/active/conversation/{conversationId}")
    public ResponseEntity<List<SupervisorSessionDTO>> getActiveSessionsForConversation(@PathVariable Long conversationId) {
        List<SupervisorSessionDTO> sessions = supervisorService.getActiveSessionsForConversation(conversationId);
        return ResponseEntity.ok(sessions);
    }
}
