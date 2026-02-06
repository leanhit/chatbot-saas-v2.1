// src/api/categoryApi.js
import axios from '@/plugins/axios';

export const categoryApi = {
    // READ: Get a category by its ID
    getCategoryByID(categoryId) {
        return axios.get(`/categories/${categoryId}`);
    },

    // UPDATE: Update a category by its ID with new parameters
    updateCategory(categoryId, params) {
        return axios.put(`/categories/${categoryId}`, params);
    },

    // DELETE: Delete a category by its ID
    deleteCategory(categoryId) {
        return axios.delete(`/categories/${categoryId}`);
    },

    // READ: Get all categories
    getAllCategories() {
        return axios.get(`/categories`);
    },

    // CREATE: Add a new category
    addCategory(params) {
        return axios.post(`/categories`, params);
    },

    // Get category tree structure
    getCategoryTree() {
        return axios.get(`/categories/tree`);
    },

    // Get categories with counts
    getCategoriesWithCount() {
        return axios.get(`/categories/with-count`);
    },

    // Get categories by parent
    getCategoriesByParent(parentId) {
        return axios.get(`/categories/parent/${parentId}`);
    }
};
