# Disaster Recovery Plan for Chatbot SaaS v2.1

## Overview

This document outlines the comprehensive disaster recovery procedures for Chatbot SaaS v2.1, ensuring business continuity and minimal downtime in case of system failures.

## Recovery Objectives

### Recovery Time Objective (RTO)
- **Critical Services**: 4 hours
- **All Services**: 8 hours
- **Full System**: 24 hours

### Recovery Point Objective (RPO)
- **Database**: 1 hour
- **Application Data**: 4 hours
- **Configuration**: 24 hours

## Disaster Scenarios

### Scenario 1: Database Corruption/Failure
**Impact**: High - Complete system unavailability
**Recovery Time**: 2-4 hours
**Procedure**:
1. Assess damage extent
2. Stop application services
3. Restore from latest database backup
4. Verify data integrity
5. Restart application services
6. Monitor system health

### Scenario 2: Application Server Failure
**Impact**: Medium - Service unavailability
**Recovery Time**: 1-2 hours
**Procedure**:
1. Identify failed components
2. Restore application from backup
3. Update configuration
4. Restart services
5. Verify functionality

### Scenario 3: Infrastructure Failure
**Impact**: High - Complete system failure
**Recovery Time**: 4-8 hours
**Procedure**:
1. Assess infrastructure damage
2. Provision new infrastructure
3. Restore from backups
4. Update DNS records
5. Test all services

### Scenario 4: Data Loss/Deletion
**Impact**: Variable - Depends on data type
**Recovery Time**: 2-6 hours
**Procedure**:
1. Identify lost data scope
2. Restore from appropriate backup
3. Verify data consistency
4. Communicate with stakeholders

## Recovery Procedures

### Phase 1: Assessment (0-30 minutes)

#### 1.1 Initial Assessment
```bash
# Check system status
systemctl status docker
systemctl status nginx
systemctl status postgresql
systemctl status redis

# Check application health
curl -f http://localhost:8080/actuator/health
curl -f http://localhost:3000
curl -f http://localhost:9000/minio/health/live

# Check database connectivity
pg_isready -h localhost -U traloitudong_user
redis-cli -p 6380 ping
```

#### 1.2 Damage Assessment
```bash
# Check logs for errors
tail -f /var/log/chatbot-saas/application.log
tail -f /var/log/nginx/error.log
tail -f /var/log/postgresql/postgresql.log

# Check system resources
df -h
free -h
top -bn1 | head -20
```

### Phase 2: Isolation (30-60 minutes)

#### 2.1 Stop Affected Services
```bash
# Stop application services
docker-compose -f docker-compose.production.yml down

# Stop system services
systemctl stop nginx
systemctl stop postgresql
systemctl stop redis-server
```

#### 2.2 Preserve Evidence
```bash
# Create evidence directory
mkdir -p /tmp/disaster_recovery/$(date +%Y%m%d_%H%M%S)

# Copy current state
cp -r /var/log/chatbot-saas /tmp/disaster_recovery/$(date +%Y%m%d_%H%M%S)/
cp -r /etc/nginx /tmp/disaster_recovery/$(date +%Y%m%d_%H%M%S)/
docker ps -a > /tmp/disaster_recovery/$(date +%Y%m%d_%H%M%S)/docker_containers.txt
```

### Phase 3: Recovery (1-4 hours)

#### 3.1 Database Recovery
```bash
# Identify latest backup
LATEST_BACKUP=$(ls -t /backups/chatbot-saas/*/traloitudong_db_*.sql.gz | head -1)

# Restore database
gunzip -c "$LATEST_BACKUP" | psql -h localhost -U traloitudong_user -d traloitudong_db

# Verify database integrity
psql -h localhost -U traloitudong_user -d traloitudong_db -c "SELECT COUNT(*) FROM users;"
```

#### 3.2 Application Recovery
```bash
# Restore application files
LATEST_APP_BACKUP=$(ls -t /backups/chatbot-saas/*/application/source_code_*.tar.gz | head -1)
tar -xzf "$LATEST_APP_BACKUP" -C /root/

# Restore configuration
LATEST_CONFIG_BACKUP=$(ls -t /backups/chatbot-saas/*/application/config_*.tar.gz | head -1)
tar -xzf "$LATEST_CONFIG_BACKUP" -C /root/ltanh/chatbot-saas-v2.1/backend/

# Restore Docker volumes
for volume in chatbot_saas_postgres_data chatbot_saas_redis_data chatbot_saas_minio_data; do
    LATEST_VOLUME_BACKUP=$(ls -t /backups/chatbot-saas/*/application/volume_${volume}_*.tar.gz | head -1)
    if [[ -f "$LATEST_VOLUME_BACKUP" ]]; then
        docker run --rm -v "$volume":/data -v "$(dirname "$LATEST_VOLUME_BACKUP")":/backup alpine tar -xzf /backup/$(basename "$LATEST_VOLUME_BACKUP") -C /data
    fi
done
```

#### 3.3 System Configuration Recovery
```bash
# Restore system configuration
LATEST_SYSTEM_BACKUP=$(ls -t /backups/chatbot-saas/*/application/system_*.tar.gz | head -1)
tar -xzf "$LATEST_SYSTEM_BACKUP" -C /

# Restore SSL certificates
LATEST_SSL_BACKUP=$(ls -t /backups/chatbot-saas/*/application/ssl_*.tar.gz | head -1)
if [[ -f "$LATEST_SSL_BACKUP" ]]; then
    tar -xzf "$LATEST_SSL_BACKUP" -C /
fi

# Apply system configuration
sysctl -p /etc/sysctl.d/99-chatbot-saas-performance.conf
systemctl restart systemd-sysctl
```

