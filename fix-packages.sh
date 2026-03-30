#!/bin/bash

echo "🔄 Reinitializing packages with English text..."

# Force reinitialize packages (this will delete existing packages and create new ones)
curl -X POST http://localhost:8080/api/v1/packages/initialize \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  2>/dev/null

echo "✅ Done! Now restart frontend and clear browser cache."
