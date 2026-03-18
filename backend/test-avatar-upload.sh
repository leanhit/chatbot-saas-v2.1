#!/bin/bash

# Test Avatar Upload API with Auto Bucket Creation
echo "=== Testing Avatar Upload API ==="

# Base URL
BASE_URL="http://localhost:8080"

# Test 1: Check/Create Avatar Buckets
echo "1. Checking/Creating Avatar Buckets..."
curl -X POST "$BASE_URL/api/avatars/check-buckets" \
  -H "Content-Type: application/json" \
  | jq '.' || echo "Bucket check failed"

echo ""

# Test 2: Upload User Avatar (requires authentication)
echo "2. Testing User Avatar Upload..."
echo "Note: This requires valid JWT token. For testing without auth, we'll get an error."

curl -X POST "$BASE_URL/api/avatars/user/avatar" \
  -H "Content-Type: multipart/form-data" \
  -F "file=@/dev/null" \
  2>/dev/null | head -c 200
echo ""

# Test 3: Upload Tenant Logo (requires tenant context)
echo "3. Testing Tenant Logo Upload..."
echo "Note: This requires tenant context. For testing without context, we'll get an error."

curl -X POST "$BASE_URL/api/avatars/tenant/logo" \
  -H "Content-Type: multipart/form-data" \
  -F "file=@/dev/null" \
  2>/dev/null | head -c 200
echo ""

echo ""
echo "=== Test Complete ==="
echo ""
echo "Expected behavior:"
echo "- Bucket check should succeed and create buckets if needed"
echo "- User avatar upload should fail without authentication (401/403)"
echo "- Tenant logo upload should fail without tenant context (400/500)"
echo ""
echo "To test with real files:"
echo "1. Create a test image: cp /path/to/image.jpg test-avatar.jpg"
echo "2. Upload with: curl -X POST ... -F \"file=@test-avatar.jpg\""
