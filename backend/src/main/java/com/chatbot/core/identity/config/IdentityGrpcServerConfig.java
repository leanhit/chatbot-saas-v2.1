package com.chatbot.core.identity.config;

import lombok.RequiredArgsConstructor;
import com.chatbot.core.identity.grpc.IdentityServiceGrpcImpl;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PreDestroy;
import java.io.IOException;

@Configuration
@Slf4j
public @RequiredArgsConstructor
class IdentityGrpcServerConfig {

    @Value("${identity.grpc.server.port}")
    private int grpcPort;

    @Value("${grpc.security.tls.enabled:false}")
    private boolean tlsEnabled;

    @Value("${grpc.security.tls.cert-chain-path:}")
    private String certChainPath;

    @Value("${grpc.security.tls.private-key-path:}")
    private String privateKeyPath;

    private final IdentityServiceGrpcImpl identityServiceGrpcImpl;

    private Server grpcServer;

    @Bean
    public Server identityGrpcServer() throws IOException {
        log.info("Starting Identity gRPC server on port: {}", grpcPort);
        
        io.grpc.ServerBuilder<?> serverBuilder = ServerBuilder.forPort(grpcPort)
                .addService(identityServiceGrpcImpl)
                .maxInboundMessageSize(10 * 1024 * 1024) // 10MB
                .maxInboundMetadataSize(10 * 1024 * 1024); // 10MB

        if (tlsEnabled && certChainPath != null && !certChainPath.isEmpty() && privateKeyPath != null && !privateKeyPath.isEmpty()) {
            serverBuilder.useTransportSecurity(new java.io.File(certChainPath), new java.io.File(privateKeyPath));
            log.info("Identity gRPC server TLS enabled");
        }
                
        try {
            grpcServer = serverBuilder.build().start();
            log.info("Identity gRPC server started successfully on port: {}", grpcPort);
        } catch (IOException e) {
            log.warn("Failed to start Identity gRPC server on port {}: {}. Falling back to dynamic port...", grpcPort, e.getMessage());
            io.grpc.ServerBuilder<?> fallbackBuilder = ServerBuilder.forPort(0)
                    .addService(identityServiceGrpcImpl)
                    .maxInboundMessageSize(10 * 1024 * 1024)
                    .maxInboundMetadataSize(10 * 1024 * 1024);
            grpcServer = fallbackBuilder.build().start();
            log.info("Identity gRPC server started successfully on dynamic port: {}", grpcServer.getPort());
        }
        
        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutting down Identity gRPC server...");
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
            log.info("Identity gRPC server stopped");
        }
    }
}
