# PgBouncer Setup Guide for Production

## Overview

PgBouncer is a lightweight connection pooler for PostgreSQL that reduces the connection overhead on PostgreSQL servers. This guide explains how to deploy PgBouncer as a middleware between your backend applications and PostgreSQL databases in a production environment.

## Why PgBouncer?

### Current Architecture Issues
- **Multiple Connection Pools**: 8 separate HikariCP pools (Identity, User, Tenant, App, Config, Message, Spokes, Shared)
- **High Connection Count**: Single instance uses 150-200 DB connections
- **Scale Out Risk**: 3-5 backend instances can exceed PostgreSQL `max_connections` limit
- **Connection Overhead**: Each connection consumes memory and CPU on PostgreSQL

### PgBouncer Benefits
- **Connection Pooling**: Reuses connections across multiple client connections
- **Reduced Overhead**: Fewer actual connections to PostgreSQL
- **Better Scalability**: Supports more backend instances without hitting connection limits
- **Transaction Mode**: Session pooling for transaction-level connection reuse
- **Performance**: Faster connection establishment and reduced latency

## Architecture

```
┌─────────────────┐
│  Backend App 1  │
│  (HikariCP)     │
└────────┬────────┘
         │
         │ JDBC Connection
         │
┌────────▼────────┐
│   PgBouncer     │
│   (Pool Mode)   │
└────────┬────────┘
         │
         │ TCP Connection
         │
┌────────▼────────┐
│   PostgreSQL    │
│   (max_conn)    │
└─────────────────┘
```

## Installation

### Ubuntu/Debian
```bash
sudo apt-get update
sudo apt-get install pgbouncer
```

### CentOS/RHEL
```bash
sudo yum install pgbouncer
```

### Docker
```bash
docker run -d \
  --name pgbouncer \
  -p 6432:6432 \
  -e POSTGRES_HOST=postgres \
  -e POSTGRES_PORT=5432 \
  -e POSTGRES_USER=chatbot_user \
  -e POSTGRES_PASSWORD=chatbot_Admin_2025 \
  -e POSTGRES_DB=chatbot_identity_db \
  -e POOL_MODE=transaction \
  edoburu/pgbouncer
```

## Configuration

### PgBouncer Configuration File (`/etc/pgbouncer/pgbouncer.ini`)

```ini
[databases]
# Hub Databases
chatbot_identity_db = host=localhost port=5432 dbname=chatbot_identity_db
chatbot_user_db = host=localhost port=5432 dbname=chatbot_user_db
chatbot_tenant_db = host=localhost port=5432 dbname=chatbot_tenant_db
chatbot_app_db = host=localhost port=5432 dbname=chatbot_app_db
chatbot_config_db = host=localhost port=5432 dbname=chatbot_config_db
chatbot_message_db = host=localhost port=5432 dbname=chatbot_message_db

[pgbouncer]
# Listen address
listen_addr = 0.0.0.0
listen_port = 6432

# Authentication
auth_type = md5
auth_file = /etc/pgbouncer/userlist.txt

# Pooling mode
pool_mode = transaction

# Connection limits
max_client_conn = 1000
default_pool_size = 25
min_pool_size = 5
reserve_pool_size = 10
reserve_pool_timeout = 3

# Timeouts
server_lifetime = 3600
server_idle_timeout = 600
server_connect_timeout = 15
query_timeout = 300

# Logging
log_connections = 1
log_disconnections = 1
log_pooler_errors = 1
log_stats = 1
stats_period = 60

# Admin
admin_users = postgres
```

### User List File (`/etc/pgbouncer/userlist.txt`)

```
"chatbot_user" "md5 hashed_password"
"postgres" "md5 hashed_password"
```

To generate MD5 hash:
```bash
echo -n "passwordusername" | md5sum
# Example: echo -n "chatbot_Admin_2025chatbot_user" | md5sum
```

## Service Management

### Start PgBouncer
```bash
sudo systemctl start pgbouncer
```

### Enable on boot
```bash
sudo systemctl enable pgbouncer
```

### Check status
```bash
sudo systemctl status pgbouncer
```

### Reload configuration
```bash
sudo pgbouncer -R /etc/pgbouncer/pgbouncer.ini
```

## Application Configuration Update

### Update `application.properties`

Change JDBC URLs to point to PgBouncer instead of direct PostgreSQL:

```properties
# Before (Direct PostgreSQL connection)
app.datasource.identity.jdbc-url=jdbc:postgresql://localhost:5433/chatbot_identity_db

# After (PgBouncer connection)
app.datasource.identity.jdbc-url=jdbc:postgresql://pgbouncer-host:6432/chatbot_identity_db
```

### Update all datasource URLs:
```properties
# Identity Hub
app.datasource.identity.jdbc-url=jdbc:postgresql://pgbouncer-host:6432/chatbot_identity_db

# User Hub
app.datasource.user.jdbc-url=jdbc:postgresql://pgbouncer-host:6432/chatbot_user_db

# Tenant Hub
app.datasource.tenant.jdbc-url=jdbc:postgresql://pgbouncer-host:6432/chatbot_tenant_db

# App Hub
app.datasource.app.jdbc-url=jdbc:postgresql://pgbouncer-host:6432/chatbot_app_db

# Config Hub
app.datasource.config.jdbc-url=jdbc:postgresql://pgbouncer-host:6432/chatbot_config_db

# Message Hub
app.datasource.message.jdbc-url=jdbc:postgresql://pgbouncer-host:6432/chatbot_message_db
```

### Adjust HikariCP Pool Sizes

Since PgBouncer handles connection pooling, you can reduce HikariCP pool sizes:

