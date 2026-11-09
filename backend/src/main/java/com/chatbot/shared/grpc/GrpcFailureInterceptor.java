package com.chatbot.shared.grpc;

import io.grpc.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Server interceptor that records non-OK gRPC calls into grpc_call_failures.
 * Register on gRPC ServerBuilder via .intercept(...).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class GrpcFailureInterceptor implements ServerInterceptor {

    private final GrpcFailureRecorder failureRecorder;

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {

        String method = call.getMethodDescriptor().getFullMethodName();
        ServerCall<ReqT, RespT> wrappedCall = new ForwardingServerCall.SimpleForwardingServerCall<>(call) {
            @Override
            public void close(Status status, Metadata trailers) {
                if (!status.isOk()) {
                    log.warn("gRPC call failed: {} -> {} ({})", method, status.getCode(), status.getDescription());
                    failureRecorder.record(method, status);
                }
                super.close(status, trailers);
            }
        };

        return next.startCall(wrappedCall, headers);
    }
}
