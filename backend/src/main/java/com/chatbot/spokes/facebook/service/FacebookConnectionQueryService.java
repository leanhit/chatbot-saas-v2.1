package com.chatbot.spokes.facebook.service;

import com.chatbot.spokes.facebook.dto.FacebookConnectionDTO;
import com.chatbot.spokes.facebook.connection.model.FacebookConnection;
import com.chatbot.spokes.facebook.connection.repository.FacebookConnectionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for querying Facebook connections using DTOs
 * This provides a clean interface for other spokes to query Facebook connection data
 * without directly accessing the repository or entity
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FacebookConnectionQueryService {
    
    private final FacebookConnectionRepository facebookConnectionRepository;
    
    /**
     * Get Facebook connection by ID as DTO
     */
    public FacebookConnectionDTO getConnectionById(UUID connectionId) {
        FacebookConnection connection = facebookConnectionRepository.findById(connectionId)
                .orElse(null);
        return FacebookConnectionDTO.fromEntity(connection);
    }
    
    /**
     * Get all active connections for a bot as DTOs
     */
    public List<FacebookConnectionDTO> getActiveConnectionsByBotId(String botId) {
        List<FacebookConnection> connections = facebookConnectionRepository
                .findAllByBotIdAndIsActiveTrue(botId);
        return connections.stream()
                .map(FacebookConnectionDTO::fromEntity)
                .collect(Collectors.toList());
    }
    
    /**
     * Get connection by page ID as DTO
     */
    public FacebookConnectionDTO getConnectionByPageId(String pageId) {
        FacebookConnection connection = facebookConnectionRepository
                .findByPageIdAndIsActiveTrue(pageId)
                .orElse(null);
        return FacebookConnectionDTO.fromEntity(connection);
    }
    
    /**
     * Get all connections for a tenant as DTOs
     */
    public List<FacebookConnectionDTO> getConnectionsByTenantId(Long tenantId) {
        List<FacebookConnection> connections = facebookConnectionRepository
                .findByTenantIdAndIsActiveTrue(tenantId);
        return connections.stream()
                .map(FacebookConnectionDTO::fromEntity)
                .collect(Collectors.toList());
    }
}
