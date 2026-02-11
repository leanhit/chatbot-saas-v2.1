package com.chatbot.modules.facebook.webhook.service;

import com.chatbot.modules.facebook.connection.model.FacebookConnection;
import com.chatbot.modules.facebook.connection.repository.FacebookConnectionRepository;
import com.chatbot.core.tenant.infra.TenantContext;
import com.chatbot.integrations.odoo.service.CustomerDataService;

import com.chatbot.modules.messaging.messStore.model.Channel;
import com.chatbot.modules.messaging.messStore.service.ConversationService;
import com.chatbot.modules.messaging.messStore.service.MessageService;
import com.chatbot.modules.messaging.takeover.service.TakeoverService;
import com.chatbot.modules.messaging.takeover.model.TakeoverMessage;
import com.chatbot.modules.messaging.messStore.model.Conversation;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class FacebookMessengerService {

    private final FacebookConnectionRepository connectionRepository;
    private final WebClient webClient;
    private final CustomerDataService customerDataService;

    private final ConversationService conversationService;
    private final MessageService messageService;
    private final TakeoverService takeoverService;

    public FacebookMessengerService(
            FacebookConnectionRepository connectionRepository,
            WebClient.Builder webClientBuilder,
            CustomerDataService customerDataService,
            ConversationService conversationService,
            MessageService messageService,
            TakeoverService takeoverService
    ) {
        this.connectionRepository = connectionRepository;
        this.webClient = webClientBuilder.build();
        this.customerDataService = customerDataService;
        this.conversationService = conversationService;
        this.messageService = messageService;
        this.takeoverService = takeoverService;
    }

    // --------------------------------------------------------------------------
    //                           SEND TEXT MESSAGE
    // --------------------------------------------------------------------------

    public void sendMessageToUser(
            String pageId,
            String recipientId,
            String messageText,
            String sender
    ) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            log.error("Không tìm thấy tenantId khi gửi message. pageId={}", pageId);
            return;
        }

        Optional<FacebookConnection> connectionOpt =
                connectionRepository.findByTenantIdAndPageId(tenantId, pageId);

        if (connectionOpt.isEmpty()) {
            log.error("Không tìm thấy FacebookConnection tenantId={}, pageId={}", tenantId, pageId);
            return;
        }

        FacebookConnection connection = connectionOpt.get();
        if (!connection.isEnabled()) {
            log.warn("FacebookConnection bị disable. pageId={}", pageId);
            return;
        }

        String accessToken = connection.getPageAccessToken();
        String url = "https://graph.facebook.com/v18.0/me/messages?access_token=" + accessToken;

        Map<String, Object> payload = Map.of(
                "recipient", Map.of("id", recipientId),
                "message", Map.of("text", messageText)
        );

        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                webClient.post()
                        .uri(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(payload)
                        .retrieve()
                        .toBodilessEntity()
                        .block();

                log.info("✅ Gửi text message thành công (attempt {}). recipientId={}", attempt, recipientId);

                saveOutgoingMessage(
                        connection.getId(),
                        recipientId,
                        messageText,
                        "TEXT",
                        null,
                        sender
                );
                return;

            } catch (Exception e) {
                log.warn("⚠️ Gửi message thất bại (attempt {}): {}", attempt, e.getMessage());
                if (attempt < 3) {
                    try {
                        Thread.sleep(1000L * attempt);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
    }

    // --------------------------------------------------------------------------
    //                           SEND IMAGE MESSAGE
    // --------------------------------------------------------------------------

    public void sendImageToUser(String pageId, String recipientId, String imageUrl) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            log.error("Không tìm thấy tenantId khi gửi image. pageId={}", pageId);
            return;
        }

        Optional<FacebookConnection> connectionOpt =
                connectionRepository.findByTenantIdAndPageId(tenantId, pageId);

        if (connectionOpt.isEmpty()) {
            log.error("Không tìm thấy FacebookConnection tenantId={}, pageId={}", tenantId, pageId);
            return;
        }

        FacebookConnection connection = connectionOpt.get();
        if (!connection.isEnabled()) {
            log.warn("FacebookConnection bị disable. pageId={}", pageId);
            return;
        }

        String accessToken = connection.getPageAccessToken();
        String url = "https://graph.facebook.com/v18.0/me/messages?access_token=" + accessToken;

        Map<String, Object> payload = Map.of(
                "recipient", Map.of("id", recipientId),
                "message", Map.of(
                        "attachment", Map.of(
                                "type", "image",
                                "payload", Map.of(
                                        "url", imageUrl,
                                        "is_reusable", true
                                )
                        )
                )
        );

        try {
            webClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(payload)
                    .retrieve()
                    .toBodilessEntity()
                    .block();

            log.info("✅ Gửi image message thành công. recipientId={}", recipientId);

            saveOutgoingMessage(
                    connection.getId(),
                    recipientId,
                    imageUrl,
                    "IMAGE",
                    Map.of("url", imageUrl, "is_reusable", true),
                    "bot"
            );

        } catch (Exception e) {
            log.error("❌ Lỗi gửi image message: {}", e.getMessage(), e);
        }
    }

    // --------------------------------------------------------------------------
    //                           SAVE OUTGOING MESSAGE
    // --------------------------------------------------------------------------

    private void saveOutgoingMessage(
            UUID connectionId,
            String externalUserId,
            String content,
            String messageType,
            Map<String, Object> rawPayload,
            String sender
    ) {
        try {
            Conversation conversation = conversationService.findOrCreate(
                    connectionId,
                    externalUserId,
                    Channel.FACEBOOK
            );

            Long conversationId = conversation.getId();

            messageService.saveMessage(
                    conversationId,
                    sender,
                    content,
                    messageType,
                    rawPayload
            );

            TakeoverMessage takeoverMessage = new TakeoverMessage(
                    String.valueOf(conversationId),
                    sender,
                    content,
                    System.currentTimeMillis()
            );
            takeoverService.saveMessage(takeoverMessage);

        } catch (Exception e) {
            log.error("❌ Lỗi saveOutgoingMessage: {}", e.getMessage(), e);
        }
    }

    // --------------------------------------------------------------------------
    //                           HANDLE BOTPRESS RESPONSE
    // --------------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    public void sendBotpressRepliesToUser(
            String pageId,
            String senderId,
            Map<String, Object> botpressResponse
    ) {
        List<Map<String, Object>> replies =
                (List<Map<String, Object>>) botpressResponse.get("responses");

        if (replies == null || replies.isEmpty()) {
            log.warn("Botpress responses rỗng");
            return;
        }

        Set<String> sentMessages = new HashSet<>();

        for (Map<String, Object> reply : replies) {
            String type = (String) reply.get("type");

            if ("text".equals(type)) {
                String text = (String) reply.get("text");
                if (text != null && sentMessages.add(text)) {
                    sendMessageToUser(pageId, senderId, text, "bot");
                    customerDataService.processAndAccumulate(pageId, senderId, text);
                }
            } else if ("image".equals(type)) {
                String imageUrl = (String) reply.get("image");
                if (imageUrl != null) {
                    sendImageToUser(pageId, senderId, imageUrl);
                }
            } else {
                log.warn("Loại message chưa hỗ trợ: {}", type);
            }
        }
    }
}