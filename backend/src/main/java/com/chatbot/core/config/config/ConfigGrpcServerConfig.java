package com.chatbot.core.config.config;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PreDestroy;
import java.io.IOException;

/**
 * Config gRPC Server Configuration - DISABLED
 * Note: Enable when ConfigServiceGrpcImpl properly implements gRPC service interface
 */
//@Configuration
//@Slf4j
public class ConfigGrpcServerConfig {

    @Value("${config.grpc.server.port:50057}")
    private int grpcPort;

    private Server grpcServer;

    //@Bean
    public Server configGrpcServer() throws IOException {
        // log.info("Starting Config gRPC server on port: {}", grpcPort);
        
        // Service will be added when ConfigServiceGrpcImpl properly implements BindableService
        grpcServer = ServerBuilder.forPort(grpcPort)
                .maxInboundMessageSize(10 * 1024 * 1024) // 10MB
                .maxInboundMetadataSize(10 * 1024 * 1024) // 10MB
                .build()
                .start();
        
        // log.info("Config gRPC server started successfully on port: {}", grpcPort);
        
        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            // log.info("Shutting down Config gRPC server...");
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
            // log.info("Config gRPC server stopped");
        }
    }
}
