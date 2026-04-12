#!/bin/bash

echo "=== SSL/TLS Setup Script for Chatbot SaaS v2.1 ==="
echo "Setting up Let's Encrypt SSL certificates"
echo ""

# Configuration
DOMAIN="cwsv.truyenthongviet.vn"
API_DOMAIN="cwsv.truyenthongviet.vn"
EMAIL="admin@truyenthongviet.vn"
WEBROOT="/var/www/certbot"
NGINX_CONF="/etc/nginx/sites-available/chatbot-saas"

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
    echo -e "${GREEN}SSL Certificate Generated Successfully!${NC}"
}

print_error() {
    echo -e "${RED}Error: $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}Warning: $1${NC}"
}

# Check if running as root
if [[ $EUID -ne 0 ]]; then
   print_error "This script must be run as root (use sudo)"
   exit 1
fi

# Update package lists
print_status "Updating package lists"
apt update

# Install Nginx if not already installed
print_status "Installing Nginx"
if ! command -v nginx &> /dev/null; then
    apt install -y nginx
    systemctl enable nginx
    systemctl start nginx
else
    print_warning "Nginx is already installed"
fi

# Install Certbot
print_status "Installing Certbot"
apt install -y certbot python3-certbot-nginx

# Create webroot directory
print_status "Creating webroot directory"
mkdir -p $WEBROOT
chown -R www-data:www-data $WEBROOT

# Create temporary Nginx configuration for ACME challenge
print_status "Creating temporary Nginx configuration"
cat > /etc/nginx/sites-available/temp-certbot << EOF
server {
    listen 80;
    server_name $DOMAIN $API_DOMAIN;
    
    location /.well-known/acme-challenge/ {
        root $WEBROOT;
    }
    
    location / {
        return 200 'OK';
        add_header Content-Type text/plain;
    }
}
EOF

# Enable temporary site
ln -sf /etc/nginx/sites-available/temp-certbot /etc/nginx/sites-enabled/
rm -f /etc/nginx/sites-enabled/default

# Test Nginx configuration
nginx -t
if [[ $? -ne 0 ]]; then
    print_error "Nginx configuration test failed"
    exit 1
fi

# Restart Nginx
systemctl restart nginx

# Wait for Nginx to start
sleep 5

# Generate SSL certificate
print_status "Generating SSL certificate for $DOMAIN"
certbot certonly \
    --webroot \
    --webroot-path=$WEBROOT \
    --email $EMAIL \
    --agree-tos \
    --no-eff-email \
    --non-interactive \
    -d $DOMAIN \
    -d $API_DOMAIN

if [[ $? -ne 0 ]]; then
    print_error "SSL certificate generation failed"
    exit 1
fi

# Create production Nginx configuration
print_status "Creating production Nginx configuration"
cat > $NGINX_CONF << 'EOF'
# Chatbot SaaS v2.1 Production Configuration
upstream backend {
    server localhost:8080;
}

server {
    listen 443 ssl http2;
    server_name yourdomain.com api.yourdomain.com;

    ssl_certificate /etc/letsencrypt/live/yourdomain.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/yourdomain.com/privkey.pem;
    
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers ECDHE-RSA-AES256-GCM-SHA512:DHE-RSA-AES256-GCM-SHA512;
    ssl_prefer_server_ciphers off;
    ssl_session_cache shared:SSL:10m;
    
    add_header Strict-Transport-Security "max-age=63072000" always;
    add_header X-Frame-Options DENY always;
    add_header X-Content-Type-Options nosniff always;
    
    location /api/ {
        proxy_pass http://backend;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
    
    location / {
        root /var/www/html;
        try_files $uri $uri/ /index.html;
    }
}

server {
    listen 80;
    server_name yourdomain.com api.yourdomain.com;
    
    location /.well-known/acme-challenge/ {
        root /var/www/certbot;
    }
    
    location / {
        return 301 https://$server_name$request_uri;
    }
}
EOF

# Replace placeholder domain with actual domain
sed -i "s/yourdomain.com/$DOMAIN/g" $NGINX_CONF

# Enable production site
ln -sf $NGINX_CONF /etc/nginx/sites-enabled/
rm -f /etc/nginx/sites-available/temp-certbot

# Test Nginx configuration
nginx -t
if [[ $? -ne 0 ]]; then
    print_error "Production Nginx configuration test failed"
    exit 1
fi

# Restart Nginx
systemctl restart nginx

# Setup auto-renewal
print_status "Setting up SSL auto-renewal"
(crontab -l 2>/dev/null; echo "0 12 * * * /usr/bin/certbot renew --quiet --deploy-hook 'systemctl reload nginx'") | crontab -

# Test SSL configuration
print_status "Testing SSL configuration"
sleep 5

# Test SSL certificate
if curl -s -I "https://$DOMAIN" | grep -q "200 OK"; then
    print_success
else
    print_warning "SSL certificate may not be working properly. Please check manually."
fi

# Display certificate information
print_status "SSL Certificate Information"
echo "Certificate files location:"
echo "  Certificate: /etc/letsencrypt/live/$DOMAIN/fullchain.pem"
echo "  Private Key: /etc/letsencrypt/live/$DOMAIN/privkey.pem"
echo ""
echo "Certificate details:"
openssl x509 -in /etc/letsencrypt/live/$DOMAIN/fullchain.pem -text -noout | grep -E "(Subject:|Issuer:|Not Before:|Not After:)"

# Display next steps
echo ""
print_status "Next Steps"
echo "1. Update your application configuration to use HTTPS URLs"
echo "2. Update CORS configuration to allow HTTPS domains"
echo "3. Test all API endpoints with HTTPS"
echo "4. Setup monitoring for certificate expiration"
echo "5. Configure CDN if needed"

# Display renewal information
echo ""
print_status "Auto-renewal Information"
echo "Auto-renewal has been configured via cron job"
echo "Certificate will be automatically renewed 30 days before expiration"
echo "You can test renewal with: certbot renew --dry-run"

echo ""
print_status "SSL Setup Complete!"
echo "Your domains $DOMAIN and $API_DOMAIN now have valid SSL certificates"
