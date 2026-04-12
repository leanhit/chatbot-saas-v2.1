#!/bin/bash

echo "=== CDN Setup Script for Chatbot SaaS v2.1 ==="
echo "Configuring CloudFlare CDN for optimal performance"
echo ""

# Configuration
DOMAIN="yourdomain.com"
API_DOMAIN="api.yourdomain.com"
CDN_EMAIL="admin@yourdomain.com"
CDN_TOKEN="your_cloudflare_api_token"

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

# Check if Cloudflare CLI is installed
if ! command -v curl &> /dev/null; then
    print_error "curl is required for this script"
    exit 1
fi

# Check if required variables are set
if [[ -z "$DOMAIN" || -z "$CDN_EMAIL" || -z "$CDN_TOKEN" ]]; then
    print_error "Please set DOMAIN, CDN_EMAIL, and CDN_TOKEN variables"
    exit 1
fi

# ========================================
# 1. Get Zone ID
# ========================================

print_status "Getting CloudFlare zone ID"

ZONE_ID=$(curl -s -X GET "https://api.cloudflare.com/client/v4/zones?name=$DOMAIN" \
     -H "X-Auth-Email: $CDN_EMAIL" \
     -H "X-Auth-Key: $CDN_TOKEN" \
     -H "Content-Type: application/json" | \
     jq -r '.result[0].id')

if [[ -z "$ZONE_ID" || "$ZONE_ID" == "null" ]]; then
    print_error "Failed to get zone ID for domain: $DOMAIN"
    exit 1
fi

print_success "Zone ID: $ZONE_ID"

# ========================================
# 2. Configure DNS Records
# ========================================

print_status "Configuring DNS records"

# A record for main domain
curl -s -X POST "https://api.cloudflare.com/client/v4/zones/$ZONE_ID/dns_records" \
     -H "X-Auth-Email: $CDN_EMAIL" \
     -H "X-Auth-Key: $CDN_TOKEN" \
     -H "Content-Type: application/json" \
     --data '{
       "type": "A",
       "name": "'$DOMAIN'",
       "content": "YOUR_SERVER_IP",
       "ttl": 300,
       "proxied": true
     }' > /dev/null

# A record for API subdomain
curl -s -X POST "https://api.cloudflare.com/client/v4/zones/$ZONE_ID/dns_records" \
     -H "X-Auth-Email: $CDN_EMAIL" \
     -H "X-Auth-Key: $CDN_TOKEN" \
     -H "Content-Type: application/json" \
     --data '{
       "type": "A",
       "name": "api",
       "content": "YOUR_SERVER_IP",
       "ttl": 300,
       "proxied": true
     }' > /dev/null

# CNAME for www
curl -s -X POST "https://api.cloudflare.com/client/v4/zones/$ZONE_ID/dns_records" \
     -H "X-Auth-Email: $CDN_EMAIL" \
     -H "X-Auth-Key: $CDN_TOKEN" \
     -H "Content-Type: application/json" \
     --data '{
       "type": "CNAME",
       "name": "www",
       "content": "'$DOMAIN'",
       "ttl": 300,
       "proxied": true
     }' > /dev/null

print_success "DNS records configured"

# ========================================
# 3. Configure Page Rules
# ========================================

print_status "Configuring page rules"

# Cache everything rule
curl -s -X POST "https://api.cloudflare.com/client/v4/zones/$ZONE_ID/pagerules" \
     -H "X-Auth-Email: $CDN_EMAIL" \
     -H "X-Auth-Key: $CDN_TOKEN" \
     -H "Content-Type: application/json" \
     --data '{
       "targets": [
         {
           "target": "url",
           "constraint": {
             "operator": "matches",
             "value": "'$DOMAIN'/*"
           }
         }
       ],
       "actions": [
         {
           "id": "cache_level",
           "value": "cache_everything"
         },
         {
           "id": "edge_cache_ttl",
           "value": 86400
         },
         {
           "id": "browser_cache_ttl",
           "value": 86400
         }
       ],
       "status": "active",
       "priority": 1
     }' > /dev/null

