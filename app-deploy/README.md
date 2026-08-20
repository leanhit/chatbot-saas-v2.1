# Chatbot SaaS v2.1 — Application Deployment & CI/CD Guide

Thư mục `app-deploy` chứa toàn bộ cấu hình đóng gói **All-In-One (Frontend + Backend + Nginx)** và kịch bản tự động hóa **CI/CD qua GitHub Actions**.

---

## 🏗️ 1. Cấu trúc thư mục `app-deploy`

```text
app-deploy/
├── Dockerfile                  # Multi-stage Dockerfile (Frontend Vue3 + Backend Java21 + Nginx)
├── docker-compose.yml          # Compose file quản lý duy nhất container ứng dụng
├── deploy.sh                   # Script khởi chạy/build nhanh 1-click
├── config/
│   └── application-docker.yml  # File cấu hình riêng cho môi trường Docker Container
├── nginx/
│   └── nginx.conf              # Reverse proxy cho Frontend static + Backend API/WebSocket
└── supervisor/
    └── supervisord.conf        # Quản lý tiến trình Nginx và Java Spring Boot
```

---

## 🚀 2. Chạy thủ công trên Server (Local / Production)

### Bước 1: Khởi chạy hạ tầng (`app-setup`) — Chỉ chạy 1 lần
```bash
cd ../app-setup
docker compose up -d
```

### Bước 2: Build & Khởi chạy App (`app-deploy`)
```bash
cd ../app-deploy
./deploy.sh
```

---

## ⚡ 3. Tự động hóa CI/CD với GitHub Actions

File workflow được đặt tại: `.github/workflows/deploy.yml`

### Cấu hình Secrets trên GitHub Repository:
Vào GitHub Repository -> **Settings** -> **Secrets and variables** -> **Actions** -> Thêm các secrets sau:

| Tên Secret | Ý nghĩa | Ví dụ |
| :--- | :--- | :--- |
| `SERVER_HOST` | Địa chỉ IP của máy chủ Server | `123.45.67.89` |
| `SERVER_USER` | Tên người dùng SSH trên Server | `ubuntu` hoặc `root` |
| `SERVER_SSH_KEY` | Nội dung Private Key SSH (`cat ~/.ssh/id_rsa`) | `-----BEGIN OPENSSH PRIVATE KEY-----...` |
| `SERVER_PORT` | *(Tùy chọn)* Cổng SSH | `22` (Mặc định) |
| `SERVER_APP_PATH` | *(Tùy chọn)* Đường dẫn thư mục `app-deploy` trên Server | `/home/ubuntu/ltanh/chatbot-saas-v2.1/app-deploy` |

### Quy trình tự động:
Khi bạn `git push` code mới lên branch `main` hoặc `master`:
1. GitHub Actions tự động checkout code & build Docker Image.
2. Push Image lên **GitHub Container Registry (ghcr.io)**.
3. SSH trực tiếp vào Server, pull Image mới và restart container ứng dụng.
