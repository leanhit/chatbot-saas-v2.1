<template>
  <div class="home-page">
    <!-- Hero Section -->
    <section class="hero-section">
      <div class="hero-container">
        <div class="hero-content">
          <div class="hero-badge">
            <span class="badge-text">🚀 Ra mắt phiên bản beta</span>
          </div>
          <h1 class="hero-title">
            Nền tảng Trả lời tự động
            <span class="hero-highlight">tăng 50% tỷ lệ chốt đơn</span>
          </h1>
          <p class="hero-description">
            AI chatbot chốt đơn tự động, tăng 50% tỷ lệ mua hàng khi tích hợp vào Fanpage, 
            nhất là chốt sales khi chạy quảng cáo tin nhắn trên Facebook. 
            Giữ chân khách hàng đến 15 giây và tự động hóa 24/7.
          </p>
          <div class="hero-actions">
            <router-link to="/login" class="btn btn-primary btn-large">
              <i class="mdi mdi-rocket-launch"></i>
              Bắt đầu miễn phí
            </router-link>
            <router-link to="/features" class="btn btn-outline btn-large">
              <i class="mdi mdi-play-circle"></i>
              Xem demo
            </router-link>
          </div>
          <div class="hero-stats">
            <div class="stat-item">
              <div class="stat-number">70%+</div>
              <div class="stat-label">Giữ chân khách hàng</div>
            </div>
            <div class="stat-item">
              <div class="stat-number">50%</div>
              <div class="stat-label">Tăng tỷ lệ chốt đơn</div>
            </div>
            <div class="stat-item">
              <div class="stat-number">24/7</div>
              <div class="stat-label">Hoạt động tự động</div>
            </div>
          </div>
        </div>
        <div class="hero-image">
          <img src="/images/chatbot-demo.png" alt="Chatbot Demo Interface" class="hero-demo-image">
        </div>
      </div>
    </section>

    <!-- Features Preview -->
    <section class="features-preview">
      <div class="container">
        <div class="section-header">
          <h2 class="section-title">Tính năng nổi bật</h2>
          <p class="section-subtitle">Khám phá sức mạnh của AI trong việc tự động hóa dịch vụ khách hàng</p>
        </div>
        <div class="features-grid">
          <div class="feature-card">
            <div class="feature-icon">
              <i class="mdi mdi-brain"></i>
            </div>
            <h3 class="feature-title">AI Thông minh</h3>
            <p class="feature-description">
              Công nghệ xử lý ngôn ngữ tự nhiên tiên tiến, hiểu ngữ cảnh và trả lời chính xác.
            </p>
          </div>
          <div class="feature-card">
            <div class="feature-icon">
              <i class="mdi mdi-integration"></i>
            </div>
            <h3 class="feature-title">Tích hợp dễ dàng</h3>
            <p class="feature-description">
              Kết nối nhanh chóng với website, app và các nền tảng kinh doanh hiện có.
            </p>
          </div>
          <div class="feature-card">
            <div class="feature-icon">
              <i class="mdi mdi-chart-line"></i>
            </div>
            <h3 class="feature-title">Phân tích chuyên sâu</h3>
            <p class="feature-description">
              Báo cáo chi tiết và insight về hành vi khách hàng để tối ưu hóa dịch vụ.
            </p>
          </div>
        </div>
        <div class="features-cta">
          <router-link to="/features" class="btn btn-outline">
            Xem tất cả tính năng
            <i class="mdi mdi-arrow-right"></i>
          </router-link>
        </div>
      </div>
    </section>

    <!-- Problem Solution Section -->
    <section class="problem-solution">
      <div class="container">
        <div class="section-header">
          <h2 class="section-title">Giải pháp bán hàng tự động</h2>
          <p class="section-subtitle">Traloitudong.com giải quyết những thách thức lớn nhất trong bán hàng online</p>
        </div>
        
        <div class="solution-grid">
          <div class="solution-item">
            <div class="problem-icon">
              <i class="mdi mdi-moon-waning-crescent"></i>
            </div>
            <h3 class="problem-title">Bỏ lỡ đơn đêm</h3>
            <p class="problem-description">
              Khách hàng nhắn tin ban đêm nhưng không có nhân viên trả lời, 
              mất đi cơ hội chốt đơn vàng.
            </p>
            <div class="solution-arrow">
              <i class="mdi mdi-arrow-down"></i>
            </div>
            <div class="solution-content">
              <h4 class="solution-title">Chốt đơn 24/7</h4>
              <p class="solution-text">
                AI tự động trả lời và chốt đơn ngay cả khi bạn đang ngủ, 
                tăng 50% đơn hàng ban đêm và ngày lễ.
              </p>
            </div>
          </div>

          <div class="solution-item">
            <div class="problem-icon">
              <i class="mdi mdi-currency-usd-off"></i>
            </div>
            <h3 class="problem-title">Chi phí nhân sự</h3>
            <p class="problem-description">
              Duy trì team sales 24/7 tốn kém nhưng vẫn không đáp ứng 
              hết tin nhắn từ khách hàng.
            </p>
            <div class="solution-arrow">
              <i class="mdi mdi-arrow-down"></i>
            </div>
            <div class="solution-content">
              <h4 class="solution-title">Tiết kiệm 80% chi phí</h4>
              <p class="solution-text">
                AI xử lý 80% câu hỏi thường gặp, 
                giảm chi phí nhân sự và tăng hiệu quả bán hàng.
              </p>
            </div>
          </div>

          <div class="solution-item">
            <div class="problem-icon">
              <i class="mdi mdi-emoticon-sad-outline"></i>
            </div>
            <h3 class="problem-title">Khách rời đi nhanh</h3>
            <p class="problem-description">
              Khách hàng chờ lâu hoặc chatbot trả lời robot, 
              gây mất thiện cảm và rời đi.
            </p>
            <div class="solution-arrow">
              <i class="mdi mdi-arrow-down"></i>
            </div>
            <div class="solution-content">
              <h4 class="solution-title">Giữ chân 15 giây</h4>
              <p class="solution-text">
                AI trả lời tự nhiên, hài hước, giữ chân 
                70% khách hàng lên đến 15 giây.
              </p>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Use Cases Section -->
    <section class="use-cases">
      <div class="container">
        <div class="section-header">
          <h2 class="section-title">Tối ưu cho mọi ngành bán hàng</h2>
          <p class="section-subtitle">Traloitudong.com được thiết kế chuyên biệt cho các ngành kinh doanh online</p>
        </div>
        
        <div class="cases-grid">
          <div class="case-card">
            <div class="case-icon">
              <i class="mdi mdi-tshirt-crew"></i>
            </div>
            <h3 class="case-title">Thời trang</h3>
            <ul class="case-list">
              <li>Tư vấn size & màu sắc</li>
              <li>Gợi ý phối đồ</li>
              <li>Chốt đơn & ship COD</li>
            </ul>
          </div>

          <div class="case-card">
            <div class="case-icon">
              <i class="mdi mdi-lipstick"></i>
            </div>
            <h3 class="case-title">Mỹ phẩm</h3>
            <ul class="case-list">
              <li>Tư vấn da & sản phẩm</li>
              <li>Gợi ý routine chăm sóc</li>
              <li>Tạo combo khuyến mãi</li>
            </ul>
          </div>

          <div class="case-card">
            <div class="case-icon">
              <i class="mdi mdi-food-fork-drink"></i>
            </div>
            <h3 class="case-title">F&B</h3>
            <ul class="case-list">
              <li>Nhận đặt bàn online</li>
              <li>Gợi ý món ăn hot</li>
              <li>Chương trình ưu đãi</li>
            </ul>
          </div>

          <div class="case-card">
            <div class="case-icon">
              <i class="mdi mdi-home-heart"></i>
            </div>
            <h3 class="case-title">Thời trang & Làm đẹp</h3>
            <ul class="case-list">
              <li>Tư vấn sản phẩm phù hợp</li>
              <li>Booking dịch vụ</li>
              <li>Chốt gói combo</li>
            </ul>
          </div>

          <div class="case-card">
            <div class="case-icon">
              <i class="mdi mdi-baby-bottle"></i>
            </div>
            <h3 class="case-title">Mẹ & Bé</h3>
            <ul class="case-list">
              <li>Tư vấn sản phẩm an toàn</li>
              <li>Hướng dẫn sử dụng</li>
              <li>Chương trình thành viên</li>
            </ul>
          </div>

          <div class="case-card">
            <div class="case-icon">
              <i class="mdi mdi-dumbbell"></i>
            </div>
            <h3 class="case-title">Sức khỏe & Fitness</h3>
            <ul class="case-list">
              <li>Tư vấn sản phẩm</li>
              <li>Booking lịch tập</li>
              <li>Gói tập cá nhân hóa</li>
            </ul>
          </div>
        </div>
      </div>
    </section>

    <!-- How It Works -->
    <section class="how-it-works">
      <div class="container">
        <div class="section-header">
          <h2 class="section-title">Cách hoạt động</h2>
          <p class="section-subtitle">Bắt đầu với Traloitudong.com chỉ trong 3 bước đơn giản</p>
        </div>
        <div class="steps-grid">
          <div class="step-item">
            <div class="step-number">1</div>
            <h3 class="step-title">Đăng ký tài khoản</h3>
            <p class="step-description">
              Tạo tài khoản miễn phí và bắt đầu thiết lập chatbot đầu tiên của bạn.
            </p>
          </div>
          <div class="step-item">
            <div class="step-number">2</div>
            <h3 class="step-title">Tùy chỉnh chatbot</h3>
            <p class="step-description">
              Cấu hình câu trả lời, tích hợp với hệ thống và thiết lập quy trình tự động.
            </p>
          </div>
          <div class="step-item">
            <div class="step-number">3</div>
            <h3 class="step-title">Triển khai & theo dõi</h3>
            <p class="step-description">
              Nhúng chatbot vào website và theo dõi hiệu quả qua dashboard chi tiết.
            </p>
          </div>
        </div>
      </div>
    </section>

    <!-- CTA Section -->
    <section class="cta-section">
      <div class="container">
        <div class="cta-content">
          <h2 class="cta-title">Sẵn sàng nâng cấp dịch vụ khách hàng?</h2>
          <p class="cta-description">
            Tham gia hàng ngàn doanh nghiệp đang sử dụng Traloitudong.com 
            để tự động hóa và cải thiện trải nghiệm khách hàng.
          </p>
          <div class="cta-actions">
            <router-link to="/login" class="btn btn-primary btn-large">
              <i class="mdi mdi-rocket-launch"></i>
              Bắt đầu ngay
            </router-link>
            <router-link to="/pricing" class="btn btn-outline btn-large">
              <i class="mdi mdi-tag"></i>
              Xem bảng giá
            </router-link>
          </div>
        </div>
      </div>
    </section>

    <!-- Testimonials Section -->
    <section class="testimonials">
      <div class="container">
        <div class="section-header">
          <h2 class="section-title">Khách hàng nói gì</h2>
          <p class="section-subtitle">Hàng ngàn shop đã tăng doanh thu với Traloitudong.com</p>
        </div>
        
        <div class="testimonials-grid">
          <div class="testimonial-card">
            <div class="testimonial-content">
              <p class="testimonial-text">
                "Sau khi tích hợp AI chatbot, đơn hàng ban đêm tăng 50%. 
                Khách hàng không phải chờ đợi nữa!"
              </p>
            </div>
            <div class="testimonial-author">
              <div class="author-avatar">
                <i class="mdi mdi-account-circle"></i>
              </div>
              <div class="author-info">
                <h4 class="author-name">Anh Minh</h4>
                <p class="author-title">Shop thời trang MN</p>
              </div>
            </div>
          </div>

          <div class="testimonial-card">
            <div class="testimonial-content">
              <p class="testimonial-text">
                "Tiết kiệm được 2 nhân viên chăm sóc khách hàng, 
                nhưng hiệu quả lại tăng gấp đôi."
              </p>
            </div>
            <div class="testimonial-author">
              <div class="author-avatar">
                <i class="mdi mdi-account-circle"></i>
              </div>
              <div class="author-info">
                <h4 class="author-name">Chị Lan</h4>
                <p class="author-title">Spa & Làm đẹp EL</p>
              </div>
            </div>
          </div>

          <div class="testimonial-card">
            <div class="testimonial-content">
              <p class="testimonial-text">
                "Chatbot trả lời rất tự nhiên, khách hàng không biết là AI. 
                Chốt đơn tự động rất hiệu quả!"
              </p>
            </div>
            <div class="testimonial-author">
              <div class="author-avatar">
                <i class="mdi mdi-account-circle"></i>
              </div>
              <div class="author-info">
                <h4 class="author-name">Anh Hùng</h4>
                <p class="author-title">Nhà hàng ABC</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- CTA Section -->
    <section class="cta-section">
      <div class="container">
        <div class="cta-content">
          <h2 class="cta-title">Sẵn sàng tăng 50% đơn hàng?</h2>
          <p class="cta-description">
            Bắt đầu ngay hôm nay với gói miễn phí. Không cần thẻ tín dụng.
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
// No logic needed - static landing page
</script>

