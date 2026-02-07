<template>
  <div class="min-h-screen bg-gray-50 dark:bg-gray-900 py-8">
    <div class="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8">
      <!-- Header -->
      <div class="mb-8">
        <h1 class="text-3xl font-bold text-gray-900 dark:text-white">{{ $t('tenant.overview.title') }}</h1>
        <p class="mt-2 text-gray-600 dark:text-gray-400">{{ $t('tenant.overview.subtitle') }}</p>
      </div>

      <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <!-- Sidebar - Logo & Basic Info -->
        <div class="lg:col-span-1">
          <div class="bg-white dark:bg-gray-800 shadow rounded-lg p-6">
            <!-- Logo Section -->
            <div class="text-center mb-6">
              <div class="relative inline-block">
                <img
                  :src="tenantLogo"
                  @error="handleLogoError"
                  class="mx-auto h-32 w-32 rounded-lg object-cover ring-4 ring-white dark:ring-gray-600 shadow-lg"
                  alt="Tenant Logo"
                />
                <button
                  @click="triggerLogoUpload"
                  class="absolute bottom-0 right-0 bg-primary text-white p-2 rounded-full shadow-lg hover:bg-primary/80 transition-colors"
                  title="Update Logo"
                >
                  <Icon icon="mdi:camera" class="h-4 w-4" />
                </button>
              </div>
              
              <h2 class="mt-4 text-xl font-semibold text-gray-900 dark:text-white">
                {{ tenantName }}
              </h2>
              <p class="mt-1 text-sm text-gray-500 dark:text-gray-400">
                {{ tenantDomain }}
              </p>
              
              <!-- Status Badges -->
              <div class="mt-3 flex justify-center gap-2">
                <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200">
                  <Icon icon="mdi:check-circle" class="mr-1 h-3 w-3" />
                  Active
                </span>
                <span v-if="tenantProfile?.plan" class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-200">
                  <Icon icon="mdi:star" class="mr-1 h-3 w-3" />
                  {{ tenantProfile.plan }}
                </span>
              </div>
            </div>

            <!-- Tenant Details -->
            <div class="space-y-3 border-t dark:border-gray-700 pt-4">
              <div v-if="tenantProfile?.contactEmail" class="flex items-center text-sm text-gray-600 dark:text-gray-400">
                <Icon icon="mdi:email" class="mr-2 h-4 w-4" />
                {{ tenantProfile.contactEmail }}
              </div>
              <div v-if="tenantProfile?.contactPhone" class="flex items-center text-sm text-gray-600 dark:text-gray-400">
                <Icon icon="mdi:phone" class="mr-2 h-4 w-4" />
                {{ tenantProfile.contactPhone }}
              </div>
              <div v-if="tenantProfile?.address" class="flex items-center text-sm text-gray-600 dark:text-gray-400">
                <Icon icon="mdi:map-marker" class="mr-2 h-4 w-4" />
                {{ tenantProfile.address }}
              </div>
              <div class="flex items-center text-sm text-gray-600 dark:text-gray-400">
                <Icon icon="mdi:calendar" class="mr-2 h-4 w-4" />
                {{ $t('tenant.overview.expiresAt') }}: {{ formatDate(tenantExpiresAt) }}
              </div>
            </div>

            <!-- External Links -->
            <div v-if="hasExternalLinks" class="mt-4 pt-4 border-t dark:border-gray-700">
              <div class="flex justify-center space-x-2">
                <button
                  v-if="tenantProfile?.website"
                  @click="openLink(tenantProfile.website)"
                  class="p-2 text-gray-400 hover:text-gray-600 transition-colors"
                  title="Website"
                >
                  <Icon icon="mdi:web" class="h-5 w-5" />
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- Main Content - Tabbed Interface -->
        <div class="lg:col-span-2">
          <div class="bg-white dark:bg-gray-800 shadow rounded-lg">
            <div class="p-6">
              <!-- Tab Navigation -->
              <div class="border-b border-gray-200 dark:border-gray-700">
                <nav class="-mb-px flex space-x-8">
                  <button
                    @click="activeTab = 'basic'"
                    :class="[
                      activeTab === 'basic'
                        ? 'border-primary text-primary'
                        : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300 dark:text-gray-400 dark:hover:text-gray-300',
                      'whitespace-nowrap py-2 px-1 border-b-2 font-medium text-sm transition-colors duration-200'
                    ]"
                  >
                    <span class="flex items-center">
                      <Icon icon="mdi:office-building" class="h-4 w-4 mr-2" />
                      {{ $t('tenant.overview.basicInfo') }}
                    </span>
                  </button>
                  <button
                    @click="activeTab = 'professional'"
                    :class="[
                      activeTab === 'professional'
                        ? 'border-primary text-primary'
                        : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300 dark:text-gray-400 dark:hover:text-gray-300',
                      'whitespace-nowrap py-2 px-1 border-b-2 font-medium text-sm transition-colors duration-200'
                    ]"
                  >
                    <span class="flex items-center">
                      <Icon icon="mdi:briefcase" class="h-4 w-4 mr-2" />
                      {{ $t('tenant.overview.professional') }}
                    </span>
                  </button>
                  <button
                    @click="activeTab = 'address'"
                    :class="[
                      activeTab === 'address'
                        ? 'border-primary text-primary'
                        : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300 dark:text-gray-400 dark:hover:text-gray-300',
                      'whitespace-nowrap py-2 px-1 border-b-2 font-medium text-sm transition-colors duration-200'
                    ]"
                  >
                    <span class="flex items-center">
                      <Icon icon="mdi:map-marker" class="h-4 w-4 mr-2" />
                      {{ $t('tenant.overview.address') }}
                    </span>
                  </button>
                </nav>
              </div>

              <!-- Tab Content -->
              <div class="p-6">
                <!-- Basic Info Tab -->
                <div v-if="activeTab === 'basic'" class="space-y-6">
                  <div class="bg-white dark:bg-gray-800 shadow rounded-lg p-6">
                    <div class="flex justify-between items-center mb-6">
                      <h3 class="text-lg font-medium text-gray-900 dark:text-white">{{ $t('tenant.overview.basicInfo') }}</h3>
                      <button
                        @click="handleEditBasic"
                        class="inline-flex items-center px-3 py-2 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary dark:bg-gray-700 dark:text-gray-300 dark:border-gray-600 dark:hover:bg-gray-600"
                      >
                        <Icon icon="mdi:pencil" class="h-4 w-4 mr-2" />
                        Edit
                      </button>
                    </div>
                    <p class="text-gray-600 dark:text-gray-400">Basic info component temporarily disabled</p>
                  </div>
                </div>
                
                <!-- Professional Tab -->
                <div v-if="activeTab === 'professional'" class="space-y-6">
                  <div class="bg-white dark:bg-gray-800 shadow rounded-lg p-6">
                    <div class="flex justify-between items-center mb-6">
                      <h3 class="text-lg font-medium text-gray-900 dark:text-white">{{ $t('tenant.overview.professional') }}</h3>
                      <button
                        @click="handleEditProfessional"
                        class="inline-flex items-center px-3 py-2 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary dark:bg-gray-700 dark:text-gray-300 dark:border-gray-600 dark:hover:bg-gray-600"
                      >
                        <Icon icon="mdi:pencil" class="h-4 w-4 mr-2" />
                        Edit
                      </button>
                    </div>
                    <div class="space-y-4">
                      <div>
                        <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">{{ $t('tenant.overview.industry') }}</label>
                        <p class="text-gray-900 dark:text-white">{{ tenantProfile?.industry || 'No industry specified' }}</p>
                      </div>
                      <div>
                        <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">{{ $t('tenant.overview.companySize') }}</label>
                        <p class="text-gray-900 dark:text-white">{{ tenantProfile?.companySize || 'No size specified' }}</p>
                      </div>
                      <div>
                        <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">{{ $t('tenant.overview.website') }}</label>
                        <a v-if="tenantProfile?.website" :href="tenantProfile.website" target="_blank" class="text-primary hover:text-primary/80">
                          {{ tenantProfile.website }}
                        </a>
                        <p v-else class="text-gray-500 dark:text-gray-400">No website provided</p>
                      </div>
                      <div>
                        <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">{{ $t('tenant.overview.description') }}</label>
                        <p class="text-gray-900 dark:text-white">{{ tenantProfile?.description || 'No description provided' }}</p>
                      </div>
                    </div>
                  </div>
                </div>
                
                <!-- Address Tab -->
                <div v-if="activeTab === 'address'" class="space-y-6">
                  <div class="bg-white dark:bg-gray-800 shadow rounded-lg p-6">
                    <div class="flex justify-between items-center mb-6">
                      <h3 class="text-lg font-medium text-gray-900 dark:text-white">{{ $t('tenant.overview.address') }}</h3>
                      <button
                        @click="handleEditAddress"
                        class="inline-flex items-center px-3 py-2 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary dark:bg-gray-700 dark:text-gray-300 dark:border-gray-600 dark:hover:bg-gray-600"
                      >
                        <Icon icon="mdi:pencil" class="h-4 w-4 mr-2" />
                        {{ tenantAddress ? 'Edit' : 'Add' }} Address
                      </button>
                    </div>
                    <div class="space-y-4">
                      <div v-if="tenantAddress" class="p-4 border rounded-lg dark:border-gray-600">
                        <div class="space-y-2">
                          <div>
                            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ $t('tenant.overview.houseNumber') }}</label>
                            <p class="text-gray-900 dark:text-white">{{ tenantAddress.houseNumber || 'Not provided' }}</p>
                          </div>
                          <div>
                            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ $t('tenant.overview.street') }}</label>
                            <p class="text-gray-900 dark:text-white">{{ tenantAddress.street || 'Not provided' }}</p>
                          </div>
                          <div>
                            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ $t('tenant.overview.ward') }}</label>
                            <p class="text-gray-900 dark:text-white">{{ tenantAddress.ward || 'Not provided' }}</p>
                          </div>
                          <div>
                            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ $t('tenant.overview.district') }}</label>
                            <p class="text-gray-900 dark:text-white">{{ tenantAddress.district || 'Not provided' }}</p>
                          </div>
                          <div>
                            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ $t('tenant.overview.province') }}</label>
                            <p class="text-gray-900 dark:text-white">{{ tenantAddress.province || 'Not provided' }}</p>
                          </div>
                          <div>
                            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ $t('tenant.overview.country') }}</label>
                            <p class="text-gray-900 dark:text-white">{{ tenantAddress.country || 'Not provided' }}</p>
                          </div>
                          <div>
                            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ $t('tenant.overview.fullAddress') }}</label>
                            <p class="text-gray-900 dark:text-white">{{ tenantAddress.fullAddress || 'Not provided' }}</p>
                          </div>
                        </div>
                      </div>
                      <div v-else class="text-center py-8 text-gray-500 dark:text-gray-400">
                        <Icon icon="mdi:map-marker-off" class="h-12 w-12 mx-auto mb-2" />
                        <p>No address found</p>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <!-- Hidden file input for logo upload -->
    <input
      ref="logoInput"
      type="file"
      accept="image/*"
      @change="handleLogoUpload"
      style="display: none"
    />

    <!-- Modals -->
    <TenantBasicInfoModalSimple
      :visible="showBasicModal"
      :tenant-data="tenant"
      @close="showBasicModal = false"
      @submit="handleBasicSubmit"
    />

    <TenantProfessionalModalSimple
      :visible="showProfessionalModal"
      :tenant-data="tenant"
      @close="showProfessionalModal = false"
      @submit="handleProfessionalSubmit"
    />

    <TenantAddressModalSimple
      :visible="showAddressModal"
      :address-data="currentAddressData"
      @close="showAddressModal = false"
      @submit="handleAddressSubmit"
    />
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { Icon } from '@iconify/vue'
import { useI18n } from 'vue-i18n'
import { useGatewayTenantStore } from '@/stores/tenant/gateway/myTenantStore'
import { tenantApi } from '@/api/tenantApi'
import TenantBasicInfoModalSimple from './components/TenantBasicInfoModalSimple.vue'
import TenantProfessionalModalSimple from './components/TenantProfessionalModalSimple.vue'
import TenantAddressModalSimple from './components/TenantAddressModalSimple.vue'
import defaultLogo from '@/assets/img/company-placeholder.png'
import { secureImageUrl } from '@/utils/imageUtils'

