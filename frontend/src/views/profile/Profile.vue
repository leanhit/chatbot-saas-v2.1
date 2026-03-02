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
              <div v-if="userProfile?.company" class="flex items-center justify-center text-sm text-gray-600 dark:text-gray-400">
                <Icon icon="mdi:office-building" class="mr-2 h-4 w-4" />
                {{ userProfile.company }}
              </div>
              <!-- Location Display -->
              <div v-if="userLocation !== 'Not set'" class="flex items-center justify-center text-sm text-gray-600 dark:text-gray-400">
                <Icon icon="mdi:map-marker" class="mr-2 h-4 w-4" />
                <span>{{ userLocation }}</span>
              </div>
              <div v-if="userProfile?.availability" class="flex items-center justify-center text-sm text-gray-600 dark:text-gray-400">
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
                    <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                      <!-- Left Column -->
                      <div class="space-y-4">
                        <div>
                          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">{{ $t('profile.fullName') }}</label>
                          <p class="text-gray-900 dark:text-white">{{ userProfile.fullName || 'Not provided' }}</p>
                        </div>
                        <div>
                          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">{{ $t('profile.phoneNumber') }}</label>
                          <p class="text-gray-900 dark:text-white">{{ userProfile.phoneNumber || 'Not provided' }}</p>
                        </div>
                      </div>
                      <!-- Right Column -->
                      <div class="space-y-4">
                        <div>
                          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">{{ $t('profile.gender') }}</label>
                          <p class="text-gray-900 dark:text-white">{{ formatGender(userProfile.gender) || 'Not provided' }}</p>
                        </div>
                        <div>
                          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">{{ $t('profile.bio') }}</label>
                          <p class="text-gray-900 dark:text-white">{{ userProfile.bio || 'Not provided' }}</p>
                        </div>
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
                    <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                      <!-- Left Column -->
                      <div class="space-y-4">
                        <div>
                          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">{{ $t('profile.jobTitle') }}</label>
                          <p class="text-gray-900 dark:text-white">{{ userProfile.jobTitle || 'Not provided' }}</p>
                        </div>
                        <div>
                          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">{{ $t('profile.department') }}</label>
                          <p class="text-gray-900 dark:text-white">{{ userProfile.department || 'Not provided' }}</p>
                        </div>
                        <div>
                          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">{{ $t('profile.company') }}</label>
                          <p class="text-gray-900 dark:text-white">{{ userProfile.company || 'Not provided' }}</p>
                        </div>
                        <div>
                          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">{{ $t('profile.experience') }}</label>
                          <p class="text-gray-900 dark:text-white">{{ userProfile.experience || 'Not provided' }}</p>
                        </div>
                        <div>
                          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">{{ $t('profile.education') }}</label>
                          <p class="text-gray-900 dark:text-white">{{ userProfile.education || 'Not provided' }}</p>
                        </div>
                      </div>
                      <!-- Right Column -->
                      <div class="space-y-4">
                        <div>
                          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">{{ $t('profile.skills') }}</label>
                          <p class="text-gray-900 dark:text-white">{{ userProfile.skills || 'Not provided' }}</p>
                        </div>
                        <div>
                          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">{{ $t('profile.certifications') }}</label>
                          <p class="text-gray-900 dark:text-white">{{ userProfile.certifications || 'Not provided' }}</p>
                        </div>
                        <div>
                          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">{{ $t('profile.languages') }}</label>
                          <p class="text-gray-900 dark:text-white">{{ userProfile.languages || 'Not provided' }}</p>
                        </div>
                        <div>
                          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">{{ $t('profile.linkedinUrl') }}</label>
                          <p class="text-gray-900 dark:text-white break-all">
                            <a v-if="userProfile.linkedinUrl" :href="userProfile.linkedinUrl" target="_blank" class="text-primary hover:underline">
                              {{ userProfile.linkedinUrl }}
                            </a>
                            <span v-else>Not provided</span>
                          </p>
                        </div>
                        <div>
                          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">{{ $t('profile.website') }}</label>
                          <p class="text-gray-900 dark:text-white break-all">
                            <a v-if="userProfile.website" :href="userProfile.website" target="_blank" class="text-primary hover:underline">
                              {{ userProfile.website }}
                            </a>
                            <span v-else>Not provided</span>
                          </p>
                        </div>
                        <div>
                          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">{{ $t('profile.portfolioUrl') }}</label>
                          <p class="text-gray-900 dark:text-white break-all">
                            <a v-if="userProfile.portfolioUrl" :href="userProfile.portfolioUrl" target="_blank" class="text-primary hover:underline">
                              {{ userProfile.portfolioUrl }}
                            </a>
                            <span v-else>Not provided</span>
                          </p>
                        </div>
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
                        :disabled="loading || !userAddress.firstAddress"
                        class="inline-flex items-center px-3 py-2 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary dark:bg-gray-700 dark:text-gray-300 dark:border-gray-600 dark:hover:bg-gray-600 disabled:opacity-50 disabled:cursor-not-allowed"
                      >
                        <Icon v-if="!loading" icon="mdi:pencil" class="h-4 w-4 mr-2" />
                        <Icon v-else icon="mdi:loading" class="h-4 w-4 mr-2 animate-spin" />
                        {{ loading ? 'Loading...' : 'Edit Address' }}
                      </button>
                    </div>
                    <div v-if="userAddress.firstAddress" class="space-y-4">
                      <!-- First row: 2 columns -->
                      <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                        <div>
                          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ $t('profile.houseNumber') }}</label>
                          <p class="text-gray-900 dark:text-white">{{ userAddress.firstAddress.houseNumber || 'Not provided' }}</p>
                        </div>
                        <div>
                          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ $t('profile.street') }}</label>
                          <p class="text-gray-900 dark:text-white">{{ userAddress.firstAddress.street || 'Not provided' }}</p>
                        </div>
                      </div>
                      
                      <!-- Second row: 2 columns with 4 fields -->
                      <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                        <div class="space-y-4">
                          <div>
                            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ $t('profile.ward') }}</label>
                            <p class="text-gray-900 dark:text-white">{{ userAddress.firstAddress.ward || 'Not provided' }}</p>
                          </div>
                          <div>
                            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ $t('profile.district') }}</label>
                            <p class="text-gray-900 dark:text-white">{{ userAddress.firstAddress.district || 'Not provided' }}</p>
                          </div>
                        </div>
                        <div class="space-y-4">
                          <div>
                            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ $t('profile.province') }}</label>
                            <p class="text-gray-900 dark:text-white">{{ userAddress.firstAddress.province || 'Not provided' }}</p>
                          </div>
                          <div>
                            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ $t('profile.country') }}</label>
                            <p class="text-gray-900 dark:text-white">{{ userAddress.firstAddress.country || 'Not provided' }}</p>
                          </div>
                        </div>
                      </div>
                      
                      <!-- Full width field -->
                      <div>
                        <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ $t('profile.fullAddress') }}</label>
                        <p class="text-gray-900 dark:text-white">{{ userAddress.firstAddress.fullAddress || 'Not provided' }}</p>
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
      :user-data="userProfile"
      @close="showBasicModal = false"
      @submit="handleBasicSubmit"
    />
    <UserProfessionalModal
      :visible="showProfessionalModal"
      :user-data="userProfile"
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
import { formatDate } from '@/utils/dateUtils'
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
    // Modal state
    const showBasicModal = ref(false)
    const showProfessionalModal = ref(false)
    const showAddressModal = ref(false)
    const currentAddressData = ref({})
    // Loading states
    const loading = ref(false)
    const avatarUploading = ref(false)
    const avatarTimestamp = ref(Date.now())
    const dataRefreshTimestamp = ref(Date.now())
    // Tab configuration
    const tabs = [
      { id: 'basic', label: 'Basic Info', icon: 'mdi:account' },
      { id: 'professional', label: 'Professional Info', icon: 'mdi:briefcase' },
      { id: 'address', label: 'Address', icon: 'mdi:map-marker' }
    ]
    // Computed properties
    const user = computed(() => authStore.currentUser)
    const userFullName = computed(() => {
      // Force reactivity by including timestamp
      dataRefreshTimestamp.value
      return authStore.currentUser?.profile?.fullName || 
             authStore.currentUser?.name || 
             authStore.currentUser?.email?.split('@')[0] || 
             'User'
    })
    const userEmail = computed(() => {
      return authStore.currentUser?.email || ''
    })
    const userProfile = computed(() => {
      // Force reactivity by including timestamp
      dataRefreshTimestamp.value
      return authStore.currentUser?.profile || {}
    })
    const userId = computed(() => {
      return authStore.currentUser?.id
    })
    const userAvatar = computed(() => {
      const user = authStore.currentUser
      const avatar = user?.profile?.avatar || user?.avatar
      if (avatar) {
        // If avatar is a full URL, secure it and add cache busting
        if (avatar.startsWith('http')) {
          const securedUrl = secureImageUrl(avatar)
          // Use reactive timestamp to force refresh when avatar is updated
          return `${securedUrl}?t=${avatarTimestamp.value}`
        }
        // If avatar is a file ID, use public backend content endpoint with cache busting
        const apiUrl = process.env.VITE_API_URL || 'http://localhost:8080/api'
        const baseUrl = apiUrl.endsWith('/') ? apiUrl.slice(0, -1) : apiUrl
        return `${baseUrl}/images/public/${avatar}/content?t=${avatarTimestamp.value}`
      }
      // Fallback to default avatar
      return defaultAvatar
    })
    const hasSocialLinks = computed(() => {
      const profile = userProfile.value
      return profile.linkedinUrl || profile.website || profile.portfolioUrl
    })
    const userAddress = computed(() => {
      // Force reactivity by including timestamp
      dataRefreshTimestamp.value
      const addresses = user.value?.addresses || []
      return {
        userValue: user.value,
        addresses: addresses,
        firstAddress: addresses.length > 0 ? addresses[0] : null,
        timestamp: dataRefreshTimestamp.value
      }
    })
    const userLocation = computed(() => {
      // Get province from first address
      return userAddress.value?.firstAddress?.province || 'Not set'
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
    const handleAvatarError = (event) => {
      const img = event.target
      const originalSrc = img.src
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
          img.src = proxyUrl
          img.onerror = () => {
            // Fallback to default avatar
            img.src = defaultAvatar
          }
        } catch (e) {
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
        const response = await usersApi.updateAvatar(formData)
        // Update user data in store
        if (response.data) {
          // Update the avatar in the store directly
          const currentUser = authStore.currentUser
          if (currentUser) {
            // Update avatar in profile if profile exists, otherwise update directly
            if (currentUser.profile) {
              currentUser.profile.avatar = response.data.avatar
            } else {
              currentUser.avatar = response.data.avatar
            }
            // Update other fields from response if they exist
            Object.keys(response.data).forEach(key => {
              if (key !== 'avatar') {
                if (currentUser.profile) {
                  currentUser.profile[key] = response.data[key]
                } else {
                  currentUser[key] = response.data[key]
                }
              }
            })
            // Save to localStorage
            localStorage.setItem('user', JSON.stringify(currentUser))
          }
          toast?.success('Avatar updated successfully!')
          // Update timestamp to force avatar refresh
          avatarTimestamp.value = Date.now()
          // Emit event to notify other components (like Header) about avatar update
          window.dispatchEvent(new CustomEvent('avatar-updated', { 
            detail: { timestamp: avatarTimestamp.value } 
          }))
          // Force refresh by calling loadUserData to get fresh data
          await loadUserData()
          // Emit event to refresh profile data
          emit('profile-updated')
        }
      } catch (error) {
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
    const handleTabChange = (tabName) => {
      activeTab.value = tabName
    }
    const handleEditBasic = () => {
      showBasicModal.value = true
    }
    const handleEditProfessional = () => {
      showProfessionalModal.value = true
    }
    const handleEditAddress = () => {
      if (!userAddress.value.firstAddress) {
        // Don't allow adding new address since it's created during registration
        toast?.error('Address not found. Please contact support.')
        return
      }
      // Only allow editing existing address
      currentAddressData.value = { ...userAddress.value.firstAddress }
      showAddressModal.value = true
    }
    // Modal submit handlers
    const handleBasicSubmit = async (formData) => {
      try {
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
        toast?.error('Failed to update basic information. Please try again.')
      }
    }
    const handleProfessionalSubmit = async (formData) => {
      try {
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
        toast?.error('Failed to update professional information. Please try again.')
      }
    }
    const handleAddressSubmit = async (formData) => {
      try {
        // Get current user ID
        const currentUserId = userId.value
        if (!currentUserId) {
          throw new Error('User ID not found')
        }
        // Only update existing address - no creation allowed
        if (!userAddress.value.firstAddress) {
          toast?.error('Address not found. Please contact support.')
          return
        }
        // Update existing address
        // Add required owner information for backend validation
        const addressUpdateData = {
          ...formData,
          ownerId: currentUserId,
          ownerType: 'USER'
        }
        await addressApi.updateAddress(userAddress.value.firstAddress?.id, addressUpdateData)
        // Close modal and refresh data
        showAddressModal.value = false
        currentAddressData.value = {}
        await loadUserData()
        // Show success message
        toast?.success('Address information updated successfully!')
      } catch (error) {
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
        // Update auth store with full user data (directly, not wrapped)
        authStore.updateAuthUser(userData)
        // Force reactivity update for computed properties
        dataRefreshTimestamp.value = Date.now()
        // Also update localStorage for persistence
        localStorage.setItem('user', JSON.stringify(userData))
      } catch (error) {
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
      userLocation,
      userFullName,
      userEmail,
      userProfile,
      hasSocialLinks,
      userAvatar,
      loading,
      avatarUploading,
      avatarTimestamp,
      dataRefreshTimestamp,
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
