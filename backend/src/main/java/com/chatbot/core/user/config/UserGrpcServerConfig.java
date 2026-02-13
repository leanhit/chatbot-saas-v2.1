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

    @Value("${user.grpc.server.port:50054}")
    private int port;

    @Bean
    public Server userGrpcServer(UserServiceGrpcImpl userService) throws IOException {
        Server server = ServerBuilder.forPort(port)
                .addService(userService)
                .build()
                .start();
        
        log.info("User gRPC server started on port {}", port);
        
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutting down User gRPC server...");
            server.shutdown();
        }));
        
        return server;
    }
}
