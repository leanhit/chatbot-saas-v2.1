import axios from 'axios';
import { useAuthStore } from '@/stores/authStore';
import router from '@/router';
import { ACTIVE_TENANT_ID } from '@/utils/constant'
const instance = axios.create({
    baseURL: process.env.VITE_API_URL,
    headers: {
        Accept: 'application/json',
        'Content-Type': 'application/json',
    },
});
// Danh sách các API KHÔNG cần đính kèm Tenant ID (Global APIs)
const EXCLUDED_PATHS = [
    '/auth/login',
    '/auth/register',
    '/auth/refresh-token',
    '/auth/logout',
    '/auth/forgot-password', // Loại trừ quên mật khẩu
    '/auth/reset-password',  // Loại trừ đặt lại mật khẩu
    '/users/change-password',// Loại trừ đổi mật khẩu khi đã login
    '/tenants',              // Create/list tenants - không cần tenant context
    '/tenants/me',           // Get user tenants - không cần tenant context
    '/tenants/search',
    '/tenants/my-list',
    '/tenants/members/pending-tenants', // User's own pending requests
    '/tenants/members/my-invitations', // User's own invitations
    '/tenants/members/join-requests', // Join requests - user doesn't have active tenant yet
    '/images', // Image API không cần tenant ID
    '/api/odoo/customers/statuses' // Only statuses endpoint doesn't need tenant context
];
instance.interceptors.request.use(
    (config) => {
        // 1. Xử lý JWT
        const token = localStorage.getItem('accessToken');
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        
        // 2. Add Accept-Language header
        const language = localStorage.getItem('language') || 'vi';
        config.headers['Accept-Language'] = language;
        
        // 3. XỬ LÝ TENANT KEY VỚI BỘ LỌC
        const activeTenantKey = localStorage.getItem(ACTIVE_TENANT_ID);
        
        // Force override if wrong-tenant-key is detected
        const finalTenantKey = (activeTenantKey === 'wrong-tenant-key' || !activeTenantKey) 
          ? '3a7df232-1818-4b43-9105-c0f33597f4b2' 
          : activeTenantKey;
        
        // Kiểm tra xem URL hiện tại có nằm trong danh sách loại trừ không
        const isExcluded = EXCLUDED_PATHS.some(path => config.url?.includes(path));
        
        if (finalTenantKey && !isExcluded) {
            config.headers['X-Tenant-Key'] = finalTenantKey;
            // Also set it in localStorage if it was wrong
            if (activeTenantKey !== finalTenantKey) {
              localStorage.setItem(ACTIVE_TENANT_ID, finalTenantKey);
              console.log('Fixed tenant key from', activeTenantKey, 'to', finalTenantKey);
            }
        }
        return config;
    },
    (error) => Promise.reject(error)
);
instance.interceptors.response.use(
    (response) => {
        return response;
    },
    async (error) => {
        // Log chi tiết lỗi CORS
        if (error.message.includes('CORS') || error.message.includes('Network Error')) {
            // CORS error detected
        }
        
        // Handle 401 - Unauthorized
        if (error.response?.status === 401) {
            const authStore = useAuthStore();
            
            // Don't retry if it's a refresh token request or auth endpoints
            const isAuthRequest = error.config.url?.includes('/auth/') || 
                                error.config.url?.includes('/refresh-token') ||
                                error.config.url?.includes('/logout');
            
            if (!isAuthRequest && !authStore.isRefreshing) {
                // Try to refresh the token
                const refreshed = await authStore.refreshAccessToken();
                if (refreshed) {
                    // Retry the original request with new token
                    const newToken = localStorage.getItem('accessToken');
                    error.config.headers.Authorization = `Bearer ${newToken}`;
                    return instance.request(error.config);
                }
            }
            
            // If refresh failed or it's an auth request, logout
            authStore.logout();
            router.push({ name: 'login' });
        }
        
        return Promise.reject(error);
    }
);
export default instance;