<style scoped>
.home-page {
  width: 100%;
}

/* Hero Section */
.hero-section {
  background: linear-gradient(135deg, #fafbfc 0%, #f3f4f6 100%);
  padding: 120px 0 80px;
  margin-top: 72px; /* Account for fixed navbar */
}

.hero-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 60px;
  align-items: center;
}

.hero-content {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.hero-badge {
  display: inline-flex;
  align-items: center;
  background: #f0f4ff;
  color: #6366f1;
  padding: 8px 16px;
  border-radius: 20px;
  font-weight: 500;
  font-size: 14px;
  width: fit-content;
}

.hero-title {
  font-size: 56px;
  font-weight: 700;
  color: #111827;
  line-height: 1.1;
  letter-spacing: -0.025em;
  margin: 0;
}

.hero-highlight {
  color: #6366f1;
}

.hero-description {
  font-size: 20px;
  color: #6b7280;
  line-height: 1.6;
  margin: 0;
}

.hero-actions {
  display: flex;
  gap: 16px;
  align-items: center;
}

.hero-stats {
  display: flex;
  gap: 40px;
}

.stat-item {
  text-align: center;
}

.stat-number {
  font-size: 32px;
  font-weight: 700;
  color: #111827;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: #6b7280;
  font-weight: 500;
}

.hero-image {
  display: flex;
  justify-content: center;
  align-items: center;
}

.hero-image-placeholder {
  width: 100%;
  height: 400px;
  background: white;
  border: 2px dashed #e5e7eb;
  border-radius: 16px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16px;
  color: #9ca3af;
}

.hero-image-placeholder i {
  font-size: 64px;
}

.hero-image-placeholder span {
  font-size: 16px;
  font-weight: 500;
}

.hero-demo-image {
  width: 100%;
  height: 400px;
  object-fit: cover;
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

/* Features Preview */
.features-preview {
  padding: 80px 0;
  background: white;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
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
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 32px;
  margin-bottom: 48px;
}

.feature-card {
  background: #fafbfc;
  padding: 32px;
  border-radius: 16px;
  text-align: center;
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
  margin: 0 auto 24px;
  color: white;
  font-size: 28px;
}

.feature-title {
  font-size: 20px;
  font-weight: 600;
  color: #111827;
  margin: 0 0 12px 0;
}

.feature-description {
  font-size: 16px;
  color: #6b7280;
  line-height: 1.6;
  margin: 0;
}

.features-cta {
  text-align: center;
}

/* Problem Solution Section */
.problem-solution {
  padding: 80px 0;
  background: white;
}

.solution-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 40px;
  max-width: 1000px;
  margin: 0 auto;
}

.solution-item {
  text-align: center;
  padding: 32px;
  background: #fafbfc;
  border-radius: 16px;
  position: relative;
}

.problem-icon {
  width: 64px;
  height: 64px;
  background: #fef2f2;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 20px;
  color: #ef4444;
  font-size: 28px;
}

.problem-title {
  font-size: 20px;
  font-weight: 600;
  color: #111827;
  margin: 0 0 12px 0;
}

.problem-description {
  color: #6b7280;
  line-height: 1.6;
  margin: 0 0 20px 0;
}

.solution-arrow {
  color: #6b7280;
  font-size: 24px;
  margin: 16px 0;
}

.solution-content {
  background: #f0fdf4;
  padding: 20px;
  border-radius: 12px;
  border: 1px solid #bbf7d0;
}

.solution-title {
  font-size: 18px;
  font-weight: 600;
  color: #166534;
  margin: 0 0 8px 0;
}

.solution-text {
  color: #15803d;
  line-height: 1.5;
  margin: 0;
}

/* Use Cases Section */
.use-cases {
  padding: 80px 0;
  background: #fafbfc;
}

.cases-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 24px;
}

