# Distributed Tracing Implementation Summary

## Overview
Implemented comprehensive distributed tracing with Micrometer Tracing (Spring Boot 3.4+) using W3C TraceContext standard. This enables automatic trace context propagation across HTTP, gRPC, and async boundaries, with traceId and spanId automatically included in all log statements via Logback MDC.

## Changes Made

### 1. Dependencies (build.gradle)
Added Micrometer Tracing dependencies:
```gradle
implementation 'io.micrometer:micrometer-tracing-bridge-brave'
implementation 'io.micrometer:micrometer-tracing'
implementation 'io.zipkin.reporter2:zipkin-reporter-brave'
```

### 2. Tracing Configuration (TracingConfig.java)
Created comprehensive tracing configuration:
- **W3C TraceContext Propagator**: Standard W3C traceparent header format
- **Observation Handlers**: For trace context propagation across boundaries
- **Thread Local Accessor**: For async trace context propagation
- **HTTP Interceptor**: Automatic trace context injection into HTTP requests

### 3. gRPC Tracing Interceptor (GrpcTracingInterceptor.java)
Created gRPC interceptor for distributed tracing:
- **Client Interceptor**: Injects trace context into gRPC metadata
- **Server Interceptor**: Extracts trace context from incoming gRPC calls
- **W3C Format**: Uses standard traceparent header format

### 4. MDC Filter (TracingMdcFilter.java)
Created servlet filter for Logback MDC population:
- **Highest Priority**: Runs before all other filters
- **MDC Population**: Adds traceId and spanId to MDC for each request
- **Automatic Cleanup**: Removes MDC values after request completion
- **Memory Safe**: Prevents MDC memory leaks

### 5. Logback Configuration (logback-spring.xml)
Updated logging pattern to include trace context:
```xml
<pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} [traceId=%X{traceId}, spanId=%X{spanId}] - %msg%n</pattern>
```

### 6. Application Properties (application.properties)
Added tracing configuration:
```properties
management.tracing.sampling.probability=1.0
management.tracing.enabled=true
management.tracing.propagation.type=w3c
management.zipkin.tracing.endpoint=http://localhost:9411/api/v2/spans
management.metrics.tracing.enabled=true
management.otlp.tracing.endpoint=http://localhost:4318/v1/spans
```

## Trace Context Flow

### HTTP Request Flow
```
Client Request (traceparent header)
    ↓
API Gateway / Load Balancer
    ↓
Spring Boot Application (TracingMdcFilter)
    ↓
MDC: traceId, spanId populated
    ↓
Controller → Service → Repository
    ↓
All logs include: [traceId=xxx, spanId=yyy]
```

### gRPC Request Flow
```
Client Application
    ↓
GrpcTracingInterceptor (Client)
    ↓
Injects traceparent into gRPC metadata
    ↓
gRPC Server
    ↓
GrpcTracingInterceptor (Server)
    ↓
Extracts trace context
    ↓
MDC: traceId, spanId populated
    ↓
All logs include: [traceId=xxx, spanId=yyy]
```

### Async Worker Flow
```
HTTP Request
    ↓
Controller starts async task
    ↓
ObservationThreadLocalAccessor preserves context
    ↓
Async Worker Thread
    ↓
MDC: traceId, spanId still available
    ↓
All logs include: [traceId=xxx, spanId=yyy]
```

## W3C TraceContext Format

### traceparent Header
```
traceparent: 00-{traceId}-{spanId}-{flags}
```

**Example:**
```
traceparent: 00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-01
```

**Components:**
- `00`: Version
- `4bf92f3577b34da6a3ce929d0e0e4736`: Trace ID (16-byte hex)
- `00f067aa0ba902b7`: Span ID (8-byte hex)
- `01`: Trace flags (01 = sampled)

## Log Output Examples

### Console Output (Development)
```
2026-08-19 17:50:00.123 [http-nio-8080-exec-1] INFO  c.c.c.UserController [traceId=4bf92f3577b34da6a3ce929d0e0e4736, spanId=00f067aa0ba902b7] - Processing user request
2026-08-19 17:50:00.125 [http-nio-8080-exec-1] INFO  c.c.c.UserService [traceId=4bf92f3577b34da6a3ce929d0e0e4736, spanId=00f067aa0ba902b7] - Fetching user data
2026-08-19 17:50:00.130 [http-nio-8080-exec-1] INFO  c.c.c.UserRepository [traceId=4bf92f3577b34da6a3ce929d0e0e4736, spanId=00f067aa0ba902b7] - Executing query
```

### JSON Output (Production)
```json
{
  "@timestamp": "2026-08-19T17:50:00.123+07:00",
  "app": "chatbot-saas",
  "profile": "production",
  "level": "INFO",
  "logger": "com.chatbot.controller.UserController",
  "traceId": "4bf92f3577b34da6a3ce929d0e0e4736",
  "spanId": "00f067aa0ba902b7",
  "message": "Processing user request"
}
```

## Integration with Observability Tools

