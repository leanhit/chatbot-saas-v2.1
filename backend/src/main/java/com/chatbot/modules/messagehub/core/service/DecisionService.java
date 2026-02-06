package com.chatbot.modules.messagehub.core.service;

import com.chatbot.modules.messagehub.core.dto.MessageRequest;
import com.chatbot.modules.messagehub.core.dto.MessageResponse;
import com.chatbot.modules.messagehub.core.model.ConversationContext;
import com.chatbot.modules.penny.service.PennyMessageProcessor;
import com.chatbot.modules.test.service.SimpleMessageTestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Decision Service for Message Hub - Tích hợp Penny Bot
 * Chuyển message đến Penny Bot để xử lý thông minh
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DecisionService {
    
    private final PennyMessageProcessor pennyMessageProcessor;
    private final SimpleMessageTestService simpleMessageTestService;

    /**
     * Make decision based on context and message intent
     * Tích hợp Penny Bot để xử lý thông minh
     * 
     * @param context Current conversation context
     * @param intent Message intent
     * @return Decision result
     */
    public DecisionResult makeDecision(ConversationContext context, String intent) {
        return makeDecision(context, intent, null);
    }
    
    /**
     * Make decision với message request đầy đủ
     */
    public DecisionResult makeDecision(ConversationContext context, String intent, MessageRequest messageRequest) {
        log.debug("Making decision for conversation: {}, intent: {}, handler: {}", 
                context.getConversationId(), intent, context.getHandlerType());

        // Rule 1: If handler_type = human → don't let bot process
        if (ConversationContext.HandlerType.HUMAN.equals(context.getHandlerType())) {
            log.info("Decision: HUMAN_REQUIRED - conversation already assigned to human");
            return DecisionResult.humanRequired("Conversation already assigned to human agent");
        }

        // Rule 2: Nếu có Penny Bot active → ưu tiên xử lý bằng Penny
        if (messageRequest != null) {
            try {
                MessageResponse pennyResponse = pennyMessageProcessor.processMessage(messageRequest, context);
                if (pennyResponse != null && "BOT_PROCESS".equals(pennyResponse.getDecision())) {
                    log.info("Decision: BOT_PROCESS - Penny bot will handle conversation: {}", context.getConversationId());
                    context.setLastIntent(intent);
                    return DecisionResult.botProcess(pennyResponse);
                }
            } catch (Exception e) {
                log.warn("Penny bot processing failed, falling back to simple test: {}", e.getMessage());
            }
        }
        
        // Fallback: Sử dụng simpleMessageTestService
        try {
            MessageResponse testResponse = simpleMessageTestService.processMessage(messageRequest);
            log.info("Decision: BOT_PROCESS - Simple test service handling conversation: {}", context.getConversationId());
            context.setLastIntent(intent);
            return DecisionResult.botProcess(testResponse);
        } catch (Exception e) {
            log.error("Simple test service failed: {}", e.getMessage());
            return DecisionResult.humanRequired("Internal error: " + e.getMessage());
        }
    }

    /**
     * Decision result holder
     */
    public static class DecisionResult {
        private final String decision;
        private final String reason;
        private final boolean shouldUpdateContext;
        private final MessageResponse pennyResponse;

        private DecisionResult(String decision, String reason, boolean shouldUpdateContext, MessageResponse pennyResponse) {
            this.decision = decision;
            this.reason = reason;
            this.shouldUpdateContext = shouldUpdateContext;
            this.pennyResponse = pennyResponse;
        }

        public static DecisionResult botProcess() {
            return new DecisionResult("BOT_PROCESS", null, true, null);
        }

        public static DecisionResult botProcess(MessageResponse pennyResponse) {
            return new DecisionResult("BOT_PROCESS", null, true, pennyResponse);
        }

        public static DecisionResult humanRequired(String reason) {
            return new DecisionResult("HUMAN_REQUIRED", reason, true, null);
        }

        public static DecisionResult blocked(String reason) {
            return new DecisionResult("BLOCKED", reason, false, null);
        }

        // Getters
        public String getDecision() { return decision; }
        public String getReason() { return reason; }
        public boolean shouldUpdateContext() { return shouldUpdateContext; }
        public MessageResponse getPennyResponse() { return pennyResponse; }
    }
}
