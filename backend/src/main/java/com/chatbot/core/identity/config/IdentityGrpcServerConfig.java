package com.chatbot.core.identity.config;

import com.chatbot.core.identity.grpc.IdentityServiceGrpcImpl;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;

@Configuration
@Slf4j
public class IdentityGrpcServerConfig {

    @Value("${spring.grpc.server.port:50051}")
    private int grpcPort;

    @Autowired
    private IdentityServiceGrpcImpl identityServiceGrpcImpl;

    private Server grpcServer;

    @PostConstruct
    public void startGrpcServer() throws IOException {
        log.info("Starting Identity gRPC server on port: {}", grpcPort);
        
        grpcServer = ServerBuilder.forPort(grpcPort)
                .addService(identityServiceGrpcImpl)
                .maxInboundMessageSize(10 * 1024 * 1024) // 10MB
                .maxInboundMetadataSize(10 * 1024 * 1024) // 10MB
                .build()
                .start();
        
        log.info("Identity gRPC server started successfully on port: {}", grpcPort);
        
        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutting down Identity gRPC server...");
            stopGrpcServer();
        }));
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

    @Bean
    public Server identityGrpcServer() {
        return grpcServer;
    }
}
