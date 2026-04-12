#!/bin/bash

echo "=== Build and Push Script for Chatbot SaaS v2.1 ==="
echo "Building and pushing Docker images to registry"
echo ""

# Configuration
REGISTRY="ghcr.io"
IMAGE_NAME="chatbot-saas/backend"
DOCKERFILE="Dockerfile.optimized"
BUILD_CONTEXT="."
PLATFORMS="linux/amd64,linux/arm64"
CACHE_TYPE="gha"

# Parse command line arguments
TAG="latest"
PUSH=true
LOAD=false
DRY_RUN=false

while [[ $# -gt 0 ]]; do
    case $1 in
        --tag)
            TAG="$2"
            shift 2
            ;;
        --registry)
            REGISTRY="$2"
            shift 2
            ;;
        --no-push)
            PUSH=false
            shift
            ;;
        --load)
            LOAD=true
            shift
            ;;
        --dry-run)
            DRY_RUN=true
            shift
            ;;
        --platforms)
            PLATFORMS="$2"
            shift 2
            ;;
        --help)
            echo "Usage: $0 [--tag TAG] [--registry REGISTRY] [--no-push] [--load] [--dry-run] [--platforms PLATFORMS]"
            echo "  --tag: Docker image tag (default: latest)"
            echo "  --registry: Docker registry (default: ghcr.io)"
            echo "  --no-push: Build only, don't push to registry"
            echo "  --load: Load image into local Docker daemon"
            echo "  --dry-run: Show commands without executing"
            echo "  --platforms: Target platforms (default: linux/amd64,linux/arm64)"
            exit 0
            ;;
        *)
            echo "Unknown option: $1"
            exit 1
            ;;
    esac
done

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

print_status() {
    echo -e "${BLUE}=== $1 ===${NC}"
}

print_success() {
    echo -e "${GREEN}SUCCESS: $1${NC}"
}

print_error() {
    echo -e "${RED}ERROR: $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}WARNING: $1${NC}"
}

# Check if Docker is installed
if ! command -v docker &> /dev/null; then
    print_error "Docker is not installed"
    exit 1
fi

# Check if Docker Buildx is available
if ! docker buildx version &> /dev/null; then
    print_error "Docker Buildx is not installed"
    exit 1
fi

# Check if we're logged into registry
if [[ "$PUSH" == "true" ]]; then
    print_status "Checking registry authentication"
    if ! docker buildx imagetools inspect $REGISTRY/$IMAGE_NAME:latest &>/dev/null; then
        print_warning "Not logged into registry $REGISTRY"
        echo "Please run: docker login $REGISTRY"
        exit 1
    fi
    print_success "Registry authentication verified"
fi

# Set full image name
FULL_IMAGE_NAME="$REGISTRY/$IMAGE_NAME:$TAG"

print_status "Building Docker image"
echo "Registry: $REGISTRY"
echo "Image: $IMAGE_NAME"
echo "Tag: $TAG"
echo "Platforms: $PLATFORMS"
echo "Dockerfile: $DOCKERFILE"
echo "Full image name: $FULL_IMAGE_NAME"

# Prepare build arguments
BUILD_ARGS=(
    --platform "$PLATFORMS"
    --file "$DOCKERFILE"
    --tag "$FULL_IMAGE_NAME"
    --tag "$REGISTRY/$IMAGE_NAME:latest"
    --cache-from "type=$CACHE_TYPE"
    --cache-to "type=$CACHE_TYPE,mode=max"
    --build-arg "BUILD_DATE=$(date -u +'%Y-%m-%dT%H:%M:%SZ')"
    --build-arg "VCS_REF=$(git rev-parse --short HEAD 2>/dev/null || echo 'unknown')"
    --build-arg "VERSION=$TAG"
    --label "org.opencontainers.image.created=$(date -u +'%Y-%m-%dT%H:%M:%SZ')"
    --label "org.opencontainers.image.revision=$(git rev-parse HEAD 2>/dev/null || echo 'unknown')"
    --label "org.opencontainers.image.version=$TAG"
    --label "org.opencontainers.image.title=Chatbot SaaS v2.1"
    --label "org.opencontainers.image.description=Production-ready Chatbot SaaS application"
    --label "org.opencontainers.image.source=https://github.com/your-org/chatbot-saas"
    --label "org.opencontainers.image.licenses=MIT"
    --label "org.opencontainers.image.vendor=Chatbot SaaS Team"
)

