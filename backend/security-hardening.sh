#!/bin/bash

echo "=== Security Hardening Script for Chatbot SaaS v2.1 ==="
echo "Applying production security configurations"
echo ""

# Configuration
PROJECT_NAME="chatbot-saas"
FIREWALL_PROFILE="production"

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

# Check if running as root
if [[ $EUID -ne 0 ]]; then
   print_error "This script must be run as root (use sudo)"
   exit 1
fi

# Update system packages
print_status "Updating system packages"
apt update && apt upgrade -y
if [[ $? -ne 0 ]]; then
    print_error "Failed to update system packages"
    exit 1
fi
print_success "System packages updated"

# Install security tools
print_status "Installing security tools"
apt install -y \
    ufw \
    fail2ban \
    rkhunter \
    chkrootkit \
    aide \
    auditd \
    logwatch \
    unattended-upgrades \
    apt-listchanges \
    debsums \
    lynis

if [[ $? -ne 0 ]]; then
    print_error "Failed to install security tools"
    exit 1
fi
print_success "Security tools installed"

# Configure UFW Firewall
print_status "Configuring UFW firewall"

# Reset firewall rules
ufw --force reset

# Default policies
ufw default deny incoming
ufw default allow outgoing

# Allow SSH (with rate limiting)
ufw limit ssh

# Allow HTTP/HTTPS
ufw allow 80/tcp
ufw allow 443/tcp

# Allow backend (internal only)
ufw allow from 127.0.0.1 to any port 8080
ufw allow from 10.0.0.0/8 to any port 8080
ufw allow from 172.16.0.0/12 to any port 8080
ufw allow from 192.168.0.0/16 to any port 8080

# Allow database (internal only)
ufw allow from 127.0.0.1 to any port 5432
ufw allow from 10.0.0.0/8 to any port 5432
ufw allow from 172.16.0.0/12 to any port 5432
ufw allow from 192.168.0.0/16 to any port 5432

# Allow monitoring ports (internal only)
ufw allow from 127.0.0.1 to any port 9090
ufw allow from 127.0.0.1 to any port 3000
ufw allow from 127.0.0.1 to any port 9100

# Enable firewall
ufw --force enable

if [[ $? -ne 0 ]]; then
    print_error "Failed to configure UFW firewall"
    exit 1
fi
print_success "UFW firewall configured"

# Configure Fail2Ban
print_status "Configuring Fail2Ban"

# Create Fail2Ban jail configuration
cat > /etc/fail2ban/jail.local << 'EOF'
[DEFAULT]
bantime = 3600
findtime = 600
maxretry = 3
backend = systemd
destemail = admin@yourdomain.com
sender = fail2ban@yourdomain.com
sendername = Fail2Ban

[sshd]
enabled = true
port = ssh
filter = sshd
logpath = /var/log/auth.log
maxretry = 3
bantime = 3600

[nginx-http-auth]
enabled = true
port = http,https
filter = nginx-http-auth
logpath = /var/log/nginx/error.log
maxretry = 3
bantime = 3600

[nginx-limit-req]
enabled = true
port = http,https
filter = nginx-limit-req
logpath = /var/log/nginx/error.log
maxretry = 10
bantime = 600

[postfix]
enabled = true
port = smtp,smtps
filter = postfix
logpath = /var/log/mail.log
maxretry = 3
bantime = 3600

[recidive]
enabled = true
filter = recidive
logpath = /var/log/fail2ban.log
action = iptables-allports[name=recidive]
bantime = 86400
findtime = 86400
maxretry = 5
EOF

# Restart Fail2Ban
systemctl enable fail2ban
systemctl restart fail2ban

if [[ $? -ne 0 ]]; then
    print_error "Failed to configure Fail2Ban"
    exit 1
fi
print_success "Fail2Ban configured"

# Configure AIDE (Advanced Intrusion Detection)
print_status "Configuring AIDE"

# Initialize AIDE database
aide --init
mv /var/lib/aide/aide.db.new /var/lib/aide/aide.db

