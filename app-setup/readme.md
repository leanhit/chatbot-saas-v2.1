
1. cấp quyền, trong thư mục chứa docker-compose.yml, chạy lệnh:

chmod +x odoo-init.sh

2. chạy

docker compose up -d 

3. tắt
3.1 clear 
docker compose -p traloitudongV2 down -v --rmi all --remove-orphans
docker compose down -v

===================================================================

## Kết nối PostgreSQL

**Tên container:** `chatbot_saas_postgres`
**Database:** `traloitudong_db`
**User:** `traloitudong_user`

### Cách 1: Kết nối trực tiếp
```bash
docker exec -it chatbot_saas_postgres psql -U traloitudong_user -d traloitudong_db
```

### Cách 2: Kết nối vào postgres database trước (khi cần drop/create database)
```bash
# Kết nối vào postgres database
docker exec -it chatbot_saas_postgres psql -U traloitudong_user -d postgres

# Sau đó drop database
DROP DATABASE traloitudong_db;

-- Kết nối vào template1 và chạy:
ALTER DATABASE template1 REFRESH COLLATION VERSION;

-- Sau đó thử lại lệnh tạo DB:
CREATE DATABASE traloitudong_db OWNER traloitudong_user;
```

### Cách 3: Dùng lệnh trực tiếp (không cần TTY)
```bash
# Xem tables
docker exec chatbot_saas_postgres psql -U traloitudong_user -d traloitudong_db -c "\dt"

# Xem packages
docker exec chatbot_saas_postgres psql -U traloitudong_user -d traloitudong_db -c "SELECT package_id, name, chatbot_limit FROM packages;"

# Reset database
docker exec chatbot_saas_postgres psql -U traloitudong_user -d postgres -c "DROP DATABASE traloitudong_db; CREATE DATABASE traloitudong_db;"
```

