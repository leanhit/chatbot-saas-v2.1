#!/bin/bash

# Generate RSA Key Pair for JWT RS256 Algorithm
# Usage: ./generate-rsa-keys.sh [key-size]

set -e

KEY_SIZE=${1:-2048}
KEY_DIR="$(dirname "$0")/../keys"
PRIVATE_KEY_FILE="$KEY_DIR/private.pem"
PUBLIC_KEY_FILE="$KEY_DIR/public.pem"

echo "🔐 Generating RSA $KEY_SIZE-bit key pair for JWT RS256..."

# Create keys directory if it doesn't exist
mkdir -p "$KEY_DIR"

# Generate private key
echo "📝 Generating private key..."
openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:$KEY_SIZE -out "$PRIVATE_KEY_FILE"

# Extract public key from private key
echo "📋 Extracting public key..."
openssl rsa -pubout -in "$PRIVATE_KEY_FILE" -out "$PUBLIC_KEY_FILE"

# Set appropriate permissions
chmod 600 "$PRIVATE_KEY_FILE"
chmod 644 "$PUBLIC_KEY_FILE"

echo "✅ RSA key pair generated successfully!"
echo ""
echo "📁 Private key: $PRIVATE_KEY_FILE"
echo "📁 Public key: $PUBLIC_KEY_FILE"
echo ""
echo "🔧 Environment variables for production:"
echo "export RSA_PRIVATE_KEY=\"$(cat $PRIVATE_KEY_FILE | tr -d '\n')\""
echo "export RSA_PUBLIC_KEY=\"$(cat $PUBLIC_KEY_FILE | tr -d '\n')\""
echo ""
echo "💡 Add these to your .env file or production secrets"
echo ""
echo "⚠️  IMPORTANT: Keep the private key secure and never expose it in client code!"
