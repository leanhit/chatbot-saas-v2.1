// ✅ Đúng — dùng instance đã setup baseURL và interceptor
import axios from '@/plugins/axios';
import { useAuthStore } from '@/stores/authStore';
import { getUserIdFromJWT, debugJWT } from '@/utils/jwtHelper';
export const usersApi = {
    login(params) {
        return axios.post('/auth/login', params);
    },
    register(params) {
        return axios.post('/auth/register', params);
    },
    getProfile() {
        // Get current user ID from auth store
        const authStore = useAuthStore();
        let userId = authStore.userId;
        // Fallback: get from JWT token
        if (!userId) {
            const token = localStorage.getItem('accessToken') || '';
            debugJWT(token); // Debug JWT content
            userId = getUserIdFromJWT(token);
        }
        return axios.get(`/v1/user-info/me`);
    },
    updateProfile(params) {
        return axios.put(`/v1/user-info/me`, params);
    },
    // 1. Update Basic Info Only - Tách biệt không gộp chung
    updateBasicInfo(params) {
        return axios.put(`/v1/user-info/me/basic-info`, params);
    },
    // 2. Update Professional Info Only - Tách biệt không gộp chung
    updateProfessionalInfo(params) {
        return axios.put(`/v1/user-info/me/professional-info`, params);
    },
    updateAvatar(formData) {
        return axios.put(`/v1/user-info/me/avatar`, formData, {
            headers: {
                'Content-Type': 'multipart/form-data',
            },
        });
    },
    changePassword(params) {
        return axios.post('/auth/change-password', params);
    },
    updateTenantLogo(formData) {
        // DEPRECATED: Use tenantApi.updateTenant() instead
        // NEW: await tenantApi.updateTenant(tenantKey, null, file)
        return axios.put('/v1/tenant/logo', formData, {
            headers: {
                'Content-Type': 'multipart/form-data',
            },
        });
    },
};
