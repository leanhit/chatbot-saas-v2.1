// Identity Hub API
import axios from '@/plugins/axios';

export interface LoginRequest {
    email: string;
    password: string;
}

export interface RefreshRequest {
    refreshToken: string;
}

export interface LoginResponse {
    accessToken: string;
    refreshToken: string;
    tokenType: string;
    expiresIn: number;
}

export interface RefreshResponse {
    accessToken: string;
    refreshToken: string;
    expiresIn: number;
}

export const identityApi = {
    login(params: LoginRequest) {
        return axios.post<LoginResponse>('/identity/login', params);
    },

    refresh(params: RefreshRequest) {
        return axios.post<RefreshResponse>('/identity/refresh', params);
    },

    validateToken() {
        return axios.get('/identity/validate');
    },

    health() {
        return axios.get('/identity/health');
    }
};
