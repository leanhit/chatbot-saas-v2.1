<template>
  <aside class="modern-sidebar" :class="{ collapsed: collapsed }">
    <!-- Logo Section -->
    <div class="sidebar-logo">
      <router-link to="/" class="logo-link">
        <img 
          src="@/assets/botpress-logo.png" 
          alt="Botpress Logo" 
          class="logo-image"
          :class="{ 'logo-collapsed': collapsed }"
        />
        <span v-if="!collapsed" class="logo-text">Botpress</span>
      </router-link>
    </div>

    <!-- Navigation Menu -->
    <nav class="sidebar-nav">
      <div class="nav-section" v-for="section in menuSections" :key="section.title">
        <div v-if="!collapsed" class="nav-section-title">
          {{ section.title }}
        </div>
        
        <ul class="nav-list">
          <li 
            v-for="item in section.items" 
            :key="item.path"
            class="nav-item"
            :class="{ 
              'nav-item-active': isActive(item.path),
              'nav-item-has-children': item.children
            }"
          >
            <!-- Menu Item with Children -->
            <template v-if="item.children">
              <button 
                @click="toggleSubmenu(item.path)"
                class="nav-button nav-button-parent"
                :class="{ 'nav-button-active': isActive(item.path) || isChildActive(item.children) }"
              >
                <i :class="item.icon" class="nav-icon"></i>
                <span v-if="!collapsed" class="nav-text">{{ item.label }}</span>
                <i 
                  v-if="!collapsed" 
                  :class="expandedMenus.includes(item.path) ? 'mdi mdi-chevron-down' : 'mdi mdi-chevron-right'"
                  class="nav-arrow"
                ></i>
              </button>
              
              <!-- Submenu -->
              <transition name="submenu">
                <ul 
                  v-show="expandedMenus.includes(item.path) && !collapsed" 
                  class="nav-submenu"
                >
                  <li 
                    v-for="child in item.children" 
                    :key="child.path"
                    class="nav-subitem"
                    :class="{ 'nav-subitem-active': isActive(child.path) }"
                  >
                    <router-link 
                      :to="child.path" 
                      class="nav-link nav-link-sub"
                      @click="handleNavigation(child.path)"
                    >
                      <i :class="child.icon" class="nav-icon nav-icon-sub"></i>
                      <span class="nav-text">{{ child.label }}</span>
                    </router-link>
                  </li>
                </ul>
              </transition>
            </template>
            
            <!-- Simple Menu Item -->
            <router-link 
              v-else
              :to="item.path" 
              class="nav-link"
              :class="{ 'nav-link-active': isActive(item.path) }"
              @click="handleNavigation(item.path)"
            >
              <i :class="item.icon" class="nav-icon"></i>
              <span v-if="!collapsed" class="nav-text">{{ item.label }}</span>
            </router-link>
          </li>
        </ul>
      </div>
    </nav>

    <!-- Sidebar Footer -->
    <div class="sidebar-footer">
      <button 
        @click="emit('toggle-collapse')"
        class="collapse-button"
        :title="collapsed ? 'Expand sidebar' : 'Collapse sidebar'"
      >
        <i :class="collapsed ? 'mdi mdi-menu-open' : 'mdi mdi-menu'" class="collapse-icon"></i>
      </button>
    </div>
  </aside>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'

// Props
interface Props {
  collapsed?: boolean
  menuItems?: any[]
}

const props = withDefaults(defineProps<Props>(), {
  collapsed: false
})

// Emits
const emit = defineEmits<{
  navigate: (path: string) => void
  'toggle-collapse': () => void
}>()

// Composables
const route = useRoute()
const { t } = useI18n()

// Reactive state
const expandedMenus = ref<string[]>([])

