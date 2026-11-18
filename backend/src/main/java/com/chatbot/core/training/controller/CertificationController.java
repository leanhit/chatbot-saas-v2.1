package com.chatbot.core.training.controller;

import com.chatbot.core.training.model.AgentCertification;
import com.chatbot.core.training.model.CertificationLevel;
import com.chatbot.core.training.service.CertificationService;
import com.chatbot.core.tenant.infra.TenantContext;
import com.chatbot.core.tenant.service.TenantPermissionValidator;
import com.chatbot.core.tenant.exception.InsufficientPermissionException;
import com.chatbot.shared.exceptions.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import com.chatbot.core.training.dto.CertificationLevelResponse;
import com.chatbot.core.training.dto.AgentCertificationResponse;
import com.chatbot.core.training.dto.CertificationLevelRequest;

@RestController
@RequestMapping("/api/certifications")
@RequiredArgsConstructor
public class CertificationController {

    private final CertificationService certificationService;
    private final TenantPermissionValidator permissionValidator;

    private void requireOwnerOrEditor() {
        Long tenantId = TenantContext.getTenantId();
        String email = permissionValidator.getCurrentUserEmail();
        if (tenantId == null || email == null || !permissionValidator.isOwnerOrEditor(tenantId, email)) {
            throw new InsufficientPermissionException(ErrorCode.INSUFFICIENT_PERMISSION,
                    "Only tenant owner or editor can modify certifications");
        }
    }

    @GetMapping("/levels")
    public ResponseEntity<List<CertificationLevelResponse>> getAllCertificationLevels() {
        Long tenantId = com.chatbot.core.tenant.infra.TenantContext.getTenantId();
        List<CertificationLevel> levels = certificationService.getAllCertificationLevels(tenantId);
        return ResponseEntity.ok(levels.stream().map(CertificationLevelResponse::from).toList());
    }

    @PostMapping("/levels")
    public ResponseEntity<CertificationLevelResponse> createCertificationLevel(@RequestBody CertificationLevelRequest request) {
        requireOwnerOrEditor();
        CertificationLevel level = new CertificationLevel();
        level.setLevelName(request.getLevelName());
        level.setDescription(request.getDescription());
        level.setRequiredTrainingIds(request.getRequiredTrainingIds());
        level.setRequiredSkillScores(request.getRequiredSkillScores());
        level.setRequiredStats(request.getRequiredStats());
        level.setBadgeIcon(request.getBadgeIcon());
        level.setBadgeColor(request.getBadgeColor());
        level.setDisplayOrder(request.getDisplayOrder());
        if (request.getActive() != null) {
            level.setActive(request.getActive());
        }
        level.setValidityDays(request.getValidityDays());
        CertificationLevel created = certificationService.createCertificationLevel(level);
        return ResponseEntity.ok(CertificationLevelResponse.from(created));
    }

    @PostMapping("/award")
    public ResponseEntity<AgentCertificationResponse> awardCertification(
            @RequestParam Long agentId,
            @RequestParam Long certificationLevelId,
            @RequestParam String issuedBy) {
        requireOwnerOrEditor();
        Long tenantId = TenantContext.getTenantId();
        AgentCertification certification = certificationService.awardCertification(agentId, certificationLevelId, issuedBy, tenantId);
        return ResponseEntity.ok(AgentCertificationResponse.from(certification));
    }

    @GetMapping("/agent/{agentId}")
    public ResponseEntity<List<AgentCertificationResponse>> getAgentCertifications(@PathVariable Long agentId) {
        Long tenantId = com.chatbot.core.tenant.infra.TenantContext.getTenantId();
        List<AgentCertification> certifications = certificationService.getAgentCertifications(agentId, tenantId);
        return ResponseEntity.ok(certifications.stream().map(AgentCertificationResponse::from).toList());
    }
}
