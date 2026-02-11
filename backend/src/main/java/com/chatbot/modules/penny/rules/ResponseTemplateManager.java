package com.chatbot.modules.penny.rules;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Response Template Manager - Quản lý templates phản hồi tùy chỉnh
 */
@Service
@Slf4j
public class ResponseTemplateManager {

    private final ResponseTemplateRepository responseTemplateRepository;
    private final ObjectMapper objectMapper;

    public ResponseTemplateManager(ResponseTemplateRepository responseTemplateRepository,
                                  ObjectMapper objectMapper) {
        this.responseTemplateRepository = responseTemplateRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Create new response template
     */
    @Transactional
    public ResponseTemplate createTemplate(UUID botId, String name, String description, String intent,
                                           String templateText, ResponseTemplate.TemplateType templateType,
                                           String language, Integer priority, Map<String, Object> variables,
                                           List<Map<String, Object>> quickReplies, String createdBy) {
        
        log.info("📝 Creating new response template: {} for bot: {}", name, botId);
        
        // Check if template name already exists
        if (responseTemplateRepository.existsByBotIdAndNameAndIsActiveTrue(botId, name)) {
            throw new IllegalArgumentException("Template with name '" + name + "' already exists for this bot");
        }
        
        ResponseTemplate template = ResponseTemplate.builder()
                .id(UUID.randomUUID())
                .botId(botId)
                .name(name)
                .description(description)
                .intent(intent)
                .templateText(templateText)
                .templateType(templateType)
                .language(language != null ? language : "vi")
                .priority(priority != null ? priority : 0)
                .isActive(true)
                .createdBy(createdBy)
                .build();
        
        // Serialize complex fields
        try {
            if (variables != null) {
                template.setVariables(objectMapper.writeValueAsString(variables));
            }
            if (quickReplies != null) {
                template.setQuickReplies(objectMapper.writeValueAsString(quickReplies));
            }
        } catch (JsonProcessingException e) {
            log.error("❌ Error serializing template data: {}", e.getMessage(), e);
            throw new IllegalArgumentException("Invalid template data format", e);
        }
        
        ResponseTemplate savedTemplate = responseTemplateRepository.save(template);
        log.info("✅ Response template created successfully: {} (ID: {})", savedTemplate.getName(), savedTemplate.getId());
        
        return savedTemplate;
    }

    /**
     * Update existing response template
     */
    @Transactional
    public ResponseTemplate updateTemplate(UUID templateId, String name, String description, String intent,
                                         String templateText, ResponseTemplate.TemplateType templateType,
                                         String language, Integer priority, Boolean isActive, 
                                         Map<String, Object> variables, List<Map<String, Object>> quickReplies,
                                         String updatedBy) {
        
        log.info("🔄 Updating response template: {}", templateId);
        
        ResponseTemplate template = responseTemplateRepository.findById(templateId)
                .orElseThrow(() -> new IllegalArgumentException("Template not found: " + templateId));
        
        // Update fields
        if (name != null && !name.trim().isEmpty()) {
            template.setName(name);
        }
        if (description != null) {
            template.setDescription(description);
        }
        if (intent != null) {
            template.setIntent(intent);
        }
        if (templateText != null) {
            template.setTemplateText(templateText);
        }
        if (templateType != null) {
            template.setTemplateType(templateType);
        }
        if (language != null) {
            template.setLanguage(language);
        }
        if (priority != null) {
            template.setPriority(priority);
        }
        if (isActive != null) {
            template.setIsActive(isActive);
        }
        
        // Update complex fields
        try {
            if (variables != null) {
                template.setVariables(objectMapper.writeValueAsString(variables));
            }
            if (quickReplies != null) {
                template.setQuickReplies(objectMapper.writeValueAsString(quickReplies));
            }
        } catch (JsonProcessingException e) {
            log.error("❌ Error serializing template data: {}", e.getMessage(), e);
            throw new IllegalArgumentException("Invalid template data format", e);
        }
        
        template.setUpdatedBy(updatedBy);
        
        ResponseTemplate updatedTemplate = responseTemplateRepository.save(template);
        log.info("✅ Response template updated successfully: {}", updatedTemplate.getId());
        
        return updatedTemplate;
    }

    /**
     * Delete template (soft delete)
     */
    @Transactional
    public boolean deleteTemplate(UUID templateId, String deletedBy) {
        log.info("🗑️ Deleting response template: {} by: {}", templateId, deletedBy);
        
        try {
            responseTemplateRepository.softDeleteTemplate(templateId);
            log.info("✅ Response template deleted successfully: {}", templateId);
            return true;
        } catch (Exception e) {
            log.error("❌ Error deleting response template {}: {}", templateId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Get templates for bot
     */
    public List<ResponseTemplate> getTemplatesForBot(UUID botId) {
        return responseTemplateRepository.findByBotIdAndIsActiveTrueOrderByPriorityDesc(botId);
    }

    /**
     * Get templates by intent
     */
    public List<ResponseTemplate> getTemplatesByIntent(UUID botId, String intent) {
        return responseTemplateRepository.findByBotIdAndIntentAndIsActiveTrueOrderByPriorityDesc(botId, intent);
    }

    /**
     * Get templates by intent and language
     */
    public List<ResponseTemplate> getTemplatesByIntentAndLanguage(UUID botId, String intent, String language) {
        return responseTemplateRepository.findByBotIdAndIntentAndLanguageAndIsActiveTrueOrderByPriorityDesc(botId, intent, language);
    }

    /**
     * Get best matching template for intent
     */
    public ResponseTemplate getBestTemplate(UUID botId, String intent, String language) {
        List<ResponseTemplate> templates = getTemplatesByIntentAndLanguage(botId, intent, language);
        
        if (templates.isEmpty()) {
            // Try fallback to default language
            templates = getTemplatesByIntentAndLanguage(botId, intent, "vi");
        }
        
        if (templates.isEmpty()) {
            return null;
        }
        
        // Return highest priority template
        return templates.get(0);
    }

    /**
     * Process template with variables
     */
    public TemplateProcessResult processTemplate(UUID templateId, Map<String, Object> variables) {
        ResponseTemplate template = responseTemplateRepository.findById(templateId)
                .orElseThrow(() -> new IllegalArgumentException("Template not found: " + templateId));
        
        if (!template.getIsActive()) {
            throw new IllegalArgumentException("Template is not active: " + templateId);
        }
        
        // Increment usage count
        template.incrementUsageCount();
        responseTemplateRepository.save(template);
        
        // Process template text
        String processedText = template.processTemplate(variables);
        
        // Parse quick replies and attachments
        List<Map<String, Object>> quickReplies = null;
        List<Map<String, Object>> attachments = null;
        
        try {
            if (template.getQuickReplies() != null) {
                quickReplies = objectMapper.readValue(template.getQuickReplies(), List.class);
            }
            if (template.getAttachments() != null) {
                attachments = objectMapper.readValue(template.getAttachments(), List.class);
            }
        } catch (JsonProcessingException e) {
            log.error("❌ Error parsing template metadata: {}", e.getMessage(), e);
        }
        
        return TemplateProcessResult.builder()
                .templateId(templateId)
                .response(processedText)
                .templateType(template.getTemplateType())
                .quickReplies(quickReplies)
                .attachments(attachments)
                .language(template.getLanguage())
                .build();
    }

    /**
     * Get template statistics
     */
    public TemplateStatistics getTemplateStatistics(UUID botId) {
        List<ResponseTemplate> templates = getTemplatesForBot(botId);
        
        long totalTemplates = templates.size();
        long totalUsage = templates.stream().mapToLong(ResponseTemplate::getUsageCount).sum();
        
        Map<String, Long> usageByIntent = templates.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        ResponseTemplate::getIntent,
                        java.util.stream.Collectors.summingLong(ResponseTemplate::getUsageCount)
                ));
        
        return TemplateStatistics.builder()
                .totalTemplates(totalTemplates)
                .totalUsage(totalUsage)
                .usageByIntent(usageByIntent)
                .mostUsedTemplates(responseTemplateRepository.findMostUsedTemplates(botId))
                .build();
    }

    // Result classes
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class TemplateProcessResult {
        private UUID templateId;
        private String response;
        private ResponseTemplate.TemplateType templateType;
        private List<Map<String, Object>> quickReplies;
        private List<Map<String, Object>> attachments;
        private String language;
        private Map<String, Object> metadata;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class TemplateStatistics {
        private long totalTemplates;
        private long totalUsage;
        private Map<String, Long> usageByIntent;
        private List<ResponseTemplate> mostUsedTemplates;
    }
}
