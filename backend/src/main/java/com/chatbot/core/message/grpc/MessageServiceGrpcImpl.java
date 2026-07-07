package com.chatbot.core.message.grpc;

import lombok.RequiredArgsConstructor;
import com.chatbot.message.grpc.MessageServiceProto.*;
import com.chatbot.message.grpc.MessageServiceGrpc;
import com.chatbot.core.message.store.service.MessageService;
import com.chatbot.core.message.store.service.ConversationService;
import com.chatbot.core.message.store.model.Message;
import com.chatbot.core.message.store.model.Conversation;
import com.chatbot.core.grpc.util.GrpcMapperUtil;
import com.chatbot.core.tenant.exception.TenantNotFoundException;
import com.chatbot.core.tenant.service.TenantService;
import io.grpc.stub.StreamObserver;
import io.grpc.Status;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Message Service gRPC Implementation
 * Hub cho xử lý tin nhắn và cuộc trò chuyện
 */
@Service
@Slf4j
public @RequiredArgsConstructor
class MessageServiceGrpcImpl extends MessageServiceGrpc.MessageServiceImplBase {
    

    private final MessageService messageService;
    

    private final ConversationService conversationService;


    private final TenantService tenantService;

    @Override
    public void sendMessage(SendMessageRequest request, StreamObserver<SendMessageResponse> responseObserver) {
        try {
            log.info("📨 gRPC: Gửi tin nhắn - Conversation: {}, Sender: {}", 
                request.getMessage().getConversationId(), request.getMessage().getSenderId());
            
            // Validate tenant context
            validateTenant(request.getTenantId());
            
            // Convert gRPC message to domain model
            Message message = convertGrpcMessageToDomain(request.getMessage());
            
            // Save message to database
            Message savedMessage = messageService.saveMessage(
                message.getConversationId(),
                message.getSender(), // Sử dụng field sender
                message.getContent(),
                message.getMessageType(),
                null // raw payload, null vì không có metadata
            );
            
            // Build response
            SendMessageResponse response = SendMessageResponse.newBuilder()
                .setSuccess(true)
                .setMessage(convertDomainMessageToGrpc(savedMessage))
                .setCorrelationId(request.getCorrelationId())
                .build();
                
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            
            log.info("[gRPC] Message sent successfully - MessageId: {}", savedMessage.getId());
            
        } catch (Exception e) {
            log.error("[gRPC] Error sending message", e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                .withDescription("Lỗi khi gửi tin nhắn: " + e.getMessage())
                .asRuntimeException());
        }
    }

    @Override
    public void receiveMessage(ReceiveMessageRequest request, StreamObserver<ReceiveMessageResponse> responseObserver) {
        try {
            log.info("[gRPC] Receive messages - Conversation: {}, Limit: {}", 
                request.getConversationId(), request.getLimit());
            
            // Validate inputs
            validateTenant(request.getTenantId());
            validateConversationId(request.getConversationId());
            
            // Get messages for conversation
            org.springframework.data.domain.Page<Message> messagePage = messageService.getMessages(
                Long.parseLong(request.getConversationId()),
                0, // page
                request.getLimit() // size
            );
            
            List<Message> messages = messagePage.getContent();
            
            // Convert to gRPC messages
            List<com.chatbot.message.grpc.MessageServiceProto.Message> grpcMessages = messages.stream()
                .map(this::convertDomainMessageToGrpc)
                .collect(Collectors.toList());
            
            // Build response
            ReceiveMessageResponse response = ReceiveMessageResponse.newBuilder()
                .setSuccess(true)
                .addAllMessages(grpcMessages)
                .setCorrelationId(request.getCorrelationId())
                .build();
                
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            
            log.info("[gRPC] Received {} messages for conversation {}", 
                grpcMessages.size(), request.getConversationId());
            
        } catch (Exception e) {
            log.error("[gRPC] Error receiving messages", e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                .withDescription("Lỗi khi nhận tin nhắn: " + e.getMessage())
                .asRuntimeException());
        }
    }

