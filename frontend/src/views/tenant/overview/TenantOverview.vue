<template>
  <div class="min-h-screen bg-gray-50 dark:bg-gray-900 py-8">
    <!-- Loading Overlay -->
    <div v-if="loading" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div class="bg-white dark:bg-gray-800 rounded-lg p-6 flex items-center space-x-3">
        <Icon icon="mdi:loading" class="h-6 w-6 animate-spin text-primary" />
        <span class="text-gray-900 dark:text-white">{{ $t('common.loading') }}</span>
      </div>
    </div>
    
    <!-- Main Content -->
    <div class="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8">
      <!-- Header -->
      <div class="mb-8">
        <h1 class="text-3xl font-bold text-gray-900 dark:text-white">{{ $t('tenant.overview.title') }}</h1>
        <p class="mt-2 text-gray-600 dark:text-gray-400">{{ $t('tenant.overview.subtitle') }}</p>
      </div>
      
      <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <!-- Sidebar -->
        <div class="lg:col-span-1 space-y-6">
          <!-- Avatar Card -->
          <AvatarCard 
            :tenant="tenant" 
            @manage-members="handleManageMembers"
            @settings="handleSettings"
            @update-logo="handleUpdateLogo"
          />
          
          <!-- Quick Stats -->
          <QuickStats :stats="stats" :tenant="tenant" />
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
                      <Icon icon="mdi:information" class="h-4 w-4 mr-2" />
                      Basic Info
                    </span>
                  </button>
                  <button
                    @click="activeTab = 'contact'"
                    :class="[
                      activeTab === 'contact'
                        ? 'border-primary text-primary'
                        : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300 dark:text-gray-400 dark:hover:text-gray-300',
                      'whitespace-nowrap py-2 px-1 border-b-2 font-medium text-sm transition-colors duration-200'
                    ]"
                  >
                    <span class="flex items-center">
                      <Icon icon="mdi:account-details" class="h-4 w-4 mr-2" />
                      Profile
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
                      Address
                    </span>
                  </button>
                </nav>
              </div>
              
              <!-- Tab Content -->
              <div class="mt-6">
                <!-- Basic Info Tab -->
                <BasicInfoTab 
                  v-if="activeTab === 'basic'"
                  :tenant="tenant"
                  :loading="loading"
                  @edit="handleEditBasic"
                />
                
                <!-- Profile Info Tab -->
                <ContactTab 
                  v-if="activeTab === 'contact'"
                  :tenant="tenant"
                  :loading="loading"
                  @edit="handleEditContact"
                />
                
                <!-- Address Tab -->
                <AddressTab 
                  v-if="activeTab === 'address'"
                  :tenant-address="tenantAddress"
                  :loading="loading"
                  @edit="handleEditAddress"
                />
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Modals -->
    <!-- Basic Info Edit Modal -->
    <BasicInfoModal
      :show="showBasicModal"
      :tenant="tenant"
      :loading="loading"
      @close="showBasicModal = false"
      @submit="handleBasicSubmit"
    />

    <!-- Contact Info Edit Modal -->
    <ContactModal
      :show="showContactModal"
      :tenant="tenant"
      :loading="loading"
      @close="showContactModal = false"
      @submit="handleContactSubmit"
    />

    <!-- Address Edit Modal -->
    <AddressModal
      :show="showAddressModal"
      :tenant-address="tenantAddress"
      :loading="loading"
      @close="showAddressModal = false"
      @submit="handleAddressSubmit"
    />
    
    <!-- Image Cropper Modal -->
    <ImageCropper
      :isVisible="showImageCropper"
      title="Crop Logo"
      :imageUrl="previewImageUrl"
      :outputSize="1200"
      @cancel="handleImageCropperCancel"
      @crop="handleLogoCrop"
    />
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { Icon } from '@iconify/vue'
import { useRouter } from 'vue-router'

// Import components
import AvatarCard from './components/AvatarCard.vue'
import QuickStats from './components/QuickStats.vue'
import BasicInfoTab from './components/tabs/BasicInfoTab.vue'
import ContactTab from './components/tabs/ContactTab.vue'
import AddressTab from './components/tabs/AddressTab.vue'
import BasicInfoModal from './components/modals/BasicInfoModal.vue'
import ContactModal from './components/modals/ContactModal.vue'
import AddressModal from './components/modals/AddressModal.vue'
import ImageCropper from '@/components/common/ImageCropper.vue'

