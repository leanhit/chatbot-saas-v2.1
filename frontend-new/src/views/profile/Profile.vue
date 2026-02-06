<template>
  <div class="min-h-screen bg-gray-50 dark:bg-gray-900 py-8">
    <!-- Loading Overlay -->
    <div v-if="loading" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div class="bg-white dark:bg-gray-800 rounded-lg p-6 flex items-center space-x-3">
        <Icon icon="mdi:loading" class="h-6 w-6 animate-spin text-primary" />
        <span class="text-gray-900 dark:text-white">Loading profile...</span>
      </div>
    </div>

    <div class="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8">
      <!-- Header -->
      <div class="mb-8">
        <h1 class="text-3xl font-bold text-gray-900 dark:text-white">{{ $t('profile.title') }}</h1>
        <p class="mt-2 text-gray-600 dark:text-gray-400">{{ $t('profile.subtitle') }}</p>
      </div>

      <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <!-- Sidebar - Avatar & Basic Info -->
        <div class="lg:col-span-1">
          <div class="bg-white dark:bg-gray-800 shadow rounded-lg p-6">
            <!-- Avatar Section -->
            <div class="text-center mb-6">
              <div class="relative inline-block">
                <img
                  :src="userAvatar"
                  class="mx-auto h-32 w-32 rounded-full object-cover ring-4 ring-white dark:ring-gray-600 shadow-lg"
                  alt="User Avatar"
                />
                <button
                  @click="triggerAvatarUpload"
                  :disabled="avatarUploading"
                  class="absolute bottom-0 right-0 bg-primary text-white p-2 rounded-full shadow-lg hover:bg-primary/80 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                  :title="avatarUploading ? 'Uploading...' : 'Update Avatar'"
                >
                  <Icon v-if="!avatarUploading" icon="mdi:camera" class="h-4 w-4" />
                  <Icon v-else icon="mdi:loading" class="h-4 w-4 animate-spin" />
                </button>
              </div>
              
              <h2 class="mt-4 text-xl font-semibold text-gray-900 dark:text-white">
                {{ userFullName }}
              </h2>
              <p class="mt-1 text-sm text-gray-500 dark:text-gray-400">
                {{ userEmail }}
              </p>
              
              <!-- Role Badges -->
              <div class="mt-3 flex justify-center gap-2">
                <span :class="getRoleBadgeClass()">
                  {{ getRoleLabel() }}
                </span>
                <span v-if="userProfile?.jobTitle" class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-200">
                  <Icon icon="mdi:briefcase" class="mr-1 h-3 w-3" />
                  {{ userProfile.jobTitle }}
                </span>
              </div>
            </div>

            <!-- Professional Details -->
            <div class="space-y-3 border-t dark:border-gray-700 pt-4">
              <div v-if="userProfile?.company" class="flex items-center text-sm text-gray-600 dark:text-gray-400">
                <Icon icon="mdi:office-building" class="mr-2 h-4 w-4" />
                {{ userProfile.company }}
              </div>
              
              <!-- Location Selector -->
              <div class="flex items-center justify-between text-sm text-gray-600 dark:text-gray-400">
                <div class="flex items-center">
                  <Icon icon="mdi:map-marker" class="mr-2 h-4 w-4" />
                  <span class="mr-2">Location:</span>
                </div>
                <select 
                  v-model="selectedLocation" 
                  @change="handleLocationChange"
                  class="text-xs bg-transparent border border-gray-300 dark:border-gray-600 rounded px-2 py-1 focus:outline-none focus:ring-1 focus:ring-primary dark:text-gray-300"
                >
                  <option value="">Select Location</option>
                  <option value="Hanoi">Hà Nội</option>
                  <option value="HCMC">TP. Hồ Chí Minh</option>
                  <option value="DaNang">Đà Nẵng</option>
                  <option value="HaiPhong">Hải Phòng</option>
                  <option value="CanTho">Cần Thơ</option>
                  <option value="Other">Khác</option>
                </select>
              </div>
              
              <div v-if="userProfile?.availability" class="flex items-center text-sm text-gray-600 dark:text-gray-400">
                <Icon icon="mdi:clock" class="mr-2 h-4 w-4" />
                {{ userProfile.availability }}
              </div>
            </div>

            <!-- Social Links -->
            <div v-if="hasSocialLinks" class="mt-4 pt-4 border-t dark:border-gray-700">
              <div class="flex justify-center space-x-2">
                <button
                  v-if="userProfile?.linkedinUrl"
                  @click="openLink(userProfile.linkedinUrl)"
                  class="p-2 text-gray-400 hover:text-blue-600 transition-colors"
                  title="LinkedIn"
                >
                  <Icon icon="mdi:linkedin" class="h-5 w-5" />
                </button>
                <button
                  v-if="userProfile?.website"
                  @click="openLink(userProfile.website)"
                  class="p-2 text-gray-400 hover:text-gray-600 transition-colors"
                  title="Website"
                >
                  <Icon icon="mdi:web" class="h-5 w-5" />
                </button>
                <button
                  v-if="userProfile?.portfolioUrl"
                  @click="openLink(userProfile.portfolioUrl)"
                  class="p-2 text-gray-400 hover:text-purple-600 transition-colors"
                  title="Portfolio"
                >
                  <Icon icon="mdi:folder" class="h-5 w-5" />
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
                      <Icon icon="mdi:account-details" class="h-4 w-4 mr-2" />
                      {{ $t('profile.basicInfo') }}
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
                      {{ $t('profile.professional') }}
                    </span>
                  </button>
                  <button
                    @click="activeTab = 'address'"
                    :class="[
                      activeTab === 'address'
                        ? 'border-primary text-primary'
                        : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300 dark:text-gray-400 dark:hover:text-gray-300',
                      'whitespace-nowrap py-2 px-1 border-b-2 font-medium text-sm transition-colors duration-200 relative group'
                    ]"
                  >
                    <span class="flex items-center">
                      <Icon icon="mdi:map-marker" class="h-4 w-4 mr-2" />
                      {{ $t('profile.address') }}
                      <div class="relative inline-block">
                        <Icon icon="mdi:information-outline" class="h-4 w-4 text-gray-400 ml-1 cursor-help" />
                        <!-- Tooltip -->
                        <div class="absolute bottom-full left-1/2 transform -translate-x-1/2 mb-2 px-3 py-2 bg-gray-900 dark:bg-gray-700 text-white text-xs rounded-lg shadow-lg opacity-0 invisible group-hover:opacity-100 group-hover:visible transition-all duration-200 whitespace-nowrap z-50">
                          <div class="absolute bottom-0 left-1/2 transform -translate-x-1/2 translate-y-1/2 rotate-45 w-2 h-2 bg-gray-900 dark:bg-gray-700"></div>
                          {{ $t('profile.address') }}
                        </div>
                      </div>
                    </span>
                  </button>
                </nav>
              </div>

              <!-- Tab Content -->
              <div class="mt-6">
                <!-- Basic Info Tab -->
                <div v-if="activeTab === 'basic'" class="space-y-6">
                  <div class="bg-white dark:bg-gray-800 shadow rounded-lg p-6">
                    <div class="flex justify-between items-center mb-6">
                      <h3 class="text-lg font-medium text-gray-900 dark:text-white">{{ $t('profile.basicInfo') }}</h3>
                      <button
                        @click="handleEditBasic"
                        :disabled="loading"
                        class="inline-flex items-center px-3 py-2 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary dark:bg-gray-700 dark:text-gray-300 dark:border-gray-600 dark:hover:bg-gray-600 disabled:opacity-50 disabled:cursor-not-allowed"
                      >
                        <Icon v-if="!loading" icon="mdi:pencil" class="h-4 w-4 mr-2" />
                        <Icon v-else icon="mdi:loading" class="h-4 w-4 mr-2 animate-spin" />
                        {{ loading ? 'Loading...' : 'Edit' }}
                      </button>
                    </div>
                    <div class="space-y-4">
                      <div>
                        <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">{{ $t('profile.fullName') }}</label>
                        <p class="text-gray-900 dark:text-white">{{ user?.profile?.fullName || 'Not provided' }}</p>
                      </div>
                      <div>
                        <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">{{ $t('profile.phoneNumber') }}</label>
                        <p class="text-gray-900 dark:text-white">{{ user?.profile?.phoneNumber || 'Not provided' }}</p>
                      </div>
                      <div>
                        <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">{{ $t('profile.gender') }}</label>
                        <p class="text-gray-900 dark:text-white">{{ formatGender(user?.profile?.gender) || 'Not provided' }}</p>
                      </div>
                      <div>
                        <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">{{ $t('profile.bio') }}</label>
                        <p class="text-gray-900 dark:text-white">{{ user?.profile?.bio || 'Not provided' }}</p>
                      </div>
                    </div>
                  </div>
                </div>
                
                <!-- Professional Info Tab -->
                <div v-if="activeTab === 'professional'" class="space-y-6">
                  <div class="bg-white dark:bg-gray-800 shadow rounded-lg p-6">
                    <div class="flex justify-between items-center mb-6">
                      <h3 class="text-lg font-medium text-gray-900 dark:text-white">{{ $t('profile.professional') }}</h3>
                      <button
                        @click="handleEditProfessional"
                        :disabled="loading"
                        class="inline-flex items-center px-3 py-2 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary dark:bg-gray-700 dark:text-gray-300 dark:border-gray-600 dark:hover:bg-gray-600 disabled:opacity-50 disabled:cursor-not-allowed"
                      >
                        <Icon v-if="!loading" icon="mdi:pencil" class="h-4 w-4 mr-2" />
                        <Icon v-else icon="mdi:loading" class="h-4 w-4 mr-2 animate-spin" />
                        {{ loading ? 'Loading...' : 'Edit' }}
                      </button>
                    </div>
                    <div class="space-y-4">
                      <div>
                        <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">{{ $t('profile.jobTitle') }}</label>
                        <p class="text-gray-900 dark:text-white">{{ user?.profile?.jobTitle || 'Not provided' }}</p>
                      </div>
                      <div>
                        <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">{{ $t('profile.department') }}</label>
                        <p class="text-gray-900 dark:text-white">{{ user?.profile?.department || 'Not provided' }}</p>
                      </div>
                      <div>
                        <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">{{ $t('profile.company') }}</label>
                        <p class="text-gray-900 dark:text-white">{{ user?.profile?.company || 'Not provided' }}</p>
                      </div>
                      <div>
                        <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">{{ $t('profile.skills') }}</label>
                        <p class="text-gray-900 dark:text-white">{{ user?.profile?.skills || 'Not provided' }}</p>
                      </div>
                      <div>
                        <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">{{ $t('profile.experience') }}</label>
                        <p class="text-gray-900 dark:text-white">{{ user?.profile?.experience || 'Not provided' }}</p>
                      </div>
                      <div>
                        <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">{{ $t('profile.education') }}</label>
                        <p class="text-gray-900 dark:text-white">{{ user?.profile?.education || 'Not provided' }}</p>
                      </div>
                      <div>
                        <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">{{ $t('profile.certifications') }}</label>
                        <p class="text-gray-900 dark:text-white">{{ user?.profile?.certifications || 'Not provided' }}</p>
                      </div>
                      <div>
                        <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">{{ $t('profile.languages') }}</label>
                        <p class="text-gray-900 dark:text-white">{{ user?.profile?.languages || 'Not provided' }}</p>
                      </div>
                    </div>
                  </div>
                </div>
                
                <!-- Address Tab -->
                <div v-if="activeTab === 'address'" class="space-y-6">
                  <div class="bg-white dark:bg-gray-800 shadow rounded-lg p-6">
                    <div class="flex justify-between items-center mb-6">
                      <h3 class="text-lg font-medium text-gray-900 dark:text-white">{{ $t('profile.address') }}</h3>
                      <button
                        @click="handleEditAddress"
                        :disabled="loading"
                        class="inline-flex items-center px-3 py-2 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary dark:bg-gray-700 dark:text-gray-300 dark:border-gray-600 dark:hover:bg-gray-600 disabled:opacity-50 disabled:cursor-not-allowed"
                      >
                        <Icon v-if="!loading" icon="mdi:pencil" class="h-4 w-4 mr-2" />
                        <Icon v-else icon="mdi:loading" class="h-4 w-4 mr-2 animate-spin" />
                        {{ loading ? 'Loading...' : (userAddress ? 'Edit' : 'Add') }} Address
                      </button>
                    </div>
                    <div class="space-y-4">
                      <div v-if="userAddress" class="p-4 border rounded-lg dark:border-gray-600">
                        <div class="space-y-2">
                          <div>
                            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ $t('profile.houseNumber') }}</label>
                            <p class="text-gray-900 dark:text-white">{{ userAddress.houseNumber || 'Not provided' }}</p>
                          </div>
                          <div>
                            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ $t('profile.street') }}</label>
                            <p class="text-gray-900 dark:text-white">{{ userAddress.street || 'Not provided' }}</p>
                          </div>
                          <div>
                            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ $t('profile.ward') }}</label>
                            <p class="text-gray-900 dark:text-white">{{ userAddress.ward || 'Not provided' }}</p>
                          </div>
                          <div>
                            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ $t('profile.district') }}</label>
                            <p class="text-gray-900 dark:text-white">{{ userAddress.district || 'Not provided' }}</p>
                          </div>
                          <div>
                            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ $t('profile.province') }}</label>
                            <p class="text-gray-900 dark:text-white">{{ userAddress.province || 'Not provided' }}</p>
                          </div>
                          <div>
                            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ $t('profile.country') }}</label>
                            <p class="text-gray-900 dark:text-white">{{ userAddress.country || 'Not provided' }}</p>
                          </div>
                          <div>
                            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ $t('profile.fullAddress') }}</label>
                            <p class="text-gray-900 dark:text-white">{{ userAddress.fullAddress || 'Not provided' }}</p>
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
    
    <!-- Hidden file input for avatar upload -->
    <input
      ref="avatarInput"
      type="file"
      accept="image/*"
      @change="handleAvatarUpload"
      style="display: none"
    />

    <!-- Modals -->
    <UserBasicInfoModal
      :visible="showBasicModal"
      :user-data="user"
      @close="showBasicModal = false"
      @submit="handleBasicSubmit"
    />

    <UserProfessionalModal
      :visible="showProfessionalModal"
      :user-data="user"
      @close="showProfessionalModal = false"
      @submit="handleProfessionalSubmit"
    />

    <UserAddressModal
      :visible="showAddressModal"
      :address-data="currentAddressData"
      @close="showAddressModal = false"
      @submit="handleAddressSubmit"
    />
  </div>
