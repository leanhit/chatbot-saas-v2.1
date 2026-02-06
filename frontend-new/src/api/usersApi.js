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

    checkUsername(username) {
        return axios.get(`/auth/check-username`, {
            params: { username },
        });
    },

    checkEmail(email) {
        return axios.get(`/auth/check-email`, {
            params: { email },
        });
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
        
        console.log('getProfile: using userId =', userId);
        
        return axios.get(`/v1/user-info/me/${userId}`);
    },

    updateProfile(params) {
        // Get current user ID from auth store with fallback
        const authStore = useAuthStore();
        let userId = authStore.userId;
        
        if (!userId) {
            const token = localStorage.getItem('accessToken') || '';
            if (token) {
                try {
                    const payload = JSON.parse(atob(token.split('.')[1]));
                    userId = parseInt(payload.sub) || parseInt(payload.userId) || parseInt(payload.id) || 1;
                } catch (e) {
                    userId = 1;
                }
            } else {
                userId = 1;
            }
        }
        
        return axios.put(`/v1/user-info/me/${userId}`, params);
    },

    // 1. Update Basic Info Only - Tách biệt không gộp chung
    updateBasicInfo(params) {
        // Get current user ID from auth store with fallback
        const authStore = useAuthStore();
        let userId = authStore.userId;
        
        if (!userId) {
            const token = localStorage.getItem('accessToken') || '';
            if (token) {
                try {
                    const payload = JSON.parse(atob(token.split('.')[1]));
                    userId = parseInt(payload.sub) || parseInt(payload.userId) || parseInt(payload.id) || 1;
                } catch (e) {
                    userId = 1;
                }
            } else {
                userId = 1;
            }
        }
        
        return axios.put(`/v1/user-info/me/${userId}/basic-info`, params);
    },

    // 2. Update Professional Info Only - Tách biệt không gộp chung
    updateProfessionalInfo(params) {
        // Get current user ID from auth store with fallback
        const authStore = useAuthStore();
        let userId = authStore.userId;
        
        if (!userId) {
            const token = localStorage.getItem('accessToken') || '';
            if (token) {
                try {
                    const payload = JSON.parse(atob(token.split('.')[1]));
                    userId = parseInt(payload.sub) || parseInt(payload.userId) || parseInt(payload.id) || 1;
                } catch (e) {
                    userId = 1;
                }
            } else {
                userId = 1;
            }
        }
        
        return axios.put(`/v1/user-info/me/${userId}/professional-info`, params);
    },

    updateAvatar(formData) {
        // Get current user ID from auth store with fallback
        const authStore = useAuthStore();
        let userId = authStore.userId;
        
        if (!userId) {
            const token = localStorage.getItem('accessToken') || '';
            if (token) {
                try {
                    const payload = JSON.parse(atob(token.split('.')[1]));
                    userId = parseInt(payload.sub) || parseInt(payload.userId) || parseInt(payload.id) || 1;
                } catch (e) {
                    userId = 1;
                }
            } else {
                userId = 1;
            }
        }
        
        return axios.put(`/v1/user-info/me/${userId}/avatar`, formData, {
            headers: {
                'Content-Type': 'multipart/form-data',
            },
        });
    },

    changePassword(params) {
        return axios.post('/auth/change-password', params);
    },

    updateTenantLogo(formData) {
        return axios.put('/v1/tenant/logo', formData, {
            headers: {
                'Content-Type': 'multipart/form-data',
            },
        });
    },
};
