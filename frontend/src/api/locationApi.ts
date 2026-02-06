import axios from '@/plugins/axios';

export const locationApi = {
  getProvinces() {
    return axios.get('/locations/provinces');
  },
  getDistricts(provinceCode: number) {
    return axios.get('/locations/districts', {
      params: { provinceCode }
    });
  },
  getWards(districtCode: number) {
    return axios.get('/locations/wards', {
      params: { districtCode }
    });
  }
};
