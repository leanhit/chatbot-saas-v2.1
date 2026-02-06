import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { userApi } from '@/api/userApi';
import { addressApi } from '@/api/addressApi';
import { ElMessage } from 'element-plus';
import type { UserInfoResponse, UserInfoRequest, UserFullProfile } from '@/types/user';
import type { AddressDetailResponseDTO, AddressRequestDTO } from '@/types/address';

export const useUserStore = defineStore('user', () => {
  // State
  const profile = ref<UserInfoResponse | null>(null);
  const addresses = ref<AddressDetailResponseDTO[]>([]);
  const loading = ref(false);

  // Getters
  const userAvatar = computed(() => profile.value?.avatar || '/default-avatar.png');
  const defaultAddress = computed(() => addresses.value.find(addr => addr.isDefault));

  // Actions: Profile
  const fetchProfile = async () => {
    loading.value = true;
    try {
      const { data } = await userApi.getMyProfile();
      profile.value = data;
      return data;
    } catch (error) {
      console.error('Failed to fetch profile', error);
    } finally {
      loading.value = false;
    }
  };

  const updateProfile = async (payload: UserInfoRequest) => {
    loading.value = true;
    try {
      const { data } = await userApi.updateProfile(payload);
      profile.value = data;
      ElMessage.success('Cập nhật thông tin cá nhân thành công');
      return data;
    } finally {
      loading.value = false;
    }
  };

  // Actions: Addresses (Tái sử dụng Address API với ownerType = 'USER')
  const fetchMyAddresses = async () => {
    if (!profile.value?.id) return;
    try {
      const { data } = await addressApi.getAddressesByOwner('USER', profile.value.id);
      addresses.value = data;
    } catch (error) {
      console.error('Failed to fetch addresses');
    }
  };

  const addUserAddress = async (payload: Omit<AddressRequestDTO, 'ownerType' | 'ownerId'>) => {
    if (!profile.value?.id) return;
    
    const requestData: AddressRequestDTO = {
      ...payload,
      ownerType: 'USER',
      ownerId: profile.value.id
    };

    try {
      const { data } = await addressApi.createAddress(requestData);
      addresses.value.push(data);
      ElMessage.success('Thêm địa chỉ mới thành công');
      return data;
    } catch (error) {
      ElMessage.error('Không thể thêm địa chỉ');
    }
  };

  const deleteAddress = async (addressId: number) => {
    try {
      await addressApi.deleteAddress(addressId);
      addresses.value = addresses.value.filter(a => a.id !== addressId);
      ElMessage.success('Đã xóa địa chỉ');
    } catch (error) {
      ElMessage.error('Xóa địa chỉ thất bại');
    }
  };

  const setAddressDefault = async (addressId: number) => {
    try {
      await addressApi.setDefault(addressId);
      // Cập nhật lại state local
      addresses.value.forEach(addr => {
        addr.isDefault = (addr.id === addressId);
      });
      ElMessage.success('Đã thay đổi địa chỉ mặc định');
    } catch (error) {
      ElMessage.error('Thao tác thất bại');
    }
  };

  return {
    profile,
    addresses,
    loading,
    userAvatar,
    defaultAddress,
    fetchProfile,
    updateProfile,
    fetchMyAddresses,
    addUserAddress,
    deleteAddress,
    setAddressDefault
  };
});