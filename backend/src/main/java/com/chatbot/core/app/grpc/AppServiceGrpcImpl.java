package com.chatbot.core.app.grpc;

import com.chatbot.core.app.grpc.AppGrpcMessage;
import com.chatbot.core.app.grpc.AppServiceGrpc;
import com.chatbot.core.app.grpc.EvaluateGuardRequest;
import com.chatbot.core.app.grpc.EvaluateGuardResponse;
import com.chatbot.core.app.grpc.GetAppRequest;
import com.chatbot.core.app.grpc.GetAppResponse;
import com.chatbot.core.app.grpc.GetSubscriptionRequest;
import com.chatbot.core.app.grpc.GetSubscriptionResponse;
import com.chatbot.core.app.grpc.RegisterAppRequest;
import com.chatbot.core.app.grpc.RegisterAppResponse;
import com.chatbot.core.app.grpc.SearchAppsRequest;
import com.chatbot.core.app.grpc.SearchAppsResponse;
import com.chatbot.core.app.grpc.SubscribeAppRequest;
import com.chatbot.core.app.grpc.SubscribeAppResponse;
import com.chatbot.core.app.grpc.SubscriptionGrpcMessage;
import com.chatbot.core.app.registry.dto.AppResponse;
import com.chatbot.core.app.registry.service.AppRegistryService;
import com.chatbot.core.app.subscription.dto.SubscriptionResponse;
import com.chatbot.core.app.subscription.service.AppSubscriptionService;
import com.chatbot.core.app.guard.service.AppGuardService;
import io.grpc.stub.StreamObserver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.logging.Logger;

@Service
public class AppServiceGrpcImpl extends AppServiceGrpc.AppServiceImplBase {
    
    private static final Logger logger = Logger.getLogger(AppServiceGrpcImpl.class.getName());
    
    @Autowired
    private AppRegistryService appRegistryService;
    
    @Autowired
    private AppSubscriptionService subscriptionService;
    
    @Autowired
    private AppGuardService guardService;
    
