<template>
  <!-- Modern Header -->
  <header class="bg-white dark:bg-gray-900 border-b border-gray-200 dark:border-gray-700 shadow-sm">
    <div class="px-4 sm:px-6 lg:px-8">
      <div class="flex items-center justify-between h-16">
        <!-- Left Section -->
        <div class="flex items-center">
          <!-- Sidebar Toggle Button -->
          <button
            @click="handleSidebarToggle"
            class="p-2 rounded-lg text-gray-500 hover:text-gray-700 hover:bg-gray-100 dark:text-gray-400 dark:hover:text-gray-200 dark:hover:bg-gray-800 transition-all duration-200"
            title="Toggle Sidebar"
          >
            <Icon :icon="sidebarOpen ? 'mdi:menu-open' : 'mdi:menu'" class="text-xl" />
          </button>
          
          <!-- Search Bar -->
          <div class="ml-4 flex-1 max-w-lg">
            <div class="relative">
              <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                <Icon icon="ei:search" class="h-5 w-5 text-gray-400" />
              </div>
              <input
                type="text"
                v-model="searchStore.searchQuery"
                :placeholder="searchPlaceholder"
                class="block w-full pl-10 pr-10 py-2 border border-gray-300 rounded-lg bg-gray-50 text-gray-900 placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent dark:bg-gray-800 dark:border-gray-600 dark:text-white dark:placeholder-gray-400 dark:focus:ring-blue-400"
              />
              <div v-if="searchStore.hasQuery" class="absolute inset-y-0 right-0 pr-3 flex items-center">
                <button
                  @click="searchStore.clearSearch"
                  class="p-1 rounded-md text-gray-400 hover:text-gray-600 dark:hover:text-gray-300"
                >
                  <Icon icon="mdi:close" class="h-4 w-4" />
                </button>
              </div>
            </div>
            <!-- Search Context Indicator -->
            <div class="mt-2">
              <SearchContextIndicator />
            </div>
          </div>
        </div>

        <!-- Right Section -->
        <div class="flex items-center space-x-2" v-if="authStore.isLoggedIn">
          <!-- Language Switcher -->
          <LanguageSwitcher />
          
          <!-- Fullscreen Toggle -->
          <button
            @click="fullscreenToggle"
            class="p-2 rounded-lg text-gray-500 hover:text-gray-700 hover:bg-gray-100 dark:text-gray-400 dark:hover:text-gray-200 dark:hover:bg-gray-800 transition-all duration-200"
            title="Toggle Fullscreen"
          >
            <Icon :icon="!fullscreenMode ? 'mdi:fullscreen' : 'mdi:fullscreen-exit'" class="text-xl" />
          </button>
          
          <!-- Theme Toggle -->
          <button
            @click="setTheme(!darkMode)"
            class="p-2 rounded-lg text-gray-500 hover:text-gray-700 hover:bg-gray-100 dark:text-gray-400 dark:hover:text-gray-200 dark:hover:bg-gray-800 transition-all duration-200"
            :title="darkMode ? 'Switch to Light Mode' : 'Switch to Dark Mode'"
          >
            <Icon :icon="darkMode ? 'ri:sun-fill' : 'ri:moon-fill'" class="text-xl" />
          </button>
          
          <!-- Notifications -->
          <div class="relative">
            <button
              @click="notifToggle"
              class="p-2 rounded-lg text-gray-500 hover:text-gray-700 hover:bg-gray-100 dark:text-gray-400 dark:hover:text-gray-200 dark:hover:bg-gray-800 transition-all duration-200 relative"
              title="Notifications"
            >
              <Icon icon="clarity:notification-line" class="text-xl" />
              <!-- Notification Counter -->
              <span 
                v-if="notificationStore.unreadCount > 0"
                class="absolute -top-1 -right-1 inline-flex items-center justify-center px-2 py-1 text-xs font-bold leading-none text-white bg-red-500 rounded-full min-w-[20px] h-5"
              >
                {{ notificationStore.unreadCount > 99 ? '99+' : notificationStore.unreadCount }}
              </span>
            </button>
            
            <!-- Notification Dropdown -->
            <transition
              enter-active-class="transition ease-out duration-200"
              enter-from-class="transform opacity-0 scale-95"
              enter-to-class="transform opacity-100 scale-100"
              leave-active-class="transition ease-in duration-75"
              leave-from-class="transform opacity-100 scale-100"
              leave-to-class="transform opacity-0 scale-95"
            >
              <div
                v-show="notification"
                class="absolute right-0 mt-2 w-80 bg-white dark:bg-gray-800 rounded-lg shadow-lg ring-1 ring-black ring-opacity-5 z-50"
              >
                <div class="p-4 border-b border-gray-200 dark:border-gray-700">
                  <div class="flex items-center justify-between">
                    <h3 class="text-sm font-medium text-gray-900 dark:text-white">Notifications</h3>
                    <button
                      @click="notificationStore.markAllAsRead()"
                      class="text-xs text-blue-600 hover:text-blue-500 dark:text-blue-400"
                    >
                      Mark all as read
                    </button>
                  </div>
                </div>
                
                <div class="max-h-96 overflow-y-auto">
                  <div v-if="!notificationStore.notifications.length" class="p-8 text-center">
                    <Icon icon="mdi:bell-off" class="mx-auto h-12 w-12 text-gray-400" />
                    <p class="mt-2 text-sm text-gray-500 dark:text-gray-400">No notifications yet</p>
                  </div>
                  
                  <div v-else class="divide-y divide-gray-200 dark:divide-gray-700">
                    <div
                      v-for="(item, index) in notificationStore.notifications.slice(0, 5)"
                      :key="index"
                      class="p-4 hover:bg-gray-50 dark:hover:bg-gray-700 cursor-pointer transition-colors"
                      @click="markAsRead(item.id)"
                    >
                      <div class="flex items-start">
                        <div class="flex-shrink-0">
                          <div class="h-8 w-8 rounded-full bg-blue-100 dark:bg-blue-900 flex items-center justify-center">
                            <Icon :icon="getNotificationIcon(item.type)" class="h-4 w-4 text-blue-600 dark:text-blue-400" />
                          </div>
                        </div>
                        <div class="ml-3 flex-1">
                          <p class="text-sm font-medium text-gray-900 dark:text-white">{{ item.title }}</p>
                          <p class="text-sm text-gray-500 dark:text-gray-400 mt-1">{{ item.message }}</p>
                          <p class="text-xs text-gray-400 dark:text-gray-500 mt-1">{{ formatTime(item.timestamp) }}</p>
                        </div>
                        <div v-if="!item.read" class="flex-shrink-0 ml-2">
                          <div class="h-2 w-2 bg-blue-600 rounded-full"></div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
                
                <div v-if="notificationStore.notifications.length > 5" class="p-4 border-t border-gray-200 dark:border-gray-700">
                  <button class="w-full text-center text-sm text-blue-600 hover:text-blue-500 dark:text-blue-400">
                    View all notifications
                  </button>
                </div>
              </div>
            </transition>
          </div>
          
          <!-- User Menu -->
          <div class="relative">
            <button
              @click="menuToggle"
              class="flex items-center p-2 rounded-lg text-gray-500 hover:text-gray-700 hover:bg-gray-100 dark:text-gray-400 dark:hover:text-gray-200 dark:hover:bg-gray-800 transition-all duration-200"
            >
              <div class="flex items-center space-x-3">
                <img
                  :src="userAvatar"
                  class="h-8 w-8 rounded-full object-cover ring-2 ring-gray-300 dark:ring-gray-600"
                  alt="User Avatar"
                  @error="handleAvatarError"
                />
                <div class="hidden lg:block text-left">
                  <p class="text-sm font-medium text-gray-900 dark:text-white">{{ userName }}</p>
                  <p class="text-xs text-gray-500 dark:text-gray-400">{{ userRole || 'User' }}</p>
                </div>
                <Icon icon="mdi:chevron-down" class="h-4 w-4" />
              </div>
            </button>
            
            <!-- User Dropdown -->
            <transition
              enter-active-class="transition ease-out duration-200"
              enter-from-class="transform opacity-0 scale-95"
              enter-to-class="transform opacity-100 scale-100"
              leave-active-class="transition ease-in duration-75"
              leave-from-class="transform opacity-100 scale-100"
              leave-to-class="transform opacity-0 scale-95"
            >
              <div
                v-show="menu"
                class="absolute right-0 mt-2 w-48 bg-white dark:bg-gray-800 rounded-lg shadow-lg ring-1 ring-black ring-opacity-5 z-50"
                @blur="menuToggleBlur"
              >
                <div class="p-4 border-b border-gray-200 dark:border-gray-700">
                  <p class="text-sm font-medium text-gray-900 dark:text-white">{{ userName }}</p>
                  <p class="text-sm text-gray-500 dark:text-gray-400">{{ userEmail || 'Unknown User' }}</p>
                </div>
                
                <div class="py-2">
                  <router-link
                    to="/profile"
                    @click="menu = false"
                    class="flex items-center px-4 py-2 text-sm text-gray-700 dark:text-gray-200 hover:bg-gray-100 dark:hover:bg-gray-700"
                  >
                    <Icon icon="mdi:account-circle" class="mr-3 h-4 w-4" />
                    Profile
                  </router-link>
                  
                  <a
                    href="#"
                    @click.prevent="handleTenantGateway"
                    class="flex items-center px-4 py-2 text-sm text-gray-700 dark:text-gray-200 hover:bg-gray-100 dark:hover:bg-gray-700"
                  >
                    <Icon icon="mdi:office-building" class="mr-3 h-4 w-4" />
                    Tenant Gateway
                  </a>
                  
                  <a
                    href="#"
                    class="flex items-center px-4 py-2 text-sm text-gray-700 dark:text-gray-200 hover:bg-gray-100 dark:hover:bg-gray-700"
                  >
                    <Icon icon="mdi:cog" class="mr-3 h-4 w-4" />
                    Settings
                  </a>
                </div>
                
                <div class="border-t border-gray-200 dark:border-gray-700 py-2">
                  <a
                    href="#"
                    @click.prevent="handleLogout"
                    class="flex items-center px-4 py-2 text-sm text-red-600 dark:text-red-400 hover:bg-red-50 dark:hover:bg-red-900/20"
                  >
                    <Icon icon="mdi:logout" class="mr-3 h-4 w-4" />
                    Sign out
                  </a>
                </div>
              </div>
            </transition>
          </div>
        </div>
        
        <!-- Login Button (when not logged in) -->
        <div v-else class="flex items-center space-x-2">
          <LanguageSwitcher />
          <router-link 
            to="/login" 
            class="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-lg text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 transition-all duration-200"
          >
            <Icon icon="mdi:login" class="mr-2 h-4 w-4" />
            Sign in
          </router-link>
        </div>
      </div>
    </div>
  </header>
