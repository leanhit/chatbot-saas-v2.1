<template>
  <div class="pricing-page">
    <!-- Hero Section -->
    <section class="hero-section">
      <div class="container">
        <div class="hero-content">
          <img src="/images/pricing-hero.png" alt="Pricing Hero" class="pricing-hero-image">
          <h1 class="hero-title">Bảng giá tối ưu cho bán hàng</h1>
          <p class="hero-description">
            Chọn gói phù hợp nhất với quy mô shop của bạn. 
            Tăng 50% đơn hàng ngay hôm nay.
          </p>
          <div class="billing-toggle">
            <button 
              @click="billingCycle = 'monthly'"
              :class="{ active: billingCycle === 'monthly' }"
              class="billing-btn"
            >
              Thanh toán hàng tháng
            </button>
            <button 
              @click="billingCycle = 'yearly'"
              :class="{ active: billingCycle === 'yearly' }"
              class="billing-btn"
            >
              Thanh toán hàng năm
              <span class="discount">Tiết kiệm 20%</span>
            </button>
          </div>
        </div>
      </div>
    </section>

    <!-- Pricing Cards -->
    <section class="pricing-cards">
      <div class="container">
        <div class="pricing-grid">
          <!-- Free Plan -->
          <div class="pricing-card">
            <div class="pricing-header">
              <h3 class="plan-name">Miễn phí</h3>
              <div class="plan-price">
                <span class="price">0</span>
                <span class="currency">₫</span>
                <span class="period">/tháng</span>
              </div>
              <p class="plan-description">Bắt đầu bán hàng tự động, không cần thẻ tín dụng</p>
            </div>
            <div class="pricing-features">
              <ul class="features-list">
                <li><i class="mdi mdi-check"></i> 1 chatbot</li>
                <li><i class="mdi mdi-check"></i> 500 cuộc hội thoại/tháng</li>
                <li><i class="mdi mdi-check"></i> Chốt đơn cơ bản</li>
              </ul>
            </div>
            <div class="pricing-action">
              <button class="btn btn-outline">Bắt đầu miễn phí</button>
            </div>
          </div>

          <!-- Starter Plan -->
          <div class="pricing-card popular">
            <div class="popular-badge">Phổ biến nhất</div>
            <div class="pricing-header">
              <h3 class="plan-name">Starter</h3>
              <div class="plan-price">
                <span class="price">{{ billingCycle === 'yearly' ? '800' : '1,000' }}</span>
                <span class="currency">₫</span>
                <span class="period">/tháng</span>
              </div>
              <p class="plan-description">Tăng 50% đơn hàng cho shop nhỏ và mới bắt đầu</p>
            </div>
            <div class="pricing-features">
              <ul class="features-list">
                <li><i class="mdi mdi-check"></i> 3 chatbots</li>
                <li><i class="mdi mdi-check"></i> 2,000 cuộc hội thoại/tháng</li>
                <li><i class="mdi mdi-check"></i> Chốt đơn nâng cao</li>
              </ul>
            </div>
            <div class="pricing-action">
              <button class="btn btn-primary">Dùng thử ngay</button>
            </div>
          </div>

          <!-- Professional Plan -->
          <div class="pricing-card">
            <div class="pricing-header">
              <h3 class="plan-name">Professional</h3>
              <div class="plan-price">
                <span class="price">{{ billingCycle === 'yearly' ? '2,400' : '3,000' }}</span>
                <span class="currency">₫</span>
                <span class="period">/tháng</span>
              </div>
              <p class="plan-description">Tối đa hóa doanh thu cho shop đang phát triển</p>
            </div>
            <div class="pricing-features">
              <ul class="features-list">
                <li><i class="mdi mdi-check"></i> 10 chatbots</li>
                <li><i class="mdi mdi-check"></i> 10,000 cuộc hội thoại/tháng</li>
                <li><i class="mdi mdi-check"></i> Tất cả tính năng cao cấp</li>
              </ul>
            </div>
            <div class="pricing-action">
              <button class="btn btn-outline">Liên hệ bán hàng</button>
            </div>
          </div>

          <!-- Enterprise Plan -->
          <div class="pricing-card enterprise">
            <div class="pricing-header">
              <h3 class="plan-name">Enterprise</h3>
              <div class="plan-price">
                <span class="price">Tùy chỉnh</span>
              </div>
              <p class="plan-description">Giải pháp toàn diện cho doanh nghiệp lớn & agency</p>
            </div>
            <div class="pricing-features">
              <ul class="features-list">
                <li><i class="mdi mdi-check"></i> Không giới hạn chatbots</li>
                <li><i class="mdi mdi-check"></i> Không giới hạn đơn hàng</li>
                <li><i class="mdi mdi-check"></i> Team chuyên hỗ trợ riêng</li>
              </ul>
            </div>
            <div class="pricing-action">
              <button class="btn btn-outline">Liên hệ chúng tôi</button>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- FAQ Section -->
    <section class="faq-section">
      <div class="container">
        <div class="section-header">
          <h2 class="section-title">Câu hỏi thường gặp</h2>
          <p class="section-subtitle">Mọi thắc mắc của bạn về giá cả và tính năng</p>
        </div>
        
        <div class="faq-grid">
          <div class="faq-item">
            <h3 class="faq-question">Tôi có thể thay đổi gói đăng ký bất cứ lúc nào không?</h3>
            <p class="faq-answer">
              Có, bạn có thể nâng cấp hoặc hạ cấp gói bất cứ lúc nào. 
              Khi nâng cấp, bạn sẽ chỉ trả phí chênh lệch cho phần còn lại của kỳ thanh toán.
            </p>
          </div>
          
          <div class="faq-item">
            <h3 class="faq-question">Có giới hạn về số lượng người dùng không?</h3>
            <p class="faq-answer">
              Không, tất cả các gói đều không giới hạn số lượng người dùng 
              trong đội ngũ quản trị của bạn.
            </p>
          </div>
          
          <div class="faq-item">
            <h3 class="faq-question">Dữ liệu của tôi có được bảo mật không?</h3>
            <p class="faq-answer">
              Hoàn toàn. Chúng tôi sử dụng mã hóa end-to-end và tuân thủ 
              các tiêu chuẩn bảo mật quốc tế để bảo vệ dữ liệu của bạn.
            </p>
          </div>
          
          <div class="faq-item">
            <h3 class="faq-question">Có cần thẻ tín dụng để đăng ký không?</h3>
            <p class="faq-answer">
              Gói miễn phí hoàn toàn không cần thẻ tín dụng. 
              Các gói trả phí sẽ yêu cầu thông tin thanh toán khi nâng cấp.
            </p>
          </div>
        </div>
      </div>
    </section>

    <!-- CTA Section -->
    <section class="cta-section">
      <div class="container">
        <div class="cta-content">
          <h2 class="cta-title">Sẵn sàng bắt đầu?</h2>
          <p class="cta-description">
            Tham gia hàng ngàn doanh nghiệp đang sử dụng Traloitudong.com 
            để nâng cao trải nghiệm khách hàng.
          </p>
          <router-link to="/login" class="btn btn-primary btn-large">
            <i class="mdi mdi-rocket-launch"></i>
            Bắt đầu miễn phí
          </router-link>
        </div>
      </div>
    </section>

    <!-- FAQ Section -->
    <section class="faq-section">
      <div class="container">
        <div class="section-header">
          <h2 class="section-title">Câu hỏi thường gặp</h2>
          <p class="section-subtitle">Mọi thắc mắc về giá cả và tính năng</p>
        </div>
        
        <div class="faq-grid">
          <div class="faq-item">
            <h3 class="faq-question">
              <i class="mdi mdi-help-circle"></i>
              Có dùng thử miễn phí không?
            </h3>
            <p class="faq-answer">
              Có! Gói Miễn phí cho phép bạn dùng 1 chatbot với 500 cuộc hội thoại/tháng. 
              Không cần thẻ tín dụng khi đăng ký.
            </p>
          </div>

          <div class="faq-item">
            <h3 class="faq-question">
              <i class="mdi mdi-help-circle"></i>
              Nâng cấp/hủy gói có dễ không?
            </h3>
            <p class="faq-answer">
              Rất dễ! Bạn có thể nâng cấp hoặc hủy gói bất cứ lúc nào. 
              Không có phí ẩn hay cam kết dài hạn.
            </p>
          </div>

          <div class="faq-item">
            <h3 class="faq-question">
              <i class="mdi mdi-help-circle"></i>
              Hỗ trợ các nền tảng nào?
            </h3>
            <p class="faq-answer">
              Hỗ trợ Facebook, Instagram, Zalo, Website, Lazada, TikTok Shop. 
              Tích hợp chỉ mất 5 phút.
            </p>
          </div>

          <div class="faq-item">
            <h3 class="faq-question">
              <i class="mdi mdi-help-circle"></i>
              Chatbot có trả lời tiếng Việt không?
            </h3>
            <p class="faq-answer">
              Chắc chắn rồi! Chatbot được training chuyên sâu cho tiếng Việt, 
              hiểu cả tiếng lóng và cách nói tự nhiên.
            </p>
          </div>
        </div>
      </div>
    </section>

    <!-- CTA Section -->
    <section class="cta-section">
      <div class="container">
        <div class="cta-content">
          <h2 class="cta-title">Sẵn sàng tăng doanh thu?</h2>
          <p class="cta-description">
            Bắt đầu với gói miễn phí và trải nghiệm ngay hôm nay.
          </p>
          <div class="cta-actions">
            <router-link to="/login" class="btn btn-primary btn-large">
              <i class="mdi mdi-rocket-launch"></i>
              Bắt đầu miễn phí
            </router-link>
            <router-link to="/contact" class="btn btn-outline btn-large">
              <i class="mdi mdi-phone"></i>
              Liên hệ tư vấn
            </router-link>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const billingCycle = ref<'monthly' | 'yearly'>('monthly')
