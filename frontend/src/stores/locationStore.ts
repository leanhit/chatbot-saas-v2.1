import { defineStore } from 'pinia';
import { ref } from 'vue';
import { locationApi } from '@/api/locationApi';

export const useLocationStore = defineStore('location', () => {
  const provinces = ref<any[]>([]);
  const districts = ref<Record<number, any[]>>({});
  const wards = ref<Record<number, any[]>>({});
  const loading = ref(false);

  const loadProvinces = async () => {
    if (provinces.value.length) return;
    loading.value = true;
    try {
      const { data } = await locationApi.getProvinces();
      provinces.value = data;
    } finally {
      loading.value = false;
    }
  };

  const loadDistricts = async (provinceCode: number) => {
    if (districts.value[provinceCode]) return;
    const { data } = await locationApi.getDistricts(provinceCode);
    districts.value[provinceCode] = data;
  };

  const loadWards = async (districtCode: number) => {
    if (wards.value[districtCode]) return;
    const { data } = await locationApi.getWards(districtCode);
    wards.value[districtCode] = data;
  };

  return {
    provinces,
    districts,
    wards,
    loading,
    loadProvinces,
    loadDistricts,
    loadWards
  };
});