# Configure AIDE
cat > /etc/aide/aide.conf << 'EOF'
# AIDE configuration for Chatbot SaaS v2.1
database=file:/var/lib/aide/aide.db
database_out=file:/var/lib/aide/aide.db.new

# Define what to check
All=p+i+n+u+g+s+m+c+md5+sha1

# Directories to check
/bin All
/sbin All
/usr/bin All
/usr/sbin All
/etc All
/var/log All
/var/www All
/opt All

# Exclude temporary directories
!/var/tmp
!/tmp
!/var/cache
!/var/lock
!/var/run
EOF

# Create daily AIDE check
cat > /etc/cron.daily/aide << 'EOF'
#!/bin/bash
/usr/bin/aide --check
EOF

chmod +x /etc/cron.daily/aide

if [[ $? -ne 0 ]]; then
    print_error "Failed to configure AIDE"
    exit 1
fi
print_success "AIDE configured"

# Configure Auditd
print_status "Configuring Auditd"

# Create audit rules
cat > /etc/audit/rules.d/chatbot-saas.rules << 'EOF'
# Audit rules for Chatbot SaaS v2.1

# Monitor file access to sensitive files
-w /etc/passwd -p wa -k identity
-w /etc/shadow -p wa -k identity
-w /etc/group -p wa -k identity
-w /etc/gshadow -p wa -k identity
-w /etc/sudoers -p wa -k identity

# Monitor SSH configuration
-w /etc/ssh/sshd_config -p wa -k ssh_config
-w /etc/ssh/sshd_config.d -p wa -k ssh_config

# Monitor Nginx configuration
-w /etc/nginx/nginx.conf -p wa -k nginx_config
-w /etc/nginx/conf.d -p wa -k nginx_config

# Monitor Docker
-w /var/lib/docker -p wa -k docker
-w /etc/docker -p wa -k docker

# Monitor system calls
-a always,exit -F arch=b64 -S execve -k process_creation
-a always,exit -F arch=b32 -S execve -k process_creation

# Monitor network configuration
-w /etc/network/interfaces -p wa -k network_config
-w /etc/hosts -p wa -k network_config

# Monitor log files
-w /var/log/ -p wa -k log_files
EOF

# Restart auditd
systemctl restart auditd
systemctl enable auditd

if [[ $? -ne 0 ]]; then
    print_error "Failed to configure Auditd"
    exit 1
fi
print_success "Auditd configured"

# Configure Unattended Upgrades
print_status "Configuring unattended upgrades"

cat > /etc/apt/apt.conf.d/50unattended-upgrades << 'EOF'
Unattended-Upgrade::Allowed-Origins {
    "${distro_id}:${distro_codename}";
    "${distro_id}:${distro_codename}-security";
    "${distro_id}:${distro_codename}-updates";
};

Unattended-Upgrade::Package-Blacklist {
};

Unattended-Upgrade::AutoFixInterruptedDpkg "true";
Unattended-Upgrade::MinimalSteps "true";
Unattended-Upgrade::InstallOnShutdown "false";
Unattended-Upgrade::Remove-Unused-Dependencies "true";
Unattended-Upgrade::Automatic-Reboot "false";
EOF

cat > /etc/apt/apt.conf.d/20auto-upgrades << 'EOF'
APT::Periodic::Update-Package-Lists "1";
APT::Periodic::Download-Upgradeable-Packages "1";
APT::Periodic::AutocleanInterval "7";
APT::Periodic::Unattended-Upgrade "1";
EOF

# Enable unattended upgrades
systemctl enable unattended-upgrades
systemctl restart unattended-upgrades

if [[ $? -ne 0 ]]; then
    print_error "Failed to configure unattended upgrades"
    exit 1
fi
print_success "Unattended upgrades configured"

# Configure Logrotate
print_status "Configuring logrotate"

