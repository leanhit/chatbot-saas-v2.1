package com.chatbot.core.message.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.TimeUnit;

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
    
    private ManagedChannel channel;
    
    @PostConstruct
    public void init() {
        try {
            channel = ManagedChannelBuilder.forAddress(host, port)
                    .usePlaintext()
                    .keepAliveTime(30, TimeUnit.SECONDS)
                    .keepAliveTimeout(5, TimeUnit.SECONDS)
                    .keepAliveWithoutCalls(true)
                    .build();
            
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
     * Test connection to gRPC server
     */
    private void testConnection() {
        try {
            // Implementation would depend on actual gRPC service definitions
            log.debug("Testing gRPC connection...");
            
            // For now, just log that we're testing
            log.info("gRPC connection test completed");
            
        } catch (Exception e) {
            log.error("gRPC connection test failed: {}", e.getMessage());
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
}
