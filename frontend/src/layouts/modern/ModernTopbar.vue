<template>
  <header class="modern-topbar">
    <!-- Left Section -->
    <div class="topbar-left">
      <button 
        @click="emit('toggleSidebar')"
        class="sidebar-toggle"
        :title="showSidebarToggle ? 'Toggle sidebar' : ''"
      >
        <i class="mdi mdi-menu"></i>
      </button>
      
      <div class="topbar-title">
        <h1>{{ pageTitle }}</h1>
      </div>
    </div>

    <!-- Center Section - Search -->
    <div class="topbar-center">
      <div class="search-container">
        <div class="search-input-wrapper">
          <i class="mdi mdi-magnify search-icon"></i>
          <input 
            type="text" 
            :placeholder="t('Search...')"
            class="search-input"
            v-model="searchQuery"
          />
        </div>
      </div>
    </div>

    <!-- Right Section -->
    <div class="topbar-right">
      <!-- Language Switch -->
      <div class="topbar-item language-switch">
        <el-dropdown @command="handleLanguageChange" trigger="click">
          <button class="topbar-button">
            <img :src="currentFlag" alt="Language" class="language-flag" />
            <i class="mdi mdi-chevron-down language-arrow"></i>
          </button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item 
                v-for="locale in supportedLocales" 
                :key="locale.value"
                :command="locale.value"
                :class="{ 'is-active': currentLocale === locale.value }"
              >
                <img :src="locale.flag" alt="" class="dropdown-flag" />
                <span>{{ locale.label }}</span>
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>

      <!-- Notifications -->
      <div class="topbar-item notifications">
        <el-badge :value="notificationCount" :hidden="notificationCount === 0">
          <button class="topbar-button">
            <i class="mdi mdi-bell"></i>
          </button>
        </el-badge>
      </div>

      <!-- User Menu -->
      <div class="topbar-item user-menu">
        <el-dropdown @command="handleUserAction" trigger="click">
          <button class="user-button">
            <div class="user-avatar">
              <img 
                :src="userAvatar" 
                :alt="userName"
                class="avatar-image"
              />
            </div>
            <div class="user-info" v-if="showUserInfo">
              <span class="user-name">{{ userName }}</span>
              <span class="user-role">{{ userRole }}</span>
            </div>
            <i class="mdi mdi-chevron-down user-arrow"></i>
          </button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">
                <i class="mdi mdi-account-circle"></i>
                {{ t('Profile') }}
              </el-dropdown-item>
              <el-dropdown-item command="workspace">
                <i class="mdi mdi-domain"></i>
                {{ t('Switch Workspace') }}
              </el-dropdown-item>
              <el-dropdown-item command="help">
                <i class="mdi mdi-help-circle"></i>
                {{ t('Help') }}
              </el-dropdown-item>
              <el-dropdown-item divided command="theme">
                <i :class="isDarkMode ? 'mdi mdi-white-balance-sunny' : 'mdi mdi-weather-night'"></i>
                {{ isDarkMode ? t('Light Mode') : t('Dark Mode') }}
              </el-dropdown-item>
              <el-dropdown-item divided command="logout">
                <i class="mdi mdi-logout"></i>
                {{ t('Logout') }}
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>
  </header>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'

// Emits
const emit = defineEmits<{
  toggleSidebar: () => void
  changeLocale: (locale: string) => void
  navigate: (path: string) => void
}>()

// Props
interface Props {
  userInfo?: any
  currentLocale?: string
  supportedLocales?: Array<{ value: string; label: string; flag: string }>
  pageTitle?: string
  showSidebarToggle?: boolean
  showUserInfo?: boolean
  notificationCount?: number
}

const props = withDefaults(defineProps<Props>(), {
  pageTitle: 'Dashboard',
  showSidebarToggle: true,
  showUserInfo: true,
  notificationCount: 0
})

// Composables
const { t, locale } = useI18n()
const router = useRouter()

// Reactive state
const searchQuery = ref('')
const isDarkMode = ref(false)

// Computed
const currentLocale = computed(() => props.currentLocale || locale.value)

const supportedLocales = computed(() => {
  return props.supportedLocales || [
    { value: 'en', label: 'English', flag: '/images/flags/us.jpg' },
    { value: 'vi', label: 'Tiếng Việt', flag: '/images/flags/vietnam.jpg' }
  ]
})

const currentFlag = computed(() => {
  const locale = supportedLocales.value.find(l => l.value === currentLocale.value)
  return locale?.flag || supportedLocales.value[0].flag
})

const userName = computed(() => {
  return props.userInfo?.name || props.userInfo?.email || 'User'
})

const userRole = computed(() => {
  return props.userInfo?.role || 'Administrator'
})

const userAvatar = computed(() => {
  return props.userInfo?.avatar || '/images/users/avatar-1.jpg'
})

// Methods
const handleLanguageChange = (newLocale: string) => {
  emit('changeLocale', newLocale)
}

