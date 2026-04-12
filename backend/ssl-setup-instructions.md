# SSL Certificate Setup Instructions

## Overview
This document provides step-by-step instructions for setting up SSL certificates for Chatbot SaaS v2.1 production deployment.

## Prerequisites
- Server with Ubuntu 20.04+ or CentOS 8+
- Domain name pointing to server IP: `cwsv.truyenthongviet.vn`
- Root access or sudo privileges
- Nginx installed

## Quick Setup (Recommended)

### 1. Run the automated SSL setup script
```bash
cd /root/ltanh/chatbot-saas-v2.1/backend
sudo ./setup-ssl.sh
```

This script will:
- Install Certbot and Nginx
- Generate SSL certificates for `cwsv.truyenthongviet.vn`
- Configure Nginx with SSL
- Setup auto-renewal
- Test SSL configuration

### 2. Verify SSL certificate
```bash
# Check certificate status
sudo certbot certificates

# Test SSL configuration
curl -I https://cwsv.truyenthongviet.vn

# Check SSL rating
openssl s_client -connect cwsv.truyenthongviet.vn:443 -tls1_2
```

## Manual Setup (Alternative)

### 1. Install Certbot
```bash
sudo apt update
sudo apt install -y certbot python3-certbot-nginx
```

### 2. Create webroot directory
```bash
sudo mkdir -p /var/www/certbot
sudo chown -R www-data:www-data /var/www/certbot
```

### 3. Generate SSL certificate
```bash
sudo certbot certonly \
    --webroot \
    --webroot-path=/var/www/certbot \
    --email admin@truyenthongviet.vn \
    --agree-tos \
    --no-eff-email \
    -d cwsv.truyenthongviet.vn
```

### 4. Update Nginx configuration
Edit `/etc/nginx/nginx.conf` or your site configuration to use:
```nginx
ssl_certificate /etc/letsencrypt/live/cwsv.truyenthongviet.vn/fullchain.pem;
ssl_certificate_key /etc/letsencrypt/live/cwsv.truyenthongviet.vn/privkey.pem;
```

### 5. Setup auto-renewal
```bash
# Add to crontab
echo "0 12 * * * /usr/bin/certbot renew --quiet --deploy-hook 'systemctl reload nginx'" | sudo crontab -
```

## Post-Setup Verification

### 1. Test SSL certificate
```bash
# Check certificate details
openssl x509 -in /etc/letsencrypt/live/cwsv.truyenthongviet.vn/fullchain.pem -text -noout

# Test SSL connection
curl -I https://cwsv.truyenthongviet.vn

# Check SSL rating (optional)
curl -s https://www.ssllabs.com/ssltest/
```

### 2. Verify application URLs
- Frontend: https://cwsv.truyenthongviet.vn
- Backend API: https://cwsv.truyenthongviet.vn/api
- Health check: https://cwsv.truyenthongviet.vn/actuator/health

### 3. Test auto-renewal
```bash
sudo certbot renew --dry-run
```

## Troubleshooting

### Common Issues

#### 1. DNS propagation
```bash
# Check DNS resolution
nslookup cwsv.truyenthongviet.vn
dig cwsv.truyenthongviet.vn

# Wait for DNS propagation (can take up to 48 hours)
```

#### 2. Port 80 blocked
```bash
# Check if port 80 is open
sudo netstat -tlnp | grep :80
sudo ufw status

# Open port 80 temporarily for certificate generation
sudo ufw allow 80/tcp
```

#### 3. Nginx configuration errors
```bash
# Test Nginx configuration
sudo nginx -t

# Check Nginx logs
sudo tail -f /var/log/nginx/error.log
```

#### 4. Certificate generation failures
```bash
# Check Certbot logs
sudo journalctl -u certbot
sudo cat /var/log/letsencrypt/letsencrypt.log

# Try with different challenge type
sudo certbot certonly --manual --preferred-challenges dns -d cwsv.truyenthongviet.vn
```

## Security Best Practices

### 1. Certificate Security
- Certificate files are automatically secured with proper permissions
- Private keys are readable only by root
- Auto-renewal prevents expiration

### 2. SSL Configuration
- Strong cipher suites configured
- HTTP/2 enabled
- Security headers added
- HSTS (HTTP Strict Transport Security) enabled

### 3. Monitoring
```bash
# Check certificate expiration
sudo certbot certificates

# Monitor SSL certificate expiry
echo "0 6 * * * /usr/bin/certbot certificates | grep -q 'Expiry' && mail -s 'SSL Certificate Expiring' admin@truyenthongviet.vn"
```

## File Locations

### Certificate Files
- Certificate: `/etc/letsencrypt/live/cwsv.truyenthongviet.vn/fullchain.pem`
- Private Key: `/etc/letsencrypt/live/cwsv.truyenthongviet.vn/privkey.pem`
- Chain: `/etc/letsencrypt/live/cwsv.truyenthongviet.vn/chain.pem`

### Configuration Files
- Nginx: `/etc/nginx/nginx.conf`
- Certbot: `/etc/letsencrypt/renewal/cwsv.truyenthongviet.vn.conf`
- Cron: `/etc/crontab`

### Log Files
- Nginx: `/var/log/nginx/`
- Certbot: `/var/log/letsencrypt/`
- System: `/var/log/syslog`

## Renewal Process

### Automatic Renewal
- Certificates auto-renew 30 days before expiration
- Cron job runs daily at 12:00 PM
- Nginx automatically reloads after renewal

### Manual Renewal
```bash
# Renew all certificates
sudo certbot renew

# Renew specific certificate
sudo certbot renew --cert-name cwsv.truyenthongviet.vn

# Force renewal (not recommended unless necessary)
sudo certbot renew --force-renewal
```

## Next Steps

After SSL setup is complete:

1. **Update Application Configuration**
   - Ensure all URLs use HTTPS
   - Update CORS settings if needed
   - Test all API endpoints

2. **Setup Monitoring**
   - Monitor certificate expiration
   - Set up alerts for SSL issues
   - Monitor SSL/TLS performance

3. **Performance Optimization**
   - Configure CDN if needed
   - Enable HTTP/2
   - Optimize SSL session caching

4. **Security Hardening**
   - Implement security headers
   - Set up firewall rules
   - Monitor SSL/TLS vulnerabilities

## Support

For SSL certificate issues:
- Let's Encrypt Community: https://community.letsencrypt.org/
- Certbot Documentation: https://certbot.eff.org/docs/
- Nginx SSL Documentation: https://nginx.org/en/docs/http/configuring_https_servers.html

## Emergency Procedures

### Certificate Revocation
```bash
# Revoke certificate (emergency only)
sudo certbot revoke --cert-path /etc/letsencrypt/live/cwsv.truyenthongviet.vn/fullchain.pem

# Delete certificate
sudo certbot delete --cert-name cwsv.truyenthongviet.vn
```

### Temporary HTTP Access
```bash
# Temporarily disable HTTPS redirect
sudo sed -i 's/return 301/#return 301/' /etc/nginx/nginx.conf
sudo systemctl reload nginx
```

Remember to re-enable HTTPS redirect after resolving issues.
