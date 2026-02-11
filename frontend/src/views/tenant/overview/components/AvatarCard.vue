<template>
  <div class="bg-white dark:bg-gray-800 shadow rounded-lg p-6">
    <!-- Profile Header -->
    <div class="text-center">
      <!-- Avatar Section - Like User Profile -->
      <div class="text-center mb-6">
        <div class="relative inline-block">
          <img
            v-if="tenantLogoUrl"
            :src="tenantLogoUrl"
            class="mx-auto h-32 w-32 rounded-full object-cover ring-4 ring-white dark:ring-gray-600 shadow-lg"
            :alt="tenant?.name || 'Tenant Logo'"
            @error="handleLogoError"
            @load="handleLogoLoad"
          />
          <div
            v-else
            class="mx-auto h-32 w-32 rounded-full bg-gradient-to-br from-blue-500 to-purple-600 flex items-center justify-center text-white text-3xl font-bold ring-4 ring-white dark:ring-gray-600 shadow-lg"
          >
            {{ getInitials() }}
          </div>
          <button
            @click="$emit('update-logo')"
            class="absolute bottom-0 right-0 bg-primary text-white p-2 rounded-full shadow-lg hover:bg-primary/80 transition-colors"
            title="Update Logo"
          >
            <Icon icon="mdi:camera" class="h-4 w-4" />
          </button>
        </div>
      </div>
      <h2 class="mt-4 text-xl font-semibold text-gray-900 dark:text-white">
        {{ tenant?.name || 'Unknown Tenant' }}
      </h2>
      <p class="mt-1 text-sm text-gray-500 dark:text-gray-400">
        {{ tenant?.domain || 'No domain' }}
      </p>
      
      <!-- Status Badges -->
      <div class="mt-3 flex justify-center gap-2">
        <span :class="getStatusBadgeClass()">
          {{ getStatusLabel() }}
        </span>
        <span v-if="tenant?.plan" class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-purple-100 text-purple-800 dark:bg-purple-900 dark:text-purple-200">
          <Icon icon="mdi:crown" class="mr-1 h-3 w-3" />
          {{ tenant.plan }}
        </span>
      </div>
    </div>
    
    <!-- User Details -->
    <div class="space-y-3 border-t dark:border-gray-700 pt-4 mt-4">
      <div v-if="tenant?.industry" class="flex items-center text-sm text-gray-600 dark:text-gray-400">
        <Icon icon="mdi:domain" class="mr-2 h-4 w-4" />
        {{ tenant.industry }}
      </div>
      <div v-if="tenant?.size" class="flex items-center text-sm text-gray-600 dark:text-gray-400">
        <Icon icon="mdi:account-group" class="mr-2 h-4 w-4" />
        {{ tenant.size }} employees
      </div>
      <div v-if="tenant?.createdAt" class="flex items-center text-sm text-gray-600 dark:text-gray-400">
        <Icon icon="mdi:calendar" class="mr-2 h-4 w-4" />
        Created {{ formatDate(tenant.createdAt) }}
      </div>
      <div class="flex items-center text-sm text-gray-600 dark:text-gray-400">
        <Icon icon="mdi:map-marker" class="mr-2 h-4 w-4" />
        {{ tenant?.location || 'Location not set' }}
      </div>
    </div>
    
    <!-- Action Links -->
    <div class="mt-4 pt-4 border-t dark:border-gray-700">
      <div class="flex justify-center space-x-2">
        <button
          v-if="tenant?.website"
          @click="openLink(tenant.website)"
          class="p-2 text-gray-400 hover:text-blue-600 transition-colors"
          title="Website"
        >
          <Icon icon="mdi:web" class="h-5 w-5" />
        </button>
        <button
          @click="$emit('manage-members')"
          class="p-2 text-gray-400 hover:text-green-600 transition-colors"
          title="Manage Members"
        >
          <Icon icon="mdi:account-group" class="h-5 w-5" />
        </button>
        <button
          @click="$emit('settings')"
          class="p-2 text-gray-400 hover:text-gray-600 transition-colors"
          title="Settings"
        >
          <Icon icon="mdi:cog" class="h-5 w-5" />
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import { computed, ref, onMounted, onUnmounted } from 'vue'
import { Icon } from '@iconify/vue'

