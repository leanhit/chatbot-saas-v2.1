#!/bin/bash

echo "=== Kubernetes Deployment Script for Chatbot SaaS v2.1 ==="
echo "Deploying to Kubernetes cluster"
echo ""

# Configuration
NAMESPACE="chatbot-saas"
KUBECONFIG_FILE="$HOME/.kube/config"
IMAGE_TAG="latest"
REGISTRY="ghcr.io"
IMAGE_NAME="chatbot-saas/backend"

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

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        --namespace)
            NAMESPACE="$2"
            shift 2
            ;;
        --image-tag)
            IMAGE_TAG="$2"
            shift 2
            ;;
        --dry-run)
            DRY_RUN="--dry-run=client"
            shift
            ;;
        --help)
            echo "Usage: $0 [--namespace NAMESPACE] [--image-tag TAG] [--dry-run]"
            echo "  --namespace: Kubernetes namespace (default: chatbot-saas)"
            echo "  --image-tag: Docker image tag (default: latest)"
            echo "  --dry-run: Show what would be deployed without applying"
            exit 0
            ;;
        *)
            print_error "Unknown option: $1"
            exit 1
            ;;
    esac
done

# Check if kubectl is installed
if ! command -v kubectl &> /dev/null; then
    print_error "kubectl is not installed"
    exit 1
fi

# Check if kubeconfig exists
if [[ ! -f "$KUBECONFIG_FILE" ]]; then
    print_error "Kubeconfig file not found at $KUBECONFIG_FILE"
    exit 1
fi

# Check cluster connectivity
print_status "Checking cluster connectivity"
if ! kubectl cluster-info &>/dev/null; then
    print_error "Cannot connect to Kubernetes cluster"
    exit 1
fi
print_success "Cluster connectivity verified"

# Create namespace if it doesn't exist
print_status "Creating namespace: $NAMESPACE"
kubectl create namespace $NAMESPACE --dry-run=client -o yaml | kubectl apply -f -
if [[ $? -ne 0 ]]; then
    print_error "Failed to create namespace"
    exit 1
fi
print_success "Namespace created/verified"

# Generate secrets from environment variables
print_status "Generating secrets"

# Check if required environment variables are set
if [[ -z "$JWT_SECRET" ]]; then
    print_error "JWT_SECRET environment variable is required"
    exit 1
fi

if [[ -z "$POSTGRES_PASSWORD" ]]; then
    print_error "POSTGRES_PASSWORD environment variable is required"
    exit 1
fi

# Create secrets file
cat > k8s/secrets-generated.yaml << EOF
apiVersion: v1
kind: Secret
metadata:
  name: chatbot-saas-secrets
  namespace: $NAMESPACE
  labels:
    app: chatbot-saas
    component: backend
type: Opaque
data:
  jwt-secret: $(echo -n "$JWT_SECRET" | base64)
  postgres-password: $(echo -n "$POSTGRES_PASSWORD" | base64)
  redis-password: $(echo -n "${REDIS_PASSWORD:-default_redis_password}" | base64)
  minio-access-key: $(echo -n "${MINIO_ACCESS_KEY:-minioadmin}" | base64)
  minio-secret-key: $(echo -n "${MINIO_SECRET_KEY:-minioadmin}" | base64)
  rabbitmq-password: $(echo -n "${RABBITMQ_PASSWORD:-admin123}" | base64)
  identity-db-url: $(echo -n "jdbc:postgresql://postgres:5432/identity_db" | base64)
  user-db-url: $(echo -n "jdbc:postgresql://postgres:5434/user_db" | base64)
  tenant-db-url: $(echo -n "jdbc:postgresql://postgres:5435/tenant_db" | base64)
  app-db-url: $(echo -n "jdbc:postgresql://postgres:5436/app_db" | base64)
  billing-db-url: $(echo -n "jdbc:postgresql://postgres:5437/billing_db" | base64)
  wallet-db-url: $(echo -n "jdbc:postgresql://postgres:5438/wallet_db" | base64)
  config-db-url: $(echo -n "jdbc:postgresql://postgres:5439/config_db" | base64)
  message-db-url: $(echo -n "jdbc:postgresql://postgres:5440/message_db" | base64)
  smtp-password: $(echo -n "${SMTP_PASSWORD:-}" | base64)
  slack-webhook-url: $(echo -n "${SLACK_WEBHOOK_URL:-}" | base64)
EOF

