<template>
  <div class="bg-white dark:bg-gray-800 shadow rounded-lg p-6">
    <div class="mb-6">
      <h3 class="text-lg font-medium text-gray-900 dark:text-white">
        {{ isEdit ? 'Edit Address' : 'Add New Address' }}
      </h3>
      <p class="mt-1 text-sm text-gray-600 dark:text-gray-400">
        {{ isEdit ? 'Update your address information' : 'Enter your address details' }}
      </p>
    </div>
    <form @submit.prevent="handleSubmit" class="space-y-6">
      <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
            House Number
          </label>
          <input
            v-model="form.houseNumber"
            type="text"
            class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:ring-primary focus:border-primary dark:bg-gray-700 dark:text-white"
            placeholder="Enter house number"
          />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
            Street *
          </label>
          <input
            v-model="form.street"
            type="text"
            required
            class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:ring-primary focus:border-primary dark:bg-gray-700 dark:text-white"
            placeholder="Enter street name"
          />
        </div>
      </div>
      <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
            Province/City *
          </label>
          <select
            v-model="form.provinceCode"
            required
            @change="onProvinceChange"
            class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:ring-primary focus:border-primary dark:bg-gray-700 dark:text-white"
          >
            <option value="">Select province</option>
            <option
              v-for="province in provinces"
              :key="province.code"
              :value="province.code"
            >
              {{ province.name }}
            </option>
          </select>
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
            District *
          </label>
          <select
            v-model="form.districtCode"
            required
            :disabled="!form.provinceCode"
            @change="onDistrictChange"
            class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:ring-primary focus:border-primary dark:bg-gray-700 dark:text-white disabled:opacity-50"
          >
            <option value="">Select district</option>
            <option
              v-for="district in currentDistricts"
              :key="district.code"
              :value="district.code"
            >
              {{ district.name }}
            </option>
          </select>
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
            Ward *
          </label>
          <select
            v-model="form.wardCode"
            required
            :disabled="!form.districtCode"
            @change="onWardChange"
            class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:ring-primary focus:border-primary dark:bg-gray-700 dark:text-white disabled:opacity-50"
          >
            <option value="">Select ward</option>
            <option
              v-for="ward in currentWards"
              :key="ward.code"
              :value="ward.code"
            >
              {{ ward.name }}
            </option>
          </select>
        </div>
      </div>
      <div class="flex items-center">
        <input
          v-model="form.country"
          type="text"
          class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:ring-primary focus:border-primary dark:bg-gray-700 dark:text-white"
          placeholder="Enter country"
        />
      </div>
      <!-- Address Preview -->
      <div v-if="addressPreview" class="p-4 bg-gray-50 dark:bg-gray-700 rounded-md">
        <p class="text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Address Preview:</p>
        <p class="text-sm text-gray-600 dark:text-gray-400 address-preview">{{ addressPreview }}</p>
      </div>
      <div class="flex justify-end space-x-3">
        <button
          type="button"
          @click="$emit('cancel')"
          class="px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm text-sm font-medium text-gray-700 dark:text-gray-300 bg-white dark:bg-gray-700 hover:bg-gray-50 dark:hover:bg-gray-600 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary"
        >
          Cancel
        </button>
        <button
          type="submit"
          :disabled="loading"
          class="px-4 py-2 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-primary hover:bg-primary/80 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary disabled:opacity-50 disabled:cursor-not-allowed"
        >
          <span v-if="!loading">{{ isEdit ? 'Update Address' : 'Create Address' }}</span>
          <span v-else>{{ isEdit ? 'Updating...' : 'Creating...' }}</span>
        </button>
      </div>
    </form>
  </div>
</template>
<script>
import { ref, computed, onMounted, watch } from 'vue'
import { useLocationStore } from '@/stores/locationStore'
export default {
  name: 'AddressForm',
  props: {
    address: {
      type: Object,
      default: null
    },
    ownerType: {
      type: String,
      required: true
    },
    ownerId: {
      type: Number,
      required: true
    }
  },
  emits: ['submit', 'cancel'],
  setup(props, { emit }) {
    const locationStore = useLocationStore()
    const loading = ref(false)
    const form = ref({
      ownerType: props.ownerType,
      ownerId: props.ownerId,
      street: '',
      houseNumber: '',
      ward: '',
      district: '',
      province: '',
      country: 'Vietnam',
      provinceCode: '',
      districtCode: '',
      wardCode: ''
    })
    const isEdit = computed(() => !!props.address)
    const provinces = computed(() => locationStore.provinces)
    const currentDistricts = computed(() => {
      if (!form.value.provinceCode) return []
      return locationStore.districts[form.value.provinceCode] || []
    })
    const currentWards = computed(() => {
      if (!form.value.districtCode) return []
      return locationStore.wards[form.value.districtCode] || []
    })
    const addressPreview = computed(() => {
      const parts = []
      if (form.value.houseNumber) parts.push(form.value.houseNumber)
      if (form.value.street) parts.push(form.value.street)
      if (form.value.ward) parts.push(form.value.ward)
      if (form.value.district) parts.push(form.value.district)
      if (form.value.province) parts.push(form.value.province)
      if (form.value.country) parts.push(form.value.country)
      return parts.join(', ')
    })
    const onProvinceChange = async () => {
      const province = provinces.value.find(p => p.code === form.value.provinceCode)
      form.value.province = province?.name || ''
      form.value.district = ''
      form.value.ward = ''
      form.value.districtCode = ''
      form.value.wardCode = ''
      if (form.value.provinceCode) {
        await locationStore.loadDistricts(form.value.provinceCode)
      }
    }
    const onDistrictChange = async () => {
      const district = currentDistricts.value.find(d => d.code === form.value.districtCode)
      form.value.district = district?.name || ''
      form.value.ward = ''
      form.value.wardCode = ''
      if (form.value.districtCode) {
        await locationStore.loadWards(form.value.districtCode)
      }
    }
    const onWardChange = () => {
      const ward = currentWards.value.find(w => w.code === form.value.wardCode)
      form.value.ward = ward?.name || ''
    }
    const handleSubmit = () => {
      emit('submit', { ...form.value })
    }
    // Initialize form with address data if editing
    const initializeForm = () => {
      if (props.address) {
        // Editing existing address - populate with existing data
        form.value = {
          ownerType: props.ownerType,
          ownerId: props.ownerId,
          street: props.address.street || '',
          houseNumber: props.address.houseNumber || '',
          ward: props.address.ward || '',
          district: props.address.district || '',
          province: props.address.province || '',
          country: props.address.country || 'Vietnam',
          provinceCode: '',
          districtCode: '',
          wardCode: ''
        }
        // Try to find and set the codes based on names
        // This would need additional logic to match names to codes
        // For now, we'll keep them empty and let user reselect
      } else {
        // Creating new address - keep empty form
        form.value = {
          ownerType: props.ownerType,
          ownerId: props.ownerId,
          street: '',
          houseNumber: '',
          ward: '',
          district: '',
          province: '',
          country: 'Vietnam',
          provinceCode: '',
          districtCode: '',
          wardCode: ''
        }
      }
    }
    // Watch for address prop changes
    watch(() => props.address, initializeForm, { immediate: true })
    onMounted(async () => {
      await locationStore.loadProvinces()
      initializeForm()
    })
    return {
      form,
      loading,
      isEdit,
      provinces,
      currentDistricts,
      currentWards,
      addressPreview,
      onProvinceChange,
      onDistrictChange,
      onWardChange,
      handleSubmit
    }
  }
}
</script>
