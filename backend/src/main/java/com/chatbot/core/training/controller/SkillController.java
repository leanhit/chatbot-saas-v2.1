package com.chatbot.core.training.controller;

import com.chatbot.core.training.model.AgentSkill;
import com.chatbot.core.training.service.SkillAssessmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/skills")
@RequiredArgsConstructor
public class SkillController {

    private final SkillAssessmentService skillAssessmentService;

    @GetMapping("/agent/{agentId}")
    public ResponseEntity<List<AgentSkill>> getAgentSkills(@PathVariable Long agentId) {
        Long tenantId = com.chatbot.core.tenant.infra.TenantContext.getTenantId();
        List<AgentSkill> skills = skillAssessmentService.getAgentSkills(agentId, tenantId);
        return ResponseEntity.ok(skills);
    }

    @PostMapping("/update")
    public ResponseEntity<AgentSkill> updateSkill(
            @RequestParam Long agentId,
            @RequestParam Integer score) {
        Long tenantId = com.chatbot.core.tenant.infra.TenantContext.getTenantId();
        AgentSkill skill = skillAssessmentService.updateSkill(agentId, score, tenantId);
        return ResponseEntity.ok(skill);
    }
}
