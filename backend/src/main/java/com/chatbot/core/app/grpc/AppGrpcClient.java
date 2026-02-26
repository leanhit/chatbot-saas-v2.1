package com.chatbot.core.app.grpc;

import com.chatbot.core.app.grpc.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class AppGrpcClient {
    
    @Value("${app.grpc.server.host:localhost}")
    private String grpcHost;
    
    @Value("${app.grpc.server.port:50054}")
    private int grpcPort;
    
    private ManagedChannel channel;
    private AppServiceGrpc.AppServiceBlockingStub blockingStub;
    private AppServiceGrpc.AppServiceStub asyncStub;
    
    public void init() {
        channel = ManagedChannelBuilder.forAddress(grpcHost, grpcPort)
            .usePlaintext()
            .keepAliveTime(30, TimeUnit.SECONDS)
            .keepAliveTimeout(5, TimeUnit.SECONDS)
            .keepAliveWithoutCalls(true)
            .build();
        
        blockingStub = AppServiceGrpc.newBlockingStub(channel);
        asyncStub = AppServiceGrpc.newStub(channel);
    }
    
    public void shutdown() {
        if (channel != null && !channel.isShutdown()) {
            try {
                channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                channel.shutdownNow();
            }
        }
    }
    
    public AppServiceGrpc.AppServiceBlockingStub getBlockingStub() {
        if (blockingStub == null) {
            init();
        }
        return blockingStub;
    }
    
    public AppServiceGrpc.AppServiceStub getAsyncStub() {
        if (asyncStub == null) {
            init();
        }
        return asyncStub;
    }
    
    // Convenience methods for common operations
    public RegisterAppResponse registerApp(RegisterAppRequest request) {
        return getBlockingStub().registerApp(request);
    }
    
    public GetAppResponse getApp(GetAppRequest request) {
        return getBlockingStub().getApp(request);
    }
    
    public SearchAppsResponse searchApps(SearchAppsRequest request) {
        return getBlockingStub().searchApps(request);
    }
    
    public SubscribeAppResponse subscribeToApp(SubscribeAppRequest request) {
        return getBlockingStub().subscribeToApp(request);
    }
    
    public GetSubscriptionResponse getSubscription(GetSubscriptionRequest request) {
        return getBlockingStub().getSubscription(request);
    }
    
    public EvaluateGuardResponse evaluateGuard(EvaluateGuardRequest request) {
        return getBlockingStub().evaluateGuard(request);
    }
}
