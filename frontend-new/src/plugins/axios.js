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
        //console.log('Request interceptor - Token:', token); // Thêm dòng này để debug
        //console.log('Request URL:', config.url); // Log URL được gọi

        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }

        // 2. XỬ LÝ TENANT ID VỚI BỘ LỌC
        const activeTenantId = localStorage.getItem(ACTIVE_TENANT_ID);

        // Kiểm tra xem URL hiện tại có nằm trong danh sách loại trừ không
        const isExcluded = EXCLUDED_PATHS.some(path => config.url?.includes(path));

        console.log(`[Axios Debug] URL: ${config.url}, ActiveTenantId: ${activeTenantId}, IsExcluded: ${isExcluded} - v2.1`);
        console.log(`[Axios Debug] EXCLUDED_PATHS:`, EXCLUDED_PATHS);
        console.log(`[Axios Debug] Current headers before:`, config.headers);

        if (activeTenantId && !isExcluded) {
            config.headers['X-Tenant-Key'] = activeTenantId; // ✅ Send tenantId as X-Tenant-Key header
            console.log(`[Tenant Context] Applied ID: ${activeTenantId} for ${config.url}`);
        } else {
            console.log(`[Tenant Context] Skipped X-Tenant-Key for ${config.url} (activeTenantId: ${activeTenantId}, isExcluded: ${isExcluded})`);
        }

        console.log(`[Axios Debug] Final headers:`, config.headers);

        return config;
    },
    (error) => Promise.reject(error)
);

instance.interceptors.response.use(
    (response) => {
        console.log(`[Axios Response] Success: ${response.config.method?.toUpperCase()} ${response.config.url} - Status: ${response.status}`);
        return response;
    },
    (error) => {
        console.error(`[Axios Response] Error: ${error.config?.method?.toUpperCase()} ${error.config?.url}`);
        console.error(`[Axios Response] Error details:`, {
            message: error.message,
            status: error.response?.status,
            statusText: error.response?.statusText,
            data: error.response?.data,
            headers: error.config?.headers
        });
        
        // Log chi tiết lỗi CORS
        if (error.message.includes('CORS') || error.message.includes('Network Error')) {
            console.error(`[CORS Error] Possible CORS issue detected`);
            console.error(`[CORS Error] Request URL: ${error.config?.url}`);
            console.error(`[CORS Error] Base URL: ${error.config?.baseURL}`);
            console.error(`[CORS Error] Full URL: ${error.config?.baseURL}${error.config?.url}`);
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