</script>

<style scoped>
.pricing-page {
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

.pricing-hero-image {
  width: 100%;
  max-width: 600px;
  height: 200px;
  object-fit: cover;
  border-radius: 16px;
  margin-bottom: 32px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
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
  margin: 0 0 40px 0;
}

.billing-toggle {
  display: inline-flex;
  background: white;
  border-radius: 12px;
  padding: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.billing-btn {
  padding: 12px 24px;
  border: none;
  background: transparent;
  border-radius: 8px;
  font-weight: 500;
  color: #6b7280;
  cursor: pointer;
  transition: all 0.2s ease;
  position: relative;
}

.billing-btn.active {
  background: #6366f1;
  color: white;
}

.discount {
  background: #10b981;
  color: white;
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 12px;
  margin-left: 8px;
}

/* Pricing Cards */
.pricing-cards {
  padding: 80px 0;
  background: white;
}

.pricing-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 32px;
  max-width: 1200px;
  margin: 0 auto;
}

.pricing-card {
  background: white;
  border: 2px solid #e5e7eb;
  border-radius: 16px;
  padding: 40px 32px;
  position: relative;
  transition: all 0.3s ease;
}

.pricing-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
}

.pricing-card.popular {
  border-color: #6366f1;
  transform: scale(1.05);
}

