import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios';
import { ElMessage } from 'element-plus';
import router from '@/router';

// Create axios instance
const api: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 30000, // 30 seconds
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: true,
});

// Request interceptor
api.interceptors.request.use(
  (config) => {
    // Get token from localStorage
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response interceptor
api.interceptors.response.use(
  (response: AxiosResponse) => {
    return response.data;
  },
  (error) => {
    const { response } = error;
    
    if (!response) {
      // Network error
      ElMessage.error('Không thể kết nối đến máy chủ');
      return Promise.reject(error);
    }

    const { status, data } = response;
    
    // Handle different status codes
    switch (status) {
      case 400:
        // Bad Request - validation error
        if (data.errors) {
          const firstError = Object.values(data.errors)[0];
          ElMessage.error(Array.isArray(firstError) ? firstError[0] : firstError);
        } else {
          ElMessage.error(data.message || 'Dữ liệu không hợp lệ');
        }
        break;
      case 401:
        // Unauthorized - wrong credentials
        if (data.message) {
          ElMessage.error(data.message);
        } else {
          ElMessage.error('Sai email hoặc mật khẩu');
        }
        break;
      case 403:
        // Forbidden
        ElMessage.error('Bạn không có quyền thực hiện hành động này');
        break;
      case 404:
        // Not found
        ElMessage.error('Không tìm thấy tài nguyên');
        break;
      case 422:
        // Validation error
        if (data.errors) {
          const firstError = Object.values(data.errors)[0];
          ElMessage.error(Array.isArray(firstError) ? firstError[0] : firstError);
        } else {
          ElMessage.error(data.message || 'Dữ liệu không hợp lệ');
        }
        break;
      case 500:
        // Server error
        ElMessage.error('Lỗi máy chủ nội bộ');
        break;
      default:
        // Other errors
        ElMessage.error(data?.message || 'Đã xảy ra lỗi');
    }

    return Promise.reject(error);
  }
);

export { api };
export default api;
