package com.chatbot.config;

import io.micrometer.tracing.Tracer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;

/**
 * Distributed Tracing Configuration with W3C TraceContext
 * Enables automatic trace context propagation across HTTP, gRPC, and async boundaries
 */
@Configuration
public class TracingConfig {

    /**
     * Add tracing interceptor to RestTemplate for HTTP client tracing
     */
    @Bean
    public ClientHttpRequestInterceptor tracingInterceptor(Tracer tracer) {
        return (request, body, execution) -> {
            io.micrometer.tracing.Span currentSpan = tracer.currentSpan();
            if (currentSpan != null) {
                String traceId = currentSpan.context().traceId();
                String spanId = currentSpan.context().spanId();
                if (traceId != null && spanId != null) {
                    // Add trace headers to outgoing HTTP requests
                    request.getHeaders().add("traceparent", 
                        "00-" + traceId + "-" + spanId + "-01");
                }
            }
            return execution.execute(request, body);
        };
    }
}
