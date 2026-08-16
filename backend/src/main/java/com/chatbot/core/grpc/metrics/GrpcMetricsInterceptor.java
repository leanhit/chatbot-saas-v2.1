package com.chatbot.core.grpc.metrics;

import io.grpc.*;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * GrpcMetricsInterceptor - gRPC Client Interceptor for Metrics Collection
 * 
 * Tracks gRPC call metrics including:
 * - Latency (response time)
 * - Success/failure rates
 * - gRPC status codes (OK, UNAVAILABLE, DEADLINE_EXCEEDED, etc.)
 * 
 * Uses Micrometer for integration with Prometheus and other monitoring systems.
 */
@Component
@Slf4j
public class GrpcMetricsInterceptor implements ClientInterceptor {

    private final MeterRegistry meterRegistry;
    private final Timer grpcCallTimer;
    private final Counter grpcSuccessCounter;
    private final Counter grpcFailureCounter;
    private final Counter grpcUnavailableCounter;
    private final Counter grpcDeadlineExceededCounter;
    private final Counter grpcInternalErrorCounter;
    private final Counter grpcInvalidArgumentCounter;
    private final Counter grpcNotFoundCounter;
    private final Counter grpcPermissionDeniedCounter;
    private final Counter grpcUnauthenticatedCounter;
    private final Counter grpcResourceExhaustedCounter;

