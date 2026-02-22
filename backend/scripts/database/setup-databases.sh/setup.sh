#!/bin/bash

# Database Setup Script
# Creates separate databases for each hub

set -e

# Database connection parameters
DB_HOST=${DB_HOST:-localhost}
DB_PORT=${DB_PORT:-5432}
DB_USER=${DB_USER:-postgres}
DB_PASSWORD=${DB_PASSWORD:-password}

# Database names
IDENTITY_DB="chatbot_identity_db"
USER_DB="chatbot_user_db"
TENANT_DB="chatbot_tenant_db"
APP_DB="chatbot_app_db"
BILLING_DB="chatbot_billing_db"
WALLET_DB="chatbot_wallet_db"
CONFIG_DB="chatbot_config_db"
MESSAGE_DB="chatbot_message_db"
SPOKES_DB="chatbot_spokes_db"

echo "🚀 Setting up Chatbot SaaS databases..."

# Function to create database
create_database() {
    local db_name=$1
    echo "📦 Creating database: $db_name"
    
    PGPASSWORD=$DB_PASSWORD psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d postgres -c "CREATE DATABASE $db_name;" || {
        echo "⚠️  Database $db_name already exists or failed to create"
    }
    
    echo "✅ Database $db_name ready"
}

# Create all databases
create_database $IDENTITY_DB
create_database $USER_DB
create_database $TENANT_DB
create_database $APP_DB
create_database $BILLING_DB
create_database $WALLET_DB
create_database $CONFIG_DB
create_database $MESSAGE_DB
create_database $SPOKES_DB

# Grant permissions
echo "🔐 Setting up permissions..."
PGPASSWORD=$DB_PASSWORD psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d postgres -c "
    GRANT ALL PRIVILEGES ON DATABASE $IDENTITY_DB TO $DB_USER;
    GRANT ALL PRIVILEGES ON DATABASE $USER_DB TO $DB_USER;
    GRANT ALL PRIVILEGES ON DATABASE $TENANT_DB TO $DB_USER;
    GRANT ALL PRIVILEGES ON DATABASE $APP_DB TO $DB_USER;
    GRANT ALL PRIVILEGES ON DATABASE $BILLING_DB TO $DB_USER;
    GRANT ALL PRIVILEGES ON DATABASE $WALLET_DB TO $DB_USER;
    GRANT ALL PRIVILEGES ON DATABASE $CONFIG_DB TO $DB_USER;
    GRANT ALL PRIVILEGES ON DATABASE $MESSAGE_DB TO $DB_USER;
    GRANT ALL PRIVILEGES ON DATABASE $SPOKES_DB TO $DB_USER;
"

echo "🎉 Database setup completed successfully!"
echo ""
echo "📋 Summary of created databases:"
echo "  - $IDENTITY_DB (Identity Hub)"
echo "  - $USER_DB (User Hub)"
echo "  - $TENANT_DB (Tenant Hub)"
echo "  - $APP_DB (App Hub)"
echo "  - $BILLING_DB (Billing Hub)"
echo "  - $WALLET_DB (Wallet Hub)"
echo "  - $CONFIG_DB (Config Hub)"
echo "  - $MESSAGE_DB (Message Hub)"
echo "  - $SPOKES_DB (Spokes)"
echo ""
echo "🔗 Connection details:"
echo "  Host: $DB_HOST"
echo "  Port: $DB_PORT"
echo "  User: $DB_USER"
