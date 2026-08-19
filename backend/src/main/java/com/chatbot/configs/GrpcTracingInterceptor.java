package com.chatbot.configs;

import io.grpc.*;
import io.micrometer.tracing.Tracer;
import org.springframework.stereotype.Component;

/**
 * gRPC Interceptor for distributed tracing
 * Propagates trace context via gRPC metadata
 */
@Component
public class GrpcTracingInterceptor implements ClientInterceptor, ServerInterceptor {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(GrpcTracingInterceptor.class);

    private final Tracer tracer;

    public GrpcTracingInterceptor(Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
            MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next) {
        
        return new ForwardingClientCall.SimpleForwardingClientCall<>(next.newCall(method, callOptions)) {
            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                // Inject trace context into gRPC metadata
                io.micrometer.tracing.Span currentSpan = tracer.currentSpan();
                if (currentSpan != null) {
                    String traceId = currentSpan.context().traceId();
                    String spanId = currentSpan.context().spanId();
                    if (traceId != null && spanId != null) {
                        // W3C traceparent format: 00-{traceId}-{spanId}-{flags}
                        String traceparent = "00-" + traceId + "-" + spanId + "-01";
                        headers.put(Metadata.Key.of("traceparent", Metadata.ASCII_STRING_MARSHALLER), traceparent);
                    }
                }
                
                super.start(responseListener, headers);
            }
        };
    }

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
        
        // Extract trace context from gRPC metadata
        String traceparent = headers.get(Metadata.Key.of("traceparent", Metadata.ASCII_STRING_MARSHALLER));
        if (traceparent != null) {
            // Parse W3C traceparent format
            String[] parts = traceparent.split("-");
            if (parts.length >= 3) {
                String traceId = parts[1];
                String spanId = parts[2];
                
                log.debug("Extracted trace context from gRPC: traceId={}, spanId={}", traceId, spanId);
            }
        }
        
        return next.startCall(call, headers);
    }
}
