#!/bin/bash

echo "Waiting for PostgreSQL..."

until pg_isready -h db -U odoo_user; do
  sleep 2
done

echo "Postgres is ready. Initializing Odoo Database..."

# Chạy init Odoo database và module base
odoo -d odoo_db -i base --db_host=db --db_user=odoo_user --db_password=odoo_Admin_2025

echo "Starting Odoo server..."
exec /entrypoint.sh odoo
