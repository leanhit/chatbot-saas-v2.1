// Package API Service
// This service handles all package related API calls
import axios from '@/plugins/axios'

const packageApi = {
  // Get all active packages
  getActivePackages: async () => {
    const response = await axios.get('/v1/packages/active')
    return response.data
  },

  // Get all packages (admin only)
  getAllPackages: async () => {
    const response = await axios.get('/v1/packages')
    return response.data
  },

  // Get package by ID
  getPackageById: async (id) => {
    const response = await axios.get(`/v1/packages/${id}`)
    return response.data
  },

  // Get package by package ID
  getPackageByPackageId: async (packageId) => {
    const response = await axios.get(`/v1/packages/by-package-id/${packageId}`)
    return response.data
  },

  // Create new package (admin only)
  createPackage: async (packageData) => {
    const response = await axios.post('/v1/packages', packageData)
    return response.data
  },

  // Update package (admin only)
  updatePackage: async (id, packageData) => {
    const response = await axios.put(`/v1/packages/${id}`, packageData)
    return response.data
  },

  // Delete package (admin only)
  deletePackage: async (id) => {
    const response = await axios.delete(`/v1/packages/${id}`)
    return response.data
  },

  // Permanently delete package (admin only)
  permanentlyDeletePackage: async (id) => {
    const response = await axios.delete(`/v1/packages/${id}/permanent`)
    return response.data
  },

  // Initialize default packages (admin only)
  initializeDefaultPackages: async () => {
    const response = await axios.post('/v1/packages/initialize')
    return response.data
  },

  // Check if packages are initialized (admin only)
  checkInitialized: async () => {
    const response = await axios.get('/v1/packages/check-initialized')
    return response.data
  }
}

export default packageApi