### Grafana Loki
```log
{app="chatbot-saas"} |= `traceId="4bf92f3577b34da6a3ce929d0e0e4736"`
```

### ELK Stack
```json
{
  "query": {
    "bool": {
      "must": [
        { "match": { "traceId": "4bf92f3577b34da6a3ce929d0e0e4736" } }
      ]
    }
  }
}
```

### Zipkin
Access Zipkin UI: http://localhost:9411
- Search by trace ID
- View service dependency graph
- Analyze latency distribution

### Jaeger (OTLP)
Access Jaeger UI: http://localhost:16686
- Configure OTLP endpoint: http://localhost:4318
- Search traces by trace ID
- View distributed trace timeline

## Usage Examples

### Manual Trace Context Access
```java
@Autowired
private Tracer tracer;

public void someMethod() {
    Span currentSpan = tracer.currentSpan();
    if (currentSpan != null) {
        String traceId = currentSpan.context().traceId();
        String spanId = currentSpan.context().spanId();
        log.info("Current trace: {}, span: {}", traceId, spanId);
    }
}
```

### Custom Span Creation
```java
@Autowired
private Tracer tracer;

public void processOrder() {
    Span span = tracer.nextSpan().name("process-order").start();
    
    try (Tracer.SpanInScope ws = tracer.withSpan(span)) {
        // Business logic here
        log.info("Processing order");
        
    } finally {
        span.end();
    }
}
```

### Adding Baggage
```java
@Autowired
private Tracer tracer;

public void someMethod() {
    Span currentSpan = tracer.currentSpan();
    if (currentSpan != null) {
        currentSpan.tag("userId", "12345");
        currentSpan.tag("tenantId", "tenant-001");
    }
}
```

## Configuration Options

### Sampling Rate
Adjust sampling probability in application.properties:
```properties
# Sample 10% of traces
management.tracing.sampling.probability=0.1

# Sample all traces (development)
management.tracing.sampling.probability=1.0
```

### Propagation Type
Change propagation format if needed:
```properties
# W3C (standard)
management.tracing.propagation.type=w3c

# B3 (Zipkin)
management.tracing.propagation.type=b3
```

### Export Endpoints
Configure different tracing backends:
```properties
# Zipkin
management.zipkin.tracing.endpoint=http://zipkin:9411/api/v2/spans

# OTLP (Jaeger/Tempo)
management.otlp.tracing.endpoint=http://jaeger:4318/v1/spans

# Wavefront
management.wavefront.tracing.endpoint=https://wavefront:2841
```

## Benefits

1. **End-to-End Visibility**: Track requests across all service boundaries
2. **Performance Analysis**: Identify bottlenecks in distributed systems
3. **Error Debugging**: Correlate errors across services using trace ID
4. **Log Correlation**: Search logs by trace ID in Grafana Loki/ELK
5. **Standard Compliance**: Uses W3C TraceContext standard
6. **Zero Code Changes**: Automatic propagation for most cases
7. **gRPC Support**: Full trace context propagation in gRPC calls
8. **Async Safety**: Thread-local context preserved across async boundaries

## Troubleshooting

### Missing traceId in Logs
- Verify TracingMdcFilter is registered (highest priority)
- Check tracer bean is properly configured
- Ensure tracing is enabled in application.properties
- Verify logback pattern includes %X{traceId}

### Broken Trace Context
- Check propagation type matches between services
- Verify W3C format is supported by all services
- Ensure interceptors are properly registered
- Check for middleware that strips headers

### High Memory Usage
- Reduce sampling probability
- Adjust MDC cleanup in TracingMdcFilter
- Monitor span creation rate
- Check for span leaks (unclosed spans)

### Performance Impact
- Tracing adds minimal overhead (<5%)
- Adjust sampling rate for high-traffic systems
- Use async logging to reduce impact
- Monitor Zipkin/Jaeger export latency

## Files Modified

1. `backend/build.gradle` - Added Micrometer Tracing dependencies
2. `backend/src/main/java/com/chatbot/configs/TracingConfig.java` - Tracing configuration (new)
3. `backend/src/main/java/com/chatbot/configs/GrpcTracingInterceptor.java` - gRPC tracing (new)
4. `backend/src/main/java/com/chatbot/configs/TracingMdcFilter.java` - MDC filter (new)
5. `backend/src/main/resources/logback-spring.xml` - Updated logging pattern
6. `backend/src/main/resources/application.properties` - Tracing configuration

## Next Steps

1. Test trace context propagation across services
2. Set up Zipkin/Jaeger for trace visualization
3. Configure Grafana Loki dashboards with trace ID search
4. Set up alerting on error traces
5. Implement custom span names for business operations
6. Add baggage for tenant/user context
7. Configure sampling rates per environment
8. Set up trace retention policies

## Security Considerations

- Trace IDs may contain sensitive information
- Consider encryption for trace headers in production
- Restrict access to tracing dashboards
- Sanitize sensitive data from span tags
- Implement trace context authentication for cross-tenant systems
