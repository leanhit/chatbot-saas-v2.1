package com.chatbot.core.tenant.service;

import com.chatbot.core.simplepayment.model.Package;
import com.chatbot.core.simplepayment.service.PackageService;
import com.chatbot.shared.penny.repository.PennyBotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PackageLimitValidationService {
    
    private final TenantPackageService tenantPackageService;
    private final PackageService packageService;
    private final PennyBotRepository pennyBotRepository;
    
    /**
     * Check if tenant can create more chatbots based on their Simple Payment package
     */
    @Transactional(readOnly = true)
    public boolean canCreateMoreChatbots(Long tenantId) {
        try {
            log.info("🔍 [STEP 1] Starting chatbot limit validation for tenant: {}", tenantId);
            
            // Get current package for tenant from Simple Payment System
            log.info("📦 [STEP 2] Getting current package for tenant: {}", tenantId);
            Package currentPackage = tenantPackageService.getCurrentTenantPackage(tenantId);
            if (currentPackage == null) {
                log.warn("❌ [STEP 2] Tenant {} has no package assigned", tenantId);
                return false;
            }
            
            log.info("✅ [STEP 2] Found package: {} (ID: {}) with chatbot limit: {}", 
                    currentPackage.getName(), currentPackage.getPackageId(), currentPackage.getChatbotLimit());
            
            // Get current chatbot count for this tenant (CORRECT: count actual chatbots)
            log.info("🤖 [STEP 3] Counting current active chatbots for tenant: {}", tenantId);
            Long currentChatbotCount = pennyBotRepository.countByTenantIdAndIsActiveTrue(tenantId);
            
            log.info("📊 [STEP 3] Current chatbot count: {}, Package limit: {} for tenant: {}", 
                    currentChatbotCount, currentPackage.getChatbotLimit(), tenantId);
            
            // Check if unlimited
            if (currentPackage.getChatbotLimit() >= Integer.MAX_VALUE) {
                log.info("♾️ [STEP 4] Tenant {} has unlimited chatbots package - ALLOWING creation", tenantId);
                return true;
            }
            
            // Check if can create more
            boolean canCreate = currentChatbotCount < currentPackage.getChatbotLimit();
            int remaining = currentPackage.getChatbotLimit() - currentChatbotCount.intValue();
            
            if (canCreate) {
                log.info("✅ [STEP 4] Tenant {} CAN create more chatbots. Current: {}, Limit: {}, Remaining: {}", 
                        tenantId, currentChatbotCount, currentPackage.getChatbotLimit(), remaining);
            } else {
                log.warn("❌ [STEP 4] Tenant {} CANNOT create more chatbots. Current: {}, Limit: {}, Remaining: {}", 
                        tenantId, currentChatbotCount, currentPackage.getChatbotLimit(), remaining);
            }
            
            return canCreate;
            
        } catch (Exception e) {
            log.error("💥 [ERROR] Error checking chatbot limit for tenant {}: {}", tenantId, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Get remaining chatbot slots for tenant
     */
    @Transactional(readOnly = true)
    public int getRemainingChatbotSlots(Long tenantId) {
        try {
            Package currentPackage = tenantPackageService.getCurrentTenantPackage(tenantId);
            if (currentPackage == null) {
                return 0;
            }
            
            // If unlimited, return large number
            if (currentPackage.getChatbotLimit() >= Integer.MAX_VALUE) {
                return Integer.MAX_VALUE;
            }
            
            // Get current chatbot count (CORRECT: count actual chatbots)
            Long currentChatbotCount = pennyBotRepository.countByTenantIdAndIsActiveTrue(tenantId);
            int remaining = currentPackage.getChatbotLimit() - currentChatbotCount.intValue();
            
            return Math.max(0, remaining);
            
        } catch (Exception e) {
            log.error("Error getting remaining chatbot slots for tenant {}", tenantId, e);
            return 0;
        }
    }
    
    /**
     * Validate chatbot creation and throw exception if limit exceeded
     */
    @Transactional(readOnly = true)
    public void validateChatbotCreation(Long tenantId) {
        log.info("🚀 [VALIDATION START] Starting chatbot creation validation for tenant: {}", tenantId);
        
        if (!canCreateMoreChatbots(tenantId)) {
            log.warn("⛔ [VALIDATION FAILED] Tenant {} cannot create more chatbots - preparing error message", tenantId);
            
            Package currentPackage = tenantPackageService.getCurrentTenantPackage(tenantId);
            
            // Get current chatbot count (CORRECT: count actual chatbots)
            log.info("🔢 [FINAL COUNT] Getting final chatbot count for tenant: {}", tenantId);
            Long currentChatbotCount = pennyBotRepository.countByTenantIdAndIsActiveTrue(tenantId);
            
            String message;
            if (currentPackage.getChatbotLimit() >= Integer.MAX_VALUE) {
                message = "Your package allows unlimited chatbots. You should be able to create more.";
                log.error("🤔 [UNEXPECTED] Tenant {} has unlimited package but validation failed", tenantId);
            } else {
                int remaining = currentPackage.getChatbotLimit() - currentChatbotCount.intValue();
                message = String.format(
                    "❌ Chatbot limit exceeded! Your %s package allows %d chatbots. You currently have %d chatbots. Remaining: %d", 
                    currentPackage.getName(), 
                    currentPackage.getChatbotLimit(), 
                    currentChatbotCount,
                    Math.max(0, remaining)
                );
                log.error("🚫 [LIMIT EXCEEDED] Tenant {} - Package: {}, Limit: {}, Current: {}, Remaining: {}", 
                        tenantId, currentPackage.getName(), currentPackage.getChatbotLimit(), 
                        currentChatbotCount, Math.max(0, remaining));
            }
            
            log.error("💥 [VALIDATION ERROR] Throwing exception for tenant {}: {}", tenantId, message);
            throw new RuntimeException(message);
        } else {
            log.info("✅ [VALIDATION PASSED] Tenant {} can create more chatbots", tenantId);
        }
    }
    
    /**
     * Get chatbot limit information for tenant
     */
    @Transactional(readOnly = true)
    public ChatbotLimitInfo getChatbotLimitInfo(Long tenantId) {
        try {
            Package currentPackage = tenantPackageService.getCurrentTenantPackage(tenantId);
            if (currentPackage == null) {
                return ChatbotLimitInfo.builder()
                        .tenantId(tenantId)
                        .packageName("No Package")
                        .packageId("none")
                        .totalLimit(0)
                        .currentCount(0)
                        .remainingSlots(0)
                        .canCreateMore(false)
                        .isUnlimited(false)
                        .build();
            }
            
            // Get current chatbot count (CORRECT: count actual chatbots)
            Long currentChatbotCount = pennyBotRepository.countByTenantIdAndIsActiveTrue(tenantId);
            boolean isUnlimited = currentPackage.getChatbotLimit() >= Integer.MAX_VALUE;
            int remaining;
            
            if (isUnlimited) {
                remaining = Integer.MAX_VALUE;
            } else {
                remaining = currentPackage.getChatbotLimit() - currentChatbotCount.intValue();
            }
            
            return ChatbotLimitInfo.builder()
                    .tenantId(tenantId)
                    .packageName(currentPackage.getName())
                    .packageId(currentPackage.getPackageId())
                    .totalLimit(currentPackage.getChatbotLimit())
                    .currentCount(currentChatbotCount.intValue())
                    .remainingSlots(Math.max(0, remaining))
                    .canCreateMore(isUnlimited || remaining > 0)
                    .isUnlimited(isUnlimited)
                    .build();
                    
        } catch (Exception e) {
            log.error("Error getting chatbot limit info for tenant {}", tenantId, e);
            return ChatbotLimitInfo.builder()
                    .tenantId(tenantId)
                    .packageName("Error")
                    .packageId("error")
                    .totalLimit(0)
                    .currentCount(0)
                    .remainingSlots(0)
                    .canCreateMore(false)
                    .isUnlimited(false)
                    .build();
        }
    }
    
    /**
     * DTO for chatbot limit information
     */
    @lombok.Data
    @lombok.Builder
    public static class ChatbotLimitInfo {
        private Long tenantId;
        private String packageName;
        private String packageId;
        private Integer totalLimit;
        private Integer currentCount;
        private Integer remainingSlots;
        private Boolean canCreateMore;
        private Boolean isUnlimited;
    }
}
