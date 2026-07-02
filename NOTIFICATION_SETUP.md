# Notification System Setup Guide

This guide helps you set up the notification system to work properly.

## Prerequisites

The notification system requires the following services to be running:

### 1. Redis Server

The notification acknowledgment system uses Redis to track read notifications.

**Installation:**

```bash
# On Ubuntu/Debian
sudo apt-get update
sudo apt-get install redis-server

# On macOS
brew install redis

# On Windows
# Download from https://redis.io/download
```

**Configuration:**

The backend is configured to connect to Redis at `localhost:6380` (see `application.properties`).

**Start Redis:**

```bash
# Start Redis on port 6380
redis-server --port 6380

# Or use systemd (if configured)
sudo systemctl start redis
```

**Verify Redis is running:**

```bash
redis-cli -p 6380 ping
# Should return: PONG
```

### 2. Backend Server

The backend must be running with WebSocket support enabled.

**Start Backend:**

```bash
cd backend
./gradlew bootRun
```

The backend will start on port 8080 by default.

**Verify WebSocket endpoint:**

```bash
# Test WebSocket connection
wscat -c ws://localhost:8080/ws/notifications?token=YOUR_JWT_TOKEN
```

### 3. Frontend Configuration

**Step 1: Copy environment file**

```bash
cd frontend
cp .env.example .env
```

**Step 2: Update .env with your configuration**

Edit `.env` file:

```bash
# WebSocket URL - must match your backend server
VUE_APP_WS_URL=ws://localhost:8080

# API URL
VUE_APP_API_URL=http://localhost:8080/api

# Enable notifications
VUE_APP_ENABLE_NOTIFICATIONS=true
VUE_APP_ENABLE_WEBSOCKET=true
```

**Step 3: Start Frontend**

```bash
npm install
npm run serve
```

## Testing the Notification System

### Manual Test with WebSocket

1. **Start all services:**
   - Redis on port 6380
   - Backend on port 8080
   - Frontend on port 8081 (or your configured port)

2. **Login to the application**
   - Navigate to http://localhost:8081
   - Login with your credentials

3. **Check WebSocket connection:**
   - Open browser DevTools (F12)
   - Go to Network tab
   - Filter by WS (WebSocket)
   - You should see a WebSocket connection to `/ws/notifications`

4. **Test notification:**
   - Use backend API to send a test notification
   - Or trigger an event that generates a notification (e.g., tenant invitation)

### Backend Test Endpoint

You can test the notification system using the backend's notification handler:

```java
// In your service or controller
@Autowired
private NotificationWebSocketHandler notificationWebSocketHandler;

// Send test notification
Map<String, Object> notification = Map.of(
    "type", "TEST",
    "title", "Test Notification",
    "message", "This is a test notification"
);
notificationWebSocketHandler.sendToUser(userId, notification);
```

## Troubleshooting

### WebSocket Connection Fails

**Problem:** WebSocket connection fails to establish

**Solutions:**
1. Check if backend is running: `curl http://localhost:8080/actuator/health`
2. Verify WebSocket URL in `.env` matches backend URL
3. Check browser console for WebSocket errors
4. Ensure no firewall is blocking WebSocket connections

### Redis Connection Fails

**Problem:** Backend cannot connect to Redis

**Solutions:**
1. Verify Redis is running: `redis-cli -p 6380 ping`
2. Check Redis configuration in `application.properties`
3. Ensure Redis port 6380 is not blocked by firewall
4. Check Redis logs for errors

### Notifications Not Showing

**Problem:** WebSocket connects but notifications don't appear

**Solutions:**
1. Check browser console for JavaScript errors
2. Verify notification store is initialized
3. Check if sound file exists: `/public/sounds/notification.mp3`
4. Test with different notification types
5. Check backend logs for notification sending errors

### Sound Not Playing

**Problem:** Notification sound doesn't play

**Solutions:**
1. Verify sound file exists at `/public/sounds/notification.mp3`
2. Check browser audio permissions
3. Test sound file directly in browser
4. Check browser console for audio errors

## Production Configuration

For production deployment:

1. **Use WSS (Secure WebSocket):**
   ```bash
   VUE_APP_WS_URL=wss://your-domain.com
   ```

2. **Configure Redis for production:**
   - Use Redis with authentication
   - Enable Redis persistence
   - Use Redis cluster for high availability

3. **Environment Variables:**
   ```bash
   # Production
   NODE_ENV=production
   VUE_APP_WS_URL=wss://api.yourdomain.com
   VUE_APP_API_URL=https://api.yourdomain.com/api
   ```

4. **Backend Configuration:**
   - Update Redis host/port in `application.properties`
   - Configure proper CORS settings
   - Enable SSL/TLS for WebSocket

## Monitoring

### Check WebSocket Connections

Monitor active WebSocket connections via backend logs:

```
✅ [Notification WS] Connected: user@example.com (ID: 123) for tenant 456
```

### Check Redis Operations

Monitor Redis operations:

```bash
redis-cli -p 6380 monitor
```

### Check Notification Acknowledgments

Check acknowledged notifications in Redis:

```bash
redis-cli -p 6380 keys "notification:ack:*"
```

## Security Considerations

1. **Authentication:** WebSocket connections require valid JWT token
2. **Authorization:** Users only receive notifications meant for them
3. **Rate Limiting:** Consider implementing rate limiting for WebSocket connections
4. **CORS:** Configure proper CORS settings for WebSocket endpoints
5. **Redis Security:** Enable Redis authentication in production

## Additional Resources

- [WebSocket MDN Documentation](https://developer.mozilla.org/en-US/docs/Web/API/WebSocket)
- [Redis Documentation](https://redis.io/documentation)
- [Spring WebSocket Documentation](https://docs.spring.io/spring-framework/reference/web/websocket.html)
