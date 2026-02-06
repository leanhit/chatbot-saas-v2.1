// @/scripts/tenant/overview/components/tenantAddressForm.ts
import { defineComponent, ref, reactive, watch, computed, onMounted } from 'vue';
import { useI18n } from 'vue-i18n';
import { useTenantAdminContextStore } from '@/stores/tenant/admin/tenantContextStore';
import { useTenantAdminSettingsStore } from '@/stores/tenant/admin/tenantSettingsStore';
import { useLocationStore } from '@/stores/locationStore';
import { InfoFilled } from '@element-plus/icons-vue';
import type { AddressRequestDTO } from '@/types/address';

export default defineComponent({
  name: 'TenantAddressForm',    
  emits: ['onChangeView'],
  components: { InfoFilled },
  setup() {
    const { t } = useI18n();
    const contextStore = useTenantAdminContextStore();
    const settingsStore = useTenantAdminSettingsStore();
    const locationStore = useLocationStore();

    const form = reactive({
      houseNumber: '',
      street: '',
      provinceCode: null as number | null,
      districtCode: null as number | null,
      wardCode: null as number | null,
    });

    const loading = computed(() => settingsStore.loading || contextStore.loading);
    const provinces = computed(() => locationStore.provinces);
    const districtOptions = computed(() => (form.provinceCode ? locationStore.districts[form.provinceCode] || [] : []));
    const wardOptions = computed(() => (form.districtCode ? locationStore.wards[form.districtCode] || [] : []));

    /**
     * Logic map từ Tên sang Code để hiển thị lên Select
     */
    const initForm = async () => {
      const address = contextStore.tenant?.address;
      if (!address) return;

      form.houseNumber = address.houseNumber || '';
      form.street = address.street || '';

      // 1. Tìm Province Code từ tên "Thành phố Hà Nội"
      const province = provinces.value.find(p => p.name === address.province);
      if (province) {
        form.provinceCode = province.code;

        // 2. Load Districts và tìm District Code từ tên "Quận Ba Đình"
        await locationStore.loadDistricts(province.code);
        const district = (locationStore.districts[province.code] || []).find(d => d.name === address.district);
        
        if (district) {
          form.districtCode = district.code;

          // 3. Load Wards và tìm Ward Code từ tên "Phường Quán Thánh"
          await locationStore.loadWards(district.code);
          const ward = (locationStore.wards[district.code] || []).find(w => w.name === address.ward);
          if (ward) {
            form.wardCode = ward.code;
          }
        }
      }
    };

    watch(() => contextStore.tenant, () => {
      initForm();
    }, { deep: true });

    watch(() => form.provinceCode, async (newVal, oldVal) => {
      // Chỉ reset nếu người dùng chủ động thay đổi (oldVal khác null)
      if (oldVal !== null) {
        form.districtCode = null;
        form.wardCode = null;
      }
      if (newVal) await locationStore.loadDistricts(newVal);
    });

    watch(() => form.districtCode, async (newVal, oldVal) => {
      if (oldVal !== null) {
        form.wardCode = null;
      }
      if (newVal) await locationStore.loadWards(newVal);
    });

    onMounted(async () => {
      await locationStore.loadProvinces();
      await initForm();
    });

    const save = async () => {
      if (!contextStore.tenant) return;

      const provinceName = provinces.value.find(p => p.code === form.provinceCode)?.name || '';
      const districtName = districtOptions.value.find(d => d.code === form.districtCode)?.name || '';
      const wardName = wardOptions.value.find(w => w.code === form.wardCode)?.name || '';

      const payload: AddressRequestDTO = {
        ownerType: 'TENANT',
        ownerId: contextStore.tenant.id,
        street: form.street,
        houseNumber: form.houseNumber,
        ward: wardName,
        district: districtName,
        province: provinceName,
        country: 'Vietnam',
        isDefault: true
      };

      const addressId = contextStore.tenant.address?.id.toString() || '';
      await settingsStore.updateAddress(contextStore.tenant.id.toString(), addressId, payload);
      await contextStore.loadTenant();
    };

    return { t, form, loading, provinces, districtOptions, wardOptions, save };
  }
});