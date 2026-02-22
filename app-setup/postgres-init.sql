-- 1. Tạo Users
CREATE USER botpress_user WITH PASSWORD 'botpress_Admin_2025';
CREATE USER odoo_user WITH PASSWORD 'odoo_Admin_2025';
CREATE USER traloitudong_user WITH PASSWORD 'traloitudong_Admin_2025';

-- 2. Tạo Databases
CREATE DATABASE botpress_db OWNER botpress_user;
CREATE DATABASE odoo_db OWNER odoo_user;
CREATE DATABASE traloitudong_db OWNER traloitudong_user;

-- 3. Cấp quyền
GRANT ALL PRIVILEGES ON DATABASE botpress_db TO botpress_user;
GRANT ALL PRIVILEGES ON DATABASE odoo_db TO odoo_user;
GRANT ALL PRIVILEGES ON DATABASE traloitudong_db TO traloitudong_user;

-- 4. Enable pgvector extension cho vector search
\c traloitudong_db;
CREATE EXTENSION IF NOT EXISTS vector;

-- 5. Cấu hình additional settings
ALTER USER traloitudong_user CREATEDB;
ALTER USER botpress_user CREATEDB;
ALTER USER odoo_user CREATEDB;

-- 6. Grant connection privileges
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO traloitudong_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO botpress_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO odoo_user;

-- 7. Grant sequence privileges
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO traloitudong_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO botpress_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO odoo_user;

