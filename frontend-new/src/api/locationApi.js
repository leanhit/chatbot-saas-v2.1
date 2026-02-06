import axios from '@/plugins/axios';

export const locationApi = {
  getProvinces() {
    return axios.get('/locations/provinces');
  },
  getDistricts(provinceCode) {
    return axios.get('/locations/districts', {
      params: { provinceCode }
    });
  },
  getWards(districtCode) {
    return axios.get('/locations/wards', {
      params: { districtCode }
    });
  }
};
