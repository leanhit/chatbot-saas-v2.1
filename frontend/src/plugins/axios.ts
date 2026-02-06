// /src/plugins/axios.ts (hoặc .js)
import axios from 'axios';
import { useAuthStore } from '@/stores/auth.js';
import router from '@/router';
import { ACTIVE_TENANT_ID } from '@/utils/constant'

const instance = axios.create({
    baseURL: '/api',
    headers: {
        Accept: 'application/json',
        'Content-Type': 'application/json',
    },
    withCredentials: true,
});

// Danh sách các API KHÔNG cần đính kèm Authorization header (Unauthenticated APIs)
const AUTH_EXCLUDED_PATHS = [
    '/identity/login',
    '/identity/register',
    '/identity/refresh',
    '/identity/health'
];

// Danh sách các API KHÔNG cần đính kèm Tenant ID (Global APIs)
const TENANT_EXCLUDED_PATHS = [
    '/identity/login',
    '/identity/register',
    '/identity/refresh',
    '/identity/health',
    '/tenants/search',
    '/tenants/me' // ✅ IMPORTANT: bootstrap endpoint
];

instance.interceptors.request.use(
    (config) => {
        // 1. Xử lý JWT Authorization header
        const token = localStorage.getItem('access_token');
        
        // Kiểm tra xem URL hiện tại có nằm trong danh sách loại trừ Authorization không
        const isAuthExcluded = AUTH_EXCLUDED_PATHS.some(path => config.url?.includes(path));
        
        // Chỉ attach Authorization header nếu:
        // - Token tồn tại và không undefined/null
        // - URL không nằm trong danh sách loại trừ
        if (token && token !== 'undefined' && !isAuthExcluded) {
            config.headers.Authorization = `Bearer ${token}`;
            console.log('[AXIOS] Authorization header ATTACHED:', config.headers.Authorization);
            console.log('[AXIOS] Request URL:', config.url);
        } else {
            // Đảm bảo không có Authorization header cho các API không cần xác thực
            if (config.headers.Authorization) {
                delete config.headers.Authorization;
            }
            console.log('[AXIOS] Authorization header REMOVED for:', config.url);
            console.log('[AXIOS] Token exists:', !!token, 'Auth excluded:', isAuthExcluded);
        }

        // 2. XỬ LÝ TENANT ID VỚI BỘ LỌC
        const activeTenantId = localStorage.getItem('tenant_id');

        // Kiểm tra xem URL hiện tại có nằm trong danh sách loại trừ Tenant ID không
        const isTenantExcluded = TENANT_EXCLUDED_PATHS.some(path => config.url?.includes(path));

        if (activeTenantId && !isTenantExcluded) {
            config.headers['X-Tenant-ID'] = activeTenantId;
            console.log(`[AXIOS] X-Tenant-ID header attached: ${activeTenantId} for ${config.url}`);
        }

        return config;
    },
    (error) => Promise.reject(error)
);

instance.interceptors.response.use(
    (response) => {
        //console.log('Response:', response.config.url, response.status); // Log response thành công
        return response;
    },
    (error) => {
        console.error('Response error:', { // Log chi tiết lỗi
            url: error.config?.url,
            status: error.response?.status,
            data: error.response?.data
        });

        if (error.response?.status === 401) {
            // Don't auto-logout on login/register failure
            if (!error.config?.url?.includes('/auth/login') && 
                !error.config?.url?.includes('/identity/login') &&
                !error.config?.url?.includes('/auth/register') &&
                !error.config?.url?.includes('/identity/register')) {
                console.log('[AXIOS] 401 Unauthorized - clearing auth and redirecting to login')
                const authStore = useAuthStore();
                authStore.logout();
                router.push('/login');
            } else {
                console.log('[AXIOS] 401 on auth endpoint - not auto-logging out')
            }
        }
        return Promise.reject(error);
    }
);

export default instance;