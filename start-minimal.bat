@echo off
title START MINIMAL BACKEND
setlocal

echo ========================================
echo START MINIMAL BACKEND
echo Project: traloitudong.com
echo ========================================

echo.
echo [OK] Force JVM timezone
set JAVA_TOOL_OPTIONS=-Duser.timezone=Asia/Ho_Chi_Minh

echo.
echo [1/3] Starting Docker services...
docker compose up -d

echo.
echo [2/3] Waiting for PostgreSQL (30s)...
timeout /t 30 /nobreak > nul

echo.
echo [3/3] Starting Spring Boot (FOREGROUND)...
cd backend

call gradlew bootRun --no-daemon --console=plain --args="--spring.profiles.active=minimal"

echo.
echo ========================================
echo BACKEND STOPPED
echo ========================================
pause
