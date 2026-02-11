<template>
  <div v-if="visible" class="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full z-50">
    <div class="relative top-20 mx-auto p-5 border w-96 shadow-lg rounded-md bg-white dark:bg-gray-800">
      <!-- Header -->
      <div class="mb-6">
        <h3 class="text-lg font-medium text-gray-900 dark:text-white">
          {{ $t('profile.editAddress') }}
        </h3>
        <p class="mt-1 text-sm text-gray-600 dark:text-gray-400">
          {{ $t('profile.updateExistingAddress') }}
        </p>
      </div>
      <!-- Form -->
      <form @submit.prevent="handleSubmit" class="space-y-4">
        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
            {{ $t('profile.houseNumber') }}
          </label>
          <input
            v-model="form.houseNumber"
            type="text"
            required
            class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:ring-primary focus:border-primary dark:bg-gray-700 dark:text-white"
            placeholder="Nhập số nhà"
          />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
            {{ $t('profile.street') }}
          </label>
          <input
            v-model="form.street"
            type="text"
            required
            class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:ring-primary focus:border-primary dark:bg-gray-700 dark:text-white"
            placeholder="Nhập tên đường"
          />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
            {{ $t('profile.province') }}
          </label>
          <select
            v-model="form.province"
            required
            @focus="loadProvinces"
            @change="onProvinceChange"
            class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:ring-primary focus:border-primary dark:bg-gray-700 dark:text-white"
          >
            <option value="">Chọn Tỉnh/Thành phố</option>
            <option v-for="province in provinces" :key="province.code" :value="province.name">
              {{ province.name }}
            </option>
          </select>
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
            {{ $t('profile.district') }}
          </label>
          <select
            v-model="form.district"
            required
            @focus="loadDistrictsForProvince"
            @change="onDistrictChange"
            :disabled="!form.province"
            class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:ring-primary focus:border-primary dark:bg-gray-700 dark:text-white disabled:opacity-50"
          >
            <option value="">Chọn Quận/Huyện</option>
            <option v-for="district in districtsList" :key="district.code" :value="district.name">
              {{ district.name }}
            </option>
          </select>
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
            {{ $t('profile.ward') }}
          </label>
          <select
            v-model="form.ward"
            required
            @focus="loadWardsForDistrict"
            :disabled="!form.district"
            class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:ring-primary focus:border-primary dark:bg-gray-700 dark:text-white disabled:opacity-50"
          >
            <option value="">Chọn Phường/Xã</option>
            <option v-for="ward in wardsList" :key="ward.code" :value="ward.name">
              {{ ward.name }}
            </option>
          </select>
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
            {{ $t('profile.country') }}
          </label>
          <input
            v-model="form.country"
            type="text"
            required
            class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:ring-primary focus:border-primary dark:bg-gray-700 dark:text-white"
            placeholder="Nhập quốc gia"
          />
        </div>
        <!-- Actions -->
        <div class="flex justify-end space-x-3 pt-4 border-t dark:border-gray-700">
          <button
            type="button"
            @click="handleClose"
            class="px-4 py-2 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary dark:bg-gray-700 dark:text-gray-300 dark:border-gray-600 dark:hover:bg-gray-600"
          >
            {{ $t('common.cancel') }}
          </button>
          <button
            type="submit"
            :disabled="loading"
            class="px-4 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-primary hover:bg-primary/80 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary disabled:opacity-50"
          >
            <span v-if="loading">{{ $t('common.loading') }}...</span>
            <span v-else>{{ $t('common.save') }}</span>
          </button>
        </div>
      </form>
    </div>
  </div>
</template>
<script>
import { ref, watch, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useLocationStore } from '@/stores/locationStore'
export default {
  name: 'UserAddressModalSimple',
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    addressData: {
      type: Object,
      default: () => ({})
    }
  },
  emits: ['close', 'submit'],
  setup(props, { emit }) {
    const { t } = useI18n()
    const locationStore = useLocationStore()
    const loading = ref(false)
    const form = ref({
      houseNumber: '',
      street: '',
      ward: '',
      district: '',
      province: '',
      country: 'Vietnam'
    })
    // Computed properties for location data
    const provinces = computed(() => locationStore.provinces)
    const districtsList = computed(() => {
      if (!form.value.province) return []
      const province = provinces.value.find(p => p.name === form.value.province)
      return province ? locationStore.districts[province.code] || [] : []
    })
    const wardsList = computed(() => {
      if (!form.value.district) return []
      const district = districtsList.value.find(d => d.name === form.value.district)
      return district ? locationStore.wards[district.code] || [] : []
    })
    // Location loading methods
    const loadProvinces = async () => {
      await locationStore.loadProvinces()
    }
    const loadDistrictsForProvince = async () => {
      if (!form.value.province) return
      const province = provinces.value.find(p => p.name === form.value.province)
      if (province) {
        await locationStore.loadDistricts(province.code)
      }
    }
    const loadWardsForDistrict = async () => {
      if (!form.value.district) return
      const district = districtsList.value.find(d => d.name === form.value.district)
      if (district) {
        await locationStore.loadWards(district.code)
      }
    }
    // Handle changes
    const onProvinceChange = () => {
      form.value.district = ''
      form.value.ward = ''
      loadDistrictsForProvince()
    }
    const onDistrictChange = () => {
      form.value.ward = ''
      loadWardsForDistrict()
    }
    // Generate full address
    const generateFullAddress = () => {
      const parts = [
        form.value.houseNumber,
        form.value.street,
        form.value.ward,
        form.value.district,
        form.value.province
      ].filter(Boolean)
      return parts.join(', ')
    }
    // Initialize form with address data
    watch(() => props.addressData, (newData) => {
      if (newData) {
        form.value = {
          houseNumber: newData.houseNumber || '',
          street: newData.street || '',
          ward: newData.ward || '',
          district: newData.district || '',
          province: newData.province || '',
          country: newData.country || 'Vietnam'
        }
        // Load location data when initializing
        if (form.value.province) {
          loadProvinces().then(() => {
            loadDistrictsForProvince().then(() => {
              loadWardsForDistrict()
            })
          })
        }
      }
    }, { immediate: true })
    const handleSubmit = async () => {
      loading.value = true
      try {
        const submitData = {
          ...form.value,
          fullAddress: generateFullAddress()
        }
        await emit('submit', submitData)
        emit('close')
      } catch (error) {
      } finally {
        loading.value = false
      }
    }
    const handleClose = () => {
      emit('close')
    }
    return {
      form,
      loading,
      provinces,
      districtsList,
      wardsList,
      loadProvinces,
      loadDistrictsForProvince,
      loadWardsForDistrict,
      onProvinceChange,
      onDistrictChange,
      handleSubmit,
      handleClose
    }
  }
}
</script>