# Add load flag if specified
if [[ "$LOAD" == "true" ]]; then
    BUILD_ARGS+=(--load)
fi

# Add dry-run flag
if [[ "$DRY_RUN" == "true" ]]; then
    echo "DRY RUN: Would execute:"
    echo "docker buildx build ${BUILD_ARGS[@]} $BUILD_CONTEXT"
    print_warning "Dry run mode - not executing build"
    exit 0
fi

# Build the image
print_status "Building image with Buildx"
if ! docker buildx build "${BUILD_ARGS[@]}" "$BUILD_CONTEXT"; then
    print_error "Docker build failed"
    exit 1
fi
print_success "Docker build completed"

# Push image to registry
if [[ "$PUSH" == "true" ]]; then
    print_status "Pushing image to registry"
    
    # Push all tags
    if ! docker buildx imagetools push "$FULL_IMAGE_NAME"; then
        print_error "Failed to push image $FULL_IMAGE_NAME"
        exit 1
    fi
    
    if [[ "$TAG" != "latest" ]]; then
        if ! docker buildx imagetools push "$REGISTRY/$IMAGE_NAME:latest"; then
            print_error "Failed to push latest tag"
            exit 1
        fi
    fi
    
    print_success "Image pushed to registry"
else
    print_warning "Skipping image push (--no-push specified)"
fi

# Generate SBOM
print_status "Generating SBOM"
if command -v syft &> /dev/null; then
    if syft "$FULL_IMAGE_NAME" -o spdx-json -o sbom.spdx.json; then
        print_success "SBOM generated: sbom.spdx.json"
    else
        print_warning "Failed to generate SBOM"
    fi
else
    print_warning "Syft not installed - skipping SBOM generation"
fi

# Security scan
print_status "Running security scan"
if command -v trivy &> /dev/null; then
    if trivy image --format json --output trivy-report.json "$FULL_IMAGE_NAME"; then
        print_success "Security scan completed: trivy-report.json"
        
        # Show summary
        echo "Security scan summary:"
        trivy image --format table "$FULL_IMAGE_NAME" | head -20
    else
        print_warning "Failed to run security scan"
    fi
else
    print_warning "Trivy not installed - skipping security scan"
fi

# Show image information
print_status "Image information"
docker buildx imagetools inspect "$FULL_IMAGE_NAME"

# Show useful commands
print_status "Useful Commands"
echo "===================="
echo "Pull image: docker pull $FULL_IMAGE_NAME"
echo "Run locally: docker run -p 8080:8080 $FULL_IMAGE_NAME"
echo "View layers: docker buildx imagetools inspect $FULL_IMAGE_NAME"
echo "Scan image: trivy image $FULL_IMAGE_NAME"
echo "Generate SBOM: syft $FULL_IMAGE_NAME"
echo ""

# Show deployment commands
print_status "Deployment Commands"
echo "======================"
echo "Update Kubernetes deployment:"
echo "kubectl set image deployment/backend=$FULL_IMAGE_NAME -n chatbot-saas"
echo ""
echo "Update Docker Compose:"
echo "sed 's|image: .*|image: $FULL_IMAGE_NAME|' docker-compose.production.yml"
echo ""

print_success "Build and push completed successfully!"
echo "Image: $FULL_IMAGE_NAME"
echo "Ready for deployment!"
