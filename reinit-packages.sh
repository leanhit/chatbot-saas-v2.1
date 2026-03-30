#!/bin/bash

echo "🔄 Reinitializing packages with English text..."

# Wait for backend to start
echo "⏳ Waiting for backend to start..."
sleep 30

# Call the initialize endpoint
curl -X POST http://localhost:8080/api/v1/packages/initialize \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  2>/dev/null || echo "❌ Failed to initialize packages"

echo "✅ Package reinitialization completed!"