// Computed - Reuse menu structure from old sidebar
const menuSections = computed(() => {
  // If menuItems prop provided, use it
  if (props.menuItems && props.menuItems.length > 0) {
    return props.menuItems
  }
  
  // Default menu structure (copied from old Sidebar.vue)
  return [
    {
      title: t('Admin'),
      items: [
        {
          path: '/tenant',
          label: t('Tenant Manager'),
          icon: 'mdi mdi-domain',
          children: [
            {
              path: '/tenant/overview',
              label: t('Overview'),
              icon: 'mdi mdi-information-outline'
            },
            {
              path: '/tenant/member',
              label: t('Member'),
              icon: 'mdi mdi-account-multiple'
            }
          ]
        }
      ]
    },
    {
      title: t('Tool'),
      items: [
        {
          path: '/connection-manager',
          label: t('Conn Manager'),
          icon: 'mdi mdi-link-variant',
          children: [
            {
              path: '/connection-manager/auto-connect',
              label: t('Auto Connect'),
              icon: 'mdi mdi-autorenew'
            },
            {
              path: '/connection-manager/hand-connect',
              label: t('Hand Connect'),
              icon: 'mdi mdi-link'
            }
          ]
        },
        {
          path: '/image-manager',
          label: t('Image Manager'),
          icon: 'mdi mdi-image-multiple',
          children: [
            {
              path: '/image-manager/images',
              label: t('Images'),
              icon: 'mdi mdi-image'
            },
            {
              path: '/image-manager/categories',
              label: t('Categories'),
              icon: 'mdi mdi-folder-multiple-image'
            }
          ]
        },
        {
          path: '/bot-manager',
          label: t('Bot Manager'),
          icon: 'mdi mdi-settings',
          children: [
            {
              path: '/bot-manager/bot-botpress',
              label: t('Botpress'),
              icon: 'mdi mdi-robot'
            },
            {
              path: '/bot-manager/create-bot',
              label: t('Create Bot'),
              icon: 'mdi mdi-plus-circle'
            }
          ]
        }
      ]
    }
  ]
})

// Methods
const isActive = (path: string): boolean => {
  return route.path === path || route.path.startsWith(path + '/')
}

const isChildActive = (children: any[]): boolean => {
  return children.some(child => isActive(child.path))
}

const toggleSubmenu = (path: string) => {
  const index = expandedMenus.value.indexOf(path)
  if (index > -1) {
    expandedMenus.value.splice(index, 1)
  } else {
    expandedMenus.value.push(path)
  }
}

const handleNavigation = (path: string) => {
  emit('navigate', path)
}

// Auto-expand parent menu if child is active
menuSections.value.forEach(section => {
  section.items.forEach((item: any) => {
    if (item.children && isChildActive(item.children)) {
      if (!expandedMenus.value.includes(item.path)) {
        expandedMenus.value.push(item.path)
      }
    }
  })
})
</script>

<style scoped>
.modern-sidebar {
  width: 280px;
  height: 100vh;
  background: #ffffff;
  color: #374151;
  display: flex;
  flex-direction: column;
  transition: width 0.3s ease;
  position: fixed;
  left: 0;
  top: 0;
  z-index: 1000;
  border-right: 1px solid #e5e7eb;
}

.modern-sidebar.collapsed {
  width: 80px;
}

/* Logo Section */
.sidebar-logo {
  padding: 24px 20px;
  border-bottom: 1px solid #e5e7eb;
  display: flex;
  align-items: center;
  justify-content: center;
}

.logo-link {
  display: flex;
  align-items: center;
  text-decoration: none;
  color: #374151;
  gap: 12px;
}

.logo-image {
  height: 32px;
  width: auto;
  transition: all 0.3s ease;
}

.logo-collapsed {
  height: 40px;
}

.logo-text {
  font-size: 18px;
  font-weight: 600;
  color: #111827;
}

/* Navigation */
.sidebar-nav {
  flex: 1;
  overflow-y: auto;
  padding: 20px 0;
}

.nav-section {
  margin-bottom: 32px;
}