```properties
# Identity Hub (reduced from 10 to 5 with PgBouncer)
app.datasource.identity.hikari.maximum-pool-size=5
app.datasource.identity.hikari.minimum-idle=1

# User Hub (reduced from 50 to 20 with PgBouncer)
app.datasource.user.hikari.maximum-pool-size=20
app.datasource.user.hikari.minimum-idle=5

# Tenant Hub (reduced from 20 to 10 with PgBouncer)
app.datasource.tenant.hikari.maximum-pool-size=10
app.datasource.tenant.hikari.minimum-idle=2

# App Hub (reduced from 10 to 5 with PgBouncer)
app.datasource.app.hikari.maximum-pool-size=5
app.datasource.app.hikari.minimum-idle=1

# Config Hub (reduced from 5 to 3 with PgBouncer)
app.datasource.config.hikari.maximum-pool-size=3
app.datasource.config.hikari.minimum-idle=1

# Message Hub (reduced from 30 to 15 with PgBouncer)
app.datasource.message.hikari.maximum-pool-size=15
app.datasource.message.hikari.minimum-idle=5
```

## Monitoring

### PgBouncer Admin Console
```bash
psql -h localhost -p 6432 -U postgres pgbouncer
```

### Show Statistics
```sql
SHOW STATS;
SHOW DATABASES;
SHOW LISTS;
SHOW USERS;
```

### Key Metrics to Monitor
- **cl_active**: Active client connections
- **cl_waiting**: Clients waiting for connections
- **sv_active**: Active server connections
- **sv_idle**: Idle server connections
- **sv_used**: Server connections in use
- **sv_tested**: Server connections being tested
- **maxwait**: Maximum wait time for connections

### Prometheus Integration
Use `pgbouncer_exporter` to expose metrics:
```bash
docker run -d \
  --name pgbouncer-exporter \
  -p 9127:9127 \
  -e PGBOUNCER_HOST=pgbouncer \
  -e PGBOUNCER_PORT=6432 \
  -e PGBOUNCER_USER=postgres \
  -e PGBOUNCER_PASSWORD=password \
  prometheuscommunity/pgbouncer-exporter
```

## Pool Modes

### Transaction Mode (Recommended)
- **Behavior**: Connection is returned to pool after each transaction
- **Use Case**: Most web applications with short-lived transactions
- **Pros**: Good balance of performance and connection reuse
- **Cons**: Not suitable for long-running transactions

### Session Mode
- **Behavior**: Connection is returned when client disconnects
- **Use Case**: Applications that maintain session state
- **Pros**: Maintains session-level features
- **Cons**: Less efficient connection reuse

### Statement Mode
- **Behavior**: Connection is returned after each statement
- **Use Case**: Applications with autocommit enabled
- **Pros**: Maximum connection reuse
- **Cons**: Breaks some PostgreSQL features (prepared statements, transactions)

## Connection Sizing Calculation

### Without PgBouncer
```
Total Connections = (HikariCP Pools) × (Backend Instances)
= (10 + 50 + 20 + 10 + 5 + 30) × 5
= 125 × 5 = 625 connections
```

### With PgBouncer
```
Total Connections = (PgBouncer Pool Size) × (Number of Databases)
= 25 × 6 = 150 connections (to PostgreSQL)
```

### Savings
- **Before**: 625 connections to PostgreSQL
- **After**: 150 connections to PostgreSQL
- **Reduction**: 76% fewer connections

## Best Practices

1. **Use Transaction Mode**: Recommended for most web applications
2. **Monitor Pool Usage**: Keep an eye on `sv_active` and `cl_waiting`
3. **Set Appropriate Timeouts**: Configure `server_idle_timeout` to prevent stale connections
4. **Enable Logging**: Monitor connection patterns and identify issues
5. **Regular Maintenance**: Restart PgBouncer periodically to clear stale connections
6. **Backup Configuration**: Keep version control of `pgbouncer.ini`
7. **Test Thoroughly**: Validate application behavior before production deployment

## Troubleshooting

### Connection Timeouts
- **Symptom**: Applications experiencing connection timeouts
- **Solution**: Increase `server_connect_timeout` and `reserve_pool_size`

### High Memory Usage
- **Symptom**: PgBouncer consuming excessive memory
- **Solution**: Reduce `max_client_conn` and `default_pool_size`

### Stale Connections
- **Symptom**: Errors due to closed server connections
- **Solution**: Adjust `server_lifetime` and `server_idle_timeout`

### Authentication Failures
- **Symptom**: "auth failed" errors in logs
- **Solution**: Verify MD5 hashes in `userlist.txt` match PostgreSQL

## Security Considerations

1. **Network Security**: Place PgBouncer in a private network
2. **Authentication**: Use MD5 or SCRAM-SHA-256 authentication
3. **TLS Encryption**: Enable TLS for production deployments
4. **Access Control**: Restrict admin console access
5. **Firewall Rules**: Limit access to specific IPs

## Production Deployment Checklist

- [ ] Install PgBouncer on dedicated server or container
- [ ] Configure `pgbouncer.ini` with appropriate pool sizes
- [ ] Set up authentication in `userlist.txt`
- [ ] Update application JDBC URLs to point to PgBouncer
- [ ] Reduce HikariCP pool sizes in application configuration
- [ ] Enable monitoring and alerting
- [ ] Test connection pooling with load testing
- [ ] Configure TLS encryption for production
- [ ] Set up log rotation
- [ ] Document configuration and procedures

## References

- [PgBouncer Official Documentation](https://www.pgbouncer.org/usage.html)
- [PostgreSQL Connection Pooling](https://wiki.postgresql.org/wiki/Pooling)
- [HikariCP Configuration](https://github.com/brettwooldridge/HikariCP#configuration-knobs-baby-knobs-little-knobs)
