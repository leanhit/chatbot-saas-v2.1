#!/bin/bash

# Health check script for Chatbot SaaS v2.1
# Returns 0 if healthy, 1 if unhealthy

HEALTH_URL="http://localhost:8080/actuator/health"
METRICS_URL="http://localhost:8080/actuator/metrics"
TIMEOUT=10

# Check if the application is responding
response=$(curl -s -o /dev/null -w "%{http_code}" --connect-timeout $TIMEOUT "$HEALTH_URL")

if [ "$response" -eq 200 ]; then
    # Check health endpoint content
    health_status=$(curl -s --connect-timeout $TIMEOUT "$HEALTH_URL" | grep -o '"status":"[^"]*"' | cut -d'"' -f4)
    
    if [ "$health_status" = "UP" ]; then
        # Check if metrics endpoint is accessible
        metrics_response=$(curl -s -o /dev/null -w "%{http_code}" --connect-timeout $TIMEOUT "$METRICS_URL")
        
        if [ "$metrics_response" -eq 200 ]; then
            echo "Application is healthy (status: UP, metrics: accessible)"
            exit 0
        else
            echo "Application is running but metrics endpoint is not accessible (HTTP $metrics_response)"
            exit 1
        fi
    else
        echo "Application health status is: $health_status"
        exit 1
    fi
else
    echo "Application is not responding (HTTP $response)"
    exit 1
fi