# Apply secrets
print_status "Applying secrets"
kubectl apply -f k8s/secrets-generated.yaml $DRY_RUN
if [[ $? -ne 0 ]]; then
    print_error "Failed to apply secrets"
    exit 1
fi
print_success "Secrets applied"

# Apply ConfigMaps
print_status "Applying ConfigMaps"
kubectl apply -f k8s/configmap.yaml -n $NAMESPACE $DRY_RUN
if [[ $? -ne 0 ]]; then
    print_error "Failed to apply ConfigMaps"
    exit 1
fi
print_success "ConfigMaps applied"

# Create Persistent Volume Claims
print_status "Creating Persistent Volume Claims"
cat > k8s/pvc.yaml << EOF
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: uploads-pvc
  namespace: $NAMESPACE
  labels:
    app: chatbot-saas
    component: backend
spec:
  accessModes:
    - ReadWriteOnce
  resources:
    requests:
      storage: 10Gi
  storageClassName: standard
EOF

kubectl apply -f k8s/pvc.yaml $DRY_RUN
if [[ $? -ne 0 ]]; then
    print_error "Failed to create PVCs"
    exit 1
fi
print_success "PVCs created"

# Update image tag in deployment
print_status "Updating image tag in deployment"
sed "s|chatbot-saas/backend:latest|${REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG}|g" k8s/backend-deployment.yaml > k8s/backend-deployment-updated.yaml

# Apply deployment
print_status "Applying deployment"
kubectl apply -f k8s/backend-deployment-updated.yaml -n $NAMESPACE $DRY_RUN
if [[ $? -ne 0 ]]; then
    print_error "Failed to apply deployment"
    exit 1
fi
print_success "Deployment applied"

# Apply services
print_status "Applying services"
kubectl apply -f k8s/backend-service.yaml -n $NAMESPACE $DRY_RUN
if [[ $? -ne 0 ]]; then
    print_error "Failed to apply services"
    exit 1
fi
print_success "Services applied"

# Apply ingress if available
if [[ -f "k8s/ingress.yaml" ]]; then
    print_status "Applying ingress"
    kubectl apply -f k8s/ingress.yaml -n $NAMESPACE $DRY_RUN
    if [[ $? -ne 0 ]]; then
        print_error "Failed to apply ingress"
        exit 1
    fi
    print_success "Ingress applied"
fi

# Wait for deployment to be ready (only if not dry-run)
if [[ -z "$DRY_RUN" ]]; then
    print_status "Waiting for deployment to be ready"
    kubectl rollout status deployment/backend -n $NAMESPACE --timeout=300s
    if [[ $? -ne 0 ]]; then
        print_error "Deployment rollout failed"
        exit 1
    fi
    print_success "Deployment is ready"
else
    print_warning "Dry run mode - not waiting for deployment"
fi

# Show deployment status
print_status "Deployment status"
kubectl get pods -n $NAMESPACE -l app=chatbot-saas,component=backend
kubectl get services -n $NAMESPACE -l app=chatbot-saas
kubectl get deployment -n $NAMESPACE -l app=chatbot-saas

# Show useful commands
print_status "Useful Commands"
echo "===================="
echo "View logs: kubectl logs -f deployment/backend -n $NAMESPACE"
echo "Exec into pod: kubectl exec -it deployment/backend -n $NAMESPACE -- bash"
echo "Scale deployment: kubectl scale deployment/backend --replicas=5 -n $NAMESPACE"
echo "Check health: kubectl port-forward svc/backend-service 8080:80 -n $NAMESPACE"
echo "Delete deployment: kubectl delete -f k8s/ -n $NAMESPACE"
echo ""

# Show next steps
print_status "Next Steps"
echo "============"
echo "1. Verify application is running: curl http://localhost:8080/actuator/health"
echo "2. Check monitoring: kubectl port-forward svc/prometheus-service 9090:9090 -n monitoring"
echo "3. Set up ingress for external access"
echo "4. Configure DNS records"
echo "5. Set up monitoring and alerting"
echo ""

if [[ -z "$DRY_RUN" ]]; then
    print_success "Kubernetes deployment completed successfully!"
    echo "Your Chatbot SaaS v2.1 is now running in Kubernetes namespace: $NAMESPACE"
else
    print_warning "Dry run completed. No actual deployment was made."
fi

# Cleanup temporary files
rm -f k8s/secrets-generated.yaml k8s/backend-deployment-updated.yaml k8s/pvc.yaml
