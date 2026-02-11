// src/main/java/com/chatbot/connection/service/FacebookConnectionService.java

package com.chatbot.modules.facebook.connection.service;

import com.chatbot.modules.facebook.connection.dto.CreateFacebookConnectionRequest;
import com.chatbot.modules.facebook.connection.dto.FacebookConnectionResponse;
import com.chatbot.modules.facebook.connection.dto.UpdateFacebookConnectionRequest;
import com.chatbot.modules.tenant.infra.TenantContext;
import com.chatbot.modules.facebook.connection.model.FacebookConnection;
import com.chatbot.modules.facebook.connection.repository.FacebookConnectionRepository;
import com.chatbot.modules.penny.service.PennyBotManager;
import com.chatbot.modules.penny.model.PennyBot;
import com.chatbot.modules.facebook.connection.exception.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
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