    @Override
    public void getMessage(GetMessageRequest request, StreamObserver<GetMessageResponse> responseObserver) {
        try {
            log.info("[gRPC] Get message - MessageId: {}", request.getMessageId());
            
            // Validate tenant context
            validateTenant(request.getTenantId());
            
            // Get message by ID
            Optional<Message> messageOpt = messageService.getMessageById(
                Long.parseLong(request.getMessageId())
            );
            
            GetMessageResponse.Builder responseBuilder = GetMessageResponse.newBuilder()
                .setCorrelationId(request.getCorrelationId());
            
            if (messageOpt.isPresent()) {
                responseBuilder
                    .setSuccess(true)
                    .setMessage(convertDomainMessageToGrpc(messageOpt.get()));
            } else {
                responseBuilder
                    .setSuccess(false)
                    .setErrorMessage("Không tìm thấy tin nhắn");
            }
            
            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();
            
        } catch (Exception e) {
            log.error("[gRPC] Error getting message", e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                .withDescription("Lỗi khi lấy tin nhắn: " + e.getMessage())
                .asRuntimeException());
        }
    }

    @Override
    public void createConversation(CreateConversationRequest request, StreamObserver<CreateConversationResponse> responseObserver) {
        try {
            log.info("[gRPC] Create conversation - Tenant: {}, Participants: {}", 
                request.getTenantId(), request.getConversation().getParticipantsCount());
            
            // Validate tenant context
            validateTenant(request.getTenantId());
            
            // Convert gRPC conversation to domain model
            Conversation conversation = convertGrpcConversationToDomain(request.getConversation());
            
            // Create conversation
            Conversation savedConversation = conversationService.createConversation(conversation);
            
            // Build response
            CreateConversationResponse response = CreateConversationResponse.newBuilder()
                .setSuccess(true)
                .setConversation(convertDomainConversationToGrpc(savedConversation))
                .setCorrelationId(request.getCorrelationId())
                .build();
                
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            
            log.info("[gRPC] Conversation created successfully - ConversationId: {}", savedConversation.getId());
            
        } catch (Exception e) {
            log.error("[gRPC] Error creating conversation", e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                .withDescription("Lỗi khi tạo cuộc trò chuyện: " + e.getMessage())
                .asRuntimeException());
        }
    }

    @Override
    public void updateMessage(UpdateMessageRequest request, StreamObserver<UpdateMessageResponse> responseObserver) {
        try {
            log.info("[gRPC] Update message - MessageId: {}", request.getMessageId());
            
            validateTenant(request.getTenantId());
            
            // Convert and update message
            Message message = convertGrpcMessageToDomain(request.getMessage());
            Message updatedMessage = messageService.updateMessage(message);
            
            UpdateMessageResponse response = UpdateMessageResponse.newBuilder()
                .setSuccess(true)
                .setMessage(convertDomainMessageToGrpc(updatedMessage))
                .setCorrelationId(request.getCorrelationId())
                .build();
                
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            
        } catch (Exception e) {
            log.error("[gRPC] Error updating message", e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                .withDescription("Lỗi khi cập nhật tin nhắn: " + e.getMessage())
                .asRuntimeException());
        }
    }

    @Override
    public void deleteMessage(DeleteMessageRequest request, StreamObserver<DeleteMessageResponse> responseObserver) {
        try {
            log.info("[gRPC] Delete message - MessageId: {}, Permanent: {}", 
                request.getMessageId(), request.getPermanent());
            
            validateTenant(request.getTenantId());
            
            // MessageService.deleteMessage chỉ nhận messageId
            messageService.deleteMessage(Long.parseLong(request.getMessageId()));
            
            DeleteMessageResponse response = DeleteMessageResponse.newBuilder()
                .setSuccess(true) // Nếu không có exception thì thành công
                .setCorrelationId(request.getCorrelationId())
                .build();
                
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            
        } catch (Exception e) {
            log.error("❌ gRPC: Lỗi khi xóa tin nhắn", e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                .withDescription("Lỗi khi xóa tin nhắn: " + e.getMessage())
                .asRuntimeException());
        }
    }

    @Override
    public void getConversation(GetConversationRequest request, StreamObserver<GetConversationResponse> responseObserver) {
        try {
            log.info("🔍 gRPC: Lấy cuộc trò chuyện - ConversationId: {}", request.getConversationId());
            
            validateTenant(request.getTenantId());
            
            // Get conversation by ID
            Optional<Conversation> conversationOpt = conversationService.findConversationById(
                Long.parseLong(request.getConversationId())
            );
            
            GetConversationResponse.Builder responseBuilder = GetConversationResponse.newBuilder()
                .setCorrelationId(request.getCorrelationId());
            
            if (conversationOpt.isPresent()) {
                responseBuilder
                    .setSuccess(true)
                    .setConversation(convertDomainConversationToGrpc(conversationOpt.get()));
            } else {
                responseBuilder
                    .setSuccess(false)
                    .setErrorMessage("Không tìm thấy cuộc trò chuyện");
            }
            
            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();
            
        } catch (Exception e) {
            log.error("❌ gRPC: Lỗi khi lấy cuộc trò chuyện", e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                .withDescription("Lỗi khi lấy cuộc trò chuyện: " + e.getMessage())
                .asRuntimeException());
        }
    }

