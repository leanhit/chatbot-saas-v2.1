#!/bin/bash

echo "Starting minimal setup for traloitudong.com backend..."

echo ""
echo "1. Starting Docker services (PostgreSQL, Redis, MinIO)..."
docker-compose up -d

echo ""
echo "2. Waiting for services to start (30 seconds)..."
sleep 30

echo ""
echo "3. Starting backend with minimal profile..."
cd backend
./gradlew bootRun --args="--spring.profiles.active=minimal"