.pricing-card.enterprise {
  background: #fafbfc;
}

.popular-badge {
  position: absolute;
  top: -12px;
  left: 50%;
  transform: translateX(-50%);
  background: #6366f1;
  color: white;
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
}

.pricing-header {
  text-align: center;
  margin-bottom: 32px;
}

.plan-name {
  font-size: 24px;
  font-weight: 600;
  color: #111827;
  margin: 0 0 16px 0;
}

.plan-price {
  display: flex;
  align-items: baseline;
  justify-content: center;
  gap: 4px;
  margin-bottom: 12px;
}

.price {
  font-size: 48px;
  font-weight: 700;
  color: #111827;
}

.currency {
  font-size: 24px;
  font-weight: 600;
  color: #6b7280;
}

.period {
  font-size: 16px;
  color: #6b7280;
}

.plan-description {
  color: #6b7280;
  margin: 0;
  line-height: 1.5;
}

.pricing-features {
  margin-bottom: 32px;
}

.features-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.features-list li {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 0;
  color: #6b7280;
  font-size: 14px;
}

.features-list i {
  font-size: 18px;
}

.features-list .mdi-check {
  color: #10b981;
}

.features-list .mdi-close {
  color: #ef4444;
}

.pricing-action {
  text-align: center;
}

/* FAQ Section */
.faq-section {
  padding: 80px 0;
  background: #fafbfc;
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

.faq-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 32px;
  max-width: 800px;
  margin: 0 auto;
}

.faq-item {
  background: white;
  padding: 32px;
  border-radius: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.faq-question {
  font-size: 18px;
  font-weight: 600;
  color: #111827;
  margin: 0 0 12px 0;
}

.faq-answer {
  color: #6b7280;
  line-height: 1.6;
  margin: 0;
}

/* CTA Section */
.cta-section {
  padding: 80px 0;
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
  color: white;
}

.cta-content {
  text-align: center;
  max-width: 600px;
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
  
  .billing-toggle {
    flex-direction: column;
    width: 100%;
  }
  
  .pricing-grid {
    grid-template-columns: 1fr;
    gap: 24px;
  }
  
  .pricing-card.popular {
    transform: none;
  }
  
  .section-title {
    font-size: 32px;
  }
  
  .faq-grid {
    grid-template-columns: 1fr;
  }
}
</style>