cat > /etc/logrotate.d/chatbot-saas << 'EOF'
/var/log/chatbot-saas/*.log {
    daily
    missingok
    rotate 30
    compress
    delaycompress
    notifempty
    create 644 root root
    postrotate
        systemctl reload rsyslog
    endscript
}

/var/log/nginx/chatbot-saas-*.log {
    daily
    missingok
    rotate 30
    compress
    delaycompress
    notifempty
    create 644 www-data www-data
    postrotate
        systemctl reload nginx
    endscript
}
EOF

if [[ $? -ne 0 ]]; then
    print_error "Failed to configure logrotate"
    exit 1
fi
print_success "Logrotate configured"

# Configure System Limits
print_status "Configuring system limits"

cat > /etc/security/limits.d/chatbot-saas.conf << 'EOF'
# System limits for Chatbot SaaS v2.1
* soft nofile 65536
* hard nofile 65536
* soft nproc 65536
* hard nproc 65536
* soft memlock unlimited
* hard memlock unlimited
EOF

# Configure kernel parameters
cat > /etc/sysctl.d/99-chatbot-saas.conf << 'EOF'
# Kernel parameters for Chatbot SaaS v2.1

# Network security
net.ipv4.ip_forward = 0
net.ipv4.conf.all.send_redirects = 0
net.ipv4.conf.default.send_redirects = 0
net.ipv4.conf.all.accept_source_route = 0
net.ipv4.conf.default.accept_source_route = 0
net.ipv4.conf.all.accept_redirects = 0
net.ipv4.conf.default.accept_redirects = 0
net.ipv4.conf.all.secure_redirects = 0
net.ipv4.conf.default.secure_redirects = 0
net.ipv4.conf.all.log_martians = 1
net.ipv4.conf.default.log_martians = 1
net.ipv4.icmp_echo_ignore_broadcasts = 1
net.ipv4.icmp_ignore_bogus_error_responses = 1
net.ipv4.conf.all.rp_filter = 1
net.ipv4.conf.default.rp_filter = 1

# SYN flood protection
net.ipv4.tcp_syncookies = 1
net.ipv4.tcp_max_syn_backlog = 2048
net.ipv4.tcp_synack_retries = 2
net.ipv4.tcp_syn_retries = 5

# Memory and process limits
vm.swappiness = 10
vm.dirty_ratio = 15
vm.dirty_background_ratio = 5
EOF

# Apply kernel parameters
sysctl -p /etc/sysctl.d/99-chatbot-saas.conf

if [[ $? -ne 0 ]]; then
    print_error "Failed to configure system limits"
    exit 1
fi
print_success "System limits configured"

# Security scan with Lynis
print_status "Running security scan with Lynis"
lynis audit system --quick --no-color > /var/log/lynis-report.txt

if [[ $? -ne 0 ]]; then
    print_warning "Lynis scan completed with warnings"
else
    print_success "Lynis scan completed"
fi

# Display security status
print_status "Security Status Summary"
echo "=================================="
echo "Firewall: $(ufw status | head -1)"
echo "Fail2Ban: $(systemctl is-active fail2ban)"
echo "AIDE: $(systemctl is-active aide)"
echo "Auditd: $(systemctl is-active auditd)"
echo "Unattended Upgrades: $(systemctl is-active unattended-upgrades)"
echo ""

# Display useful commands
print_status "Security Management Commands"
echo "=================================="
echo "Check firewall status: ufw status verbose"
echo "Check Fail2Ban status: fail2ban-client status"
echo "Check audit logs: ausearch -k recent"
echo "Run AIDE check: aide --check"
echo "View Lynis report: cat /var/log/lynis-report.txt"
echo ""

# Display next steps
print_status "Security Hardening Complete!"
echo "=================================="
echo "1. Review Lynis report for additional recommendations"
echo "2. Configure email alerts for security events"
echo "3. Set up regular security scans"
echo "4. Monitor security logs regularly"
echo "5. Test security configurations"
echo ""

print_success "Security hardening completed successfully!"
echo "Your Chatbot SaaS v2.1 is now secured with production-grade security measures"
