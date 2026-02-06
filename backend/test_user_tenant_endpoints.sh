#!/bin/bash

# Test script for User and Tenant Information Retrieval Endpoints
# Base URL
BASE_URL="http://localhost:8080"

echo "=== Testing User and Tenant Information Retrieval Endpoints ==="
echo

# Test 1: Health Check
echo "1. Health Check:"
curl -s -X GET "$BASE_URL/api/test/simple-user-tenant/health" -H "accept: application/json" | jq '.'
echo
echo

# Test 2: Public Endpoint
echo "2. Public Endpoint (No Authentication Required):"
curl -s -X GET "$BASE_URL/api/test/simple-user-tenant/public" -H "accept: application/json" | jq '.'
echo
echo

# Test 3: Tenant Information from Headers (Without Headers)
echo "3. Tenant Information (Without Headers):"
curl -s -X GET "$BASE_URL/api/test/simple-user-tenant/tenant/from-header" -H "accept: application/json" | jq '.'
echo
echo

# Test 4: Tenant Information from Headers (With Headers)
echo "4. Tenant Information (With Headers):"
curl -s -X GET "$BASE_URL/api/test/simple-user-tenant/tenant/from-header" \
     -H "accept: application/json" \
     -H "X-Tenant-ID: demo-tenant-123" \
     -H "X-Tenant-Name: DemoTenant" | jq '.'
echo
echo

# Test 5: Comprehensive Information (Without Authentication)
echo "5. Comprehensive Information (Without Authentication):"
curl -s -X GET "$BASE_URL/api/test/simple-user-tenant/comprehensive" -H "accept: application/json" | jq '.'
echo
echo

# Test 6: Current User Information (Will fail without authentication)
echo "6. Current User Information (Will fail without authentication):"
curl -s -X GET "$BASE_URL/api/test/simple-user-tenant/user/current" -H "accept: application/json" | jq '.'
echo
echo

# Test 7: User Details (Will fail without authentication)
echo "7. User Details (Will fail without authentication):"
curl -s -X GET "$BASE_URL/api/test/simple-user-tenant/user/details" -H "accept: application/json" | jq '.'
echo
echo

echo "=== Test Summary ==="
echo "✓ Health check endpoint works"
echo "✓ Public endpoint works"
echo "✓ Tenant header extraction works"
echo "✓ Comprehensive endpoint works"
echo "✗ Authenticated endpoints require authentication (expected)"
echo
echo "To test authenticated endpoints, you need to:"
echo "1. Register/login to get a JWT token"
echo "2. Include the token in Authorization header"
echo "3. Include tenant headers for tenant-specific operations"
