package com.chatbot.core.wallet.config;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PreDestroy;
import java.io.IOException;

/**
 * Wallet gRPC Server Configuration - DISABLED
 * Note: Enable when WalletServiceGrpcImpl properly implements gRPC service interface
 */
//@Configuration
//@Slf4j
public class WalletGrpcServerConfig {

    @Value("${wallet.grpc.server.port:50056}")
    private int grpcPort;

    private Server grpcServer;

    //@Bean
    public Server walletGrpcServer() throws IOException {
        // log.info("Starting Wallet gRPC server on port: {}", grpcPort);
        
        // Service will be added when WalletServiceGrpcImpl properly implements BindableService
        grpcServer = ServerBuilder.forPort(grpcPort)
                .maxInboundMessageSize(10 * 1024 * 1024) // 10MB
                .maxInboundMetadataSize(10 * 1024 * 1024) // 10MB
                .build()
                .start();
        
        // log.info("Wallet gRPC server started successfully on port: {}", grpcPort);
        
        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            // log.info("Shutting down Wallet gRPC server...");
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
            // log.info("Wallet gRPC server stopped");
        }
    }
}
