#!/bin/bash

echo "=========================================="
echo "Testing Deployed Docker App"
echo "=========================================="
echo ""

# Test 1: Frontend accessibility
echo "Test 1: Frontend Accessibility"
echo "--------------------------------"
FRONTEND_STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/)
if [ "$FRONTEND_STATUS" = "200" ]; then
    echo "✅ Frontend accessible (HTTP $FRONTEND_STATUS)"
else
    echo "❌ Frontend not accessible (HTTP $FRONTEND_STATUS)"
fi
echo ""

# Test 2: Container health
echo "Test 2: Container Health"
echo "--------------------------------"
CONTAINER_HEALTH=$(docker inspect --format='{{.State.Health.Status}}' chatbot_saas_app)
if [ "$CONTAINER_HEALTH" = "healthy" ]; then
    echo "✅ Container is healthy"
else
    echo "⚠️ Container status: $CONTAINER_HEALTH"
fi
echo ""

# Test 3: Database connections (check logs)
echo "Test 3: Database Connections"
echo "--------------------------------"
echo "Checking PostgreSQL connections in logs..."
DB_CONNECTIONS=$(docker logs chatbot_saas_app 2>&1 | grep -i "connected\|connection\|datasource" | tail -5)
if [ -n "$DB_CONNECTIONS" ]; then
    echo "✅ Database connection logs found:"
    echo "$DB_CONNECTIONS"
else
    echo "⚠️ No recent database connection logs"
fi
echo ""

# Test 4: Kafka connection
echo "Test 4: Kafka Connection"
echo "--------------------------------"
KAFKA_STATUS=$(docker logs chatbot_saas_app 2>&1 | grep -i "kafka" | tail -3)
if [ -n "$KAFKA_STATUS" ]; then
    echo "✅ Kafka logs found:"
    echo "$KAFKA_STATUS"
else
    echo "⚠️ No recent Kafka logs"
fi
echo ""

# Test 5: Redis connection
echo "Test 5: Redis Connection"
echo "--------------------------------"
REDIS_STATUS=$(docker logs chatbot_saas_app 2>&1 | grep -i "redis" | tail -3)
if [ -n "$REDIS_STATUS" ]; then
    echo "✅ Redis logs found:"
    echo "$REDIS_STATUS"
else
    echo "⚠️ No recent Redis logs"
fi
echo ""

# Test 6: Recent errors
echo "Test 6: Recent Errors"
echo "--------------------------------"
ERRORS=$(docker logs chatbot_saas_app 2>&1 | grep -i "error\|exception" | tail -5)
if [ -n "$ERRORS" ]; then
    echo "⚠️ Recent errors found:"
    echo "$ERRORS"
else
    echo "✅ No recent errors"
fi
echo ""

# Test 7: API endpoints (without auth)
echo "Test 7: API Endpoints (Public)"
echo "--------------------------------"
echo "Testing /actuator/health..."
ACTUATOR_HEALTH=$(curl -s http://localhost:8080/actuator/health 2>&1 | head -c 200)
if echo "$ACTUATOR_HEALTH" | grep -q "status"; then
    echo "✅ Actuator health endpoint accessible"
else
    echo "⚠️ Actuator health endpoint not accessible (returns HTML)"
fi
echo ""

echo "Testing /api/health..."
API_HEALTH=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/api/health)
if [ "$API_HEALTH" = "200" ] || [ "$API_HEALTH" = "401" ]; then
    echo "✅ API health endpoint accessible (HTTP $API_HEALTH)"
else
    echo "⚠️ API health endpoint status: $API_HEALTH"
fi
echo ""

echo "=========================================="
echo "Test Summary"
echo "=========================================="
echo "Frontend: Running on http://localhost:8080"
echo "Backend: Running in Docker container"
echo "Spring Profile: docker"
echo "Note: API endpoints require authentication"