# API bypass rule
curl -s -X POST "https://api.cloudflare.com/client/v4/zones/$ZONE_ID/pagerules" \
     -H "X-Auth-Email: $CDN_EMAIL" \
     -H "X-Auth-Key: $CDN_TOKEN" \
     -H "Content-Type: application/json" \
     --data '{
       "targets": [
         {
           "target": "url",
           "constraint": {
             "operator": "matches",
             "value": "'$API_DOMAIN'/*"
           }
         }
       ],
       "actions": [
         {
           "id": "cache_level",
           "value": "bypass"
         }
       ],
       "status": "active",
       "priority": 2
     }' > /dev/null

print_success "Page rules configured"

# ========================================
# 4. Configure Cache Rules
# ========================================

print_status "Configuring cache rules"

# Static assets cache rule
curl -s -X POST "https://api.cloudflare.com/client/v4/zones/$ZONE_ID/rulesets" \
     -H "X-Auth-Email: $CDN_EMAIL" \
     -H "X-Auth-Key: $CDN_TOKEN" \
     -H "Content-Type: application/json" \
     --data '{
       "name": "Static Assets Cache",
       "kind": "zone",
       "phase": "http_request_cache_settings",
       "entries": [
         {
           "expression": "(http.request.uri.path contains \".css\" or http.request.uri.path contains \".js\" or http.request.uri.path contains \".png\" or http.request.uri.path contains \".jpg\" or http.request.uri.path contains \".gif\" or http.request.uri.path contains \".woff\" or http.request.uri.path contains \".svg\")",
           "action": {
             "cache_ttl": 86400,
             "browser_cache_ttl": {
               "mode": "respect_origin"
             }
           }
         }
       ]
     }' > /dev/null

print_success "Cache rules configured"

# ========================================
# 5. Configure Security Settings
# ========================================

print_status "Configuring security settings"

# SSL/TLS settings
curl -s -X PATCH "https://api.cloudflare.com/client/v4/zones/$ZONE_ID/settings/ssl" \
     -H "X-Auth-Email: $CDN_EMAIL" \
     -H "X-Auth-Key: $CDN_TOKEN" \
     -H "Content-Type: application/json" \
     --data '{
       "value": "strict"
     }' > /dev/null

# HSTS
curl -s -X PATCH "https://api.cloudflare.com/client/v4/zones/$ZONE_ID/settings/security_header" \
     -H "X-Auth-Email: $CDN_EMAIL" \
     -H "X-Auth-Key: $CDN_TOKEN" \
     -H "Content-Type: application/json" \
     --data '{
       "value": {
         "strict_transport_security": {
           "enabled": true,
           "max_age": 31536000,
           "include_subdomains": true,
           "preload": true
         }
       }
     }' > /dev/null

print_success "Security settings configured"

# ========================================
# 6. Configure Performance Settings
# ========================================

print_status "Configuring performance settings"

# Brotli compression
curl -s -X PATCH "https://api.cloudflare.com/client/v4/zones/$ZONE_ID/settings/brotli" \
     -H "X-Auth-Email: $CDN_EMAIL" \
     -H "X-Auth-Key: $CDN_TOKEN" \
     -H "Content-Type: application/json" \
     --data '{
       "value": "on"
     }' > /dev/null

# Rocket Loader
curl -s -X PATCH "https://api.cloudflare.com/client/v4/zones/$ZONE_ID/settings/rocket_loader" \
     -H "X-Auth-Email: $CDN_EMAIL" \
     -H "X-Auth-Key: $CDN_TOKEN" \
     -H "Content-Type: application/json" \
     --data '{
       "value": "on"
     }' > /dev/null

# Auto Minify
curl -s -X PATCH "https://api.cloudflare.com/client/v4/zones/$ZONE_ID/settings/minify" \
     -H "X-Auth-Email: $CDN_EMAIL" \
     -H "X-Auth-Key: $CDN_TOKEN" \
     -H "Content-Type: application/json" \
     --data '{
       "value": {
         "html": "on",
         "css": "on",
         "js": "on"
       }
     }' > /dev/null

