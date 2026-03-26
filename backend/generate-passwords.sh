#!/bin/bash
echo "=== Generating Strong Passwords ==="
echo "PostgreSQL Password (256-bit):"
openssl rand -base64 32
echo ""
echo "JWT Secret (64 characters):"
openssl rand -base64 64
echo ""
echo "MinIO Password (256-bit):"
openssl rand -base64 32
echo ""
echo "RabbitMQ Password (256-bit):"
openssl rand -base64 32
echo ""
echo "Redis Password (256-bit):"
openssl rand -base64 32
echo ""
echo "Grafana Password (256-bit):"
openssl rand -base64 32