export default {
  name: 'TenantOverview',
  components: {
    Icon,
    TenantBasicInfoModalSimple,
    TenantProfessionalModalSimple,
    TenantAddressModalSimple
  },
  emits: ['tenant-updated'],
  setup(props, { emit }) {
    const tenantStore = useGatewayTenantStore()
    const logoInput = ref(null)
    const activeTab = ref('basic')
    
    // Modal state
    const showBasicModal = ref(false)
    const showProfessionalModal = ref(false)
    const showAddressModal = ref(false)
    const currentAddressData = ref({})
    
    // Tab configuration
    const tabs = [
      { id: 'basic', label: 'Basic Info', icon: 'mdi:office-building' },
      { id: 'professional', label: 'Professional', icon: 'mdi:briefcase' },
      { id: 'address', label: 'Address', icon: 'mdi:map-marker' }
    ]

    // Computed properties
    const tenant = computed(() => tenantStore.currentTenant)
    
    const tenantName = computed(() => {
      return tenant.value?.name || 'Unknown Tenant'
    })

    const tenantDomain = computed(() => {
      return tenant.value?.domain || tenant.value?.tenantKey || 'No domain'
    })

    const tenantKey = computed(() => {
      return tenant.value?.tenantKey || tenant.value?.id || ''
    })

    const tenantExpiresAt = computed(() => {
      return tenant.value?.expiresAt
    })

    const tenantProfile = computed(() => {
      return tenant.value?.profile || {}
    })

    const tenantLogo = computed(() => {
      const currentTenant = tenant.value
      const logo = currentTenant?.profile?.logoUrl || currentTenant?.logo
      
      if (logo) {
        // If logo is a full URL, secure it (giống user avatar)
        if (logo.startsWith('http')) {
          return secureImageUrl(logo)
        }
        // If logo is a file ID, use public backend content endpoint (giống user avatar)
        const apiUrl = process.env.VITE_API_URL || 'http://localhost:8080/api'
        const baseUrl = apiUrl.endsWith('/') ? apiUrl.slice(0, -1) : apiUrl
        return `${baseUrl}/images/public/${logo}/content`
      }
      // Fallback to default logo (giống user avatar)
      return defaultLogo
    })

    const tenantAddress = computed(() => {
      const addresses = tenant.value?.addresses || []
      return addresses.length > 0 ? addresses[0] : null
    })


    const hasExternalLinks = computed(() => {
      return tenantProfile.value?.website
    })
    const getStatusBadgeClass = (status) => {
      switch (status) {
        case 'ACTIVE':
          return 'inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200'
        case 'INACTIVE':
          return 'inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200'
        case 'SUSPENDED':
          return 'inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200'
        default:
          return 'inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-gray-100 text-gray-800 dark:bg-gray-900 dark:text-gray-200'
      }
    }

    const getStatusLabel = (status) => {
      switch (status) {
        case 'ACTIVE': return 'Active'
        case 'INACTIVE': return 'Inactive'
        case 'SUSPENDED': return 'Suspended'
        default: return 'Unknown'
      }
    }

    const formatDate = (dateString) => {
      if (!dateString) return 'No date'
      return new Date(dateString).toLocaleDateString()
    }

    const openLink = (url) => {
      window.open(url, '_blank')
    }

    const triggerLogoUpload = () => {
      logoInput.value.click()
    }

    const handleLogoError = (event) => {
      const img = event.target
      const originalSrc = img.src
      
      console.error('🖼️ Tenant logo failed to load:', originalSrc)
      
      // If it's a data URL error, just use default logo
      if (originalSrc.startsWith('data:')) {
        console.log('🖼️ Data URL failed, using default logo')
        img.src = defaultLogo
        return
      }
      
      // Try to handle SSL errors with proxy (giống user avatar)
      if (originalSrc && originalSrc.includes('cwsv.truyenthongviet.vn:9000')) {
        try {
          const urlObj = new URL(originalSrc)
          
          // Check if we're in development or production
          const isDevelopment = window.location.hostname === 'localhost'
          
          let proxyUrl
          if (isDevelopment) {
            // Development: use local proxy
            proxyUrl = `http://localhost:3004/files${urlObj.pathname}${urlObj.search}`
          } else {
            // Production: use production proxy on same domain
            proxyUrl = `/files${urlObj.pathname}${urlObj.search}`
          }
          
          console.log('🖼️ Trying proxy URL:', proxyUrl)
          img.src = proxyUrl
          
          img.onerror = () => {
            console.error('🖼️ Proxy URL also failed:', proxyUrl)
            // Fallback to default logo
            img.src = defaultLogo
          }
        } catch (e) {
          console.error('🖼️ Error creating proxy URL:', e)
          // Fallback to default logo
          img.src = defaultLogo
        }
      } else {
        // For other errors, just set default logo
        console.log('🖼️ Setting default logo for non-SSL error')
        img.src = defaultLogo
      }
    }

    const handleLogoUpload = async (event) => {
      const file = event.target.files[0]
      if (!file) return

      // Validate file type
      if (!file.type.startsWith('image/')) {
        alert('Please select an image file')
        return
      }

      // Validate file size (max 5MB)
      if (file.size > 5 * 1024 * 1024) {
        alert('Image size should be less than 5MB')
        return
      }

      try {
        // Use new tenant API endpoint
        const response = await tenantApi.updateTenantLogo(file)
        
        console.log('Logo uploaded successfully:', response.data)
        
        // Show success message
        alert('Logo updated successfully!')
        
        // Refresh tenant data
        await loadTenantData()
      } catch (error) {
        console.error('Logo upload error:', error)
        alert('Failed to upload logo. Please try again.')
      } finally {
        // Clear the file input
        event.target.value = ''
      }
    }

    const openEmail = (email) => {
      window.open(`mailto:${email}`, '_blank')
    }

    const handleTabChange = (tabName) => {
      activeTab.value = tabName
      console.log('Tab changed to:', tabName)
    }

    const handleEditBasic = () => {
      showBasicModal.value = true
    }

    const handleEditProfessional = () => {
      showProfessionalModal.value = true
    }

    const handleEditAddress = () => {
      if (tenantAddress.value) {
        currentAddressData.value = { ...tenantAddress.value }
      } else {
        // New address - reset form
        currentAddressData.value = {
          houseNumber: '',
          street: '',
          ward: '',
          district: '',
          province: '',
          country: 'Vietnam'
        }
      }
      showAddressModal.value = true
    }

    // Modal submit handlers
    const handleBasicSubmit = async (formData) => {
      try {
        console.log('Submitting basic info:', formData)
        // TODO: Call API to update basic info
        // await tenantApi.updateTenantBasicInfo(tenant.value?.tenantKey, formData)
        
        // Close modal and refresh data
        showBasicModal.value = false
        await loadTenantData()
      } catch (error) {
        console.error('Error updating basic info:', error)
        alert('Failed to update basic information. Please try again.')
      }
    }

    const handleProfessionalSubmit = async (formData) => {
      try {
        console.log('Submitting professional info:', formData)
        // TODO: Call API to update professional info
        // await tenantApi.updateTenantProfessionalInfo(tenant.value?.tenantKey, formData)
        
        // Close modal and refresh data
        showProfessionalModal.value = false
        await loadTenantData()
      } catch (error) {
        console.error('Error updating professional info:', error)
        alert('Failed to update professional information. Please try again.')
      }
    }

    const handleAddressSubmit = async (formData) => {
      try {
        console.log('Submitting address info:', formData)
        
        // TODO: Call API to update/create address
        if (tenantAddress.value) {
          // Update existing address
          console.log('Updating existing address')
          // await addressApi.updateAddress(tenant.value?.tenantKey, tenantAddress.value.id, formData)
        } else {
          // Create new address
          console.log('Creating new address')
          // await addressApi.createAddress(tenant.value?.tenantKey, formData)
        }
        
        // Close modal and refresh data
        showAddressModal.value = false
        currentAddressData.value = {}
        await loadTenantData()
      } catch (error) {
        console.error('Error updating address:', error)
        alert('Failed to update address. Please try again.')
      }
    }

    const loadTenantData = async () => {
      try {
        console.log('🔍 Loading tenant data...', {
          currentTenant: tenant.value,
          tenantId: tenant.value?.id,
          tenantKey: tenant.value?.tenantKey
        })
        
        if (tenant.value?.id) {
          // Load by ID (primary method since backend only returns id)
          console.log('📡 Calling getTenantDetail with ID:', tenant.value.id)
          const response = await tenantApi.getTenantDetail(tenant.value.id)
          tenantStore.setCurrentTenant(response.data)
          console.log('✅ Tenant data loaded by ID:', response.data)
        } else if (tenant.value?.tenantKey) {
          // Fallback: load by tenantKey (if available)
          console.log('📡 Calling getTenantDetailByTenantKey with key:', tenant.value.tenantKey)
          const response = await tenantApi.getTenantDetailByTenantKey(tenant.value.tenantKey)
          tenantStore.setCurrentTenant(response.data)
          console.log('✅ Tenant data loaded by key:', response.data)
        } else {
          console.warn('❌ No tenant ID or tenantKey found')
        }
      } catch (error) {
        console.error('❌ Error loading tenant data:', error)
        console.error('Error details:', {
          message: error.message,
          response: error.response?.data,
          status: error.response?.status
        })
      }
    }

    // Load tenant data on mount
    onMounted(() => {
      loadTenantData()
    })

    return {
      tenantStore,
      tenant,
      logoInput,
      activeTab,
      tabs,
      showBasicModal,
      showProfessionalModal,
      showAddressModal,
      currentAddressData,
      tenantAddress,
      tenantName,
      tenantDomain,
      tenantKey,
      tenantExpiresAt,
      tenantProfile,
      tenantLogo,
      hasExternalLinks,
      getStatusBadgeClass,
      getStatusLabel,
      formatDate,
      triggerLogoUpload,
      handleLogoError,
      handleLogoUpload,
      openLink,
      openEmail,
      handleTabChange,
      handleEditBasic,
      handleEditProfessional,
      handleEditAddress,
      handleBasicSubmit,
      handleProfessionalSubmit,
      handleAddressSubmit,
      loadTenantData
    }
  }
}
</script>
