#!/bin/bash
# Production Deployment Script for truyenthongviet.vn (103.149.99.7)

set -e

echo "=================================================="
echo "  Production Deployment - truyenthongviet.vn"
echo "=================================================="

# Check if running on production server
if [ ! -f /etc/letsencrypt/live/truyenthongviet.vn/fullchain.pem ]; then
    echo "❌ ERROR: Let's Encrypt certificate not found for truyenthongviet.vn"
    echo "   Expected: /etc/letsencrypt/live/truyenthongviet.vn/fullchain.pem"
    exit 1
fi

echo "✅ Let's Encrypt certificate found for truyenthongviet.vn"

# Check Docker
if ! command -v docker &> /dev/null; then
    echo "❌ ERROR: Docker not installed"
    exit 1
fi

# Check Docker Compose
if ! command -v docker-compose &> /dev/null; then
    echo "❌ ERROR: Docker Compose not installed"
    exit 1
fi

echo "✅ Docker and Docker Compose found"

# Stop existing container if running
echo "🛑 Stopping existing container..."
docker-compose down || true

# Build and start
echo "🔨 Building and starting container..."
docker-compose up -d --build

echo "=================================================="
echo "  Deployment Completed Successfully!"
echo "=================================================="
echo "Access points:"
echo "  - Frontend: https://truyenthongviet.vn"
echo "  - Backend API: https://truyenthongviet.vn/api"
echo "  - Health Check: https://truyenthongviet.vn/health"
echo "=================================================="
