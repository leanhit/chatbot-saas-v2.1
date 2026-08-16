package com.chatbot.core.message.grpc;

import com.chatbot.core.grpc.resilience.GrpcResilienceConfig;
import com.chatbot.core.tenant.exception.GrpcIntegrationException;
import com.chatbot.message.grpc.MessageServiceGrpc;
import com.chatbot.message.grpc.MessageServiceProto.*;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Message gRPC Client - Internal communication client
 */
@Service
@Slf4j
public class MessageGrpcClient {
    
    @Value("${message.grpc.client.host:localhost}")
    private String host;
    
    @Value("${message.grpc.client.port:50058}")
    private int port;
    
    @Value("${message.grpc.client.timeout:30}")
    private int timeoutSeconds;
    
    @Value("${grpc.security.tls.enabled:false}")
    private boolean tlsEnabled;

    private ManagedChannel channel;
    private MessageServiceGrpc.MessageServiceBlockingStub blockingStub;

    private final CircuitBreaker circuitBreaker;
    private final Retry retry;

    public MessageGrpcClient(CircuitBreaker messageGrpcCircuitBreaker, Retry messageGrpcRetry) {
        this.circuitBreaker = messageGrpcCircuitBreaker;
        this.retry = messageGrpcRetry;
    }
    
    @PostConstruct
    public void init() {
        try {
            ManagedChannelBuilder<?> builder = ManagedChannelBuilder.forAddress(host, port)
                    .keepAliveTime(30, TimeUnit.SECONDS)
                    .keepAliveTimeout(5, TimeUnit.SECONDS)
                    .keepAliveWithoutCalls(true);
            
            if (tlsEnabled) {
                builder.useTransportSecurity();
            } else {
                builder.usePlaintext();
            }
            
            channel = builder.build();
            blockingStub = MessageServiceGrpc.newBlockingStub(channel);

            log.info("Message gRPC client initialized: {}:{}", host, port);

            // Test connection
            testConnection();
            
        } catch (Exception e) {
            log.error("Failed to initialize Message gRPC client: {}", e.getMessage());
        }
    }
    
