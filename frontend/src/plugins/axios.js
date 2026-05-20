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
    '/api/customers/statuses' // Only statuses endpoint doesn't need tenant context
];

let isRefreshing = false;
let failedQueue = [];

const processQueue = (error, token = null) => {
    failedQueue.forEach(prom => {
        if (error) {
            prom.reject(error);
        } else {
            prom.resolve(token);
        }
    });
    
    failedQueue = [];
};

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
        
        // 3. XỬ LÝ TENANT KEY KHÔNG HARDCODE
        const activeTenantKey = localStorage.getItem(ACTIVE_TENANT_ID);
        const isExcluded = EXCLUDED_PATHS.some(path => config.url?.includes(path));
        
        if (activeTenantKey && !isExcluded) {
            config.headers['X-Tenant-Key'] = activeTenantKey;
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
        const originalRequest = error.config;
        
        // Log chi tiết lỗi CORS
        if (error.message.includes('CORS') || error.message.includes('Network Error')) {
            // CORS error detected
        }
        
        // Handle 401 - Unauthorized
        if (error.response?.status === 401 && originalRequest) {
            const authStore = useAuthStore();
            
            // Don't retry if it's a refresh token request or auth endpoints
            const isAuthRequest = originalRequest.url?.includes('/auth/') || 
                                  originalRequest.url?.includes('/refresh-token') ||
                                  originalRequest.url?.includes('/logout');
            
            if (!isAuthRequest) {
                if (isRefreshing) {
                    return new Promise(function(resolve, reject) {
                        failedQueue.push({ resolve, reject });
                    }).then(token => {
                        originalRequest.headers.Authorization = 'Bearer ' + token;
                        return instance.request(originalRequest);
                    }).catch(err => {
                        return Promise.reject(err);
                    });
                }

                originalRequest._retry = true;
                isRefreshing = true;

                try {
                    const newToken = await authStore.refreshAccessToken();
                    if (newToken) {
                        processQueue(null, newToken);
                        originalRequest.headers.Authorization = 'Bearer ' + newToken;
                        return instance.request(originalRequest);
                    } else {
                        throw new Error('Refresh failed');
                    }
                } catch (refreshError) {
                    processQueue(refreshError, null);
                    authStore.logout();
                    router.push({ name: 'login' });
                    return Promise.reject(refreshError);
                } finally {
                    isRefreshing = false;
                }
            } else {
                // If it's an auth request, logout
                authStore.logout();
                router.push({ name: 'login' });
            }
        }
        
        return Promise.reject(error);
    }
);

export default instance;