    @Override
    public void sendBatchMessages(SendBatchMessagesRequest request, StreamObserver<SendBatchMessagesResponse> responseObserver) {
        try {
            log.info("📦 gRPC: Gửi tin nhắn hàng loạt - Số lượng: {}", request.getMessagesCount());
            
            validateTenant(request.getTenantId());
            
            List<Message> savedMessages = new ArrayList<>();
            List<String> failedIds = new ArrayList<>();
            
            for (com.chatbot.message.grpc.MessageServiceProto.Message grpcMessage : request.getMessagesList()) {
                try {
                    Message message = convertGrpcMessageToDomain(grpcMessage);
                    Message savedMessage = messageService.saveMessage(
                        message.getConversationId(),
                        message.getSender(), // Sử dụng field sender
                        message.getContent(),
                        message.getMessageType(),
                        null // raw payload, null vì không có metadata
                    );
                    savedMessages.add(savedMessage);
                } catch (Exception e) {
                    failedIds.add(grpcMessage.getId());
                    log.error("❌ Lỗi khi gửi tin nhắn {}: {}", grpcMessage.getId(), e.getMessage());
                }
            }
            
            SendBatchMessagesResponse response = SendBatchMessagesResponse.newBuilder()
                .setSuccess(failedIds.isEmpty())
                .addAllMessages(savedMessages.stream().map(this::convertDomainMessageToGrpc).collect(Collectors.toList()))
                .addAllFailedMessageIds(failedIds)
                .setCorrelationId(request.getCorrelationId())
                .build();
                
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            
            log.info("✅ gRPC: Đã gửi {} tin nhắn thành công, {} thất bại", 
                savedMessages.size(), failedIds.size());
            
        } catch (Exception e) {
            log.error("❌ gRPC: Lỗi khi gửi tin nhắn hàng loạt", e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                .withDescription("Lỗi khi gửi tin nhãn hàng loạt: " + e.getMessage())
                .asRuntimeException());
        }
    }

    @Override
    public void getMessagesByConversation(GetMessagesByConversationRequest request, StreamObserver<GetMessagesByConversationResponse> responseObserver) {
        try {
            log.info("📥 gRPC: Lấy tin nhắn theo cuộc trò chuyện - Conversation: {}, Limit: {}", 
                request.getConversationId(), request.getLimit());
            
            // Validate inputs
            validateTenant(request.getTenantId());
            validateConversationId(request.getConversationId());
            
            // Get messages for conversation
            org.springframework.data.domain.Page<Message> messagePage = messageService.getMessages(
                Long.parseLong(request.getConversationId()),
                0, // page
                request.getLimit() // size
            );
            
            List<Message> messages = messagePage.getContent();
            
            GetMessagesByConversationResponse response = GetMessagesByConversationResponse.newBuilder()
                .setSuccess(true)
                .addAllMessages(messages.stream().map(this::convertDomainMessageToGrpc).collect(Collectors.toList()))
                .setHasMore(messages.size() >= request.getLimit())
                .setCorrelationId(request.getCorrelationId())
                .build();
                
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            
        } catch (Exception e) {
            log.error("❌ gRPC: Lỗi khi lấy tin nhắn theo cuộc trò chuyện", e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                .withDescription("Lỗi khi lấy tin nhắn theo cuộc trò chuyện: " + e.getMessage())
                .asRuntimeException());
        }
    }
    private void validateTenant(String tenantId) {
        if (tenantId == null || tenantId.trim().isEmpty()) {
            throw Status.INVALID_ARGUMENT
                .withDescription("Tenant ID is required")
                .asRuntimeException();
        }

        try {
            Long tenantIdLong = Long.parseLong(tenantId);
            // Validate tenant exists
            tenantService.getTenant(tenantIdLong);
        } catch (NumberFormatException e) {
            throw Status.INVALID_ARGUMENT
                .withDescription("Invalid Tenant ID format: " + tenantId)
                .asRuntimeException();
        } catch (TenantNotFoundException e) {
            log.warn("Tenant not found: {}", tenantId);
            throw Status.NOT_FOUND
                .withDescription("Tenant not found: " + tenantId)
                .asRuntimeException();
        } catch (Exception e) {
            log.error("Error validating tenant: {}", tenantId, e);
            throw Status.INTERNAL
                .withDescription("Error validating tenant: " + e.getMessage())
                .asRuntimeException();
        }
    }

