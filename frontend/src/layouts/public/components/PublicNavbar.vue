<template>
  <nav class="public-navbar">
    <div class="navbar-container">
      <!-- Logo -->
      <div class="navbar-brand">
        <router-link to="/" class="brand-link">
          <div class="brand-logo">
            <i class="mdi mdi-robot"></i>
          </div>
          <span class="brand-text">Traloitudong.com</span>
        </router-link>
      </div>

      <!-- Desktop Menu -->
      <div class="navbar-menu">
        <router-link to="/" class="nav-link">Trang chủ</router-link>
        <router-link to="/features" class="nav-link">Tính năng</router-link>
        <router-link to="/pricing" class="nav-link">Giá</router-link>
        <router-link to="/guide" class="nav-link">Hướng dẫn</router-link>
        <router-link to="/careers" class="nav-link">Tuyển dụng</router-link>
        <router-link to="/login" class="nav-link nav-link-primary">Đăng nhập</router-link>
      </div>

      <!-- Mobile Menu Button -->
      <button 
        @click="toggleMobileMenu" 
        class="mobile-menu-button"
        :class="{ active: mobileMenuOpen }"
      >
        <span></span>
        <span></span>
        <span></span>
      </button>
    </div>

    <!-- Mobile Menu -->
    <div class="mobile-menu" :class="{ active: mobileMenuOpen }">
      <router-link to="/" @click="closeMobileMenu" class="mobile-nav-link">Trang chủ</router-link>
      <router-link to="/features" @click="closeMobileMenu" class="mobile-nav-link">Tính năng</router-link>
      <router-link to="/pricing" @click="closeMobileMenu" class="mobile-nav-link">Giá</router-link>
      <router-link to="/guide" @click="closeMobileMenu" class="mobile-nav-link">Hướng dẫn</router-link>
      <router-link to="/careers" @click="closeMobileMenu" class="mobile-nav-link">Tuyển dụng</router-link>
      <router-link to="/login" @click="closeMobileMenu" class="mobile-nav-link mobile-nav-link-primary">Đăng nhập</router-link>
    </div>
  </nav>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const mobileMenuOpen = ref(false)

const toggleMobileMenu = () => {
  mobileMenuOpen.value = !mobileMenuOpen.value
}

const closeMobileMenu = () => {
  mobileMenuOpen.value = false
}
</script>

<style scoped>
.public-navbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  background: white;
  border-bottom: 1px solid #e5e7eb;
  z-index: 1000;
  transition: all 0.3s ease;
}

.navbar-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
  height: 72px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.navbar-brand {
  flex-shrink: 0;
}

.brand-link {
  display: flex;
  align-items: center;
  gap: 12px;
  text-decoration: none;
  color: #111827;
  font-weight: 600;
  font-size: 18px;
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
  color: #111827;
}

.navbar-menu {
  display: flex;
  align-items: center;
  gap: 32px;
}

.nav-link {
  color: #6b7280;
  text-decoration: none;
  font-weight: 500;
  font-size: 15px;
  transition: color 0.2s ease;
  position: relative;
}

.nav-link:hover {
  color: #111827;
}

.nav-link.router-link-active {
  color: #6366f1;
}

.nav-link-primary {
  background: #6366f1;
  color: white;
  padding: 10px 20px;
  border-radius: 8px;
  transition: all 0.2s ease;
}

.nav-link-primary:hover {
  background: #4f46e5;
  color: white;
}

.mobile-menu-button {
  display: none;
  flex-direction: column;
  gap: 4px;
  background: transparent;
  border: none;
  cursor: pointer;
  padding: 8px;
}

.mobile-menu-button span {
  width: 24px;
  height: 2px;
  background: #6b7280;
  transition: all 0.3s ease;
}

.mobile-menu-button.active span:nth-child(1) {
  transform: rotate(45deg) translate(5px, 5px);
}

.mobile-menu-button.active span:nth-child(2) {
  opacity: 0;
}

.mobile-menu-button.active span:nth-child(3) {
  transform: rotate(-45deg) translate(7px, -6px);
}

.mobile-menu {
  display: none;
  position: absolute;
  top: 72px;
  left: 0;
  right: 0;
  background: white;
  border-bottom: 1px solid #e5e7eb;
  padding: 24px;
  flex-direction: column;
  gap: 16px;
}

.mobile-menu.active {
  display: flex;
}

.mobile-nav-link {
  color: #6b7280;
  text-decoration: none;
  font-weight: 500;
  font-size: 16px;
  padding: 12px 16px;
  border-radius: 8px;
  transition: all 0.2s ease;
}

.mobile-nav-link:hover {
  background: #f9fafb;
  color: #111827;
}

.mobile-nav-link.router-link-active {
  color: #6366f1;
  background: #f0f4ff;
}

.mobile-nav-link-primary {
  background: #6366f1;
  color: white;
  text-align: center;
}

.mobile-nav-link-primary:hover {
  background: #4f46e5;
  color: white;
}

/* Responsive */
@media (max-width: 768px) {
  .navbar-container {
    padding: 0 16px;
  }
  
  .navbar-menu {
    display: none;
  }
  
  .mobile-menu-button {
    display: flex;
  }
  
  .brand-text {
    font-size: 16px;
  }
  
  .brand-logo {
    width: 32px;
    height: 32px;
    font-size: 16px;
  }
}
</style>