export default {
  name: 'AvatarCard',
  components: {
    Icon
  },
  props: {
    tenant: {
      type: Object,
      default: () => ({})
    }
  },
  emits: ['manage-members', 'settings', 'update-logo'],
  setup(props) {
    const logoError = ref(false)
    const logoTimestamp = ref(Date.now())
    
    // Listen for logo update events
    const handleLogoUpdate = (event) => {
      logoTimestamp.value = event.detail.timestamp
      logoError.value = false // Reset error state
    }
    
    onMounted(() => {
      window.addEventListener('tenant-logo-updated', handleLogoUpdate)
    })
    
    onUnmounted(() => {
      window.removeEventListener('tenant-logo-updated', handleLogoUpdate)
    })
    const getInitials = () => {
      const name = props.tenant?.name || 'Unknown'
      return name.split(' ').map(word => word.charAt(0).toUpperCase()).join('').slice(0, 2)
    }
    
    const getStatusBadgeClass = () => {
      const status = props.tenant?.status
      switch (status) {
        case 'ACTIVE':
          return 'inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200'
        case 'INACTIVE':
          return 'inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200'
        case 'PENDING':
          return 'inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200'
        default:
          return 'inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-gray-100 text-gray-800 dark:bg-gray-900 dark:text-gray-200'
      }
    }
    
    const getStatusLabel = () => {
      const status = props.tenant?.status
      switch (status) {
        case 'ACTIVE': return 'Active'
        case 'INACTIVE': return 'Inactive'
        case 'PENDING': return 'Pending'
        default: return 'Unknown'
      }
    }
    
    const openLink = (url) => {
      window.open(url, '_blank')
    }
    
    // Tenant logo URL logic (similar to user avatar)
    const tenantLogoUrl = computed(() => {
      if (logoError.value) return null
      
      // Debug: Log tenant data structure
      console.log('Tenant data:', props.tenant)
      console.log('Tenant profile:', props.tenant?.profile)
      
      const logoUrl = props.tenant?.profile?.logoUrl || props.tenant?.logoUrl
      console.log('Logo URL found:', logoUrl)
      
      if (!logoUrl) return null
      
      // Add cache busting timestamp
      const timestamp = logoTimestamp.value
      
      // Get backend API URL
      const apiUrl = process.env.VITE_API_URL || 'https://chat.truyenthongviet.vn/api'
      const baseUrl = apiUrl.endsWith('/') ? apiUrl.slice(0, -1) : apiUrl
      
      // Handle different URL formats
      if (logoUrl.startsWith('http')) {
        // Full URL case
        return `${logoUrl}?t=${timestamp}`
      } else if (logoUrl.startsWith('/api/')) {
        // API relative URL case - remove /api/ prefix since baseUrl already includes /api
        const pathWithoutApi = logoUrl.replace('/api', '')
        const fullUrl = `${baseUrl}${pathWithoutApi}?t=${timestamp}`
        console.log('Constructed full URL:', fullUrl)
        return fullUrl
      } else {
        // File ID case - construct public URL
        const finalUrl = `${baseUrl}/images/public/${logoUrl}/content?t=${timestamp}`
        console.log('Constructed URL from file ID:', finalUrl)
        return finalUrl
      }
    })
    
    // Handle logo load error
    const handleLogoError = () => {
      console.warn('Failed to load tenant logo, falling back to initials')
      logoError.value = true
    }
    
    // Handle logo load success
    const handleLogoLoad = () => {
      console.log('Tenant logo loaded successfully')
      logoError.value = false
    }
    
    // Method to refresh logo (for cache busting)
    const refreshLogo = () => {
      logoError.value = false
      logoTimestamp.value = Date.now()
    }
    
    const formatDate = (dateString) => {
      if (!dateString) return 'Not available'
      const date = new Date(dateString)
      return date.toLocaleDateString('en-US', {
        year: 'numeric',
        month: 'long',
        day: 'numeric'
      })
    }
    
    return {
      getInitials,
      getStatusBadgeClass,
      getStatusLabel,
      openLink,
      formatDate,
      tenantLogoUrl,
      handleLogoError,
      handleLogoLoad,
      refreshLogo,
      logoTimestamp
    }
  }
}
</script>
