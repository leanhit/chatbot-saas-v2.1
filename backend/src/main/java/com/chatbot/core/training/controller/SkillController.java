package com.chatbot.core.training.controller;

import com.chatbot.core.training.model.AgentSkill;
import com.chatbot.core.training.service.SkillAssessmentService;
import com.chatbot.core.tenant.infra.TenantContext;
import com.chatbot.core.tenant.service.TenantPermissionValidator;
import com.chatbot.core.tenant.exception.InsufficientPermissionException;
import com.chatbot.shared.exceptions.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/skills")
@RequiredArgsConstructor
public class SkillController {

    private final SkillAssessmentService skillAssessmentService;
    private final TenantPermissionValidator permissionValidator;

    private void requireOwnerOrEditor() {
        Long tenantId = TenantContext.getTenantId();
        String email = permissionValidator.getCurrentUserEmail();
        if (tenantId == null || email == null || !permissionValidator.isOwnerOrEditor(tenantId, email)) {
            throw new InsufficientPermissionException(ErrorCode.INSUFFICIENT_PERMISSION,
                    "Only tenant owner or editor can update skills");
        }
    }

    @GetMapping("/agent/{agentId}")
    public ResponseEntity<List<AgentSkill>> getAgentSkills(@PathVariable Long agentId) {
        Long tenantId = com.chatbot.core.tenant.infra.TenantContext.getTenantId();
        List<AgentSkill> skills = skillAssessmentService.getAgentSkills(agentId, tenantId);
        return ResponseEntity.ok(skills);
    }

    @PostMapping("/update")
    public ResponseEntity<AgentSkill> updateSkill(
            @RequestParam Long agentId,
            @RequestParam Integer score,
            @RequestParam(required = false) String category) {
        requireOwnerOrEditor();
        Long tenantId = TenantContext.getTenantId();
        AgentSkill skill = skillAssessmentService.updateSkill(agentId, category, score, tenantId);
        return ResponseEntity.ok(skill);
    }
}