    @Override
    public void registerApp(RegisterAppRequest request, StreamObserver<RegisterAppResponse> responseObserver) {
        try {
            // Convert gRPC request to DTO
            com.chatbot.core.app.registry.dto.RegisterAppRequest dto = convertToRegisterAppRequest(request);
            
            // Register app
            AppResponse appResponse = appRegistryService.registerApp(dto, request.getUserId());
            
            // Convert response to gRPC
            RegisterAppResponse response = convertToRegisterAppGrpcResponse(appResponse);
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL
                .withDescription(e.getMessage())
                .asRuntimeException());
        }
    }
    
    @Override
    public void getApp(GetAppRequest request, StreamObserver<GetAppResponse> responseObserver) {
        try {
            AppResponse appResponse = appRegistryService.getAppById(request.getAppId());
            GetAppResponse response = convertToGetAppGrpcResponse(appResponse);
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL
                .withDescription(e.getMessage())
                .asRuntimeException());
        }
    }
    
    @Override
    public void searchApps(SearchAppsRequest request, StreamObserver<SearchAppsResponse> responseObserver) {
        try {
            List<AppResponse> apps = appRegistryService.searchApps(
                request.getNameFilter(),
                request.getAppType(),
                request.getStatus(),
                request.getIsActive(),
                org.springframework.data.domain.PageRequest.of(0, 100)
            ).getContent();
            
            SearchAppsResponse response = convertToSearchAppsGrpcResponse(apps);
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL
                .withDescription(e.getMessage())
                .asRuntimeException());
        }
    }
    
    @Override
    public void subscribeToApp(SubscribeAppRequest request, StreamObserver<SubscribeAppResponse> responseObserver) {
        try {
            com.chatbot.core.app.subscription.dto.SubscribeAppRequest dto = convertToSubscribeAppRequest(request);
            SubscriptionResponse subscriptionResponse = subscriptionService.subscribeToApp(dto, request.getUserId());
            
            SubscribeAppResponse response = convertToSubscribeAppGrpcResponse(subscriptionResponse);
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL
                .withDescription(e.getMessage())
                .asRuntimeException());
        }
    }
    
    @Override
    public void getSubscription(GetSubscriptionRequest request, StreamObserver<GetSubscriptionResponse> responseObserver) {
        try {
            SubscriptionResponse subscriptionResponse = subscriptionService.getSubscriptionById(request.getSubscriptionId());
            GetSubscriptionResponse response = convertToGetSubscriptionGrpcResponse(subscriptionResponse);
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL
                .withDescription(e.getMessage())
                .asRuntimeException());
        }
    }
    
    @Override
    public void evaluateGuard(EvaluateGuardRequest request, StreamObserver<EvaluateGuardResponse> responseObserver) {
        try {
            boolean result = guardService.evaluateGuard(request.getAppId(), request.getContext(), request.getRequestData());
            
            EvaluateGuardResponse response = EvaluateGuardResponse.newBuilder()
                .setAllowed(result)
                .setMessage(result ? "Access allowed" : "Access denied")
                .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL
                .withDescription(e.getMessage())
                .asRuntimeException());
        }
    }
    
    // Helper methods for conversion between DTOs and gRPC messages
    private com.chatbot.core.app.registry.dto.RegisterAppRequest convertToRegisterAppRequest(RegisterAppRequest request) {
        com.chatbot.core.app.registry.dto.RegisterAppRequest dto = new com.chatbot.core.app.registry.dto.RegisterAppRequest();
        dto.setName(request.getName());
        dto.setDisplayName(request.getDisplayName());
        dto.setDescription(request.getDescription());
        dto.setAppType(com.chatbot.core.app.registry.model.AppType.valueOf(request.getAppType()));
        dto.setVersion(request.getVersion());
        dto.setApiEndpoint(request.getApiEndpoint());
        dto.setWebhookUrl(request.getWebhookUrl());
        dto.setConfigSchema(request.getConfigSchema());
        dto.setDefaultConfig(request.getDefaultConfig());
        dto.setIsPublic(request.getIsPublic());
        return dto;
    }
    
    private RegisterAppResponse convertToRegisterAppGrpcResponse(AppResponse appResponse) {
        return RegisterAppResponse.newBuilder()
            .setApp(convertToAppGrpcMessage(appResponse))
            .setMessage("App registered successfully")
            .build();
    }
    
    private GetAppResponse convertToGetAppGrpcResponse(AppResponse appResponse) {
        return GetAppResponse.newBuilder()
            .setApp(convertToAppGrpcMessage(appResponse))
            .build();
    }
    
    private SearchAppsResponse convertToSearchAppsGrpcResponse(List<AppResponse> apps) {
        List<AppGrpcMessage> appMessages = apps.stream()
            .map(this::convertToAppGrpcMessage)
            .collect(Collectors.toList());
        
        return SearchAppsResponse.newBuilder()
            .addAllApps(appMessages)
            .setTotalCount(apps.size())
            .build();
    }
    
    private com.chatbot.core.app.subscription.dto.SubscribeAppRequest convertToSubscribeAppRequest(SubscribeAppRequest request) {
        com.chatbot.core.app.subscription.dto.SubscribeAppRequest dto = new com.chatbot.core.app.subscription.dto.SubscribeAppRequest();
        dto.setAppId(request.getAppId());
        dto.setTenantId(request.getTenantId());
        dto.setSubscriptionPlan(com.chatbot.core.app.subscription.model.SubscriptionPlan.valueOf(request.getSubscriptionPlan()));
        dto.setAutoRenew(request.getAutoRenew());
        return dto;
    }
    
    private SubscribeAppResponse convertToSubscribeAppGrpcResponse(SubscriptionResponse subscriptionResponse) {
        return SubscribeAppResponse.newBuilder()
            .setSubscription(convertToSubscriptionGrpcMessage(subscriptionResponse))
            .setMessage("Subscription created successfully")
            .build();
    }
    
    private GetSubscriptionResponse convertToGetSubscriptionGrpcResponse(SubscriptionResponse subscriptionResponse) {
        return GetSubscriptionResponse.newBuilder()
            .setSubscription(convertToSubscriptionGrpcMessage(subscriptionResponse))
            .build();
    }
    
    private AppGrpcMessage convertToAppGrpcMessage(AppResponse appResponse) {
        AppGrpcMessage.Builder builder = AppGrpcMessage.newBuilder()
            .setId(appResponse.getId())
            .setName(appResponse.getName())
            .setDisplayName(appResponse.getDisplayName())
            .setDescription(appResponse.getDescription())
            .setAppType(appResponse.getAppType().toString())
            .setStatus(appResponse.getStatus().toString())
            .setIsActive(appResponse.getIsActive())
            .setIsPublic(appResponse.getIsPublic());
        
        if (appResponse.getVersion() != null) {
            builder.setVersion(appResponse.getVersion());
        }
        if (appResponse.getApiEndpoint() != null) {
            builder.setApiEndpoint(appResponse.getApiEndpoint());
        }
        if (appResponse.getWebhookUrl() != null) {
            builder.setWebhookUrl(appResponse.getWebhookUrl());
        }
        
        return builder.build();
    }
    
    private SubscriptionGrpcMessage convertToSubscriptionGrpcMessage(SubscriptionResponse subscriptionResponse) {
        SubscriptionGrpcMessage.Builder builder = SubscriptionGrpcMessage.newBuilder()
            .setId(subscriptionResponse.getId())
            .setAppId(subscriptionResponse.getApp().getId())
            .setTenantId(subscriptionResponse.getTenantId())
            .setUserId(subscriptionResponse.getUserId())
            .setSubscriptionPlan(subscriptionResponse.getSubscriptionPlan().toString())
            .setSubscriptionStatus(subscriptionResponse.getSubscriptionStatus().toString())
            .setAutoRenew(subscriptionResponse.getAutoRenew());
        
        if (subscriptionResponse.getSubscriptionStart() != null) {
            builder.setSubscriptionStart(subscriptionResponse.getSubscriptionStart().toString());
        }
        if (subscriptionResponse.getSubscriptionEnd() != null) {
            builder.setSubscriptionEnd(subscriptionResponse.getSubscriptionEnd().toString());
        }
        if (subscriptionResponse.getTrialEnd() != null) {
            builder.setTrialEnd(subscriptionResponse.getTrialEnd().toString());
        }
        
        return builder.build();
    }
}
