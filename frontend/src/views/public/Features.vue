<template>
  <div class="features-page">
    <!-- Hero Section -->
    <section class="hero-section">
      <div class="container">
        <div class="hero-content">
          <h1 class="hero-title">Tính năng mạnh mẽ</h1>
          <p class="hero-description">
            Khám phá toàn bộ công cụ và tính năng giúp doanh nghiệp của bạn 
            tự động hóa dịch vụ khách hàng và tăng trưởng hiệu quả.
          </p>
        </div>
      </div>
    </section>

    <!-- Core Features -->
    <section class="core-features">
      <div class="container">
        <div class="section-header">
          <h2 class="section-title">Tính năng cốt lõi</h2>
          <p class="section-subtitle">Những công cụ mạnh mẽ nhất cho chatbot của bạn</p>
        </div>
        
        <div class="features-grid">
          <div class="feature-card">
            <img src="/images/natural-chat.png" alt="Natural Chat" class="feature-image">
            <h3 class="feature-title">Trả lời tự nhiên</h3>
            <p class="feature-description">
              AI trả lời như người thật, hài hước và giữ chân khách hàng đến 15 giây.
            </p>
          </div>

          <div class="feature-card">
            <img src="/images/multi-platform.png" alt="24/7 Operation" class="feature-image">
            <h3 class="feature-title">Chốt đơn 24/7</h3>
            <p class="feature-description">
              Tăng 50% đơn hàng ban đêm và ngày lễ khi nhân viên đang nghỉ.
            </p>
          </div>

          <div class="feature-card">
            <img src="/images/customer-segmentation.png" alt="Customer Segmentation" class="feature-image">
            <h3 class="feature-title">Phân loại khách hàng</h3>
            <p class="feature-description">
              Tag màu sắc: Xanh (có SĐT), Vàng (chuyển đổi tốt), Đỏ (cần support).
            </p>
          </div>

          <div class="feature-card">
            <img src="/images/image-responses.png" alt="Image Responses" class="feature-image">
            <h3 class="feature-title">Trả lời bằng hình ảnh</h3>
            <p class="feature-description">
              Gửi ảnh sản phẩm trực tiếp, tăng tỷ lệ chốt đơn lên 30%.
            </p>
          </div>

          <div class="feature-card">
            <img src="/images/facebook-ads.png" alt="Facebook Ads" class="feature-image">
            <h3 class="feature-title">Tích hợp Facebook Ads</h3>
            <p class="feature-description">
              Tăng điểm chất lượng quảng cáo, giảm chi phí và tăng chuyển đổi.
            </p>
          </div>

          <div class="feature-card">
            <img src="/images/llm-smart.png" alt="LLM Smart" class="feature-image">
            <h3 class="feature-title">LLM thông minh</h3>
            <p class="feature-description">
              Trả lời các câu hỏi phức tạp ngoài kịch bản bán hàng.
            </p>
          </div>
        </div>
      </div>
    </section>

    <!-- CTA Section -->
    <section class="cta-section">
      <div class="container">
        <div class="cta-content">
          <h2 class="cta-title">Sẵn sàng trải nghiệm?</h2>
          <p class="cta-description">
            Bắt đầu ngay với gói miễn phí hoặc liên hệ chúng tôi để được tư vấn 
            giải pháp phù hợp nhất cho doanh nghiệp của bạn.
          </p>
          <div class="cta-actions">
            <router-link to="/login" class="btn btn-primary btn-large">
              <i class="mdi mdi-rocket-launch"></i>
              Dùng thử miễn phí
            </router-link>
            <router-link to="/pricing" class="btn btn-outline btn-large">
              <i class="mdi mdi-tag"></i>
              Xem bảng giá
            </router-link>
          </div>
        </div>
      </div>
    </section>

    <!-- Integration Section -->
    <section class="integration-section">
      <div class="container">
        <div class="section-header">
          <h2 class="section-title">Tích hợp dễ dàng</h2>
          <p class="section-subtitle">Kết nối nhanh với các nền tảng bạn đang sử dụng</p>
        </div>
        
        <div class="integration-grid">
          <div class="integration-item">
            <div class="integration-icon">
              <i class="mdi mdi-facebook"></i>
            </div>
            <h3 class="integration-title">Facebook</h3>
            <p class="integration-description">Fanpage & Messenger</p>
          </div>

          <div class="integration-item">
            <div class="integration-icon">
              <i class="mdi mdi-web"></i>
            </div>
            <h3 class="integration-title">Website</h3>
            <p class="integration-description">Embed & Popup</p>
          </div>

          <div class="integration-item">
            <div class="integration-icon">
              <i class="mdi mdi-instagram"></i>
            </div>
            <h3 class="integration-title">Instagram</h3>
            <p class="integration-description">Direct Messages</p>
          </div>

          <div class="integration-item">
            <div class="integration-icon">
              <i class="mdi mdi-zalo"></i>
            </div>
            <h3 class="integration-title">Zalo</h3>
            <p class="integration-description">Zalo OA</p>
          </div>

          <div class="integration-item">
            <div class="integration-icon">
              <i class="mdi mdi-shopping"></i>
            </div>
            <h3 class="integration-title">Lazada</h3>
            <p class="integration-description">Chat Seller Center</p>
          </div>

          <div class="integration-item">
            <div class="integration-icon">
              <i class="mdi mdi-music-note"></i>
            </div>
            <h3 class="integration-title">TikTok</h3>
            <p class="integration-description">TikTok Shop</p>
          </div>
        </div>
      </div>
    </section>

    <!-- CTA Section -->
    <section class="cta-section">
      <div class="container">
        <div class="cta-content">
          <h2 class="cta-title">Sẵn sàng tự động hóa bán hàng?</h2>
          <p class="cta-description">
            Khám phá toàn bộ tính năng và bắt đầu tăng doanh thu ngay hôm nay.
          </p>
          <div class="cta-actions">
            <router-link to="/login" class="btn btn-primary btn-large">
              <i class="mdi mdi-rocket-launch"></i>
              Bắt đầu miễn phí
            </router-link>
            <router-link to="/pricing" class="btn btn-outline btn-large">
              <i class="mdi mdi-tag"></i>
              Xem bảng giá
            </router-link>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
