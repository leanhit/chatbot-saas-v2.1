// stores/tenant-admin/tenantAdminSettings.store.js
import { defineStore } from 'pinia'
import { ref } from 'vue'
import { tenantApi } from '@/api/tenantApi'
import { TenantBasicInfoRequest, TenantProfileRequest, validateTenantBasicInfoRequest, validateTenantProfileRequest } from '@/types/tenant'
export const useTenantAdminSettingsStore = defineStore('tenantAdminSettings', () => {
  const loading = ref(false)
  const updateBasicInfo = async (tenantId, payload) => {
    // Validate payload using types
    const errors = validateTenantBasicInfoRequest(payload);
    if (errors.length > 0) {
      throw new Error(errors.join(', '));
    }
    
    loading.value = true
    try {
      await tenantApi.updateTenant(tenantId, payload)
    } finally {
      loading.value = false
    }
  }
  const updateProfile = async (tenantId, payload) => {
    // Validate payload using types
    const errors = validateTenantProfileRequest(payload);
    if (errors.length > 0) {
      throw new Error(errors.join(', '));
    }
    
    loading.value = true
    try {
      await tenantApi.updateTenantProfile(tenantId, payload)
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
