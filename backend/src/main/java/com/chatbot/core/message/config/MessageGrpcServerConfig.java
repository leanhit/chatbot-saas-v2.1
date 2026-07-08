package com.chatbot.core.message.config;

import com.chatbot.core.message.grpc.MessageServiceGrpcImpl;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import javax.annotation.PreDestroy;
import java.io.IOException;

/**
 * Message gRPC Server Configuration
 * Controlled via conditional property message.grpc.server.enabled
 */
@Configuration
@Slf4j
@ConditionalOnProperty(name = "message.grpc.server.enabled", havingValue = "true", matchIfMissing = false)
public class MessageGrpcServerConfig {

    @Value("${message.grpc.server.port:50058}")
    private int grpcPort;

    @Value("${grpc.security.tls.enabled:false}")
    private boolean tlsEnabled;

    @Value("${grpc.security.tls.cert-chain-path:}")
    private String certChainPath;

    @Value("${grpc.security.tls.private-key-path:}")
    private String privateKeyPath;

    private final MessageServiceGrpcImpl messageServiceGrpcImpl;
    private Server grpcServer;

    public MessageGrpcServerConfig(MessageServiceGrpcImpl messageServiceGrpcImpl) {
        this.messageServiceGrpcImpl = messageServiceGrpcImpl;
    }

    @Bean
    public Server messageGrpcServer() throws IOException {
        log.info("Starting Message gRPC server on port: {}", grpcPort);
        
        io.grpc.ServerBuilder<?> serverBuilder = ServerBuilder.forPort(grpcPort)
                .addService(messageServiceGrpcImpl)
                .maxInboundMessageSize(10 * 1024 * 1024) // 10MB
                .maxInboundMetadataSize(10 * 1024 * 1024); // 10MB

        if (tlsEnabled && certChainPath != null && !certChainPath.isEmpty() && privateKeyPath != null && !privateKeyPath.isEmpty()) {
            serverBuilder.useTransportSecurity(new java.io.File(certChainPath), new java.io.File(privateKeyPath));
            log.info("Message gRPC server TLS enabled");
        }
                
        grpcServer = serverBuilder.build().start();
        
        log.info("Message gRPC server started successfully on port: {}", grpcPort);
        
        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutting down Message gRPC server...");
            stopGrpcServer();
        }));
        
        return grpcServer;
    }

    @PreDestroy
    public void stopGrpcServer() {
        if (grpcServer != null) {
            grpcServer.shutdown();
            try {
                if (!grpcServer.awaitTermination(30, java.util.concurrent.TimeUnit.SECONDS)) {
                    grpcServer.shutdownNow();
                }
            } catch (InterruptedException e) {
                grpcServer.shutdownNow();
                Thread.currentThread().interrupt();
            }
            log.info("Message gRPC server stopped");
        }
    }
}
