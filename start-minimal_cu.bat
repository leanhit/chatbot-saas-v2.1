@echo off
echo Starting minimal setup for trailoitduong.com backend...

echo.
echo 1. Starting Docker services (PostgreSQL, Redis, MinIO)...
docker compose up -d

echo.
echo 2. Waiting for services to start (30 seconds)...
timeout /t 30 /nobreak

echo.
echo 3. Starting backend with minimal profile...
cd backend
.\gradlew.bat bootRun -Duser.timezone=Asia/Ho_Chi_Minh --args="--spring.profiles.active=minimal"

pause