</template>
<style></style>
<script>
  import { Icon } from "@iconify/vue";
  import { useRouter } from "vue-router";
  import { useI18n } from 'vue-i18n';
  import { ref, onUnmounted, computed } from 'vue';
  import { fullscreen } from "@/helper/fullscreen";
  import { setDarkMode, loadDarkMode } from "@/helper/theme";
  import LanguageSwitcher from "./LanguageSwitcher.vue";
  import SearchContextIndicator from "./common/SearchContextIndicator.vue";
  import { useAuthStore } from '@/stores/authStore';
  import { useSearchStore } from '@/stores/searchStore';
  import { useNotificationStore } from '@/stores/notification/notificationStore';
  import { usersApi } from '@/api/usersApi';
  import { secureImageUrl } from '@/utils/imageUtils';
  export default {
    props: {
      sidebarOpen: {
        type: Boolean,
        default: true
      }
    },
    data() {
      return {
        menu: false,
        darkMode: false,
        notification: false,
        fullscreenMode: false
      };
    },
    components: {
      Icon,
      LanguageSwitcher,
      SearchContextIndicator,
    },
    setup() {
      const { t } = useI18n()
      const authStore = useAuthStore();
      const searchStore = useSearchStore();
      const notificationStore = useNotificationStore();
      const router = useRouter();
      const avatarTimestamp = ref(Date.now());
      
      // Computed property for search placeholder based on context
      const searchPlaceholder = computed(() => {
        switch (searchStore.searchContext) {
          case 'members':
            return t('header.searchMembers') || 'Search members...';
          case 'projects':
            return t('header.searchProjects') || 'Search projects...';
          case 'files':
            return t('header.searchFiles') || 'Search files...';
          default:
            return t('header.searchPlaceholder') || 'Search...';
        }
      });
      // Listen for avatar updates from profile component
      const handleAvatarUpdate = () => {
        avatarTimestamp.value = Date.now();
      };
      // Add event listener for avatar updates
      window.addEventListener('avatar-updated', handleAvatarUpdate);
      // Cleanup on unmount
      onUnmounted(() => {
        window.removeEventListener('avatar-updated', handleAvatarUpdate);
      });
      return {
        authStore,
        searchStore,
        notificationStore,
        router,
        t,
        avatarTimestamp,
        searchPlaceholder
      };
    },
    computed: {
      userName() {
        return this.authStore.currentUser?.profile?.fullName || 
               this.authStore.currentUser?.name || 
               this.authStore.currentUser?.email?.split('@')[0] || 
               'User';
      },
      userEmail() {
        return this.authStore.currentUser?.email || '';
      },
      userRole() {
        return this.authStore.currentUser?.systemRole || this.authStore.currentUser?.role || 'User';
      },
      userAvatar() {
        const user = this.authStore.currentUser;
        const avatar = user?.profile?.avatar || user?.avatar;
        if (avatar) {
          // If avatar is a full URL, secure it and add cache busting
          if (avatar.startsWith('http')) {
            const securedUrl = secureImageUrl(avatar);
            // Use reactive timestamp to force refresh when avatar is updated
            return `${securedUrl}?t=${this.avatarTimestamp}`;
          }
          // If avatar is a file ID, use public backend content endpoint with cache busting
          const apiUrl = process.env.VITE_API_URL || 'http://localhost:8080/api';
          const baseUrl = apiUrl.endsWith('/') ? apiUrl.slice(0, -1) : apiUrl;
          return `${baseUrl}/images/public/${avatar}/content?t=${this.avatarTimestamp}`;
        }
        // Fallback to default avatar
        return require("@/assets/img/user.jpg");
      }
    },
    watch: {
      $route() {
        this.menu = false;
        this.notification = false;
      }
    },
    methods: {
      fullscreen,
      setDarkMode,
      loadDarkMode,
      // handle sidebar toggle
      handleSidebarToggle() {
        this.$emit('sidebarToggle');
      },
      menuToggle: function () {
        this.menu = !this.menu;
      },
      menuToggleBlur: function () {
        this.menu = false;
      },
      notifToggle: function () {
        this.notification = !this.notification;
      },
      notifToggleBlur: function () {
        this.notification = false;
      },
      limitText(message) {
        const text =
          message.length > 25 ? message.substring(0, 25) + "..." : message;
        return text;
      },
      fullscreenToggle() {
        this.fullscreenMode = !this.fullscreenMode;
        this.fullscreen(this.fullscreenMode);
      },
      // set theme to dark and light
      setTheme(bool) {
        this.darkMode = bool;
        this.setDarkMode(bool);
      },
      // handle logout
      handleLogout() {
        this.authStore.logout();
        this.menu = false;
      },
      // handle avatar image error
      handleAvatarError(event) {
        const img = event.target;
        const originalSrc = img.src;
        // Try to handle Botpress SSL errors with proxy
        if (originalSrc && originalSrc.includes('cwsv.truyenthongviet.vn:9000')) {
          try {
            const urlObj = new URL(originalSrc);
            // Check if we're in development or production
            const isDevelopment = window.location.hostname === 'localhost';
            let proxyUrl;
            if (isDevelopment) {
              // Development: use local proxy
              proxyUrl = `http://localhost:3004/files${urlObj.pathname}${urlObj.search}`;
            } else {
              // Production: use production proxy on same domain
              proxyUrl = `/files${urlObj.pathname}${urlObj.search}`;
            }
            img.src = proxyUrl;
            img.onerror = () => {
              // Fallback to default avatar
              img.src = require("@/assets/img/user.jpg");
            };
          } catch (e) {
            // Fallback to default avatar
            img.src = require("@/assets/img/user.jpg");
          }
        } else {
          // For other errors (like profile URL), just set default avatar
          img.src = require("@/assets/img/user.jpg");
        }
      },
      // handle tenant gateway click
      handleTenantGateway() {
        // Navigate to tenant gateway
        this.$router.push('/tenant-gateway').then(() => {
        }).catch(error => {
        });
        this.menu = false;
      },
      imageAssets(url) {
        return require("@/assets/img/" + url);
      },
      // Get notification icon based on type
      getNotificationIcon(type) {
        const icons = {
          'info': 'mdi:information',
          'success': 'mdi:check-circle',
          'warning': 'mdi:alert',
          'error': 'mdi:alert-circle',
          'system_alert': 'mdi:alert',
          'tenant_invitation': 'mdi:account-plus',
          'join_request': 'mdi:account-question',
          'agent_takeover': 'mdi:account-switch'
        };
        return icons[type] || 'mdi:bell';
      },
      // Format timestamp
      formatTime(timestamp) {
        if (!timestamp) return '';
        
        const date = new Date(timestamp);
        const now = new Date();
        const diff = now - date;
        
        // Less than 1 minute
        if (diff < 60000) {
          return 'Just now';
        }
        
        // Less than 1 hour
        if (diff < 3600000) {
          const minutes = Math.floor(diff / 60000);
          return `${minutes} minute${minutes > 1 ? 's' : ''} ago`;
        }
        
        // Less than 24 hours
        if (diff < 86400000) {
          const hours = Math.floor(diff / 3600000);
          return `${hours} hour${hours > 1 ? 's' : ''} ago`;
        }
        
        // More than 24 hours
        const days = Math.floor(diff / 86400000);
        return `${days} day${days > 1 ? 's' : ''} ago`;
      },
      // Mark notification as read
      markAsRead(id) {
        this.notificationStore.markAsRead(id);
      }
    },
    mounted() {
      // get theme dark or light with loadDarkMode()
      this.darkMode = this.loadDarkMode();
      document.onfullscreenchange = (event) => {
        if (document.fullscreenElement) {
          this.fullscreenMode = true;
        } else {
          this.fullscreenMode = false;
        }
      };
    },
  };
</script>