    @PreDestroy
    public void shutdown() {
        try {
            if (channel != null && !channel.isShutdown()) {
                channel.shutdown()
                        .awaitTermination(timeoutSeconds, TimeUnit.SECONDS);
                log.info("Message gRPC client shutdown completed");
            }
        } catch (InterruptedException e) {
            log.warn("Message gRPC client shutdown interrupted");
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Test connection to gRPC server with actual RPC call
     */
    private void testConnection() {
        try {
            // Use a lightweight GetMessage request to test connection
            GetMessageRequest request = GetMessageRequest.newBuilder()
                    .setMessageId("health-check")
                    .setTenantId("health-check")
                    .setUserId("health-check")
                    .build();

            GetMessageResponse response = blockingStub.withDeadlineAfter(5, TimeUnit.SECONDS)
                    .getMessage(request);

            log.info("✅ Message gRPC connection test PASSED - Server is responsive");

        } catch (io.grpc.StatusRuntimeException e) {
            if (e.getStatus().getCode() == io.grpc.Status.Code.NOT_FOUND) {
                // Expected - health-check message doesn't exist, but connection works
                log.info("✅ Message gRPC connection test PASSED - Server is responsive (expected NOT_FOUND error)");
            } else {
                log.warn("❌ Message gRPC connection test failed: {} - {}", e.getStatus().getCode(), e.getStatus().getDescription());
            }
        } catch (Exception e) {
            log.error("❌ Message gRPC connection test failed: {}", e.getMessage());
        }
    }
    
    /**
     * Get the managed channel for use by other services
     */
    public ManagedChannel getChannel() {
        return channel;
    }
    
    /**
     * Check if client is ready
     */
    public boolean isReady() {
        return channel != null && !channel.isShutdown() && !channel.isTerminated();
    }
    
    /**
     * Get connection status
     */
    public String getConnectionStatus() {
        if (channel == null) {
            return "NOT_INITIALIZED";
        }
        if (channel.isShutdown()) {
            return "SHUTDOWN";
        }
        if (channel.isTerminated()) {
            return "TERMINATED";
        }
        return "CONNECTED";
    }
    
    /**
     * Reconnect to gRPC server
     */
    public void reconnect() {
        log.info("Reconnecting to gRPC server...");
        shutdown();
        init();
    }
    
    /**
     * Get server info
     */
    public String getServerInfo() {
        return String.format("Message gRPC Server - %s:%d (Status: %s)",
                           host, port, getConnectionStatus());
    }

    /**
     * Send a message with circuit breaker and retry
     */
    public SendMessageResponse sendMessage(Message message, String tenantId, String userId) {
        Supplier<SendMessageResponse> supplier = CircuitBreaker.decorateSupplier(circuitBreaker,
            Retry.decorateSupplier(retry, () -> {
                try {
                    SendMessageRequest request = SendMessageRequest.newBuilder()
                            .setMessage(message)
                            .setTenantId(tenantId)
                            .setUserId(userId)
                            .build();
                    return blockingStub.sendMessage(request);
                } catch (io.grpc.StatusRuntimeException e) {
                    log.error("gRPC sendMessage failed: {} - {}", e.getStatus().getCode(), e.getStatus().getDescription());
                    throw new GrpcIntegrationException("Failed to send message: " + e.getStatus().getDescription(), e);
                }
            }));

        try {
            return supplier.get();
        } catch (Exception e) {
            log.error("Circuit breaker/retry failed for sendMessage: {}", e.getMessage());
            throw new GrpcIntegrationException("Failed to send message after retry: " + e.getMessage(), e);
        }
    }

    /**
     * Receive messages for a conversation with circuit breaker and retry
     */
    public ReceiveMessageResponse receiveMessage(String conversationId, String tenantId, String userId, long lastReceivedAt, int limit) {
        Supplier<ReceiveMessageResponse> supplier = CircuitBreaker.decorateSupplier(circuitBreaker,
            Retry.decorateSupplier(retry, () -> {
                try {
                    ReceiveMessageRequest request = ReceiveMessageRequest.newBuilder()
                            .setConversationId(conversationId)
                            .setTenantId(tenantId)
                            .setUserId(userId)
                            .setLastReceivedAt(lastReceivedAt)
                            .setLimit(limit)
                            .build();
                    return blockingStub.receiveMessage(request);
                } catch (io.grpc.StatusRuntimeException e) {
                    log.error("gRPC receiveMessage failed: {} - {}", e.getStatus().getCode(), e.getStatus().getDescription());
                    throw new GrpcIntegrationException("Failed to receive messages: " + e.getStatus().getDescription(), e);
                }
            }));

        try {
            return supplier.get();
        } catch (Exception e) {
            log.error("Circuit breaker/retry failed for receiveMessage: {}", e.getMessage());
            throw new GrpcIntegrationException("Failed to receive messages after retry: " + e.getMessage(), e);
        }
    }

    /**
     * Get a specific message with circuit breaker and retry
     */
    public GetMessageResponse getMessage(String messageId, String tenantId, String userId) {
        Supplier<GetMessageResponse> supplier = CircuitBreaker.decorateSupplier(circuitBreaker,
            Retry.decorateSupplier(retry, () -> {
                try {
                    GetMessageRequest request = GetMessageRequest.newBuilder()
                            .setMessageId(messageId)
                            .setTenantId(tenantId)
                            .setUserId(userId)
                            .build();
                    return blockingStub.getMessage(request);
                } catch (io.grpc.StatusRuntimeException e) {
                    log.error("gRPC getMessage failed for messageId {}: {} - {}", messageId, e.getStatus().getCode(), e.getStatus().getDescription());
                    throw new GrpcIntegrationException("Failed to get message: " + e.getStatus().getDescription(), e);
                }
            }));

        try {
            return supplier.get();
        } catch (Exception e) {
            log.error("Circuit breaker/retry failed for getMessage: {}", e.getMessage());
            throw new GrpcIntegrationException("Failed to get message after retry: " + e.getMessage(), e);
        }
    }

    /**
     * Create a conversation with circuit breaker and retry
     */
    public CreateConversationResponse createConversation(Conversation conversation, String tenantId, String userId) {
        Supplier<CreateConversationResponse> supplier = CircuitBreaker.decorateSupplier(circuitBreaker,
            Retry.decorateSupplier(retry, () -> {
                try {
                    CreateConversationRequest request = CreateConversationRequest.newBuilder()
                            .setConversation(conversation)
                            .setTenantId(tenantId)
                            .setUserId(userId)
                            .build();
                    return blockingStub.createConversation(request);
                } catch (io.grpc.StatusRuntimeException e) {
                    log.error("gRPC createConversation failed: {} - {}", e.getStatus().getCode(), e.getStatus().getDescription());
                    throw new GrpcIntegrationException("Failed to create conversation: " + e.getStatus().getDescription(), e);
                }
            }));

        try {
            return supplier.get();
        } catch (Exception e) {
            log.error("Circuit breaker/retry failed for createConversation: {}", e.getMessage());
            throw new GrpcIntegrationException("Failed to create conversation after retry: " + e.getMessage(), e);
        }
    }

    /**
     * Get a conversation with circuit breaker and retry
     */
    public GetConversationResponse getConversation(String conversationId, String tenantId, String userId) {
        Supplier<GetConversationResponse> supplier = CircuitBreaker.decorateSupplier(circuitBreaker,
            Retry.decorateSupplier(retry, () -> {
                try {
                    GetConversationRequest request = GetConversationRequest.newBuilder()
                            .setConversationId(conversationId)
                            .setTenantId(tenantId)
                            .setUserId(userId)
                            .build();
                    return blockingStub.getConversation(request);
                } catch (io.grpc.StatusRuntimeException e) {
                    log.error("gRPC getConversation failed for conversationId {}: {} - {}", conversationId, e.getStatus().getCode(), e.getStatus().getDescription());
                    throw new GrpcIntegrationException("Failed to get conversation: " + e.getStatus().getDescription(), e);
                }
            }));

        try {
            return supplier.get();
        } catch (Exception e) {
            log.error("Circuit breaker/retry failed for getConversation: {}", e.getMessage());
            throw new GrpcIntegrationException("Failed to get conversation after retry: " + e.getMessage(), e);
        }
    }

    /**
     * List conversations with circuit breaker and retry
     */
    public ListConversationsResponse listConversations(String tenantId, String userId, ConversationStatus status, ConversationType type, int page, int size) {
        Supplier<ListConversationsResponse> supplier = CircuitBreaker.decorateSupplier(circuitBreaker,
            Retry.decorateSupplier(retry, () -> {
                try {
                    ListConversationsRequest request = ListConversationsRequest.newBuilder()
                            .setTenantId(tenantId)
                            .setUserId(userId)
                            .setStatus(status)
                            .setType(type)
                            .setPage(page)
                            .setSize(size)
                            .build();
                    return blockingStub.listConversations(request);
                } catch (io.grpc.StatusRuntimeException e) {
                    log.error("gRPC listConversations failed: {} - {}", e.getStatus().getCode(), e.getStatus().getDescription());
                    throw new GrpcIntegrationException("Failed to list conversations: " + e.getStatus().getDescription(), e);
                }
            }));

        try {
            return supplier.get();
        } catch (Exception e) {
            log.error("Circuit breaker/retry failed for listConversations: {}", e.getMessage());
            throw new GrpcIntegrationException("Failed to list conversations after retry: " + e.getMessage(), e);
        }
    }

    /**
     * Get the blocking stub for direct use if needed
     */
    public MessageServiceGrpc.MessageServiceBlockingStub getBlockingStub() {
        return blockingStub;
    }
}
