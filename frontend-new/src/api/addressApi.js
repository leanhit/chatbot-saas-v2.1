import axios from '@/plugins/axios';

export const addressApi = {
  // Get single address by owner
  getAddressByOwner(ownerType, ownerId) {
    return axios.get(`/addresses/owner/${ownerType}/${ownerId}`);
  },

  // Get or create single address by owner (ensures address exists)
  getOrCreateAddressByOwner(ownerType, ownerId) {
    return axios.get(`/addresses/owner/${ownerType}/${ownerId}/ensure`);
  },

  // Get address by ID
  getAddressById(addressId) {
    return axios.get(`/addresses/${addressId}`);
  },

  // Create new address
  createAddress(data) {
    return axios.post('/addresses', data);
  },

  // Update address
  updateAddress(addressId, data) {
    return axios.put(`/addresses/${addressId}`, data);
  },

  // Delete address
  deleteAddress(addressId) {
    return axios.delete(`/addresses/${addressId}`);
  },

  // Update address fields - new endpoint for partial update
  updateAddressFields(addressId, data) {
    return axios.put(`/addresses/${addressId}/fields`, data);
  }
};