### Phase 4: Validation (4-6 hours)

#### 4.1 Service Validation
```bash
# Start services in order
systemctl start postgresql
systemctl start redis-server
systemctl start nginx

# Start application
cd /root/ltanh/chatbot-saas-v2.1/backend
docker-compose -f docker-compose.production.yml up -d

# Wait for services to start
sleep 60

# Validate services
curl -f http://localhost:8080/actuator/health
curl -f http://localhost:3000
curl -f http://localhost:9000/minio/health/live
```

#### 4.2 Data Validation
```bash
# Validate database data
psql -h localhost -U traloitudong_user -d traloitudong_db -c "
SELECT 
    (SELECT COUNT(*) FROM users) as user_count,
    (SELECT COUNT(*) FROM chatbots) as chatbot_count,
    (SELECT COUNT(*) FROM messages) as message_count;
"

# Validate application functionality
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"test123"}'
```

#### 4.3 Performance Validation
```bash
# Check system performance
/usr/local/bin/performance-monitor.sh

# Check application performance
curl -s http://localhost:8080/actuator/metrics | grep jvm.memory.used
```

### Phase 5: Communication (6-8 hours)

#### 5.1 Internal Communication
- Notify development team
- Notify operations team
- Document recovery actions
- Update monitoring systems

#### 5.2 External Communication
- Notify stakeholders if needed
- Update status pages
- Prepare customer communications

## Emergency Contacts

### Primary Contacts
- **System Administrator**: admin@yourdomain.com
- **DevOps Lead**: devops@yourdomain.com
- **Database Administrator**: dba@yourdomain.com

### Secondary Contacts
- **Application Support**: support@yourdomain.com
- **Management**: management@yourdomain.com

## Backup Locations

### Local Backups
- **Path**: /backups/chatbot-saas/
- **Retention**: 30 days
- **Types**: Database, Application, Configuration

### Remote Backups
- **Provider**: AWS S3 / Google Cloud Storage
- **Path**: s3://chatbot-saas-backups/
- **Retention**: 90 days
- **Types**: Full system backups

### Off-site Backups
- **Location**: Secondary data center
- **Frequency**: Daily
- **Types**: Critical data only

## Recovery Testing

### Monthly Testing
- Test database restore procedures
- Test application restore procedures
- Validate backup integrity
- Update recovery documentation

### Quarterly Testing
- Full disaster recovery drill
- Test communication procedures
- Validate recovery time objectives
- Update recovery procedures

### Annual Testing
- Complete system recovery test
- Test all disaster scenarios
- Validate recovery point objectives
- Update disaster recovery plan

## Prevention Measures

### Redundancy
- Database replication
- Load balancing
- Failover systems
- Geographic distribution

### Monitoring
- System health monitoring
- Performance monitoring
- Backup monitoring
- Security monitoring

### Automation
- Automated backups
- Automated failover
- Automated recovery procedures
- Automated notifications

## Post-Recovery Actions

### 1. Root Cause Analysis
- Investigate disaster cause
- Document findings
- Implement preventive measures
- Update procedures

### 2. System Hardening
- Apply security patches
- Update configurations
- Improve monitoring
- Enhance backup procedures

### 3. Documentation Updates
- Update recovery procedures
- Document lessons learned
- Update contact information
- Update system documentation

### 4. Training
- Train staff on new procedures
- Conduct recovery drills
- Update knowledge base
- Share lessons learned

## Checklist

### Pre-Disaster Checklist
- [ ] Backups are current and verified
- [ ] Recovery procedures are documented
- [ ] Contact information is current
- [ ] Recovery tools are available
- [ ] Monitoring systems are operational

### Post-Disaster Checklist
- [ ] All services are operational
- [ ] Data integrity is verified
- [ ] Performance is acceptable
- [ ] Security is maintained
- [ ] Documentation is updated

### Monthly Checklist
- [ ] Backup integrity verified
- [ ] Recovery procedures tested
- [ ] Contact information updated
- [ ] Documentation reviewed
- [ ] Training conducted

## Escalation Procedures

### Level 1: Minor Issues
- Response time: 1 hour
- Resolution time: 4 hours
- Escalation: After 2 hours

### Level 2: Major Issues
- Response time: 30 minutes
- Resolution time: 8 hours
- Escalation: After 4 hours

### Level 3: Critical Issues
- Response time: 15 minutes
- Resolution time: 24 hours
- Escalation: Immediate

## Success Criteria

### Technical Success
- All services are operational
- Data integrity is maintained
- Performance is acceptable
- Security is maintained

### Business Success
- Customer impact is minimal
- Revenue loss is minimized
- Brand reputation is maintained
- Regulatory compliance is maintained

### Operational Success
- Recovery objectives are met
- Team performance is effective
- Communication is clear
- Documentation is accurate

## Review and Maintenance

### Monthly Review
- Review recovery procedures
- Update contact information
- Validate backup systems
- Check monitoring systems

### Quarterly Review
- Conduct full recovery drill
- Update disaster scenarios
- Review success metrics
- Update prevention measures

### Annual Review
- Complete plan review
- Update all procedures
- Conduct full system test
- Update documentation

---

**Document Version**: 1.0
**Last Updated**: $(date)
**Next Review**: $(date -d "+3 months" +"%Y-%m-%d")
**Approved By**: System Administrator

This disaster recovery plan should be reviewed and updated regularly to ensure it remains current and effective.
