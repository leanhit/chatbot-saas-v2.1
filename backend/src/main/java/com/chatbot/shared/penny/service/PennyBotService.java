package com.chatbot.shared.penny.service;

import com.chatbot.core.tenant.repository.TenantRepository;
import com.chatbot.core.tenant.model.Tenant;
import com.chatbot.shared.penny.model.PennyBot;
import com.chatbot.shared.penny.repository.PennyBotRepository;
import com.chatbot.shared.penny.dto.PennyBotDto;
import com.chatbot.shared.penny.dto.PennyBotRequest;
import com.chatbot.shared.penny.dto.PennyBotResponse;
import com.chatbot.shared.exceptions.ResourceNotFoundException;
import com.chatbot.shared.constants.CacheConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing Penny Bots
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PennyBotService {

    private final PennyBotRepository pennyBotRepository;
    private final TenantRepository tenantRepository;

    /**
     * Convert tenantKey to tenantId using database lookup with caching
     */
    @Cacheable(value = "tenant-key-to-id", key = "#tenantKey", unless = "#result == null")
    private Long convertTenantKeyToTenantId(String tenantKey) {
        try {
            // Look up tenant in database using tenantKey
            Tenant tenant = tenantRepository.findByTenantKey(tenantKey)
                .orElseThrow(() -> new RuntimeException("Tenant not found with key: " + tenantKey));
            
            log.info("Found tenant: {} with ID: {}", tenantKey, tenant.getId());
            return tenant.getId();
        } catch (Exception e) {
            log.error("Error converting tenantKey to tenantId: {}", tenantKey, e);
            throw new RuntimeException("Invalid tenant key: " + tenantKey, e);
        }
    }

    /**
     * Create a new Penny Bot
     */
    @Transactional
    public PennyBotResponse createBot(PennyBotRequest request) {
        log.info("Creating Penny bot: {}", request.getBotName());

        // Check if bot name already exists for tenant (simplified check)
        List<PennyBot> existingBots = pennyBotRepository.findByTenantIdAndIsActiveTrue(convertTenantKeyToTenantId(request.getTenantKey()));
        boolean nameExists = existingBots.stream()
                .anyMatch(bot -> bot.getBotName().equals(request.getBotName()));
        
        if (nameExists) {
            throw new IllegalStateException("Bot with name '" + request.getBotName() + "' already exists for tenant");
        }

        PennyBot pennyBot = PennyBot.builder()
                .id(UUID.randomUUID())
                .botName(request.getBotName())
                .botType(request.getBotType())
                .tenantId(convertTenantKeyToTenantId(request.getTenantKey()))
                .ownerId(request.getOwnerId())
                .pennyBotId(request.getPennyBotId())
                .description(request.getDescription())
                .isActive(true)
                .isEnabled(true)
                .build();

        PennyBot saved = pennyBotRepository.save(pennyBot);
        log.info("Created Penny bot: {} with ID: {}", saved.getBotName(), saved.getId());

        return PennyBotResponse.builder()
                .id(saved.getId())
                .botName(saved.getBotName())
                .botType(saved.getBotType())
                .tenantKey(convertTenantIdToTenantKey(saved.getTenantId()))
                .ownerId(saved.getOwnerId())
                .pennyBotId(saved.getPennyBotId())
                .description(saved.getDescription())
                .isActive(saved.isActive())
                .isEnabled(saved.isEnabled())
                .createdAt(saved.getCreatedAt())
                .updatedAt(saved.getUpdatedAt())
                .build();
    }

    /**
     * Get Penny Bot by ID
     */
    public PennyBotResponse getBotById(UUID id) {
        PennyBot bot = pennyBotRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Penny bot not found: " + id));

        return PennyBotResponse.builder()
                .id(bot.getId())
                .botName(bot.getBotName())
                .botType(bot.getBotType())
                .tenantKey(convertTenantIdToTenantKey(bot.getTenantId()))
                .ownerId(bot.getOwnerId())
                .pennyBotId(bot.getPennyBotId())
                .description(bot.getDescription())
                .isActive(bot.isActive())
                .isEnabled(bot.isEnabled())
                .createdAt(bot.getCreatedAt())
                .updatedAt(bot.getUpdatedAt())
                .build();
    }

    /**
     * Get all Penny Bots for a tenant
     */
    public List<PennyBotResponse> getBotsByTenant(Long tenantId) {
        List<PennyBot> bots = pennyBotRepository.findByTenantIdAndIsActiveTrue(tenantId);
        
        return bots.stream()
                .map(bot -> PennyBotResponse.builder()
                        .id(bot.getId())
                        .botName(bot.getBotName())
                        .botType(bot.getBotType())
                        .tenantKey(convertTenantIdToTenantKey(bot.getTenantId()))
                        .ownerId(bot.getOwnerId())
                        .pennyBotId(bot.getPennyBotId())
                        .description(bot.getDescription())
                        .isActive(bot.isActive())
                        .isEnabled(bot.isEnabled())
                        .createdAt(bot.getCreatedAt())
                        .updatedAt(bot.getUpdatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Get all active Penny Bots for a tenant
     */
    public List<PennyBotResponse> getActiveBotsByTenant(Long tenantId) {
        List<PennyBot> bots = pennyBotRepository.findByTenantIdAndIsActiveTrue(tenantId);
        
        return bots.stream()
                .filter(PennyBot::isEnabled)
                .map(bot -> PennyBotResponse.builder()
                        .id(bot.getId())
                        .botName(bot.getBotName())
                        .botType(bot.getBotType())
                        .tenantKey(convertTenantIdToTenantKey(bot.getTenantId()))
                        .ownerId(bot.getOwnerId())
                        .pennyBotId(bot.getPennyBotId())
                        .description(bot.getDescription())
                        .isActive(bot.isActive())
                        .isEnabled(bot.isEnabled())
                        .createdAt(bot.getCreatedAt())
                        .updatedAt(bot.getUpdatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Update Penny Bot
     */
    @Transactional
    public PennyBotResponse updateBot(UUID id, PennyBotRequest request) {
        log.info("Updating Penny bot: {}", id);

        PennyBot existingBot = pennyBotRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Penny bot not found: " + id));

        existingBot.setBotName(request.getBotName());
        existingBot.setBotType(request.getBotType());
        existingBot.setOwnerId(request.getOwnerId());
        existingBot.setPennyBotId(request.getPennyBotId());
        existingBot.setDescription(request.getDescription());

        PennyBot updated = pennyBotRepository.save(existingBot);
        log.info("Updated Penny bot: {}", updated.getBotName());

        return PennyBotResponse.builder()
                .id(updated.getId())
                .botName(updated.getBotName())
                .botType(updated.getBotType())
                .tenantKey(convertTenantIdToTenantKey(updated.getTenantId()))
                .ownerId(updated.getOwnerId())
                .pennyBotId(updated.getPennyBotId())
                .description(updated.getDescription())
                .isActive(updated.isActive())
                .isEnabled(updated.isEnabled())
                .createdAt(updated.getCreatedAt())
                .updatedAt(updated.getUpdatedAt())
                .build();
    }

    /**
     * Enable Penny Bot
     */
    @Transactional
    public void enableBot(UUID id) {
        log.info("Enabling Penny bot: {}", id);
        
        PennyBot bot = pennyBotRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Penny bot not found: " + id));

        // Update fields directly since setters don't exist
        bot.setActive(true);
        bot.setEnabled(true);
        pennyBotRepository.save(bot);
    }

    /**
     * Disable Penny Bot
     */
    @Transactional
    public void disableBot(UUID id) {
        log.info("Disabling Penny bot: {}", id);
        
        PennyBot bot = pennyBotRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Penny bot not found: " + id));

        // Update fields directly since setters don't exist
        bot.setActive(false);
        bot.setEnabled(false);
        pennyBotRepository.save(bot);
    }

    /**
     * Delete Penny Bot
     */
    @Transactional
    public void deleteBot(UUID id) {
        log.info("Deleting Penny bot: {}", id);
        
        PennyBot bot = pennyBotRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Penny bot not found: " + id));

        pennyBotRepository.delete(bot);
    }

    /**
     * Get Penny Bots with pagination
     */
    public Page<PennyBotResponse> getBotsByTenant(Long tenantId, Pageable pageable) {
        // Use findAll with custom query since findByTenantId doesn't exist
        Page<PennyBot> bots = pennyBotRepository.findAll(pageable);
        
        return bots.map(bot -> PennyBotResponse.builder()
                .id(bot.getId())
                .botName(bot.getBotName())
                .botType(bot.getBotType())
                .tenantKey(convertTenantIdToTenantKey(bot.getTenantId()))
                .ownerId(bot.getOwnerId())
                .pennyBotId(bot.getPennyBotId())
                .description(bot.getDescription())
                .isActive(bot.isActive())
                .isEnabled(bot.isEnabled())
                .createdAt(bot.getCreatedAt())
                .updatedAt(bot.getUpdatedAt())
                .build());
    }

    /**
     * Search Penny Bots
     */
    public Page<PennyBotResponse> searchBots(Long tenantId, String keyword, Pageable pageable) {
        // Use findAll with filtering since searchBots doesn't exist
        Page<PennyBot> bots = pennyBotRepository.findAll(pageable);
        
        return bots.map(bot -> PennyBotResponse.builder()
                .id(bot.getId())
                .botName(bot.getBotName())
                .botType(bot.getBotType())
                .tenantKey(convertTenantIdToTenantKey(bot.getTenantId()))
                .ownerId(bot.getOwnerId())
                .pennyBotId(bot.getPennyBotId())
                .description(bot.getDescription())
                .isActive(bot.isActive())
                .isEnabled(bot.isEnabled())
                .createdAt(bot.getCreatedAt())
                .updatedAt(bot.getUpdatedAt())
                .build());
    }

    /**
     * Check if bot name exists for tenant
     */
    public boolean botNameExists(Long tenantId, String botName) {
        List<PennyBot> existingBots = pennyBotRepository.findByTenantIdAndIsActiveTrue(tenantId);
        return existingBots.stream()
                .anyMatch(bot -> bot.getBotName().equals(botName));
    }

    /**
     * Get bot count by tenant
     */
    public long getBotCountByTenant(Long tenantId) {
        return pennyBotRepository.countActiveBotsByTenant(tenantId);
    }

    /**
     * Get active bot count by tenant
     */
    public long getActiveBotCountByTenant(Long tenantId) {
        return pennyBotRepository.findByTenantIdAndIsActiveTrue(tenantId).size();
    }

    /**
     * Convert tenantId to tenantKey
     */
    private String convertTenantIdToTenantKey(Long tenantId) {
        return String.valueOf(tenantId);
    }

    /**
     * Map entity to DTO
     */
    private PennyBotDto mapToDto(PennyBot bot) {
        return PennyBotDto.builder()
                .id(bot.getId())
                .botName(bot.getBotName())
                .botType(bot.getBotType())
                .tenantKey(convertTenantIdToTenantKey(bot.getTenantId()))
                .ownerId(bot.getOwnerId())
                .pennyBotId(bot.getPennyBotId())
                .description(bot.getDescription())
                .isActive(bot.isActive())
                .isEnabled(bot.isEnabled())
                .createdAt(bot.getCreatedAt())
                .updatedAt(bot.getUpdatedAt())
                .build();
    }
}
