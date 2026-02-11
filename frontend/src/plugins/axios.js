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
    '/auth/forgot-password', // Loại trừ quên mật khẩu
    '/auth/reset-password',  // Loại trừ đặt lại mật khẩu
    '/users/change-password',// Loại trừ đổi mật khẩu khi đã login
    '/tenants',              // Create/list tenants - không cần tenant context
    '/tenants/me',           // Get user tenants - không cần tenant context
    '/tenants/search',
    '/tenants/my-list',
    '/images' // Image API không cần tenant ID
];
instance.interceptors.request.use(
    (config) => {
        // 1. Xử lý JWT
        const token = localStorage.getItem('accessToken');
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        // 2. XỬ LÝ TENANT KEY VỚI BỘ LỌC
        const activeTenantKey = localStorage.getItem(ACTIVE_TENANT_ID);
        // Kiểm tra xem URL hiện tại có nằm trong danh sách loại trừ không
        const isExcluded = EXCLUDED_PATHS.some(path => config.url?.includes(path));
        if (activeTenantKey && !isExcluded) {
            config.headers['X-Tenant-Key'] = activeTenantKey; // ✅ Send tenantKey
        }
        return config;
    },
    (error) => Promise.reject(error)
);
instance.interceptors.response.use(
    (response) => {
        return response;
    },
    (error) => {
        // Log chi tiết lỗi CORS
        if (error.message.includes('CORS') || error.message.includes('Network Error')) {
            // CORS error detected
        }
        if (error.response?.status === 401) {
            const authStore = useAuthStore();
            authStore.logout();
            router.push({ name: 'login' });
        }
        return Promise.reject(error);
    }
);
export default instance;
