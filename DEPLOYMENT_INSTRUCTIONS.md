# Production Deployment Instructions

## Deploy to truyenthongviet.vn (103.149.99.7)

### Prerequisites
- SSH access to 103.149.99.7
- Docker and Docker Compose installed on production server
- Let's Encrypt certificates for truyenthongviet.vn already installed

### Step 1: Upload code to production server
```bash
# From local machine
scp -r /root/chatbot-saas-v2.1 root@103.149.99.7:/root/
```

### Step 2: SSH into production server
```bash
ssh root@103.149.99.7
```

### Step 3: Navigate to app-deploy directory
```bash
cd /root/chatbot-saas-v2.1/app-deploy
```

### Step 4: Run production deployment script
```bash
./deploy-production.sh
```

### Step 5: Verify deployment
```bash
# Check container status
docker ps

# Check container logs
docker logs chatbot_saas_app -f

# Test health endpoint
curl https://truyenthongviet.vn/health

# Test API
curl https://truyenthongviet.vn/api/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"email":"test@example.com","password":"Admin@123","confirmPassword":"Admin@123"}'
```

## Troubleshooting

### Container not starting
```bash
# Check logs
docker logs chatbot_saas_app

# Check nginx config
docker exec chatbot_saas_app nginx -t
```

### SSL Certificate issues
```bash
# Verify certificate exists
ls -la /etc/letsencrypt/live/truyenthongviet.vn/

# Renew certificate if needed
certbot renew
```

### Port conflicts
```bash
# Check what's using port 80/443
netstat -tlnp | grep -E ':(80|443)'

# Stop conflicting services
systemctl stop nginx  # if system nginx is running
```

## Configuration Files

### Docker Compose
- File: `app-deploy/docker-compose.yml`
- Exposes port 80 (HTTP) and 443 (HTTPS)
- Mounts Let's Encrypt certificates from host

### Nginx Config
- File: `app-deploy/nginx/nginx.conf`
- Includes domain-specific configs from `host-sites/`
- Handles both HTTP and HTTPS

### Environment Variables
- File: `app-deploy/config/application-docker.yml`
- Database connections, Redis, MinIO, etc.
- CORS allowed origins include truyenthongviet.vn

## Frontend Configuration

### .env file (already updated)
- API URL: https://truyenthongviet.vn/api/
- WebSocket URL: wss://truyenthongviet.vn/ws/takeover
- Presence WebSocket: wss://truyenthongviet.vn/ws/presence
