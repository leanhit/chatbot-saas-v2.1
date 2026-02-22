package com.chatbot.core.app.config;

import com.chatbot.core.app.grpc.AppServiceGrpcImpl;
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
public class AppGrpcServerConfig {

    @Value("${app.grpc.server.port:50054}")
    private int grpcPort;

    @Autowired
    private AppServiceGrpcImpl appServiceGrpcImpl;

    private Server grpcServer;

    @Bean
    public Server appGrpcServer() throws IOException {
        log.info("Starting App gRPC server on port: {}", grpcPort);
        
        grpcServer = ServerBuilder.forPort(grpcPort)
                .addService(appServiceGrpcImpl)
                .maxInboundMessageSize(10 * 1024 * 1024) // 10MB
                .maxInboundMetadataSize(10 * 1024 * 1024) // 10MB
                .build()
                .start();
        
        log.info("App gRPC server started successfully on port: {}", grpcPort);
        
        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutting down App gRPC server...");
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
            log.info("App gRPC server stopped");
        }
    }
}
