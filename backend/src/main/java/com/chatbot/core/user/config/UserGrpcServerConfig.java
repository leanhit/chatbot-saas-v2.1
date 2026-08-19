package com.chatbot.core.user.config;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import com.chatbot.core.user.grpc.UserServiceGrpcImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * User gRPC Server Configuration
 */
@Configuration
@Slf4j
public class UserGrpcServerConfig {

    @Value("${user.grpc.server.port:50052}")
    private int port;

    @Value("${grpc.security.tls.enabled:false}")
    private boolean tlsEnabled;

    @Value("${grpc.security.tls.cert-chain-path:}")
    private String certChainPath;

    @Value("${grpc.security.tls.private-key-path:}")
    private String privateKeyPath;

    @Bean
    public Server userGrpcServer(UserServiceGrpcImpl userService) throws IOException {
        io.grpc.ServerBuilder<?> serverBuilder = ServerBuilder.forPort(port)
                .addService(userService);
                
        if (tlsEnabled && certChainPath != null && !certChainPath.isEmpty() && privateKeyPath != null && !privateKeyPath.isEmpty()) {
            serverBuilder.useTransportSecurity(new java.io.File(certChainPath), new java.io.File(privateKeyPath));
        }
                
        Server server;
        try {
            server = serverBuilder.build().start();
            log.info("User gRPC server started on port {}", port);
        } catch (IOException e) {
            log.warn("Failed to start User gRPC server on port {}: {}. Falling back to dynamic port...", port, e.getMessage());
            server = ServerBuilder.forPort(0).addService(userService).build().start();
            log.info("User gRPC server started on dynamic port {}", server.getPort());
        }
        
        final Server finalServer = server;
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutting down User gRPC server...");
            finalServer.shutdown();
        }));
        
        return server;
    }
}