// No logic needed - static features page
</script>

<style scoped>
.features-page {
  width: 100%;
}

/* Hero Section */
.hero-section {
  background: linear-gradient(135deg, #fafbfc 0%, #f3f4f6 100%);
  padding: 120px 0 80px;
  margin-top: 72px;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
}

.hero-content {
  text-align: center;
  max-width: 800px;
  margin: 0 auto;
}

.hero-title {
  font-size: 48px;
  font-weight: 700;
  color: #111827;
  margin: 0 0 24px 0;
  letter-spacing: -0.025em;
}

.hero-description {
  font-size: 20px;
  color: #6b7280;
  line-height: 1.6;
  margin: 0;
}

/* Core Features */
.core-features {
  padding: 80px 0;
  background: white;
}

.section-header {
  text-align: center;
  margin-bottom: 60px;
}

.section-title {
  font-size: 40px;
  font-weight: 700;
  color: #111827;
  margin: 0 0 16px 0;
  letter-spacing: -0.025em;
}

.section-subtitle {
  font-size: 18px;
  color: #6b7280;
  margin: 0;
  line-height: 1.6;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
  gap: 32px;
}

.feature-card {
  background: #fafbfc;
  padding: 32px;
  border-radius: 16px;
  transition: all 0.3s ease;
}

.feature-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
}

.feature-icon {
  width: 64px;
  height: 64px;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 24px;
  color: white;
  font-size: 28px;
}

.feature-image {
  width: 64px;
  height: 64px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 24px;
  object-fit: cover;
}

.feature-title {
  font-size: 24px;
  font-weight: 600;
  color: #111827;
  margin: 0 0 12px 0;
}

.feature-description {
  font-size: 16px;
  color: #6b7280;
  line-height: 1.6;
  margin: 0 0 20px 0;
}

.feature-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.feature-list li {
  padding: 8px 0;
  color: #6b7280;
  font-size: 14px;
  position: relative;
  padding-left: 20px;
}

.feature-list li::before {
  content: '✓';
  position: absolute;
  left: 0;
  color: #10b981;
  font-weight: 600;
}

/* CTA Section */
.cta-section {
  padding: 80px 0;
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
  color: white;
}

.cta-content {
  text-align: center;
  max-width: 800px;
  margin: 0 auto;
}

.cta-title {
  font-size: 40px;
  font-weight: 700;
  margin: 0 0 16px 0;
  letter-spacing: -0.025em;
}

.cta-description {
  font-size: 18px;
  line-height: 1.6;
  margin: 0 0 32px 0;
  opacity: 0.9;
}

.cta-actions {
  display: flex;
  gap: 16px;
  justify-content: center;
  flex-wrap: wrap;
}

/* Buttons */
.btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  border-radius: 8px;
  font-weight: 600;
  font-size: 15px;
  text-decoration: none;
  transition: all 0.2s ease;
  cursor: pointer;
  border: none;
}

.btn-large {
  padding: 16px 32px;
  font-size: 16px;
}

.btn-primary {
  background: #6366f1;
  color: white;
}

.btn-primary:hover {
  background: #4f46e5;
  color: white;
}

.btn-outline {
  background: transparent;
  color: #6366f1;
  border: 2px solid #6366f1;
}

.btn-outline:hover {
  background: #6366f1;
  color: white;
}

.cta-section .btn-primary {
  background: white;
  color: #6366f1;
}

.cta-section .btn-primary:hover {
  background: #f9fafb;
  color: #6366f1;
}

.cta-section .btn-outline {
  background: transparent;
  color: white;
  border-color: white;
}

.cta-section .btn-outline:hover {
  background: white;
  color: #6366f1;
}

/* Integration Section */
.integration-section {
  padding: 80px 0;
  background: #f9fafb;
}

.integration-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 32px;
  margin-top: 60px;
}

.integration-item {
  background: white;
  padding: 32px 24px;
  border-radius: 16px;
  text-align: center;
  transition: all 0.3s ease;
  border: 1px solid #e5e7eb;
}

.integration-item:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
}

.integration-icon {
  width: 64px;
  height: 64px;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 16px;
  color: white;
  font-size: 28px;
}

.integration-title {
  font-size: 18px;
  font-weight: 600;
  color: #111827;
  margin: 0 0 8px 0;
}

.integration-description {
  font-size: 14px;
  color: #6b7280;
  margin: 0;
}

/* Responsive */
@media (max-width: 768px) {
  .hero-section {
    padding: 80px 0 60px;
  }
  
  .hero-title {
    font-size: 36px;
  }
  
  .hero-description {
    font-size: 18px;
  }
  
  .section-title {
    font-size: 32px;
  }
  
  .features-grid {
    grid-template-columns: 1fr;
  }
  
  .advanced-grid {
    grid-template-columns: 1fr;
  }
  
  .advanced-item {
    flex-direction: column;
    text-align: center;
  }
  
  .cta-actions {
    flex-direction: column;
    width: 100%;
  }
  
  .btn {
    width: 100%;
    justify-content: center;
  }
}
</style>
