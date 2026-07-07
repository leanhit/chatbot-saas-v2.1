package com.chatbot.core.message.store.service;

import com.chatbot.core.message.store.model.Conversation;
import com.chatbot.core.message.store.dto.ConversationStatisticsDTO;
import com.chatbot.core.message.store.repository.ConversationRepository;
import com.chatbot.core.message.store.repository.MessageRepository;
import com.chatbot.spokes.facebook.connection.repository.FacebookConnectionRepository;
import com.chatbot.core.tenant.infra.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ConversationStatisticsServiceTest {

    @Mock
    private ConversationRepository conversationRepo;
    
    @Mock
    private MessageRepository messageRepo;
    
    @Mock
    private FacebookConnectionRepository facebookConnectionRepo;

    @InjectMocks
    private ConversationStatisticsService statisticsService;

    @BeforeEach
    void setUp() {
        TenantContext.setTenantId(1L);
    }
    
    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void testGetConversationStatistics() {
        String ownerId = "owner-1";
        Long tenantId = 1L;

        when(conversationRepo.countByTenantId(tenantId)).thenReturn(100L);
        when(conversationRepo.countByTenantIdAndCreatedAtAfter(eq(tenantId), any(LocalDateTime.class))).thenReturn(20L);
        when(conversationRepo.countDistinctActiveUsers(eq(tenantId), any(LocalDateTime.class))).thenReturn(15L);
        when(conversationRepo.countByTenantIdAndIsTakenOverByAgent(tenantId, true)).thenReturn(5L);
        when(messageRepo.countByConversationTenantId(tenantId)).thenReturn(1000L);
        when(messageRepo.countByConversationTenantIdAndCreatedAtAfter(eq(tenantId), any(LocalDateTime.class))).thenReturn(50L);
        when(messageRepo.countBySenderAndTenantId("bot", tenantId)).thenReturn(800L);
        when(facebookConnectionRepo.countByTenantIdAndIsActiveTrue(tenantId)).thenReturn(3L);

        ConversationStatisticsDTO stats = statisticsService.getConversationStatistics(ownerId);

        assertEquals(100L, stats.getTotalConversations());
        assertEquals(15L, stats.getActiveUsers());
        assertEquals(50L, stats.getPendingMessages());
        assertEquals(5L, stats.getActiveTakeovers());
        assertEquals(1000L, stats.getTotalMessages());
        assertEquals(800L, stats.getBotResponses());
        assertEquals(3L, stats.getActiveConnections());
        assertEquals(20.0, stats.getGrowthRate(), 0.01);
        assertEquals(80.0, stats.getResponseRate(), 0.01);
    }
}