const handleUserAction = (action: string) => {
  switch (action) {
    case 'profile':
      emit('navigate', '/profile')
      break
    case 'workspace':
      emit('navigate', '/tenant-gateway')
      break
    case 'help':
      emit('navigate', '/help')
      break
    case 'theme':
      toggleTheme()
      break
    case 'logout':
      handleLogout()
      break
  }
}

const toggleTheme = () => {
  isDarkMode.value = !isDarkMode.value
  // Here you would typically update a global theme state
  document.documentElement.classList.toggle('dark', isDarkMode.value)
}

const handleLogout = () => {
  // Reuse logout logic from old Navbar.vue
  localStorage.clear()
  router.push('/login')
}

// Search functionality (placeholder)
const handleSearch = () => {
  if (searchQuery.value.trim()) {
    console.log('Searching for:', searchQuery.value)
    // Implement search logic here
  }
}
</script>

<style scoped>
.modern-topbar {
  height: 70px;
  background: white;
  border-bottom: 1px solid #e2e8f0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  position: sticky;
  top: 0;
  z-index: 999;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

/* Left Section */
.topbar-left {
  display: flex;
  align-items: center;
  gap: 16px;
  flex: 1;
  min-width: 0;
}

.sidebar-toggle {
  width: 40px;
  height: 40px;
  border: none;
  background: #f8fafc;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
  color: #64748b;
}

.sidebar-toggle:hover {
  background: #e2e8f0;
  color: #475569;
}

.topbar-title h1 {
  font-size: 20px;
  font-weight: 600;
  color: #1e293b;
  margin: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* Center Section */
.topbar-center {
  flex: 2;
  max-width: 600px;
  display: flex;
  justify-content: center;
}

.search-container {
  width: 100%;
  max-width: 400px;
}

.search-input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.search-icon {
  position: absolute;
  left: 12px;
  color: #94a3b8;
  font-size: 18px;
  z-index: 1;
}

.search-input {
  width: 100%;
  height: 40px;
  padding: 0 16px 0 40px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  background: #f8fafc;
  font-size: 14px;
  transition: all 0.2s ease;
  outline: none;
}

.search-input:focus {
  border-color: #667eea;
  background: white;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.search-input::placeholder {
  color: #94a3b8;
}

/* Right Section */
.topbar-right {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
  justify-content: flex-end;
}

.topbar-item {
  position: relative;
}

.topbar-button {
  width: 40px;
  height: 40px;
  border: none;
  background: transparent;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
  color: #64748b;
  position: relative;
}

.topbar-button:hover {
  background: #f1f5f9;
  color: #475569;
}

/* Language Switch */
.language-flag {
  width: 20px;
  height: 16px;
  border-radius: 2px;
}

.language-arrow {
  font-size: 14px;
  margin-left: 4px;
}

/* Notifications */
:deep(.el-badge__content) {
  font-size: 10px;
  height: 16px;
  line-height: 16px;
  padding: 0 4px;
  min-width: 16px;
}

/* User Menu */
.user-button {
  width: auto;
  padding: 8px 12px;
  gap: 12px;
  display: flex;
  align-items: center;
  background: transparent;
  border: 1px solid transparent;
  border-radius: 8px;
  transition: all 0.2s ease;
}

.user-button:hover {
  background: #f8fafc;
  border-color: #e2e8f0;
}

.user-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  overflow: hidden;
  flex-shrink: 0;
}

.avatar-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.user-info {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  min-width: 0;
}

.user-name {
  font-size: 14px;
  font-weight: 500;
  color: #1e293b;
  line-height: 1.2;
}

.user-role {
  font-size: 12px;
  color: #64748b;
  line-height: 1.2;
}

.user-arrow {
  font-size: 14px;
  color: #94a3b8;
}

/* Dropdown Styles */
:deep(.el-dropdown-menu) {
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
  padding: 8px;
}

:deep(.el-dropdown-menu__item) {
  padding: 8px 12px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 14px;
  color: #374151;
  transition: all 0.2s ease;
}

:deep(.el-dropdown-menu__item:hover) {
  background: #f8fafc;
  color: #1e293b;
}

:deep(.el-dropdown-menu__item.is-active) {
  background: #ede9fe;
  color: #7c3aed;
}

:deep(.el-dropdown-menu__item i) {
  font-size: 16px;
  width: 16px;
  text-align: center;
}

.dropdown-flag {
  width: 16px;
  height: 12px;
  border-radius: 2px;
}

/* Responsive */
@media (max-width: 1024px) {
  .topbar-center {
    display: none;
  }
  
  .user-info {
    display: none;
  }
  
  .user-button {
    padding: 8px;
  }
}

@media (max-width: 768px) {
  .modern-topbar {
    padding: 0 16px;
  }
  
  .topbar-title h1 {
    font-size: 18px;
  }
  
  .language-switch {
    display: none;
  }
}
</style>