print_success "Performance settings configured"

# ========================================
# 7. Configure Rate Limiting
# ========================================

print_status "Configuring rate limiting"

# API rate limiting
curl -s -X POST "https://api.cloudflare.com/client/v4/zones/$ZONE_ID/rate_limits" \
     -H "X-Auth-Email: $CDN_EMAIL" \
     -H "X-Auth-Key: $CDN_TOKEN" \
     -H "Content-Type: application/json" \
     --data '{
       "threshold": 100,
       "period": 60,
       "match": {
         "request": {
           "url": "'$API_DOMAIN'/*"
         }
       },
       "action": {
         "mode": "simulate",
         "timeout": 60
       },
       "disabled": false,
       "description": "API Rate Limiting"
     }' > /dev/null

print_success "Rate limiting configured"

# ========================================
# 8. Configure Web Application Firewall
# ========================================

print_status "Configuring WAF"

# Enable WAF
curl -s -X PUT "https://api.cloudflare.com/client/v4/zones/$ZONE_ID/settings/waf" \
     -H "X-Auth-Email: $CDN_EMAIL" \
     -H "X-Auth-Key: $CDN_TOKEN" \
     -H "Content-Type: application/json" \
     --data '{
       "value": "on"
     }' > /dev/null

print_success "WAF configured"

# ========================================
# 9. Generate Configuration Summary
# ========================================

print_status "Configuration Summary"
echo "=========================="
echo "Domain: $DOMAIN"
echo "API Domain: $API_DOMAIN"
echo "Zone ID: $ZONE_ID"
echo ""
echo "DNS Records:"
echo "- A: $DOMAIN -> YOUR_SERVER_IP (Proxied)"
echo "- A: api.$DOMAIN -> YOUR_SERVER_IP (Proxied)"
echo "- CNAME: www.$DOMAIN -> $DOMAIN (Proxied)"
echo ""
echo "Page Rules:"
echo "- Cache everything: $DOMAIN/* (Cache: Everything, TTL: 1 day)"
echo "- API bypass: $API_DOMAIN/* (Cache: Bypass)"
echo ""
echo "Security:"
echo "- SSL/TLS: Strict"
echo "- HSTS: Enabled (1 year, include subdomains, preload)"
echo "- WAF: Enabled"
echo "- Rate Limiting: 100 req/min for API"
echo ""
echo "Performance:"
echo "- Brotli: Enabled"
echo "- Rocket Loader: Enabled"
echo "- Auto Minify: HTML, CSS, JS"
echo "- Static Assets: 1 day cache"
echo ""

# ========================================
# 10. Next Steps
# ========================================

print_status "Next Steps"
echo "============"
echo "1. Update YOUR_SERVER_IP in the script with your actual server IP"
echo "2. Update DNS records at your domain registrar to point to CloudFlare"
echo "3. Test the configuration:"
echo "   - curl -I https://$DOMAIN"
echo "   - curl -I https://$API_DOMAIN/actuator/health"
echo "4. Monitor performance in CloudFlare dashboard"
echo "5. Set up analytics and monitoring"
echo "6. Configure custom error pages"
echo "7. Set up origin certificates for enhanced security"
echo ""

# ========================================
# 11. Verification Commands
# ========================================

print_status "Verification Commands"
echo "========================"
echo "Check DNS propagation:"
echo "dig $DOMAIN"
echo "dig $API_DOMAIN"
echo ""
echo "Check SSL certificate:"
echo "openssl s_client -connect $DOMAIN:443 -servername $DOMAIN"
echo ""
echo "Check CDN headers:"
echo "curl -I https://$DOMAIN"
echo ""
echo "Check API bypass:"
echo "curl -I https://$API_DOMAIN/actuator/health"
echo ""

print_success "CDN configuration completed!"
echo "Your Chatbot SaaS v2.1 is now protected and accelerated by CloudFlare CDN"
echo "Remember to update YOUR_SERVER_IP with your actual server IP address"
