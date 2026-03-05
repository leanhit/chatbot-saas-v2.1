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

import com.chatbot.core.user.model.User;
import com.chatbot.core.user.repository.UserRepository;
import com.chatbot.core.tenant.membership.model.TenantMember;
import com.chatbot.core.tenant.membership.model.TenantRole;
import com.chatbot.core.tenant.membership.model.MembershipStatus;
import com.chatbot.core.tenant.membership.repository.TenantMemberRepository;

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
    private final UserRepository userRepository;
    private final TenantMemberRepository tenantMemberRepository;

    public FacebookConnectionService(FacebookConnectionRepository connectionRepository, 
                                PennyBotManager pennyBotManager,
                                UserRepository userRepository,
                                TenantMemberRepository tenantMemberRepository) {
        this.connectionRepository = connectionRepository;
        this.pennyBotManager = pennyBotManager;
        this.userRepository = userRepository;
        this.tenantMemberRepository = tenantMemberRepository;
    }

    public String createConnection(String ownerId, CreateFacebookConnectionRequest request) {
        // Check create permissions
        if (!canCreateConnections(ownerId, request.getBotId())) {
            throw new RuntimeException("Insufficient privileges to create connections.");
        }
        
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
        // Check view permissions
        if (!canViewConnections(ownerId)) {
            throw new RuntimeException("Insufficient privileges to view connections.");
        }
        
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
        
        // Check update permissions
        if (!canUpdateConnection(ownerId, connection)) {
            throw new RuntimeException("Insufficient privileges to update this connection.");
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

    public void deleteConnection(String id, String ownerId) {
        // Check admin permissions (System ADMIN or Tenant ADMIN/OWNER)
        if (!hasAdminPrivileges(ownerId)) {
            throw new RuntimeException("Admin privileges required to delete connections.");
        }
        
        UUID connectionId = UUID.fromString(id);
        
        // Verify ownership before deletion
        FacebookConnection connection = connectionRepository.findById(connectionId)
                .orElseThrow(() -> new RuntimeException("Connection not found."));
        
        if (!connection.getOwnerId().equals(ownerId)) {
            throw new RuntimeException("Access denied: You can only delete your own connections.");
        }
        
        log.info("🗑️ Admin user {} deleting Facebook connection: {}", ownerId, connectionId);
        connectionRepository.deleteById(connectionId);
    }
    
    private boolean hasAdminPrivileges(String userId) {
        try {
            // Check system role first
            User user = userRepository.findByEmail(userId)
                    .orElse(null);
            
            if (user != null && user.getSystemRole() == com.chatbot.core.identity.model.SystemRole.ADMIN) {
                return true;
            }
            
            if (user == null) return false;
            
            // Check tenant role (current tenant context)
            Long tenantId = TenantContext.getTenantId();
            if (tenantId != null) {
                TenantMember tenantMember = tenantMemberRepository
                        .findByTenant_IdAndUser_Id(tenantId, user.getId())
                        .orElse(null);
                
                return tenantMember != null && 
                       tenantMember.getStatus() == MembershipStatus.ACTIVE &&
                       (tenantMember.getRole() == TenantRole.OWNER || 
                        tenantMember.getRole() == TenantRole.ADMIN);
            }
            
            return false;
        } catch (Exception e) {
            log.error("Error checking admin privileges for user: {}", userId, e);
            return false;
        }
    }
    
    private boolean canCreateConnections(String userId, String botId) {
        try {
            User user = userRepository.findByEmail(userId).orElse(null);
            if (user == null) return false;
            
            // System ADMIN can create anywhere
            if (user.getSystemRole() == com.chatbot.core.identity.model.SystemRole.ADMIN) {
                return true;
            }
            
            Long tenantId = TenantContext.getTenantId();
            if (tenantId == null) return false;
            
            TenantMember member = tenantMemberRepository
                    .findByTenant_IdAndUser_Id(tenantId, user.getId())
                    .orElse(null);
            
            if (member == null || member.getStatus() != MembershipStatus.ACTIVE) {
                return false;
            }
            
            // OWNER/ADMIN: Can create connections for any bot
            if (member.getRole() == TenantRole.OWNER || member.getRole() == TenantRole.ADMIN) {
                return true;
            }
            
            // EDITOR: Can create connections for their own bots
            if (member.getRole() == TenantRole.EDITOR) {
                // TODO: Check if user owns this bot
                return true; // Simplified for now
            }
            
            // MEMBER: Cannot create
            return false;
            
        } catch (Exception e) {
            log.error("Error checking create permissions for user: {}", userId, e);
            return false;
        }
    }
    
    private boolean canUpdateConnection(String userId, FacebookConnection connection) {
        try {
            User user = userRepository.findByEmail(userId).orElse(null);
            if (user == null) return false;
            
            // System ADMIN can update anything
            if (user.getSystemRole() == com.chatbot.core.identity.model.SystemRole.ADMIN) {
                return true;
            }
            
            // Owner can always update their own connections
            if (connection.getOwnerId().equals(userId)) {
                return true;
            }
            
            Long tenantId = TenantContext.getTenantId();
            if (tenantId == null) return false;
            
            TenantMember member = tenantMemberRepository
                    .findByTenant_IdAndUser_Id(tenantId, user.getId())
                    .orElse(null);
            
            if (member == null || member.getStatus() != MembershipStatus.ACTIVE) {
                return false;
            }
            
            // OWNER/ADMIN: Can update any connection in tenant
            return member.getRole() == TenantRole.OWNER || member.getRole() == TenantRole.ADMIN;
            
        } catch (Exception e) {
            log.error("Error checking update permissions for user: {}", userId, e);
            return false;
        }
    }
    
    private boolean canViewConnections(String userId) {
        try {
            User user = userRepository.findByEmail(userId).orElse(null);
            if (user == null) return false;
            
            // System ADMIN can view anything
            if (user.getSystemRole() == com.chatbot.core.identity.model.SystemRole.ADMIN) {
                return true;
            }
            
            Long tenantId = TenantContext.getTenantId();
            if (tenantId == null) return false;
            
            TenantMember member = tenantMemberRepository
                    .findByTenant_IdAndUser_Id(tenantId, user.getId())
                    .orElse(null);
            
            return member != null && member.getStatus() == MembershipStatus.ACTIVE;
            
        } catch (Exception e) {
            log.error("Error checking view permissions for user: {}", userId, e);
            return false;
        }
    }
}
