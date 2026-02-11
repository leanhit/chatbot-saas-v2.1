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
  // Update address (old endpoint - kept for backward compatibility)
  updateAddress(addressId, data) {
    // Ensure ownerId and ownerType are included in the request
    const updateData = {
      ...data,
      // These fields are required by the backend validation
      ownerId: data.ownerId,
      ownerType: data.ownerType
    };
    return axios.put(`/addresses/${addressId}`, updateData);
  },
  // Update tenant address by tenantKey (UUID)
  updateTenantAddress(tenantKey, data) {
    return axios.put(`/addresses/tenant/${tenantKey}`, data);
  },
  // Update user address
  updateUserAddress(ownerType, ownerId, data) {
    return axios.put(`/addresses/user/${ownerType}/${ownerId}`, data);
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
