package com.chatbot.core.app.guard.service;

import com.chatbot.core.app.guard.model.AppGuard;
import com.chatbot.core.app.guard.model.GuardRule;
import com.chatbot.core.app.guard.model.GuardType;
import com.chatbot.shared.exceptions.ResourceNotFoundException;
import com.chatbot.shared.exceptions.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class AppGuardService {
    
    @Autowired
    private com.chatbot.core.app.guard.repository.AppGuardRepository appGuardRepository;
    
    @Autowired
    private com.chatbot.core.app.guard.repository.GuardRuleRepository guardRuleRepository;
    
    @Autowired
    private AccessControlService accessControlService;
    
    public AppGuard createGuard(Long appId, String guardName, GuardType guardType, String description, Long createdBy) {
        // Validate app exists
        if (!appGuardRepository.existsByAppId(appId)) {
            // Check if app exists in registry
            // This would typically call appRegistryService
        }
        
        // Check if guard with same name already exists for this app
        if (appGuardRepository.existsByAppIdAndGuardName(appId, guardName)) {
            throw new ValidationException("Guard with name '" + guardName + "' already exists for this app");
        }
        
        AppGuard guard = AppGuard.builder()
                .appId(appId)
                .guardName(guardName)
                .guardType(guardType)
                .description(description)
                .build();
        guard.setCreatedBy(String.valueOf(createdBy));
        
        return appGuardRepository.save(guard);
    }
    
    @Transactional(readOnly = true)
    public AppGuard getGuardById(Long id) {
        return appGuardRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Guard not found with id: " + id));
    }
    
    @Transactional(readOnly = true)
    public List<AppGuard> getGuardsByAppId(Long appId) {
        return appGuardRepository.findByAppId(appId);
    }
    
    @Transactional(readOnly = true)
    public List<AppGuard> getActiveGuardsByAppId(Long appId) {
        return appGuardRepository.findByAppIdAndIsActive(appId, true);
    }
    
    @Transactional(readOnly = true)
    public List<AppGuard> getGuardsByType(GuardType guardType) {
        return appGuardRepository.findByGuardType(guardType);
    }
    
    public AppGuard updateGuard(Long id, String guardName, GuardType guardType, String description, Long updatedBy) {
        AppGuard existingGuard = getGuardById(id);
        
        // Check if name is being changed and if new name already exists
        if (!existingGuard.getGuardName().equals(guardName) && 
            appGuardRepository.existsByAppIdAndGuardNameAndIdNot(existingGuard.getAppId(), guardName, id)) {
            throw new ValidationException("Guard with name '" + guardName + "' already exists for this app");
        }
        
        existingGuard.setGuardName(guardName);
        existingGuard.setGuardType(guardType);
        existingGuard.setDescription(description);
        existingGuard.setUpdatedBy(String.valueOf(updatedBy));
        
        return appGuardRepository.save(existingGuard);
    }
    
    public void toggleGuardActivation(Long id, Boolean isActive, Long updatedBy) {
        AppGuard guard = getGuardById(id);
        guard.setIsActive(isActive);
        guard.setUpdatedBy(String.valueOf(updatedBy));
        appGuardRepository.save(guard);
    }
    
    public void deleteGuard(Long id) {
        if (!appGuardRepository.existsById(id)) {
            throw new ResourceNotFoundException("Guard not found with id: " + id);
        }
        appGuardRepository.deleteById(id);
    }
    
    public GuardRule addRuleToGuard(Long guardId, String ruleName, String ruleCondition, String ruleAction, String ruleParameters) {
        AppGuard guard = getGuardById(guardId);
        
        // Check if rule with same name already exists for this guard
        if (guardRuleRepository.existsByAppGuardIdAndRuleName(guardId, ruleName)) {
            throw new ValidationException("Rule with name '" + ruleName + "' already exists for this guard");
        }
        
        GuardRule rule = new GuardRule(guard, ruleName, ruleCondition, ruleAction);
        rule.setRuleParameters(ruleParameters);
        
        return guardRuleRepository.save(rule);
    }
    
    @Transactional(readOnly = true)
    public List<GuardRule> getRulesByGuardId(Long guardId) {
        return guardRuleRepository.findByAppGuardId(guardId);
    }
    
    @Transactional(readOnly = true)
    public List<GuardRule> getActiveRulesByGuardId(Long guardId) {
        return guardRuleRepository.findByAppGuardIdAndIsActive(guardId, true);
    }
    
    public GuardRule updateRule(Long id, String ruleName, String ruleCondition, String ruleAction, String ruleParameters) {
        GuardRule existingRule = guardRuleRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Rule not found with id: " + id));
        
        // Check if name is being changed and if new name already exists
        if (!existingRule.getRuleName().equals(ruleName) && 
            guardRuleRepository.existsByAppGuardIdAndRuleNameAndIdNot((Long) existingRule.getAppGuard().getId(), ruleName, id)) {
            throw new ValidationException("Rule with name '" + ruleName + "' already exists for this guard");
        }
        
        existingRule.setRuleName(ruleName);
        existingRule.setRuleCondition(ruleCondition);
        existingRule.setRuleAction(ruleAction);
        existingRule.setRuleParameters(ruleParameters);
        
        return guardRuleRepository.save(existingRule);
    }
    
    public void toggleRuleActivation(Long id, Boolean isActive) {
        GuardRule rule = guardRuleRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Rule not found with id: " + id));
        
        rule.setIsActive(isActive);
        guardRuleRepository.save(rule);
    }
    
    public void deleteRule(Long id) {
        if (!guardRuleRepository.existsById(id)) {
            throw new ResourceNotFoundException("Rule not found with id: " + id);
        }
        guardRuleRepository.deleteById(id);
    }
    
    @Transactional(readOnly = true)
    public boolean evaluateGuard(Long appId, String context, Object requestData) {
        List<AppGuard> activeGuards = getActiveGuardsByAppId(appId);
        
        for (AppGuard guard : activeGuards) {
            List<GuardRule> activeRules = getActiveRulesByGuardId((Long) guard.getId());
            
            for (GuardRule rule : activeRules) {
                if (!accessControlService.evaluateRule(rule, context, requestData)) {
                    return false;
                }
            }
        }
        
        return true;
    }
    
    @Transactional(readOnly = true)
    public List<String> getGuardEvaluationResults(Long appId, String context, Object requestData) {
        List<AppGuard> activeGuards = getActiveGuardsByAppId(appId);
        
        return activeGuards.stream()
            .flatMap(guard -> getActiveRulesByGuardId((Long) guard.getId()).stream())
            .map(rule -> accessControlService.evaluateRuleWithResult(rule, context, requestData))
            .collect(Collectors.toList());
    }
}
