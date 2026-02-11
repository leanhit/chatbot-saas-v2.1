// src/api/imageApi.js
import axios from '@/plugins/axios';
export const imageApi = {
    getImageByID(imageId) {
        return axios.get(`/images/${imageId}`);
    },
    deleteImage(imageId) {
        return axios.delete(`/images/${imageId}`);
    },
    // Sửa đổi phương thức getAllImages để nhận các tham số phân trang rõ ràng
    getAllImages(page = 0, size = 10) {
        return axios.get('/images', {
            params: {
                page,
                size,
            },
        });
    },
    addImage1(params) {
        return axios.post(`/images`, params);
    },
    updateImage1(imageId, params) {
        return axios.put(`/images/${imageId}`, params);
    },
    updateImage(imageId, formData) {
        return axios.put(`/images/${imageId}`, formData, {
            headers: {
                'Content-Type': 'multipart/form-data',
            },
        });
    },
    addImage(formData) {
        return axios.post(`/images`, formData, {
            headers: {
                'Content-Type': 'multipart/form-data',
            },
        });
    },
    // Get images by owner type and ID
    getImagesByOwner(ownerType, ownerId) {
        return axios.get(`/images/owner/${ownerType}/${ownerId}`);
    },
    // Upload multiple images
    uploadMultipleImages(formData) {
        return axios.post(`/images/batch`, formData, {
            headers: {
                'Content-Type': 'multipart/form-data',
            },
        });
    },
    // Get image statistics
    getImageStatistics() {
        return axios.get('/images/statistics');
    }
};
