// stores/tenant-admin/tenantAdminSettings.store.js
import { defineStore } from 'pinia'
import { ref } from 'vue'
import { tenantApi } from '@/api/tenantApi'

export const useTenantAdminSettingsStore = defineStore('tenantAdminSettings', () => {
  const loading = ref(false)

  const updateBasicInfo = async (tenantId, payload) => {
    loading.value = true
    try {
      await tenantApi.updateTenantBasicInfo(tenantId, payload)
      // ElMessage.success('Đã cập nhật thông tin cơ bản') // Comment out for Windzo
    } finally {
      loading.value = false
    }
  }

  const updateProfile = async (tenantId, payload) => {
    loading.value = true
    try {
      await tenantApi.updateTenantProfile(tenantId, payload)
      // ElMessage.success('Đã cập nhật profile') // Comment out for Windzo
    } finally {
      loading.value = false
    }
  }

  const updateAddress = async (tenantId, addressId, payload) => {
    loading.value = true
    try {
      await tenantApi.updateTenantAddress(tenantId, addressId, payload)
      // ElMessage.success('Đã cập nhật địa chỉ') // Comment out for Windzo
    } finally {
      loading.value = false
    }
  }

  const suspendTenant = async (tenantId) => {
    await tenantApi.suspendTenant(tenantId)
    // ElMessage.warning('Tenant đã bị tạm dừng') // Comment out for Windzo
  }

  const activateTenant = async (tenantId) => {
    await tenantApi.activateTenant(tenantId)
    // ElMessage.success('Tenant đã được kích hoạt') // Comment out for Windzo
  }

  return {
    loading,
    updateBasicInfo,
    updateProfile,
    updateAddress,
    suspendTenant,
    activateTenant
  }
})
