// src/main/java/com/chatbot/connection/service/FacebookConnectionService.java

package com.chatbot.spokes.facebook.connection.service;

import com.chatbot.spokes.facebook.connection.dto.CreateFacebookConnectionRequest;
import com.chatbot.spokes.facebook.connection.dto.FacebookConnectionResponse;
import com.chatbot.spokes.facebook.connection.dto.UpdateFacebookConnectionRequest;
import com.chatbot.core.tenant.infra.TenantContext;
import com.chatbot.spokes.facebook.connection.model.FacebookConnection;
import com.chatbot.spokes.facebook.connection.repository.FacebookConnectionRepository;
import com.chatbot.shared.penny.service.PennyBotManager;
import com.chatbot.shared.penny.model.PennyBot;
import com.chatbot.spokes.facebook.connection.exception.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.HashMap;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class FacebookConnectionService {

    private final FacebookConnectionRepository connectionRepository;
    private final PennyBotManager pennyBotManager;

    public FacebookConnectionService(FacebookConnectionRepository connectionRepository, PennyBotManager pennyBotManager) {
        this.connectionRepository = connectionRepository;
        this.pennyBotManager = pennyBotManager;
    }

    public String createConnection(String ownerId, CreateFacebookConnectionRequest request) {
        
        // 1. AUTO-CREATE BOT nếu chưa có botId
        String botId = request.getBotId();
        if (botId == null || botId.isEmpty()) {
            log.info("🤖 Auto-creating Penny bot for connection");
            PennyBot newBot = pennyBotManager.autoCreateBotForConnection(ownerId, request.getPageId());
            if (newBot == null) {
                throw new RuntimeException("Failed to auto-create Penny bot for connection");
            }
            botId = newBot.getId().toString();
            log.info("✅ Auto-created Penny bot: {}", botId);
        } else {
            // TODO: Validate bot ownership if needed
            log.info("🔍 Using existing bot: {}", botId);
        }

        // 2. KIỂM TRA TÍNH DUY NHẤT CỦA FANPAGE
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new IllegalStateException("Không tìm thấy tenant ID trong context");
        }
        
        if (connectionRepository.findByTenantIdAndPageIdAndIsActiveTrue(tenantId, request.getPageId()).isPresent()) {
            throw new ResourceAlreadyExistsException(
                "Fanpage with ID " + request.getPageId() + " is already connected to an active bot. Please disconnect the existing bot first."
            );
        }

        // 3. TẠO KẾT NỐI MỚI
        FacebookConnection newConnection = new FacebookConnection();
        newConnection.setId(UUID.randomUUID());
        newConnection.setBotId(botId);
        newConnection.setBotName(request.getBotName());
        newConnection.setPageId(request.getPageId());
        newConnection.setFanpageUrl(request.getFanpageUrl());
        newConnection.setPageAccessToken(request.getPageAccessToken());
        newConnection.setOwnerId(ownerId);
        newConnection.setFbUserId("");
        newConnection.setCreatedAt(LocalDateTime.now());
        newConnection.setUpdatedAt(LocalDateTime.now());
        newConnection.setEnabled(request.isEnabled());
        newConnection.setActive(true);

        connectionRepository.save(newConnection);
        
        return newConnection.getId().toString();
    }

    public List<FacebookConnectionResponse> getConnectionsByOwnerId(String ownerId) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new RuntimeException("Không tìm thấy tenant ID trong context");
        }
        List<FacebookConnection> connections = connectionRepository.findByOwnerIdAndIsActiveTrueAndTenantId(ownerId, tenantId);
        return connections.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public Page<FacebookConnectionResponse> getConnectionsByOwnerId(String ownerId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new RuntimeException("Không tìm thấy tenant ID trong context");
        }
        Page<FacebookConnection> connectionsPage = connectionRepository.findByOwnerIdAndIsActiveTrueAndTenantId(ownerId, tenantId, pageable);
        List<FacebookConnectionResponse> dtoList = connectionsPage.getContent().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return new PageImpl<>(dtoList, pageable, connectionsPage.getTotalElements());
    }

    
    public List<FacebookConnectionResponse> getConnectionsByOwnerIdAll(String ownerId) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new RuntimeException("Không tìm thấy tenant ID trong context");
        }
        List<FacebookConnection> connections = connectionRepository.findByOwnerIdAndTenantId(ownerId, tenantId);
        return connections.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public Page<FacebookConnectionResponse> getConnectionsByOwnerIdAll(String ownerId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new RuntimeException("Không tìm thấy tenant ID trong context");
        }
        Page<FacebookConnection> connectionsPage = connectionRepository.findByOwnerIdAndTenantId(ownerId, tenantId, pageable);
        List<FacebookConnectionResponse> dtoList = connectionsPage.getContent().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return new PageImpl<>(dtoList, pageable, connectionsPage.getTotalElements());
    }

    public Page<FacebookConnectionResponse> getConnectionsByBotId(String botId, String ownerId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new RuntimeException("Không tìm thấy tenant ID trong context");
        }
        
        // Get connections by botId and verify ownership
        Page<FacebookConnection> connectionsPage = connectionRepository.findByBotIdAndOwnerIdAndTenantId(botId, ownerId, tenantId, pageable);
        List<FacebookConnectionResponse> dtoList = connectionsPage.getContent().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return new PageImpl<>(dtoList, pageable, connectionsPage.getTotalElements());
    }

    public List<Map<String, Object>> getConnectionsByBotIdList(String botId, String ownerId) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new RuntimeException("Không tìm thấy tenant ID trong context");
        }
        
        // Get connections by botId and verify ownership
        List<FacebookConnection> connections = connectionRepository.findByBotIdAndOwnerIdAndTenantId(botId, ownerId, tenantId);
        return connections.stream()
                .map(this::convertToMap)
                .collect(Collectors.toList());
    }


    private FacebookConnectionResponse convertToDto(FacebookConnection connection) {
        FacebookConnectionResponse dto = new FacebookConnectionResponse();
        dto.setId(connection.getId());
        dto.setBotId(connection.getBotId());
        dto.setBotName(connection.getBotName());
        dto.setPageId(connection.getPageId());
        dto.setPageAccessToken(connection.getPageAccessToken());
        dto.setFanpageUrl(connection.getFanpageUrl());
        dto.setEnabled(connection.isEnabled());
        dto.setActive(connection.isActive());
        dto.setCreatedAt(connection.getCreatedAt());
        dto.setUpdatedAt(connection.getUpdatedAt());
        return dto;
    }

    // Convert to Map format for frontend compatibility
    private Map<String, Object> convertToMap(FacebookConnection connection) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", connection.getId().toString());
        map.put("botId", connection.getBotId());
        map.put("name", connection.getBotName());
        map.put("platform", "facebook");
        map.put("connectionType", "FACEBOOK");
        map.put("pageId", connection.getPageId());
        map.put("fanpageUrl", connection.getFanpageUrl());
        map.put("isActive", connection.isActive());
        map.put("isHealthy", connection.isActive() && connection.isEnabled()); // Mock health status
        map.put("isEnabled", connection.isEnabled());
        map.put("createdAt", connection.getCreatedAt().toString());
        map.put("updatedAt", connection.getUpdatedAt().toString());
        map.put("lastUsedAt", null); // Mock - not tracked in current model
        map.put("description", "Facebook connection for page: " + connection.getPageId());
        return map;
    }

    public void updateConnection(UUID connectionId, String ownerId, UpdateFacebookConnectionRequest request) {
        FacebookConnection connection = connectionRepository.findById(connectionId)
                .orElseThrow(() -> new RuntimeException("Connection not found."));
        if (!connection.getOwnerId().equals(ownerId)) {
            throw new RuntimeException("Access denied.");
        }
        if (request.getBotName() != null) {
            connection.setBotName(request.getBotName());
        }
        if (request.getBotId() != null) {
            connection.setBotId(request.getBotId());
        }
        if (request.getPageAccessToken() != null) {
            connection.setPageAccessToken(request.getPageAccessToken());
        }
        if (request.getFanpageUrl() != null) {
            connection.setFanpageUrl(request.getFanpageUrl());
        }
        if (request.getPageId() != null) {
            connection.setPageId(request.getPageId());
        }
        if (request.getIsEnabled() != null) {
            connection.setEnabled(request.getIsEnabled());
        }
        if (request.getIsActive() != null) {
            connection.setActive(request.getIsActive());
        }
        connection.setUpdatedAt(LocalDateTime.now());
        connectionRepository.save(connection);
    }

    public void deleteConnection(String id) {
        UUID connectionId = UUID.fromString(id);
        connectionRepository.deleteById(connectionId);
    }
}
