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
    public ResponseEntity<List<CertificationLevel>> getAllCertificationLevels() {
        Long tenantId = com.chatbot.core.tenant.infra.TenantContext.getTenantId();
        List<CertificationLevel> levels = certificationService.getAllCertificationLevels(tenantId);
        return ResponseEntity.ok(levels);
    }

    @PostMapping("/levels")
    public ResponseEntity<CertificationLevel> createCertificationLevel(@RequestBody CertificationLevel level) {
        requireOwnerOrEditor();
        CertificationLevel created = certificationService.createCertificationLevel(level);
        return ResponseEntity.ok(created);
    }

    @PostMapping("/award")
    public ResponseEntity<AgentCertification> awardCertification(
            @RequestParam Long agentId,
            @RequestParam Long certificationLevelId,
            @RequestParam String issuedBy) {
        requireOwnerOrEditor();
        Long tenantId = TenantContext.getTenantId();
        AgentCertification certification = certificationService.awardCertification(agentId, certificationLevelId, issuedBy, tenantId);
        return ResponseEntity.ok(certification);
    }

    @GetMapping("/agent/{agentId}")
    public ResponseEntity<List<AgentCertification>> getAgentCertifications(@PathVariable Long agentId) {
        Long tenantId = com.chatbot.core.tenant.infra.TenantContext.getTenantId();
        List<AgentCertification> certifications = certificationService.getAgentCertifications(agentId, tenantId);
        return ResponseEntity.ok(certifications);
    }
}