.case-card {
  background: white;
  padding: 32px 24px;
  border-radius: 16px;
  text-align: center;
  transition: all 0.3s ease;
  border: 1px solid #e5e7eb;
}

.case-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
}

.case-icon {
  width: 56px;
  height: 56px;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 16px;
  color: white;
  font-size: 24px;
}

.case-title {
  font-size: 18px;
  font-weight: 600;
  color: #111827;
  margin: 0 0 16px 0;
}

.case-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.case-list li {
  padding: 6px 0;
  color: #6b7280;
  font-size: 14px;
  position: relative;
  padding-left: 16px;
}

.case-list li::before {
  content: '•';
  position: absolute;
  left: 0;
  color: #6366f1;
  font-weight: 600;
}

/* How It Works */
.how-it-works {
  padding: 80px 0;
  background: #fafbfc;
}

.steps-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 40px;
}

.step-item {
  text-align: center;
  position: relative;
}

.step-number {
  width: 60px;
  height: 60px;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 24px;
  font-weight: 700;
  margin: 0 auto 24px;
}

.step-title {
  font-size: 20px;
  font-weight: 600;
  color: #111827;
  margin: 0 0 12px 0;
}

.step-description {
  font-size: 16px;
  color: #6b7280;
  line-height: 1.6;
  margin: 0;
}

