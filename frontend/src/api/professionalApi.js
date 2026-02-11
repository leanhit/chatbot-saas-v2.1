import axios from '@/plugins/axios';
export const professionalApi = {
  getProfessionalByOwner(ownerType, ownerId) {
    return axios.get(`/professionals/${ownerType}/${ownerId}`);
  },
  createProfessional(data) {
    return axios.post('/professionals', data);
  },
  updateProfessional(id, data) {
    return axios.put(`/professionals/${id}`, data);
  },
  deleteProfessional(id) {
    return axios.delete(`/professionals/${id}`);
  }
};
