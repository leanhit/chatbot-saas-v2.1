package com.chatbot.core.tenant.config;

import com.chatbot.core.tenant.grpc.TenantServiceGrpcImpl;
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
public class GrpcServerConfig {

    @Value("${spring.grpc.server.port:50052}")
    private int grpcPort;

    @Autowired
    private TenantServiceGrpcImpl tenantServiceGrpcImpl;

    private Server grpcServer;

    @PostConstruct
    public void startGrpcServer() throws IOException {
        log.info("Starting gRPC server on port: {}", grpcPort);
        
        grpcServer = ServerBuilder.forPort(grpcPort)
                .addService(tenantServiceGrpcImpl)
                .maxInboundMessageSize(10 * 1024 * 1024) // 10MB
                .maxInboundMetadataSize(10 * 1024 * 1024) // 10MB
                .build()
                .start();
        
        log.info("gRPC server started successfully on port: {}", grpcPort);
        
        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutting down gRPC server...");
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
            log.info("gRPC server stopped");
        }
    }

    @Bean
    public Server grpcServer() {
        return grpcServer;
    }
}
