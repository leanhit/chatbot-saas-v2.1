package com.chatbot.core.billing.grpc;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * gRPC Server Configuration for Billing Hub
 * Note: gRPC server will be properly configured after proto compilation
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class BillingGrpcServerConfig {

    @Value("${billing.grpc.server.port:50055}")
    private int grpcServerPort;

    @Bean
    public Server billingGrpcServer() throws IOException {
        log.info("Starting Billing gRPC server on port: {}", grpcServerPort);
        
        Server server = ServerBuilder.forPort(grpcServerPort)
                .build()
                .start();
        
        log.info("Billing gRPC server started successfully on port: {}", grpcServerPort);
        
        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutting down Billing gRPC server...");
            server.shutdown();
            try {
                if (!server.awaitTermination(30, java.util.concurrent.TimeUnit.SECONDS)) {
                    log.warn("Billing gRPC server did not terminate within 30 seconds");
                    server.shutdownNow();
                }
            } catch (InterruptedException e) {
                log.error("Interrupted while waiting for Billing gRPC server termination", e);
                server.shutdownNow();
            }
        }));
        
        return server;
    }
}