    private void validateConversationId(String conversationId) {
        if (conversationId == null || conversationId.trim().isEmpty()) {
            throw Status.INVALID_ARGUMENT
                .withDescription("Conversation ID is required")
                .asRuntimeException();
        }

        try {
            Long.parseLong(conversationId);
        } catch (NumberFormatException e) {
            throw Status.INVALID_ARGUMENT
                .withDescription("Invalid Conversation ID format: " + conversationId)
                .asRuntimeException();
        }
    }

    private void validateMessageId(String messageId) {
        if (messageId == null || messageId.trim().isEmpty()) {
            throw Status.INVALID_ARGUMENT
                .withDescription("Message ID is required")
                .asRuntimeException();
        }

        try {
            Long.parseLong(messageId);
        } catch (NumberFormatException e) {
            throw Status.INVALID_ARGUMENT
                .withDescription("Invalid Message ID format: " + messageId)
                .asRuntimeException();
        }
    }

    private Message convertGrpcMessageToDomain(com.chatbot.message.grpc.MessageServiceProto.Message grpcMessage) {
        Message message = new Message();
        message.setConversationId(GrpcMapperUtil.stringToLong(grpcMessage.getConversationId()));
        message.setSender(grpcMessage.getSenderId());
        message.setContent(grpcMessage.getContent());
        message.setMessageType(GrpcMapperUtil.enumToString(grpcMessage.getType()));
        message.setExternalMessageId(grpcMessage.getExternalId());
        return message;
    }

    private com.chatbot.message.grpc.MessageServiceProto.Message convertDomainMessageToGrpc(Message domainMessage) {
        return com.chatbot.message.grpc.MessageServiceProto.Message.newBuilder()
            .setId(GrpcMapperUtil.longToString(domainMessage.getId()))
            .setConversationId(GrpcMapperUtil.longToString(domainMessage.getConversationId()))
            .setSenderId(domainMessage.getSender())
            .setContent(domainMessage.getContent())
            .setType(MessageType.valueOf(domainMessage.getMessageType()))
            .setExternalId(GrpcMapperUtil.nullSafeString(domainMessage.getExternalMessageId()))
            .setCreatedAt(GrpcMapperUtil.localDateTimeToTimestamp(domainMessage.getCreatedAt()))
            .build();
    }

    private Conversation convertGrpcConversationToDomain(com.chatbot.message.grpc.MessageServiceProto.Conversation grpcConversation) {
        Conversation conversation = new Conversation();
        conversation.setTenantId(GrpcMapperUtil.stringToLong(grpcConversation.getTenantId()));
        conversation.setConnectionId(UUID.randomUUID());
        conversation.setExternalUserId(grpcConversation.getParticipantsList().isEmpty() ? "" : grpcConversation.getParticipantsList().get(0));
        conversation.setUserName(grpcConversation.getTitle());
        conversation.setStatus("open");
        conversation.setIsTakenOverByAgent(false);
        conversation.setIsClosedByAgent(false);
        return conversation;
    }

    private com.chatbot.message.grpc.MessageServiceProto.Conversation convertDomainConversationToGrpc(Conversation domainConversation) {
        return com.chatbot.message.grpc.MessageServiceProto.Conversation.newBuilder()
            .setId(GrpcMapperUtil.longToString(domainConversation.getId()))
            .setTenantId(GrpcMapperUtil.longToString(domainConversation.getTenantId()))
            // Tạo participants list từ externalUserId
            .addAllParticipants(domainConversation.getExternalUserId() != null ? 
                java.util.Arrays.asList(domainConversation.getExternalUserId()) : 
                java.util.Collections.emptyList())
            .setType(ConversationType.CONVERSATION_TYPE_DIRECT)
            .setTitle(domainConversation.getUserName() != null ? domainConversation.getUserName() : "")
            .setDescription("")
            .setStatus(domainConversation.getStatus() != null ? 
                ConversationStatus.valueOf(domainConversation.getStatus().toUpperCase()) : 
                ConversationStatus.CONVERSATION_STATUS_ACTIVE)
            .setCreatedAt(domainConversation.getCreatedAt() != null ? 
                domainConversation.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() : 0)
            .setUpdatedAt(domainConversation.getUpdatedAt() != null ? 
                domainConversation.getUpdatedAt().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() : 0)
            .build();
    }
}