/* CTA Section */
.cta-section {
  padding: 100px 0;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  color: white;
  text-align: center;
}

.cta-content {
  max-width: 800px;
  margin: 0 auto;
}

.cta-title {
  font-size: 48px;
  font-weight: 700;
  margin: 0 0 24px 0;
  letter-spacing: -0.025em;
}

.cta-description {
  font-size: 20px;
  margin: 0 0 40px 0;
  opacity: 0.9;
}

.cta-actions {
  display: flex;
  gap: 16px;
  justify-content: center;
  flex-wrap: wrap;
}

.cta-actions .btn-primary {
  background: white;
  color: #6366f1;
  border: none;
}

.cta-actions .btn-primary:hover {
  background: #f3f4f6;
}

.cta-actions .btn-outline {
  background: transparent;
  color: white;
  border: 2px solid white;
}

.cta-actions .btn-outline:hover {
  background: white;
  color: #6366f1;
}

/* Testimonials Section */
.testimonials {
  padding: 100px 0;
  background: #f9fafb;
}

.testimonials-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
  gap: 32px;
  margin-top: 60px;
}

.testimonial-card {
  background: white;
  border-radius: 16px;
  padding: 32px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  border: 1px solid #e5e7eb;
}

.testimonial-content {
  margin-bottom: 24px;
}

.testimonial-text {
  font-size: 18px;
  line-height: 1.6;
  color: #374151;
  font-style: italic;
  margin: 0;
}

.testimonial-author {
  display: flex;
  align-items: center;
  gap: 16px;
}

.author-avatar {
  width: 48px;
  height: 48px;
  background: #f3f4f6;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #6b7280;
  font-size: 24px;
}

.author-info {
  flex: 1;
}

.author-name {
  font-size: 16px;
  font-weight: 600;
  color: #111827;
  margin: 0 0 4px 0;
}

.author-title {
  font-size: 14px;
  color: #6b7280;
  margin: 0;
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

/* Responsive */
@media (max-width: 1024px) {
  .hero-container {
    grid-template-columns: 1fr;
    gap: 40px;
    text-align: center;
  }
  
  .hero-title {
    font-size: 48px;
  }
  
  .hero-actions {
    justify-content: center;
  }
  
  .hero-stats {
    justify-content: center;
  }
}

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
  
  .hero-actions {
    flex-direction: column;
    width: 100%;
  }
  
  .hero-stats {
    flex-direction: column;
    gap: 24px;
  }
  
  .section-title {
    font-size: 32px;
  }
  
  .features-grid,
  .steps-grid {
    grid-template-columns: 1fr;
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
