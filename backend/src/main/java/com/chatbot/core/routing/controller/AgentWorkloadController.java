package com.chatbot.core.routing.controller;

import com.chatbot.core.routing.dto.AgentWorkloadDTO;
import com.chatbot.core.routing.dto.RoutingAssignmentResult;
import com.chatbot.core.routing.service.IntelligentRoutingService;
import com.chatbot.core.tenant.infra.TenantContext;
import com.chatbot.core.tenant.service.TenantPermissionValidator;
import com.chatbot.core.tenant.exception.InsufficientPermissionException;
import com.chatbot.shared.exceptions.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/routing")
@RequiredArgsConstructor
@Slf4j
public class AgentWorkloadController {

    private final IntelligentRoutingService routingService;
    private final TenantPermissionValidator permissionValidator;

    private void requireOwnerOrEditor() {
        Long tenantId = TenantContext.getTenantId();
        String email = permissionValidator.getCurrentUserEmail();
        if (tenantId == null || email == null || !permissionValidator.isOwnerOrEditor(tenantId, email)) {
            throw new InsufficientPermissionException(ErrorCode.INSUFFICIENT_PERMISSION,
                    "Only tenant owner or editor can manually assign conversations");
        }
    }

    @GetMapping("/agent-workloads")
    public ResponseEntity<List<AgentWorkloadDTO>> getAgentWorkloads(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search) {
        List<AgentWorkloadDTO> workloads = routingService.getAgentWorkloads(status, search);
        return ResponseEntity.ok(workloads);
    }

    @GetMapping("/agent-workloads/{agentId}")
    public ResponseEntity<AgentWorkloadDTO> getAgentWorkload(@PathVariable Long agentId) {
        AgentWorkloadDTO workload = routingService.getAgentWorkloadById(agentId);
        return ResponseEntity.ok(workload);
    }

    @PostMapping("/assign")
    public ResponseEntity<RoutingAssignmentResult> assignConversation(
            @RequestBody Map<String, Object> request) {
        requireOwnerOrEditor();
        Long conversationId = ((Number) request.get("conversationId")).longValue();
        Long agentId = ((Number) request.get("agentId")).longValue();
        String reason = request.containsKey("reason") ? (String) request.get("reason") : null;
        
        RoutingAssignmentResult result = routingService.assignConversationToAgent(conversationId, agentId, reason);
        return ResponseEntity.ok(result);
    }
}
