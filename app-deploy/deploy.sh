#!/usr/bin/env bash

set -e

echo "=================================================="
echo "      Deploying Chatbot SaaS Application          "
echo "=================================================="

# Move to script directory
cd "$(dirname "$0")"

# 1. Check if infrastructure network exists
if ! docker network inspect chatbot_saas_internal >/dev/null 2>&1; then
    echo "ERROR: Network 'chatbot_saas_internal' not found!"
    echo "Please start the infrastructure stack first:"
    echo "  cd ../app-setup && docker compose up -d"
    exit 1
fi

# 2. Build and start app container
echo "Building and starting chatbot-app container..."
docker compose up -d --build

echo ""
echo "=================================================="
echo "      Deployment Completed Successfully!          "
echo "=================================================="
echo "Access points:"
echo "  - Frontend Dashboard: http://localhost"
echo "  - Backend API: http://localhost/api"
echo "  - Health Check: http://localhost/health"
echo "=================================================="