</div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { Icon } from '@iconify/vue'
import { useI18n } from 'vue-i18n'
import { useAuthStore } from '@/stores/authStore'
import { secureImageUrl } from '@/utils/imageUtils'
import { usersApi } from '@/api/usersApi'
import { addressApi } from '@/api/addressApi'
import { getCurrentInstance } from 'vue'
import UserBasicInfoModal from './components/UserBasicInfoModalSimple.vue'
import UserProfessionalModal from './components/UserProfessionalModalSimple.vue'
import UserAddressModal from './components/UserAddressModalSimple.vue'
import defaultAvatar from '@/assets/img/user.jpg'

export default {
  name: 'Profile',
  components: {
    Icon,
    UserBasicInfoModal,
    UserProfessionalModal,
    UserAddressModal
  },
  emits: ['profile-updated'],
  setup(props, { emit }) {
    const { t } = useI18n()
    const authStore = useAuthStore()
    const instance = getCurrentInstance()
    const toast = instance?.appContext.config.globalProperties.$toast
    const avatarInput = ref(null)
    const activeTab = ref('basic')
    const selectedLocation = ref(localStorage.getItem('userLocation') || '')
    
    // Modal state
    const showBasicModal = ref(false)
    const showProfessionalModal = ref(false)
    const showAddressModal = ref(false)
    const currentAddressData = ref({})
    
    // Loading states
    const loading = ref(false)
    const avatarUploading = ref(false)
    
    // Tab configuration
    const tabs = [
      { id: 'basic', label: 'Basic Info', icon: 'mdi:account' },
      { id: 'professional', label: 'Professional Info', icon: 'mdi:briefcase' },
      { id: 'address', label: 'Address', icon: 'mdi:map-marker' }
    ]

    // Computed properties
    const user = computed(() => authStore.user)
    
    const userFullName = computed(() => {
      return authStore.currentUser?.profile?.fullName || 
             authStore.currentUser?.name || 
             authStore.currentUser?.email?.split('@')[0] || 
             'User'
    })

    const userEmail = computed(() => {
      return authStore.currentUser?.email || ''
    })

    const userProfile = computed(() => {
      return authStore.currentUser?.profile || {}
    })

    const userId = computed(() => {
      return authStore.currentUser?.id
    })

    const userAvatar = computed(() => {
      const user = authStore.currentUser
      const avatar = user?.profile?.avatar || user?.avatar
      
      if (avatar) {
        // If avatar is a full URL, secure it
        if (avatar.startsWith('http')) {
          return secureImageUrl(avatar)
        }
        // If avatar is a file ID, use public backend content endpoint
        const apiUrl = process.env.VITE_API_URL || 'http://localhost:8080/api'
        const baseUrl = apiUrl.endsWith('/') ? apiUrl.slice(0, -1) : apiUrl
        return `${baseUrl}/images/public/${avatar}/content`
      }
      // Fallback to default avatar
      return defaultAvatar
    })

    const hasSocialLinks = computed(() => {
      const profile = userProfile.value
      return profile.linkedinUrl || profile.website || profile.portfolioUrl
    })

    const userAddress = computed(() => {
      const addresses = user.value?.addresses || []
      return addresses.length > 0 ? addresses[0] : null
    })

    // Methods
    const getRoleBadgeClass = () => {
      const role = authStore.currentUser?.systemRole
      switch (role) {
        case 'ADMIN':
          return 'inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200'
        case 'USER':
          return 'inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200'
        default:
          return 'inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-gray-100 text-gray-800 dark:bg-gray-900 dark:text-gray-200'
      }
    }

    const getRoleLabel = () => {
      const role = authStore.currentUser?.systemRole
      switch (role) {
        case 'ADMIN': return 'Administrator'
        case 'USER': return 'User'
        default: return 'Unknown'
      }
    }

    const formatGender = (gender) => {
      switch (gender) {
        case 'MALE': return 'Male'
        case 'FEMALE': return 'Female'
        case 'OTHER': return 'Other'
        default: return gender || 'Not specified'
      }
    }

    const handleLocationChange = (event) => {
      const location = event.target.value
      selectedLocation.value = location
      
      // Save to localStorage
      if (location) {
        localStorage.setItem('userLocation', location)
      } else {
        localStorage.removeItem('userLocation')
      }
      
      console.log('Location changed to:', location)
      
      // Optionally emit event or update user profile
      // emit('location-changed', location)
    }

    const handleAvatarError = (event) => {
      const img = event.target
      const originalSrc = img.src
      
      console.error('Avatar failed to load:', originalSrc)
      
      // Try to handle Botpress SSL errors with proxy
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
          
          console.log('Trying proxy URL:', proxyUrl)
          img.src = proxyUrl
          
          img.onerror = () => {
            console.error('Proxy URL also failed:', proxyUrl)
            // Fallback to default avatar
            img.src = defaultAvatar
          }
        } catch (e) {
          console.error('Error creating proxy URL:', e)
          // Fallback to default avatar
          img.src = defaultAvatar
        }
      } else {
        // Fallback to default avatar for other errors
        img.src = defaultAvatar
      }
    }

    const triggerAvatarUpload = () => {
      avatarInput.value?.click()
    }

    const handleAvatarUpload = async (event) => {
      const file = event.target.files[0]
      if (!file) return

      // Validate file type
      if (!file.type.startsWith('image/')) {
        toast?.error('Please select an image file')
        return
      }

      // Validate file size (max 5MB)
      if (file.size > 5 * 1024 * 1024) {
        toast?.error('Image size should be less than 5MB')
        return
      }

      avatarUploading.value = true
      try {
        const formData = new FormData()
        formData.append('avatar', file)

        console.log('Uploading avatar...')

        const response = await usersApi.updateAvatar(formData)
        
        // Update user data in store
        if (response.data) {
          console.log('Avatar upload response:', response.data)
          console.log('Current user in store:', authStore.currentUser)
          
          // Wrap response data in profile object for proper store update
          const profileUpdate = {
            profile: {
              avatar: response.data.avatar,
              // Add other profile fields if needed
              fullName: response.data.fullName,
              phoneNumber: response.data.phoneNumber,
              gender: response.data.gender,
              bio: response.data.bio,
              jobTitle: response.data.jobTitle,
              department: response.data.department,
              company: response.data.company,
              linkedinUrl: response.data.linkedinUrl,
              website: response.data.website,
              location: response.data.location,
              skills: response.data.skills,
              experience: response.data.experience,
              education: response.data.education,
              certifications: response.data.certifications,
              languages: response.data.languages,
              availability: response.data.availability,
              hourlyRate: response.data.hourlyRate,
              portfolioUrl: response.data.portfolioUrl
            }
          }
          
          authStore.updateAuthUser(profileUpdate)
          console.log('User after update:', authStore.currentUser)
          
          toast?.success('Avatar updated successfully!')
          
          // Vue reactivity will automatically update all places using userAvatar computed property
          // No need for manual force refresh
          
          // Emit event to refresh profile data
          emit('profile-updated')
        }

      } catch (error) {
        console.error('Avatar upload error:', error)
        toast?.error('Failed to upload avatar. Please try again.')
      } finally {
        // Clear the file input and reset loading state
        event.target.value = ''
        avatarUploading.value = false
      }
    }

    const openLink = (url) => {
      window.open(url, '_blank')
    }

    const formatDate = (date) => {
      if (!date) return 'N/A'
      return new Date(date).toLocaleDateString()
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
      if (userAddress.value) {
        currentAddressData.value = { ...userAddress.value }
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
        
        // Get current user ID
        const currentUserId = userId.value
        if (!currentUserId) {
          throw new Error('User ID not found')
        }
        
        // Call API to update user basic info
        await usersApi.updateBasicInfo(formData)
        
        // Close modal and refresh data
        showBasicModal.value = false
        await loadUserData()
        
        // Show success message
        toast?.success('Basic information updated successfully!')
        
      } catch (error) {
        console.error('Error updating basic info:', error)
        toast?.error('Failed to update basic information. Please try again.')
      }
    }

    const handleProfessionalSubmit = async (formData) => {
      try {
        console.log('Submitting professional info:', formData)
        
        // Get current user ID
        const currentUserId = userId.value
        if (!currentUserId) {
          throw new Error('User ID not found')
        }
        
        // Call API to update professional info
        await usersApi.updateProfessionalInfo(formData)
        
        // Close modal and refresh data
        showProfessionalModal.value = false
        await loadUserData()
        
        // Show success message
        toast?.success('Professional information updated successfully!')
        
      } catch (error) {
        console.error('Error updating professional info:', error)
        toast?.error('Failed to update professional information. Please try again.')
      }
    }

    const handleAddressSubmit = async (formData) => {
      try {
        console.log('Submitting address info:', formData)
        
        // Get current user ID
        const currentUserId = userId.value
        if (!currentUserId) {
          throw new Error('User ID not found')
        }
        
        // Call API to update/create address
        if (userAddress.value) {
          // Update existing address
          console.log('Updating existing address:', userAddress.value.id)
          await addressApi.updateAddress(userAddress.value.id, formData)
        } else {
          // Create new address
          console.log('Creating new address')
          await addressApi.createAddress({
            ...formData,
            ownerType: 'USER',
            ownerId: currentUserId
          })
        }
        
        // Close modal and refresh data
        showAddressModal.value = false
        currentAddressData.value = {}
        await loadUserData()
        
        // Show success message
        toast?.success('Address information updated successfully!')
        
      } catch (error) {
        console.error('Error updating address:', error)
        toast?.error('Failed to update address. Please try again.')
      }
    }

    const loadUserData = async () => {
      loading.value = true
      try {
        const response = await usersApi.getProfile()
        const userData = response.data
        
        if (!userData) {
          throw new Error('No user data received')
        }
        
        console.log('User data loaded:', userData)
        
        // Update auth store with full user data
        authStore.updateAuthUser({
          user: userData,
          // Update profile data specifically
          profile: userData.profile || {}
        })
        
        // Also update localStorage for persistence
        localStorage.setItem('user', JSON.stringify(userData))
        
      } catch (error) {
        console.error('Error loading user data:', error)
        toast?.error('Failed to load user data. Please try refreshing the page.')
      } finally {
        loading.value = false
      }
    }

    // Load user data on mount
    onMounted(() => {
      loadUserData()
    })

    return {
      authStore,
      user,
      avatarInput,
      activeTab,
      showBasicModal,
      showProfessionalModal,
      showAddressModal,
      currentAddressData,
      userAddress,
      userFullName,
      userEmail,
      userProfile,
      hasSocialLinks,
      userAvatar,
      loading,
      avatarUploading,
      getRoleBadgeClass,
      getRoleLabel,
      formatGender,
      formatDate,
      triggerAvatarUpload,
      handleAvatarUpload,
      openLink,
      handleTabChange,
      handleEditBasic,
      handleEditProfessional,
      handleEditAddress,
      handleBasicSubmit,
      handleProfessionalSubmit,
      handleAddressSubmit,
      loadUserData
    }
  }
}
</script>
