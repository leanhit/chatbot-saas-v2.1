<template>
  <div class="bg-white dark:bg-gray-800 shadow rounded-lg p-6">
    <div class="flex justify-between items-center mb-6">
      <h3 class="text-lg font-medium text-gray-900 dark:text-white">{{ $t('profile.address') }}</h3>
      <button
        @click="handleEdit"
        class="inline-flex items-center px-3 py-2 border border-transparent text-sm leading-4 font-medium rounded-md text-white bg-primary hover:bg-primary/80 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary"
      >
        <Icon icon="mdi:pencil" class="h-4 w-4 mr-1" />
        {{ $t('profile.editAddress') }}
      </button>
    </div>

    <div v-if="loading" class="text-center py-8">
      <div class="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-primary"></div>
      <p class="mt-2 text-sm text-gray-600 dark:text-gray-400">{{ $t('profile.loading') }}</p>
    </div>

    <div v-else-if="!address" class="text-center py-8">
      <Icon icon="mdi:map-marker" class="mx-auto h-12 w-12 text-gray-400" />
      <p class="mt-2 text-sm text-gray-600 dark:text-gray-400">{{ $t('profile.noData') }}</p>
      <button
        @click="handleCreate"
        class="mt-4 inline-flex items-center px-3 py-2 border border-transparent text-sm leading-4 font-medium rounded-md text-white bg-primary hover:bg-primary/80 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary"
      >
        <Icon icon="mdi:plus" class="h-4 w-4 mr-1" />
        {{ $t('profile.addAddress') }}
      </button>
    </div>

    <div v-else class="space-y-4">
      <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div v-if="address.houseNumber">
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ $t('profile.houseNumber') }}</label>
          <p class="text-sm text-gray-900 dark:text-white">{{ address.houseNumber }}</p>
        </div>
        
        <div v-if="address.street">
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ $t('profile.street') }}</label>
          <p class="text-sm text-gray-900 dark:text-white">{{ address.street }}</p>
        </div>
        
        <div v-if="address.ward">
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ $t('profile.ward') }}</label>
          <p class="text-sm text-gray-900 dark:text-white">{{ address.ward }}</p>
        </div>
        
        <div v-if="address.district">
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ $t('profile.district') }}</label>
          <p class="text-sm text-gray-900 dark:text-white">{{ address.district }}</p>
        </div>
        
        <div v-if="address.province">
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ $t('profile.province') }}</label>
          <p class="text-sm text-gray-900 dark:text-white">{{ address.province }}</p>
        </div>
        
        <div v-if="address.country">
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ $t('profile.country') }}</label>
          <p class="text-sm text-gray-900 dark:text-white">{{ address.country }}</p>
        </div>
      </div>

      <!-- Full Address -->
      <div v-if="fullAddress" class="mt-6 p-4 bg-gray-50 dark:bg-gray-700 rounded-md">
        <p class="text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ $t('profile.fullAddress') }}:</p>
        <p class="text-sm text-gray-600 dark:text-gray-400">{{ fullAddress }}</p>
      </div>
    </div>

    <!-- Address Form Modal/Overlay -->
    <div v-if="showForm" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div class="bg-white dark:bg-gray-800 rounded-lg p-6 w-full max-w-2xl max-h-screen overflow-y-auto">
        <div class="flex justify-between items-center mb-4">
          <h3 class="text-lg font-medium text-gray-900 dark:text-white">
            {{ isEdit ? $t('profile.editAddress') : $t('profile.addAddress') }}
          </h3>
          <button
            @click="handleCancel"
            class="text-gray-400 hover:text-gray-600 dark:hover:text-gray-300"
          >
            <Icon icon="mdi:close" class="h-6 w-6" />
          </button>
        </div>
        
        <AddressForm
          :address="editingAddress"
          :owner-type="ownerType"
          :owner-id="ownerId"
          @submit="handleSubmit"
          @cancel="handleCancel"
        />
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { Icon } from '@iconify/vue'
import { useI18n } from 'vue-i18n'
import { addressApi } from '@/api/addressApi'
import AddressForm from './AddressForm.vue'

export default {
  name: 'SingleAddress',
  components: {
    Icon,
    AddressForm
  },
  props: {
    ownerType: {
      type: String,
      required: true
    },
    ownerId: {
      type: Number,
      required: true
    }
  },
  emits: ['updated'],
  setup(props, { emit }) {
    const { t } = useI18n()
    const address = ref(null)
    const loading = ref(false)
    const showForm = ref(false)
    const editingAddress = ref(null)

    const isEdit = computed(() => !!editingAddress.value)
    
    const fullAddress = computed(() => {
      if (!address.value) return ''
      
      const parts = []
      if (address.value.houseNumber) parts.push(address.value.houseNumber)
      if (address.value.street) parts.push(address.value.street)
      if (address.value.ward) parts.push(address.value.ward)
      if (address.value.district) parts.push(address.value.district)
      if (address.value.province) parts.push(address.value.province)
      if (address.value.country) parts.push(address.value.country)
      
      return parts.join(', ')
    })

    const loadAddress = async () => {
      loading.value = true
      try {
        const response = await addressApi.getAddressByOwner(props.ownerType, props.ownerId)
        address.value = response.data
      } catch (error) {
        console.error('Error loading address:', error)
        // If 404, address doesn't exist - that's ok for single address model
        if (error.response?.status !== 404) {
          // Show error for other types of errors
        }
        address.value = null
      } finally {
        loading.value = false
      }
    }

    const handleEdit = () => {
      editingAddress.value = address.value
      showForm.value = true
    }

    const handleCreate = () => {
      editingAddress.value = null
      showForm.value = true
    }

    const handleCancel = () => {
      showForm.value = false
      editingAddress.value = null
    }

    const handleSubmit = async (formData) => {
      try {
        if (isEdit.value) {
          // Update existing address
          await addressApi.updateAddress(address.value.id, formData)
        } else {
          // Create new address
          await addressApi.createAddress(formData)
        }
        
        // Reload address data
        await loadAddress()
        
        // Close form
        handleCancel()
        
        // Emit update event
        emit('updated')
      } catch (error) {
        console.error('Error saving address:', error)
        // Handle error (show toast, etc.)
      }
    }

    onMounted(() => {
      loadAddress()
    })

    return {
      address,
      loading,
      showForm,
      editingAddress,
      isEdit,
      fullAddress,
      handleEdit,
      handleCreate,
      handleCancel,
      handleSubmit,
      t
    }
  }
}
</script>
