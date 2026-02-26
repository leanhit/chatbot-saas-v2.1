package com.chatbot.core.user.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * User gRPC Client - For calling User Hub from other hubs
 * NOTE: Disabled to prevent port binding conflicts with UserGrpcServer
 */
// @Component
// @DependsOn("userGrpcServer")
@Slf4j
public class UserGrpcClient {

    @Value("${user.grpc.server.host:localhost}")
    private String host;

    @Value("${user.grpc.server.port:50052}")
    private int port;

    private ManagedChannel channel;

    @PostConstruct
    public void init() {
        channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();
        log.info("User gRPC client initialized: {}:{}", host, port);
    }

    @PreDestroy
    public void cleanup() {
        if (channel != null) {
            channel.shutdown();
        }
    }

    // TODO: Implement client methods after proto compilation
    // public boolean validateUser(String userId) { ... }
    // public UserProfileResponse getUserProfile(String userId) { ... }
}
