// stores/tenant-admin/tenantAdminSettings.store.ts
import { defineStore } from 'pinia'
import { ref } from 'vue'
import { tenantApi } from '@/api/tenantApi'
import type {
  TenantBasicInfoRequest,
  TenantProfileRequest
} from '@/types/tenant'
import type { AddressRequestDTO } from '@/types/address'
import { ElMessage } from 'element-plus'

export const useTenantAdminSettingsStore = defineStore('tenantAdminSettings', () => {
  const loading = ref(false)

  const updateBasicInfo = async (
    tenantId: string,
    payload: TenantBasicInfoRequest
  ) => {
    loading.value = true
    try {
      await tenantApi.updateTenantBasicInfo(tenantId, payload)
      ElMessage.success('Đã cập nhật thông tin cơ bản')
    } finally {
      loading.value = false
    }
  }

  const updateProfile = async (
    tenantId: string,
    payload: TenantProfileRequest
  ) => {
    loading.value = true
    try {
      await tenantApi.updateTenantProfile(tenantId, payload)
      ElMessage.success('Đã cập nhật profile')
    } finally {
      loading.value = false
    }
  }

  const updateAddress = async (
    tenantId: string,
    addressId: string,
    payload: AddressRequestDTO
  ) => {
    loading.value = true
    try {
      
      await tenantApi.updateTenantAddress(tenantId, addressId, payload)
      ElMessage.success('Đã cập nhật địa chỉ')
    } finally {
      loading.value = false
    }
  }

  const suspendTenant = async (tenantId: string) => {
    await tenantApi.suspendTenant(tenantId)
    ElMessage.warning('Tenant đã bị tạm dừng')
  }

  const activateTenant = async (tenantId: string) => {
    await tenantApi.activateTenant(tenantId)
    ElMessage.success('Tenant đã được kích hoạt')
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
