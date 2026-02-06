<template>
  <div class="app-shell">
    <!-- Modern Topbar -->
    <ModernTopbar 
      :user-info="userInfo"
      :current-locale="currentLocale"
      :supported-locales="supportedLocales"
      @toggle-sidebar="toggleSidebar"
      @change-locale="$emit('changeLocale', $event)"
    />

    <div class="app-body">
      <!-- Modern Sidebar -->
      <ModernSidebar 
        :collapsed="sidebarCollapsed"
        :menu-items="menuItems"
        :current-route="currentRoute"
        @navigate="handleNavigation"
      />

      <!-- Main Content Area -->
      <main class="main-content" :class="{ 'sidebar-collapsed': sidebarCollapsed }">
        <div class="content-wrapper">
          <!-- Page Header -->
          <div class="page-header" v-if="showPageHeader">
            <h1 class="page-title">{{ pageTitle }}</h1>
            <div class="page-actions">
              <slot name="page-actions" />
            </div>
          </div>

          <!-- Page Content -->
          <div class="page-content">
            <slot />
          </div>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import ModernTopbar from './ModernTopbar.vue'
import ModernSidebar from './ModernSidebar.vue'

// Props
interface Props {
  userInfo?: any
  currentLocale?: string
  supportedLoccales?: Array<{ value: string; label: string; flag: string }>
  menuItems?: any[]
  showPageHeader?: boolean
  pageTitle?: string
}

const props = withDefaults(defineProps<Props>(), {
  showPageHeader: true,
  pageTitle: 'Dashboard'
})

// Emits
const emit = defineEmits<{
  changeLocale: [locale: string]
  navigate: [route: string]
}>()

// Composables
const route = useRoute()
const router = useRouter()

// Reactive state
const sidebarCollapsed = ref(false)
const isMobile = ref(false)

// Computed
const currentRoute = computed(() => route.path)

// Methods
const toggleSidebar = () => {
  sidebarCollapsed.value = !sidebarCollapsed.value
}

const handleNavigation = (navigationPath: string) => {
  emit('navigate', navigationPath)
  router.push(navigationPath)
}

const checkMobile = () => {
  isMobile.value = window.innerWidth < 768
  if (isMobile.value) {
    sidebarCollapsed.value = true
  }
}

// Lifecycle
onMounted(() => {
  checkMobile()
  window.addEventListener('resize', checkMobile)
})

onUnmounted(() => {
  window.removeEventListener('resize', checkMobile)
})
</script>

<style scoped>
.app-shell {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #f8fafc;
}

.app-body {
  display: flex;
  flex: 1;
  overflow: hidden;
}

.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  margin-left: 280px;
  transition: margin-left 0.3s ease;
  overflow: hidden;
}

.main-content.sidebar-collapsed {
  margin-left: 80px;
}

.content-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: auto;
  padding: 24px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e2e8f0;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #1e293b;
  margin: 0;
}

.page-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.page-content {
  flex: 1;
}

/* Mobile Responsive */
@media (max-width: 768px) {
  .main-content {
    margin-left: 0;
  }
  
  .main-content.sidebar-collapsed {
    margin-left: 0;
  }
  
  .content-wrapper {
    padding: 16px;
  }
  
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
  
  .page-title {
    font-size: 20px;
  }
}
</style>
