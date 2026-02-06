package com.chatbot.modules.messagehub.core.service;

import com.chatbot.modules.messagehub.core.model.ConversationContext;
import com.chatbot.modules.messagehub.core.repository.ConversationContextRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

/**
 * Context Store service for Message Hub MVP
 * Manages conversation context persistence and retrieval
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ContextStore {

    private final ConversationContextRepository contextRepository;

    /**
     * Get or create conversation context
     */
    public ConversationContext getOrCreateContext(String conversationId, UUID userId, UUID tenantId) {
        log.debug("Getting or creating context for conversation: {}", conversationId);
        
        Optional<ConversationContext> existing = contextRepository.findByConversationId(conversationId);
        
        if (existing.isPresent()) {
            log.debug("Found existing context for conversation: {}", conversationId);
            return existing.get();
        }
        
        log.debug("Creating new context for conversation: {}", conversationId);
        ConversationContext newContext = new ConversationContext();
        newContext.setConversationId(conversationId);
        newContext.setUserId(userId);
        newContext.setTenantId(tenantId);
        newContext.setHandlerType(ConversationContext.HandlerType.BOT);
        newContext.setAskedPriceCount(0);
        
        return contextRepository.save(newContext);
    }

    /**
     * Get or create context by conversation ID (simple version)
     */
    public ConversationContext getOrCreate(String conversationId) {
        log.debug("Getting or creating context for conversation: {}", conversationId);
        
        Optional<ConversationContext> existing = contextRepository.findByConversationId(conversationId);
        
        if (existing.isPresent()) {
            log.debug("Found existing context for conversation: {}", conversationId);
            return existing.get();
        }
        
        log.debug("Creating new context for conversation: {}", conversationId);
        ConversationContext newContext = new ConversationContext();
        newContext.setConversationId(conversationId);
        // Use default UUID values for simple version
        newContext.setUserId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
        newContext.setTenantId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
        newContext.setHandlerType(ConversationContext.HandlerType.BOT);
        newContext.setAskedPriceCount(0);
        
        return contextRepository.save(newContext);
    }

    /**
     * Update conversation context
     */
    public ConversationContext updateContext(ConversationContext context) {
        log.debug("Updating context for conversation: {}", context.getConversationId());
        return contextRepository.save(context);
    }

    /**
     * Get context by conversation ID
     */
    public Optional<ConversationContext> getContext(String conversationId) {
        return contextRepository.findByConversationId(conversationId);
    }
}
