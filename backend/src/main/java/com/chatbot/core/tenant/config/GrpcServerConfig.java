package com.chatbot.core.tenant.config;

import lombok.RequiredArgsConstructor;
import com.chatbot.core.tenant.grpc.TenantServiceGrpcImpl;
import com.chatbot.core.tenant.grpc.GrpcAuthInterceptor;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.ServerInterceptors;
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
public @RequiredArgsConstructor
class GrpcServerConfig {

    @Value("${tenant.grpc.server.port:50053}")
    private int grpcPort;

    @Value("${grpc.security.tls.enabled:false}")
    private boolean tlsEnabled;

    @Value("${grpc.security.tls.cert-chain-path:}")
    private String certChainPath;

    @Value("${grpc.security.tls.private-key-path:}")
    private String privateKeyPath;

    private final TenantServiceGrpcImpl tenantServiceGrpcImpl;


    private final GrpcAuthInterceptor grpcAuthInterceptor;

    private Server grpcServer;

    @PostConstruct
    public void startGrpcServer() throws IOException {
        log.info("Starting gRPC server on port: {}", grpcPort);
        
        io.grpc.ServerBuilder<?> serverBuilder = ServerBuilder.forPort(grpcPort)
                .addService(ServerInterceptors.intercept(tenantServiceGrpcImpl, grpcAuthInterceptor))
                .maxInboundMessageSize(10 * 1024 * 1024) // 10MB
                .maxInboundMetadataSize(10 * 1024 * 1024); // 10MB

        if (tlsEnabled && certChainPath != null && !certChainPath.isEmpty() && privateKeyPath != null && !privateKeyPath.isEmpty()) {
            serverBuilder.useTransportSecurity(new java.io.File(certChainPath), new java.io.File(privateKeyPath));
            log.info("Tenant gRPC server TLS enabled");
        }
                
        grpcServer = serverBuilder.build().start();
        
        log.info("gRPC server started successfully on port: {} with authentication enabled", grpcPort);
        
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
