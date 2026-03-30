#!/bin/bash

echo "🔄 Force reinitializing packages with English text..."

# Call the new force-reinitialize endpoint
curl -X POST http://localhost:8080/api/v1/packages/force-reinitialize \
  -H "Content-Type: application/json" \
  2>/dev/null

echo "✅ Packages reinitialized with English text!"
echo "🌐 Now refresh your browser and check the payment page."