// Import store and API
import { useTenantAdminContextStore } from '@/stores/tenant/admin/tenantContextStore'
import { tenantApi } from '@/api/tenantApi'
import { addressApi } from '@/api/addressApi'
import { dateTimeLocalToIso } from '@/utils/dateUtils'
import { getCurrentInstance } from 'vue'

export default {
  name: 'TenantOverview',
  components: {
    Icon,
    AvatarCard,
    QuickStats,
    BasicInfoTab,
    ContactTab,
    AddressTab,
    BasicInfoModal,
    ContactModal,
    AddressModal,
    ImageCropper
  },
  setup() {
    const router = useRouter()
    const tenantStore = useTenantAdminContextStore()
    const instance = getCurrentInstance()
    const toast = instance?.appContext.config.globalProperties.$toast
    
    // Reactive state
    const activeTab = ref('basic')
    const showBasicModal = ref(false)
    const showContactModal = ref(false)
    const showAddressModal = ref(false)
    const showImageCropper = ref(false)
    const selectedImageFile = ref(null)
    const previewImageUrl = ref('')
    
    // Real stats data from API
    const stats = ref({
      activeUsers: 0,
      totalProjects: 0,
      storageUsed: '0 GB',
      apiCalls: '0'
    })
    
    // Load tenant statistics
    const loadStats = async () => {
      try {
        const tenantKey = tenantStore.activeTenantKey
        if (!tenantKey) return
        
        // Call API to get tenant statistics
        // Note: This endpoint needs to be implemented in backend
        // const response = await tenantApi.getTenantStats(tenantKey)
        // stats.value = response.data
        
        // For now, keep default values until backend endpoint is ready
        console.log('Tenant stats endpoint not yet implemented')
      } catch (error) {
        console.error('Failed to load tenant stats:', error)
      }
    }
    
    // Computed properties
    const tenant = computed(() => tenantStore.tenant)
    const loading = computed(() => tenantStore.loading)
    const tenantAddress = computed(() => {
      // Ưu tiên đọc từ tenant.address (object đơn từ backend response)
      if (tenant.value?.address) {
        return tenant.value.address
      }
      // Fallback: đọc từ mảng addresses (cho tương thích)
      const addresses = tenant.value?.addresses || []
      return addresses.length > 0 ? addresses[0] : null
    })
    
    // Methods
    const handleEditBasic = () => {
      showBasicModal.value = true
    }
    
    const handleEditContact = () => {
      showContactModal.value = true
    }
    
    const handleEditAddress = () => {
      if (!tenantAddress.value?.id) {
        // Don't allow adding new address since it's created during tenant creation
        toast?.error('Address not found. Please contact support.')
        return
      }
      // Only allow editing existing address
      showAddressModal.value = true
    }
    
    const handleManageMembers = () => {
      router.push('/tenant/members')
    }
    
    const handleSettings = () => {
      router.push('/tenant/settings')
    }
    
    const handleUpdateLogo = () => {
      // Trigger logo upload - similar to user profile
      const input = document.createElement('input')
      input.type = 'file'
      input.accept = 'image/*'
      input.onchange = async (event) => {
        const file = event.target.files[0]
        if (file) {
          await handleLogoUpload(file)
        }
      }
      input.click()
    }
    
    const handleLogoUpload = async (file) => {
      try {
        // Log file details before validation
        console.log('🔄 [FRONTEND LOGO] File selected:', {
          name: file.name,
          size: file.size,
          type: file.type,
          lastModified: file.lastModified
        })
        
        // Validate file is not empty
        if (file.size === 0) {
          console.error('❌ [FRONTEND LOGO] File is empty:', file.name)
          toast?.error('Please select a valid image file (file is empty)')
          return
        }
        
        // Validate file
        if (!file.type.startsWith('image/')) {
          console.error('❌ [FRONTEND LOGO] Invalid file type:', file.type, file.name)
          toast?.error('Please select an image file')
          return
        }
        
        // Store file and show cropper
        selectedImageFile.value = file
        previewImageUrl.value = URL.createObjectURL(file)
        showImageCropper.value = true
      } catch (error) {
        console.error('❌ [FRONTEND LOGO] Error in handleLogoUpload:', error)
        toast?.error('Failed to process image. Please try again.')
      }
    }
    
    const handleLogoCrop = async (croppedFile) => {
      try {
        console.log('✅ [FRONTEND LOGO] Logo cropped:', {
          originalSize: selectedImageFile.value.size,
          croppedSize: croppedFile.size,
          reduction: Math.round((1 - croppedFile.size / selectedImageFile.value.size) * 100) + '%'
        })
        
        // Validate file size (max 5MB)
        if (croppedFile.size > 5 * 1024 * 1024) {
          console.error('❌ [FRONTEND LOGO] Cropped file too large:', croppedFile.size, croppedFile.name)
          toast?.error('Image size should be less than 5MB')
          return
        }
        
        console.log('✅ [FRONTEND LOGO] File validation passed:', croppedFile.name)
        
        // Call tenant API to update logo
        console.log('📤 [FRONTEND LOGO] Sending request to backend:', {
          tenantKey: tenantStore.activeTenantKey,
          endpoint: `/tenant/${tenantStore.activeTenantKey}/logo`,
          originalFileSize: selectedImageFile.value.size,
          croppedFileSize: croppedFile.size,
          fileName: croppedFile.name
        })
        
        const response = await tenantApi.uploadTenantLogo(tenantStore.activeTenantKey, croppedFile)
        console.log('✅ [FRONTEND LOGO] Backend response:', response.data)
        
        // Refresh tenant data
        await tenantStore.loadTenant()
        
        // Trigger logo refresh event
        window.dispatchEvent(new CustomEvent('tenant-logo-updated', { 
          detail: { timestamp: Date.now() } 
        }))
        
        toast?.success('Logo updated successfully!')
        
        // Close cropper modal after successful upload
        showImageCropper.value = false
        // Clean up preview URL
        if (previewImageUrl.value) {
          URL.revokeObjectURL(previewImageUrl.value)
          previewImageUrl.value = ''
        }
        selectedImageFile.value = null
      } catch (error) {
        console.error('❌ [FRONTEND LOGO] Upload error:', error)
        
        // Handle 413 Payload Too Large error specifically
        let errorMessage = 'Failed to update logo'
        if (error.response?.status === 413) {
          console.error('File size too large - 413 error:', error)
          errorMessage = 'File size too large. Please choose a smaller image (max 10MB).'
        } else if (error.response?.status === 400 && error.response?.data?.error?.includes('size')) {
          console.error('File size validation error:', error.response.data.error)
          errorMessage = 'File size too large. Please choose a smaller image (max 10MB).'
        } else if (error.response?.data?.message) {
          errorMessage = error.response.data.message
        } else if (error.message) {
          errorMessage = error.message
        }
        
        console.error('Error details:', {
          message: errorMessage,
          response: error.response?.data,
          status: error.response?.status
        })
        
        toast?.error(errorMessage)
      }
    }
    
    const handleImageCropperCancel = () => {
      showImageCropper.value = false
      // Clean up preview URL
      if (previewImageUrl.value) {
        URL.revokeObjectURL(previewImageUrl.value)
        previewImageUrl.value = ''
      }
      selectedImageFile.value = null
    }
    
    // Modal submit handlers
    const handleBasicSubmit = async (formData) => {
      try {
        // Call API to update tenant basic info
        // Only send fields that are actually filled in
        const updateData = {}
        
        // Only include non-empty fields
        if (formData.name && formData.name.trim()) {
          updateData.name = formData.name
        }
        if (formData.status) {
          updateData.status = formData.status
        }
        if (formData.visibility) {
          updateData.visibility = formData.visibility
        }
        if (formData.expiresAt) {
          // Convert datetime-local to ISO string for backend
          const isoDate = dateTimeLocalToIso(formData.expiresAt)
          if (isoDate) {
            updateData.expiresAt = isoDate
          }
        }
        
        const response = await tenantApi.updateTenant(tenantStore.activeTenantKey, updateData)
        
        // Update tenant data directly from response
        if (response.data) {
          const currentTenant = tenant.value
          if (currentTenant) {
            Object.keys(response.data).forEach(key => {
              currentTenant[key] = response.data[key]
            })
          }
        }
        
        // Close modal
        showBasicModal.value = false
        // Show success message
        toast?.success('Basic info updated successfully')
      } catch (error) {
        toast?.error('Error updating basic info')
      }
    }
    
    const handleContactSubmit = async (formData) => {
      try {
        // Call API to update profile info
        // Only send fields that are actually filled in
        const updateData = {}
        
        // Only include non-empty fields
        if (formData.description && formData.description.trim()) {
          updateData.description = formData.description
        }
        if (formData.industry && formData.industry.trim()) {
          updateData.industry = formData.industry
        }
        if (formData.plan && formData.plan.trim()) {
          updateData.plan = formData.plan
        }
        if (formData.companySize && formData.companySize.trim()) {
          updateData.companySize = formData.companySize
        }
        if (formData.legalName && formData.legalName.trim()) {
          updateData.legalName = formData.legalName
        }
        if (formData.taxCode && formData.taxCode.trim()) {
          updateData.taxCode = formData.taxCode
        }
        if (formData.contactEmail && formData.contactEmail.trim()) {
          updateData.contactEmail = formData.contactEmail
        }
        if (formData.contactPhone && formData.contactPhone.trim()) {
          updateData.contactPhone = formData.contactPhone
        }
        if (formData.logoUrl && formData.logoUrl.trim()) {
          updateData.logoUrl = formData.logoUrl
        }
        if (formData.faviconUrl && formData.faviconUrl.trim()) {
          updateData.faviconUrl = formData.faviconUrl
        }
        if (formData.primaryColor && formData.primaryColor.trim()) {
          updateData.primaryColor = formData.primaryColor
        }
        
        const response = await tenantApi.updateTenantProfile(tenantStore.activeTenantKey, updateData)
        
        // Update tenant data directly from response
        if (response.data) {
          const currentTenant = tenant.value
          if (currentTenant && currentTenant.profile) {
            Object.keys(response.data).forEach(key => {
              currentTenant.profile[key] = response.data[key]
            })
          }
        }
        
        // Close modal
        showContactModal.value = false
        // Show success message
        toast?.success('Profile info updated successfully')
      } catch (error) {
        toast?.error('Error updating profile info')
      }
    }
    
    const handleAddressSubmit = async (formData) => {
      try {
        // Use activeTenantKey from store (UUID like other tabs)
        const tenantKey = tenantStore.activeTenantKey
        if (!tenantKey) {
          toast?.error('Tenant ID not found')
          return
        }
        
        // Only update existing address - no creation allowed
        if (!tenantAddress.value?.id) {
          toast?.error('Address not found. Please contact support.')
          return
        }
        
        // Update tenant address using tenantKey endpoint (consistent with other tabs)
        await addressApi.updateTenantAddress(tenantKey, formData)
        
        // Close modal and refresh data
        showAddressModal.value = false
        await tenantStore.loadTenant()
        
        // Refresh logo cache busting if logo was updated
        if (formData.logoUrl) {
          // Trigger logo refresh in child components
          window.dispatchEvent(new CustomEvent('tenant-logo-updated', { 
            detail: { timestamp: Date.now() } 
          }))
        }
        
        // Show success message
        toast?.success('Address updated successfully')
      } catch (error) {
        toast?.error('Error updating address')
      }
    }
    
    // Load tenant data on mount
    onMounted(async () => {
      try {
        await tenantStore.loadTenant()
        await loadStats() // Load statistics
        console.log('Tenant data loaded')
      } catch (error) {
        toast?.error('Error loading tenant data')
      }
    })
    
    return {
      // State
      activeTab,
      showBasicModal,
      showContactModal,
      showAddressModal,
      showImageCropper,
      selectedImageFile,
      previewImageUrl,
      // Data
      tenant,
      tenantAddress,
      stats,
      loading,
      // Methods
      handleEditBasic,
      handleEditContact,
      handleEditAddress,
      handleManageMembers,
      handleSettings,
      handleUpdateLogo,
      handleLogoUpload,
      handleLogoCrop,
      handleImageCropperCancel,
      handleBasicSubmit,
      handleContactSubmit,
      handleAddressSubmit
    }
  }
}
</script>