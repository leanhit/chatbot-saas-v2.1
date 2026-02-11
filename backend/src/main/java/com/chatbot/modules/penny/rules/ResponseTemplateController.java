package com.chatbot.modules.penny.rules;

import com.chatbot.modules.tenant.infra.TenantContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Response Template Controller - API quản lý templates phản hồi tùy chỉnh
 */
@RestController
@RequestMapping("/api/penny/bots/{botId}/templates")
@Slf4j
public class ResponseTemplateController {

    private final ResponseTemplateManager responseTemplateManager;

    public ResponseTemplateController(ResponseTemplateManager responseTemplateManager) {
        this.responseTemplateManager = responseTemplateManager;
    }

    /**
     * Create new response template
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createTemplate(
            @PathVariable String botId,
            @RequestBody Map<String, Object> request,
            Principal principal) {
        
        // Validate tenant context
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Tenant context not found. Please provide X-Tenant-Key header"
            ));
        }
        
        String createdBy = principal.getName();
        UUID botUuid = UUID.fromString(botId);
        
        try {
            ResponseTemplate template = responseTemplateManager.createTemplate(
                botUuid,
                (String) request.get("name"),
                (String) request.get("description"),
                (String) request.get("intent"),
                (String) request.get("templateText"),
                ResponseTemplate.TemplateType.valueOf((String) request.getOrDefault("templateType", "TEXT")),
                (String) request.getOrDefault("language", "vi"),
                (Integer) request.get("priority"),
                (Map<String, Object>) request.get("variables"),
                (List<Map<String, Object>>) request.get("quickReplies"),
                createdBy
            );
            
            return ResponseEntity.ok(Map.of(
                "templateId", template.getId().toString(),
                "name", template.getName(),
                "intent", template.getIntent(),
                "templateType", template.getTemplateType().name(),
                "language", template.getLanguage(),
                "priority", template.getPriority(),
                "isActive", template.getIsActive(),
                "message", "Response template created successfully"
            ));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("❌ Error creating response template: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Internal server error"
            ));
        }
    }

    /**
     * Update existing response template
     */
    @PutMapping("/{templateId}")
    public ResponseEntity<Map<String, Object>> updateTemplate(
            @PathVariable String botId,
            @PathVariable String templateId,
            @RequestBody Map<String, Object> request,
            Principal principal) {
        
        String updatedBy = principal.getName();
        UUID templateUuid = UUID.fromString(templateId);
        
        try {
            ResponseTemplate template = responseTemplateManager.updateTemplate(
                templateUuid,
                (String) request.get("name"),
                (String) request.get("description"),
                (String) request.get("intent"),
                (String) request.get("templateText"),
                request.get("templateType") != null ? 
                    ResponseTemplate.TemplateType.valueOf((String) request.get("templateType")) : null,
                (String) request.get("language"),
                (Integer) request.get("priority"),
                (Boolean) request.get("isActive"),
                (Map<String, Object>) request.get("variables"),
                (List<Map<String, Object>>) request.get("quickReplies"),
                updatedBy
            );
            
            return ResponseEntity.ok(Map.of(
                "templateId", template.getId().toString(),
                "name", template.getName(),
                "intent", template.getIntent(),
                "templateType", template.getTemplateType().name(),
                "language", template.getLanguage(),
                "priority", template.getPriority(),
                "isActive", template.getIsActive(),
                "usageCount", template.getUsageCount(),
                "message", "Response template updated successfully"
            ));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("❌ Error updating response template: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Internal server error"
            ));
        }
    }

    /**
     * Get all templates for bot
     */
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getTemplates(@PathVariable String botId) {
        UUID botUuid = UUID.fromString(botId);
        
        List<ResponseTemplate> templates = responseTemplateManager.getTemplatesForBot(botUuid);
        
        List<Map<String, Object>> response = templates.stream()
            .map(template -> {
                Map<String, Object> templateMap = new java.util.HashMap<>();
                templateMap.put("templateId", template.getId().toString());
                templateMap.put("name", template.getName());
                templateMap.put("description", template.getDescription());
                templateMap.put("intent", template.getIntent());
                templateMap.put("templateType", template.getTemplateType().name());
                templateMap.put("language", template.getLanguage());
                templateMap.put("priority", template.getPriority());
                templateMap.put("isActive", template.getIsActive());
                templateMap.put("usageCount", template.getUsageCount());
                templateMap.put("createdAt", template.getCreatedAt().toString());
                templateMap.put("updatedAt", template.getUpdatedAt().toString());
                return templateMap;
            })
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get templates by intent
     */
    @GetMapping("/by-intent/{intent}")
    public ResponseEntity<List<Map<String, Object>>> getTemplatesByIntent(
            @PathVariable String botId,
            @PathVariable String intent) {
        
        UUID botUuid = UUID.fromString(botId);
        
        List<ResponseTemplate> templates = responseTemplateManager.getTemplatesByIntent(botUuid, intent);
        
        List<Map<String, Object>> response = templates.stream()
            .map(template -> {
                Map<String, Object> templateMap = new java.util.HashMap<>();
                templateMap.put("templateId", template.getId().toString());
                templateMap.put("name", template.getName());
                templateMap.put("intent", template.getIntent());
                templateMap.put("language", template.getLanguage());
                templateMap.put("priority", template.getPriority());
                templateMap.put("usageCount", template.getUsageCount());
                return templateMap;
            })
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get best template for intent
     */
    @GetMapping("/best/{intent}")
    public ResponseEntity<Map<String, Object>> getBestTemplate(
            @PathVariable String botId,
            @PathVariable String intent,
            @RequestParam(defaultValue = "vi") String language) {
        
        UUID botUuid = UUID.fromString(botId);
        
        ResponseTemplate template = responseTemplateManager.getBestTemplate(botUuid, intent, language);
        
        if (template == null) {
            return ResponseEntity.notFound().build();
        }
        
        Map<String, Object> response = new java.util.HashMap<>();
        response.put("templateId", template.getId().toString());
        response.put("name", template.getName());
        response.put("intent", template.getIntent());
        response.put("templateType", template.getTemplateType().name());
        response.put("language", template.getLanguage());
        response.put("priority", template.getPriority());
        response.put("usageCount", template.getUsageCount());
        response.put("templateText", template.getTemplateText());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Process template with variables
     */
    @PostMapping("/{templateId}/process")
    public ResponseEntity<Map<String, Object>> processTemplate(
            @PathVariable String botId,
            @PathVariable String templateId,
            @RequestBody Map<String, Object> variables) {
        
        UUID templateUuid = UUID.fromString(templateId);
        
        try {
            ResponseTemplateManager.TemplateProcessResult result = 
                responseTemplateManager.processTemplate(templateUuid, variables);
            
            Map<String, Object> response = new java.util.HashMap<>();
            response.put("templateId", result.getTemplateId().toString());
            response.put("response", result.getResponse());
            response.put("templateType", result.getTemplateType().name());
            response.put("language", result.getLanguage());
            response.put("quickReplies", result.getQuickReplies());
            response.put("attachments", result.getAttachments());
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("❌ Error processing template: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Internal server error"
            ));
        }
    }

    /**
     * Delete template
     */
    @DeleteMapping("/{templateId}")
    public ResponseEntity<Map<String, String>> deleteTemplate(
            @PathVariable String botId,
            @PathVariable String templateId,
            Principal principal) {
        
        String deletedBy = principal.getName();
        UUID templateUuid = UUID.fromString(templateId);
        
        boolean success = responseTemplateManager.deleteTemplate(templateUuid, deletedBy);
        
        if (success) {
            return ResponseEntity.ok(Map.of(
                "message", "Response template deleted successfully",
                "templateId", templateId
            ));
        } else {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to delete response template",
                "templateId", templateId
            ));
        }
    }

    /**
     * Get template statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getTemplateStatistics(@PathVariable String botId) {
        UUID botUuid = UUID.fromString(botId);
        
        ResponseTemplateManager.TemplateStatistics stats = 
            responseTemplateManager.getTemplateStatistics(botUuid);
        
        Map<String, Object> response = new java.util.HashMap<>();
        response.put("totalTemplates", stats.getTotalTemplates());
        response.put("totalUsage", stats.getTotalUsage());
        response.put("usageByIntent", stats.getUsageByIntent());
        
        // Convert most used templates to response format
        List<Map<String, Object>> mostUsed = stats.getMostUsedTemplates().stream()
            .map(template -> {
                Map<String, Object> templateMap = new java.util.HashMap<>();
                templateMap.put("templateId", template.getId().toString());
                templateMap.put("name", template.getName());
                templateMap.put("intent", template.getIntent());
                templateMap.put("usageCount", template.getUsageCount());
                return templateMap;
            })
            .collect(Collectors.toList());
        response.put("mostUsedTemplates", mostUsed);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Preview template with sample variables
     */
    @PostMapping("/{templateId}/preview")
    public ResponseEntity<Map<String, Object>> previewTemplate(
            @PathVariable String botId,
            @PathVariable String templateId,
            @RequestBody Map<String, Object> variables) {
        
        UUID templateUuid = UUID.fromString(templateId);
        
        try {
            // Get template without incrementing usage
            ResponseTemplate template = responseTemplateManager.getTemplatesForBot(UUID.fromString(botId))
                .stream()
                .filter(t -> t.getId().equals(templateUuid))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Template not found"));
            
            // Process template with variables
            String processedText = template.processTemplate(variables);
            
            return ResponseEntity.ok(Map.of(
                "templateId", templateId,
                "templateName", template.getName(),
                "processedText", processedText,
                "variables", variables
            ));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("❌ Error previewing template: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Internal server error"
            ));
        }
    }
}
