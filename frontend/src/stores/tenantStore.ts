import { defineStore } from 'pinia';
import { ref } from 'vue';
import { tenantApi } from '@/api/tenantApi';
import { ACTIVE_TENANT_ID, TENANT_DATA } from '@/utils/constant'
import type {
  TenantResponse,
  CreateTenantRequest,
  TenantProfileRequest,
  TenantBasicInfoRequest,
  TenantDetailResponse,
  TenantSearchResponse
} from '@/types/tenant';

import type { AddressRequestDTO } from '@/types/address';

export const useTenantStore = defineStore('tenant', () => {
  // --- State ---
  const savedTenant = localStorage.getItem(TENANT_DATA);
  const currentTenant = ref<TenantDetailResponse | null>(savedTenant ? JSON.parse(savedTenant) : null);
  const userTenants = ref<TenantDetailResponse[]>([]);
  const searchResults = ref<TenantSearchResponse[]>([]);
  const loading = ref(false);

  // --- Helpers ---
  const setCurrentTenant = (data: TenantDetailResponse) => {
    currentTenant.value = data;
    localStorage.setItem(TENANT_DATA, JSON.stringify(data));
    localStorage.setItem(ACTIVE_TENANT_ID, data.id.toString());
  };

  const setCurrentTenantById = async (tenantId: number) => {
    try {
      const { data } = await tenantApi.getTenantDetail(tenantId.toString());
      setCurrentTenant(data);
    } catch (error) {
      console.error('Failed to set tenant by ID:', tenantId, error);
      // Fallback: set minimal tenant info
      const minimalTenant = {
        id: tenantId,
        name: `Tenant ${tenantId}`,
        status: 'ACTIVE'
      } as TenantDetailResponse;
      setCurrentTenant(minimalTenant);
    }
  };

  const clearTenant = () => {
    currentTenant.value = null;
    localStorage.removeItem(ACTIVE_TENANT_ID);
    localStorage.removeItem(TENANT_DATA);
  };

  // --- Actions ---

  // 1. Tìm kiếm Tenant (Hàm bạn đang bị thiếu)
  const searchTenants = async (keyword: string) => {
    if (!keyword.trim()) return;
    loading.value = true;
    try {
      const { data } = await tenantApi.searchTenant(keyword);
      searchResults.value = data.content || [];
    } finally {
      loading.value = false;
    }
  };

  // 2. Chuyển đổi Tenant (Vào Workspace)
  const switchTenant = async (tenantId: string) => {
    const data = await getTenantDetail(tenantId);
    setCurrentTenant(data);
  };

  // 3. Tạo Tenant mới
  const createTenant = async (payload: CreateTenantRequest) => {
    loading.value = true;
    try {
      const { data } = await tenantApi.createTenant(payload);
      return data;
    } finally {
      loading.value = false;
    }
  };

  const getTenantDetail = async (tenantId: string) => {
    loading.value = true;
    try {
      const { data } = await tenantApi.getTenantDetail(tenantId);
      return data;
    } finally {
      loading.value = false;
    }
  };

  const fetchUserTenants = async () => {
    loading.value = true;
    try {
      const { data } = await tenantApi.getUserTenants();
      userTenants.value = data;
    } finally {
      loading.value = false;
    }
  };

  const suspendTenant = async (id: string) => {
    await tenantApi.suspendTenant(id);
    await fetchUserTenants(); // Cập nhật lại danh sách để thấy status thay đổi
  };

  const activateTenant = async (id: string) => {
    await tenantApi.activateTenant(id);
    await fetchUserTenants();
  };

  // Các hàm update Profile/Address giữ nguyên logic của bạn...
  const updateTenantBasicInfo = async (tenantId: string, payload: TenantBasicInfoRequest) => { /* logic */ };
  const updateTenantProfile = async (tenantId: string, payload: TenantProfileRequest) => { /* logic */ };
  const updateTenantAddress = async (tenantId: string, addressId: string, payload: AddressRequestDTO) => { /* logic */ };

  // --- Return ---
  return {
    currentTenant,
    userTenants,
    searchResults,
    loading,
    searchTenants,
    switchTenant,
    createTenant,
    getTenantDetail,
    fetchUserTenants,
    updateTenantBasicInfo,
    updateTenantProfile,
    updateTenantAddress,
    suspendTenant,
    activateTenant,
    setCurrentTenantById,
    clearTenant
  };
});