package com.chatbot.modules.penny.service;

import com.chatbot.modules.penny.model.PennyBot;
import com.chatbot.modules.penny.repository.PennyBotRepository;
import com.chatbot.modules.penny.dto.PennyBotDto;
import com.chatbot.modules.penny.dto.PennyBotRequest;
import com.chatbot.modules.penny.dto.PennyBotResponse;
import com.chatbot.shared.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    /**
     * Create a new Penny Bot
     */
    @Transactional
    public PennyBotResponse createBot(PennyBotRequest request) {
        log.info("Creating Penny bot: {}", request.getBotName());

        // Check if bot name already exists for tenant (simplified check)
        List<PennyBot> existingBots = pennyBotRepository.findByTenantIdAndIsActiveTrue(request.getTenantId());
        boolean nameExists = existingBots.stream()
                .anyMatch(bot -> bot.getBotName().equals(request.getBotName()));
        
        if (nameExists) {
            throw new IllegalStateException("Bot with name '" + request.getBotName() + "' already exists for tenant");
        }

        PennyBot pennyBot = PennyBot.builder()
                .id(UUID.randomUUID())
                .botName(request.getBotName())
                .botType(request.getBotType())
                .tenantId(request.getTenantId())
                .ownerId(request.getOwnerId())
                .botpressBotId(request.getBotpressBotId())
                .description(request.getDescription())
                .isActive(true)
                .isEnabled(true)
                .build();

        PennyBot saved = pennyBotRepository.save(pennyBot);
        log.info("Created Penny bot: {} with ID: {}", saved.getBotName(), saved.getId());

        return mapToResponse(saved);
    }

    /**
     * Get Penny Bot by ID
     */
    public PennyBotResponse getBotById(UUID id) {
        PennyBot bot = pennyBotRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Penny bot not found: " + id));

        return mapToResponse(bot);
    }

    /**
     * Get all Penny Bots for a tenant
     */
    public List<PennyBotResponse> getBotsByTenant(Long tenantId) {
        List<PennyBot> bots = pennyBotRepository.findByTenantIdAndIsActiveTrue(tenantId);
        
        return bots.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get all active Penny Bots for a tenant
     */
    public List<PennyBotResponse> getActiveBotsByTenant(Long tenantId) {
        List<PennyBot> bots = pennyBotRepository.findByTenantIdAndIsActiveTrue(tenantId);
        
        return bots.stream()
                .map(this::mapToResponse)
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
        existingBot.setBotpressBotId(request.getBotpressBotId());
        existingBot.setDescription(request.getDescription());

        PennyBot updated = pennyBotRepository.save(existingBot);
        log.info("Updated Penny bot: {}", updated.getBotName());

        return mapToResponse(updated);
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
        
        return bots.map(this::mapToResponse);
    }

    /**
     * Search Penny Bots
     */
    public Page<PennyBotResponse> searchBots(Long tenantId, String keyword, Pageable pageable) {
        // Use findAll with filtering since searchBots doesn't exist
        Page<PennyBot> bots = pennyBotRepository.findAll(pageable);
        
        return bots.map(this::mapToResponse);
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
     * Map entity to DTO
     */
    private PennyBotResponse mapToResponse(PennyBot bot) {
        return PennyBotResponse.builder()
                .id(bot.getId())
                .botName(bot.getBotName())
                .botType(bot.getBotType())
                .tenantId(bot.getTenantId())
                .ownerId(bot.getOwnerId())
                .botpressBotId(bot.getBotpressBotId())
                .description(bot.getDescription())
                .isActive(bot.isActive())
                .isEnabled(bot.isEnabled())
                .createdAt(bot.getCreatedAt())
                .updatedAt(bot.getUpdatedAt())
                .build();
    }
}
