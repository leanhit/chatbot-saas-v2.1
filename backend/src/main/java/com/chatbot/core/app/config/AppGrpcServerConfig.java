package com.chatbot.core.app.config;

import lombok.RequiredArgsConstructor;
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
public @RequiredArgsConstructor
class AppGrpcServerConfig {

    @Value("${app.grpc.server.port:50054}")
    private int grpcPort;

    @Value("${grpc.security.tls.enabled:false}")
    private boolean tlsEnabled;

    @Value("${grpc.security.tls.cert-chain-path:}")
    private String certChainPath;

    @Value("${grpc.security.tls.private-key-path:}")
    private String privateKeyPath;

    private final AppServiceGrpcImpl appServiceGrpcImpl;

    private Server grpcServer;

    @Bean
    public Server appGrpcServer() throws IOException {
        log.info("Starting App gRPC server on port: {}", grpcPort);
        
        io.grpc.ServerBuilder<?> serverBuilder = ServerBuilder.forPort(grpcPort)
                .addService(appServiceGrpcImpl)
                .maxInboundMessageSize(10 * 1024 * 1024) // 10MB
                .maxInboundMetadataSize(10 * 1024 * 1024); // 10MB

        if (tlsEnabled && certChainPath != null && !certChainPath.isEmpty() && privateKeyPath != null && !privateKeyPath.isEmpty()) {
            serverBuilder.useTransportSecurity(new java.io.File(certChainPath), new java.io.File(privateKeyPath));
            log.info("App gRPC server TLS enabled");
        }
                
        try {
            grpcServer = serverBuilder.build().start();
            log.info("App gRPC server started successfully on port: {}", grpcPort);
        } catch (IOException e) {
            log.warn("Failed to start App gRPC server on port {}: {}. Falling back to dynamic port...", grpcPort, e.getMessage());
            io.grpc.ServerBuilder<?> fallbackBuilder = ServerBuilder.forPort(0)
                    .addService(appServiceGrpcImpl)
                    .maxInboundMessageSize(10 * 1024 * 1024)
                    .maxInboundMetadataSize(10 * 1024 * 1024);
            grpcServer = fallbackBuilder.build().start();
            log.info("App gRPC server started successfully on dynamic port: {}", grpcServer.getPort());
        }
        
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