.nav-section:last-child {
  margin-bottom: 0;
}

.nav-section-title {
  padding: 8px 24px;
  font-size: 11px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  color: #9ca3af;
  margin-bottom: 8px;
}

.nav-list {
  list-style: none;
  margin: 0;
  padding: 0;
}

.nav-item {
  margin-bottom: 2px;
}

.nav-button,
.nav-link {
  display: flex;
  align-items: center;
  padding: 10px 24px;
  color: #6b7280;
  text-decoration: none;
  border: none;
  background: transparent;
  width: 100%;
  text-align: left;
  cursor: pointer;
  border-radius: 0;
  transition: all 0.2s ease;
  font-size: 14px;
  font-weight: 500;
}

.nav-button:hover,
.nav-link:hover {
  background: #f9fafb;
  color: #374151;
}

.nav-button-active,
.nav-link-active {
  background: #f3f4f6;
  color: #111827;
  border-left: 3px solid #6366f1;
  font-weight: 600;
}

.nav-icon {
  font-size: 18px;
  width: 20px;
  text-align: center;
  margin-right: 12px;
  color: #6b7280;
  transition: color 0.2s ease;
}

.nav-button:hover .nav-icon,
.nav-link:hover .nav-icon {
  color: #374151;
}

.nav-button-active .nav-icon,
.nav-link-active .nav-icon {
  color: #6366f1;
}

.nav-text {
  flex: 1;
  font-size: 14px;
  font-weight: 500;
}

.nav-arrow {
  font-size: 14px;
  color: #9ca3af;
  transition: transform 0.2s ease;
}

/* Submenu */
.nav-submenu {
  list-style: none;
  margin: 0;
  padding: 0;
  background: #fafbfc;
  border-left: 2px solid #f3f4f6;
}

.nav-subitem {
  margin-bottom: 1px;
}

.nav-link-sub {
  padding: 8px 24px 8px 56px;
  color: #6b7280;
  font-size: 13px;
  font-weight: 400;
}

.nav-link-sub:hover {
  background: #f3f4f6;
  color: #374151;
}

.nav-subitem-active .nav-link-sub {
  background: #f3f4f6;
  color: #6366f1;
  font-weight: 500;
}

.nav-icon-sub {
  font-size: 14px;
  margin-right: 8px;
}

/* Sidebar Footer */
.sidebar-footer {
  padding: 16px;
  border-top: 1px solid #e5e7eb;
  display: flex;
  justify-content: center;
}

.collapse-button {
  background: transparent;
  border: 1px solid #e5e7eb;
  color: #6b7280;
  padding: 8px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.collapse-button:hover {
  background: #f9fafb;
  color: #374151;
  border-color: #d1d5db;
}

.collapse-icon {
  font-size: 18px;
}

/* Collapsed State */
.collapsed .nav-section-title {
  display: none;
}

.collapsed .nav-text,
.collapsed .nav-arrow {
  display: none;
}

.collapsed .nav-button,
.collapsed .nav-link {
  justify-content: center;
  padding: 16px;
}

.collapsed .nav-icon {
  margin: 0;
}

/* Transitions */
.submenu-enter-active,
.submenu-leave-active {
  transition: all 0.3s ease;
  overflow: hidden;
}

.submenu-enter-from,
.submenu-leave-to {
  max-height: 0;
  opacity: 0;
}

.submenu-enter-to,
.submenu-leave-from {
  max-height: 300px;
  opacity: 1;
}

/* Scrollbar */
.sidebar-nav::-webkit-scrollbar {
  width: 4px;
}

.sidebar-nav::-webkit-scrollbar-track {
  background: #f9fafb;
}

.sidebar-nav::-webkit-scrollbar-thumb {
  background: #d1d5db;
  border-radius: 2px;
}

.sidebar-nav::-webkit-scrollbar-thumb:hover {
  background: #9ca3af;
}
</style>
