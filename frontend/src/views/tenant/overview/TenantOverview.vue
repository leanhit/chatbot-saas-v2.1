<template>
  <div class="min-h-screen bg-gray-50 dark:bg-gray-900 py-8">
    <!-- Loading Overlay -->
    <div v-if="loading" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div class="bg-white dark:bg-gray-800 rounded-lg p-6 flex items-center space-x-3">
        <Icon icon="mdi:loading" class="h-6 w-6 animate-spin text-primary" />
        <span class="text-gray-900 dark:text-white">Loading...</span>
      </div>
    </div>
    
    <!-- Main Content -->
    <div class="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8">
      <!-- Header -->
      <div class="mb-8">
        <h1 class="text-3xl font-bold text-gray-900 dark:text-white">Tenant Overview</h1>
        <p class="mt-2 text-gray-600 dark:text-gray-400">Manage your tenant information and settings</p>
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
    AddressModal
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
    
    // Mock stats data - could be replaced with real API call
    const stats = ref({
      activeUsers: 156,
      totalProjects: 42,
      storageUsed: '2.4 GB',
      apiCalls: '1.2M'
    })
    
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
        // Validate file
        if (!file.type.startsWith('image/')) {
          toast?.error('Please select an image file')
          return
        }
        
        if (file.size > 5 * 1024 * 1024) {
          toast?.error('Image size should be less than 5MB')
          return
        }
        
        // Call tenant API to update logo
        const response = await tenantApi.updateTenantLogo(file)
        
        // Refresh tenant data
        await tenantStore.loadTenant()
        
        // Trigger logo refresh event
        window.dispatchEvent(new CustomEvent('tenant-logo-updated', { 
          detail: { timestamp: Date.now() } 
        }))
        
        toast?.success('Logo updated successfully!')
      } catch (error) {
        console.error('Logo upload error:', error)
        toast?.error('Failed to update logo')
      }
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
        
        const response = await tenantApi.updateTenantBasicInfo(tenantStore.activeTenantId, updateData)
        
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
        
        const response = await tenantApi.updateTenantProfile(tenantStore.activeTenantId, updateData)
        
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
        // Use activeTenantId from store (UUID like other tabs)
        const tenantKey = tenantStore.activeTenantId
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
      handleBasicSubmit,
      handleContactSubmit,
      handleAddressSubmit
    }
  }
}
</script>