    public GrpcMetricsInterceptor(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        
        // Timer for measuring gRPC call latency
        this.grpcCallTimer = Timer.builder("grpc.client.calls.duration")
                .description("gRPC client call duration")
                .tag("service", "all")
                .register(meterRegistry);
        
        // Counters for different outcomes
        this.grpcSuccessCounter = Counter.builder("grpc.client.calls")
                .description("gRPC client calls")
                .tag("status", "OK")
                .register(meterRegistry);
        
        this.grpcFailureCounter = Counter.builder("grpc.client.calls")
                .description("gRPC client calls")
                .tag("status", "FAILED")
                .register(meterRegistry);
        
        this.grpcUnavailableCounter = Counter.builder("grpc.client.calls")
                .description("gRPC client calls")
                .tag("status", "UNAVAILABLE")
                .register(meterRegistry);
        
        this.grpcDeadlineExceededCounter = Counter.builder("grpc.client.calls")
                .description("gRPC client calls")
                .tag("status", "DEADLINE_EXCEEDED")
                .register(meterRegistry);
        
        this.grpcInternalErrorCounter = Counter.builder("grpc.client.calls")
                .description("gRPC client calls")
                .tag("status", "INTERNAL")
                .register(meterRegistry);
        
        this.grpcInvalidArgumentCounter = Counter.builder("grpc.client.calls")
                .description("gRPC client calls")
                .tag("status", "INVALID_ARGUMENT")
                .register(meterRegistry);
        
        this.grpcNotFoundCounter = Counter.builder("grpc.client.calls")
                .description("gRPC client calls")
                .tag("status", "NOT_FOUND")
                .register(meterRegistry);
        
        this.grpcPermissionDeniedCounter = Counter.builder("grpc.client.calls")
                .description("gRPC client calls")
                .tag("status", "PERMISSION_DENIED")
                .register(meterRegistry);
        
        this.grpcUnauthenticatedCounter = Counter.builder("grpc.client.calls")
                .description("gRPC client calls")
                .tag("status", "UNAUTHENTICATED")
                .register(meterRegistry);
        
        this.grpcResourceExhaustedCounter = Counter.builder("grpc.client.calls")
                .description("gRPC client calls")
                .tag("status", "RESOURCE_EXHAUSTED")
                .register(meterRegistry);
        
        log.info("✅ gRPC Metrics Interceptor initialized with Micrometer");
    }

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
            MethodDescriptor<ReqT, RespT> method,
            CallOptions callOptions,
            Channel next) {
        
        String fullMethodName = method.getFullMethodName();
        String serviceName = extractServiceName(fullMethodName);
        String methodName = extractMethodName(fullMethodName);
        
        return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(
                next.newCall(method, callOptions)) {
            
            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                Listener<RespT> metricsListener = new ForwardingClientCallListener.SimpleForwardingClientCallListener<RespT>(responseListener) {
                    private final long startTime = System.nanoTime();
                    
                    @Override
                    public void onClose(Status status, Metadata trailers) {
                        long duration = System.nanoTime() - startTime;
                        
                        // Record latency
                        grpcCallTimer.record(duration, TimeUnit.NANOSECONDS);
                        
                        // Record status-specific metrics
                        if (status.isOk()) {
                            grpcSuccessCounter.increment();
                            log.debug("✅ gRPC call succeeded: {} - {}ms", fullMethodName, 
                                    TimeUnit.NANOSECONDS.toMillis(duration));
                        } else {
                            grpcFailureCounter.increment();
                            
                            // Record specific status codes
                            switch (status.getCode()) {
                                case UNAVAILABLE:
                                    grpcUnavailableCounter.increment();
                                    log.warn("❌ gRPC call UNAVAILABLE: {} - {}", fullMethodName, status.getDescription());
                                    break;
                                case DEADLINE_EXCEEDED:
                                    grpcDeadlineExceededCounter.increment();
                                    log.warn("⏱️ gRPC call DEADLINE_EXCEEDED: {} - {}ms", fullMethodName, 
                                            TimeUnit.NANOSECONDS.toMillis(duration));
                                    break;
                                case INTERNAL:
                                    grpcInternalErrorCounter.increment();
                                    log.error("💥 gRPC call INTERNAL: {} - {}", fullMethodName, status.getDescription());
                                    break;
                                case INVALID_ARGUMENT:
                                    grpcInvalidArgumentCounter.increment();
                                    log.warn("⚠️ gRPC call INVALID_ARGUMENT: {} - {}", fullMethodName, status.getDescription());
                                    break;
                                case NOT_FOUND:
                                    grpcNotFoundCounter.increment();
                                    log.debug("🔍 gRPC call NOT_FOUND: {} - {}", fullMethodName, status.getDescription());
                                    break;
                                case PERMISSION_DENIED:
                                    grpcPermissionDeniedCounter.increment();
                                    log.warn("🚫 gRPC call PERMISSION_DENIED: {} - {}", fullMethodName, status.getDescription());
                                    break;
                                case UNAUTHENTICATED:
                                    grpcUnauthenticatedCounter.increment();
                                    log.warn("🔐 gRPC call UNAUTHENTICATED: {} - {}", fullMethodName, status.getDescription());
                                    break;
                                case RESOURCE_EXHAUSTED:
                                    grpcResourceExhaustedCounter.increment();
                                    log.warn("📊 gRPC call RESOURCE_EXHAUSTED: {} - {}", fullMethodName, status.getDescription());
                                    break;
                                default:
                                    log.warn("❓ gRPC call failed with status {}: {} - {}", status.getCode(), 
                                            fullMethodName, status.getDescription());
                            }
                        }
                        
                        super.onClose(status, trailers);
                    }
                };
                
                super.start(metricsListener, headers);
            }
        };
    }
    
    /**
     * Extract service name from full method name (e.g., "com.chatbot.user.UserService/GetUser" -> "UserService")
     */
    private String extractServiceName(String fullMethodName) {
        int lastSlashIndex = fullMethodName.lastIndexOf('/');
        if (lastSlashIndex > 0) {
            String servicePart = fullMethodName.substring(0, lastSlashIndex);
            int lastDotIndex = servicePart.lastIndexOf('.');
            if (lastDotIndex >= 0) {
                return servicePart.substring(lastDotIndex + 1);
            }
            return servicePart;
        }
        return "unknown";
    }
    
    /**
     * Extract method name from full method name (e.g., "com.chatbot.user.UserService/GetUser" -> "GetUser")
     */
    private String extractMethodName(String fullMethodName) {
        int lastSlashIndex = fullMethodName.lastIndexOf('/');
        if (lastSlashIndex >= 0 && lastSlashIndex < fullMethodName.length() - 1) {
            return fullMethodName.substring(lastSlashIndex + 1);
        }
        return "unknown";
    }
}
