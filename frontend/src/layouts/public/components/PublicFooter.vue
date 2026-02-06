<template>
  <footer class="public-footer">
    <div class="footer-container">
      <div class="footer-content">
        <!-- Brand Section -->
        <div class="footer-section">
          <div class="footer-brand">
            <div class="brand-logo">
              <i class="mdi mdi-robot"></i>
            </div>
            <span class="brand-text">Traloitudong.com</span>
          </div>
          <p class="footer-description">
            Nền tảng trả lời tự động thông minh cho doanh nghiệp của bạn. 
            Tăng cường trải nghiệm khách hàng và tối ưu hóa vận hành.
          </p>
          <div class="footer-social">
            <a href="#" class="social-link">
              <i class="mdi mdi-facebook"></i>
            </a>
            <a href="#" class="social-link">
              <i class="mdi mdi-twitter"></i>
            </a>
            <a href="#" class="social-link">
              <i class="mdi mdi-linkedin"></i>
            </a>
          </div>
        </div>

        <!-- Product Section -->
        <div class="footer-section">
          <h3 class="footer-title">Sản phẩm</h3>
          <ul class="footer-links">
            <li><a href="/features">Tính năng</a></li>
            <li><a href="/pricing">Bảng giá</a></li>
            <li><a href="#">Tích hợp</a></li>
            <li><a href="#">API</a></li>
          </ul>
        </div>

        <!-- Company Section -->
        <div class="footer-section">
          <h3 class="footer-title">Công ty</h3>
          <ul class="footer-links">
            <li><a href="#">Về chúng tôi</a></li>
            <li><a href="#">Blog</a></li>
            <li><a href="#">Tuyển dụng</a></li>
            <li><a href="#">Liên hệ</a></li>
          </ul>
        </div>

        <!-- Support Section -->
        <div class="footer-section">
          <h3 class="footer-title">Hỗ trợ</h3>
          <ul class="footer-links">
            <li><a href="#">Trung tâm trợ giúp</a></li>
            <li><a href="#">Tài liệu</a></li>
            <li><a href="#">Cộng đồng</a></li>
            <li><a href="#">Status</a></li>
          </ul>
        </div>
      </div>

      <!-- Bottom Bar -->
      <div class="footer-bottom">
        <div class="footer-bottom-content">
          <p class="copyright">
            © 2024 Traloitudong.com. Đã đăng ký bản quyền.
          </p>
          <div class="footer-legal">
            <a href="#">Điều khoản</a>
            <a href="#">Quyền riêng tư</a>
          </div>
        </div>
      </div>
    </div>
  </footer>
</template>

<script setup lang="ts">
// No logic needed - static footer
</script>

<style scoped>
.public-footer {
  background: #111827;
  color: white;
  margin-top: auto;
}

.footer-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
}

.footer-content {
  display: grid;
  grid-template-columns: 2fr 1fr 1fr 1fr;
  gap: 48px;
  padding: 64px 0;
}

.footer-section {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.footer-brand {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.brand-logo {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 20px;
}

.brand-text {
  font-size: 20px;
  font-weight: 700;
  color: white;
}

.footer-description {
  color: #9ca3af;
  line-height: 1.6;
  margin-bottom: 24px;
}

.footer-social {
  display: flex;
  gap: 16px;
}

.social-link {
  width: 40px;
  height: 40px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #9ca3af;
  text-decoration: none;
  transition: all 0.2s ease;
}

.social-link:hover {
  background: rgba(255, 255, 255, 0.2);
  color: white;
}

.footer-title {
  font-size: 16px;
  font-weight: 600;
  color: white;
  margin: 0;
}

.footer-links {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.footer-links a {
  color: #9ca3af;
  text-decoration: none;
  transition: color 0.2s ease;
}

.footer-links a:hover {
  color: white;
}

.footer-bottom {
  border-top: 1px solid #374151;
  padding: 24px 0;
}

.footer-bottom-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.copyright {
  color: #9ca3af;
  margin: 0;
}

.footer-legal {
  display: flex;
  gap: 24px;
}

.footer-legal a {
  color: #9ca3af;
  text-decoration: none;
  transition: color 0.2s ease;
}

.footer-legal a:hover {
  color: white;
}

/* Responsive */
@media (max-width: 1024px) {
  .footer-content {
    grid-template-columns: 1fr 1fr;
    gap: 32px;
  }
}

@media (max-width: 768px) {
  .footer-container {
    padding: 0 16px;
  }
  
  .footer-content {
    grid-template-columns: 1fr;
    gap: 32px;
    padding: 48px 0;
  }
  
  .footer-bottom-content {
    flex-direction: column;
    gap: 16px;
    text-align: center;
  }
  
  .footer-legal {
    justify-content: center;
  }
}
</style>
