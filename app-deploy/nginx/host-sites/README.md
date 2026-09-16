# Host Nginx Site Configurations

This directory contains host-level Nginx reverse proxy site configurations for dual-domain deployment (`truyenthongviet.vn` and `startai.vn`).

## Site Configurations Included

1. **`startai.vn.conf`**: Handles HTTP -> HTTPS redirect and SSL reverse proxy to `http://127.0.0.1:8080` for `startai.vn` and `www.startai.vn`.
2. **`truyenthongviet.vn.conf`**: Handles HTTP -> HTTPS redirect and SSL reverse proxy to `http://127.0.0.1:8080` for `truyenthongviet.vn` and `www.truyenthongviet.vn`.

## Installation / Restore Commands

To install or restore these site configurations on a VPS server:

```bash
# Copy site configurations to /etc/nginx/sites-available/
sudo cp app-deploy/nginx/host-sites/startai.vn.conf /etc/nginx/sites-available/
sudo cp app-deploy/nginx/host-sites/truyenthongviet.vn.conf /etc/nginx/sites-available/

# Create symbolic links to enable sites
sudo ln -sf /etc/nginx/sites-available/startai.vn.conf /etc/nginx/sites-enabled/
sudo ln -sf /etc/nginx/sites-available/truyenthongviet.vn.conf /etc/nginx/sites-enabled/

# Test configuration and reload Nginx
sudo nginx -t
sudo systemctl reload nginx
```
