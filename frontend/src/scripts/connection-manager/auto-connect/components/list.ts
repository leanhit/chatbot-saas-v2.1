import { useI18n } from 'vue-i18n';
import { ref, reactive, watch, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { filterDataFunction, splitData, formatDateTime } from '@/utils/search';
import { fbConnectionApi } from '@/api/fbConnectionApi';
import { useDataconnectionStore } from '@/stores/connectionStore';
import { useSearchStore } from '@/stores/search';
import { useFacebookStore } from '@/stores/facebook';
import { sendAddConnections } from './autoConnectHandler';

export default {
  props: ['viewSettings'],
  emits: ['onChangeView'],
  setup(props, context) {
    const { t } = useI18n();
    const connectionStore = useDataconnectionStore();
    const searchStore = useSearchStore();
    const facebookStore = useFacebookStore();

    const filterData = ref('');
    const filter = ref('ALL');
    const tempList = ref([]); // dữ liệu gốc
    const listItems = ref([]); // dữ liệu hiển thị sau phân trang
    const isLoading = ref(false);
    const connectedPageIds = ref([]); // danh sách pageId đã connect

    const pagePagination = reactive({
      pageSize: 15,
      currentPage: 1,
      totalItems: 0,
    });

    const selectedBotId = ref('traloitudong');
    const botIdOptions = ref([
      { name: "Bot test", value: "traloitudong" },
      { name: "khoa học", value: "testflowqa" },
      { name: "Loathongbao", value: "tingbox" },
    ]);

    /** 📦 Hàm áp dụng phân trang */
    const applyPagination = (dataList: any[]) => {
      pagePagination.totalItems = dataList.length;
      listItems.value = splitData(dataList, pagePagination);
    };

    /** 🔄 Lấy danh sách connection */
    const refreshDataFn = async () => {
      isLoading.value = true;
      try {
        await connectionStore.getAllConnections({ page: 0, size: 999 });
        tempList.value = connectionStore.connection.content || [];
        pagePagination.currentPage = 1;
        applyPagination(tempList.value);
      } catch (error) {
        console.error('❌ Failed to fetch connections:', error);
        ElMessage.error(t('Error loading connections'));
      } finally {
        isLoading.value = false;
      }
    };

    /** 🔄 Lấy danh sách các pageId đã connect */
    const fetchConnectedPageIds = async () => {
      try {
        const res = await fbConnectionApi.getAllConnections({ page: 0, size: 999 });
        if (res.data?.content) {
          connectedPageIds.value = res.data.content.map(conn => conn.pageId);
        }
      } catch (err) {
        console.error('❌ Error fetching connected pages:', err);
      }
    };

    /** 🧩 Auto connect Facebook */
    const handleAutoConnect = () => {
      if (typeof window.FB === 'undefined') {
        ElMessage.error('Facebook SDK chưa load! Vui lòng thử lại.');
        return;
      }

      const botpressPermissions = [
        'public_profile',
        'email',
        'pages_messaging',
        'pages_show_list',
        'pages_read_engagement',
        'pages_manage_posts',
        'pages_messaging_subscriptions',
        'pages_read_user_content'
      ];

      window.FB.login(
        (response) => {
          if (response.authResponse) {
            const { accessToken, userID } = response.authResponse;
            facebookStore.setFacebookData({ accessToken, userID });
            sendAddConnections(accessToken, selectedBotId.value, refreshDataFn);
          } else {
            ElMessage.error('Đăng nhập Facebook thất bại.');
          }
        },
        { scope: botpressPermissions.join(',') }
      );
    };

    /** 🗑️ Xóa kết nối */
    const deleteConfig = async (id: string) => {
      ElMessageBox.confirm(
        t('Are you sure you want to delete this connection?'),
        t('Warning'),
        {
          confirmButtonText: t('Yes'),
          cancelButtonText: t('No'),
          type: 'warning',
        }
      )
        .then(async () => {
          isLoading.value = true;
          try {
            await fbConnectionApi.deleteConfig(id);
            ElMessage.success(t('Connection deleted successfully'));
            await refreshDataFn();
          } catch (error) {
            ElMessage.error(t('Failed to delete connection'));
          } finally {
            isLoading.value = false;
          }
        })
        .catch(() => {
          ElMessage.info(t('Delete action cancelled'));
        });
    };

    /** 🔁 Đổi trạng thái (enable/disable) */
    const toggleStatus = async (itemData: any, newStatus: boolean) => {
      try {
        isLoading.value = true;
        const updatedData = { ...itemData, isEnabled: newStatus };
        const res = await fbConnectionApi.updateConfig(itemData.id, updatedData);

        if (res.data) {
          itemData.enabled = newStatus;
          ElMessage.success(t('Status updated successfully'));
        } else {
          ElMessage.error(t('Failed to update status'));
        }
      } catch (err) {
        console.error(err);
        ElMessage.error(t('Error updating status'));
      } finally {
        isLoading.value = false;
      }
    };

    /** 🔍 Theo dõi thay đổi từ ô tìm kiếm */
    watch(
      () => searchStore.query,
      (newVal) => {
        const filtered = newVal
          ? filterDataFunction(newVal, tempList.value)
          : tempList.value;

        pagePagination.currentPage = 1;
        applyPagination(filtered);
      }
    );

    /** 📜 Thay đổi kích thước trang */
    const handleSizeChange = (size: number) => {
      pagePagination.pageSize = size;
      pagePagination.currentPage = 1;
      applyPagination(tempList.value);
    };

    /** 📜 Thay đổi trang hiện tại */
    const handleCurrentChange = (page: number) => {
      pagePagination.currentPage = page;
      applyPagination(tempList.value);
    };

    /** ⏳ Khi component mount */
    onMounted(() => {
      refreshDataFn();
      fetchConnectedPageIds();
    });

    /** 👀 Theo dõi thay đổi trang / pageSize */
    watch(
      () => [pagePagination.pageSize, pagePagination.currentPage],
      () => applyPagination(tempList.value)
    );

    return {
      t,
      pagePagination,
      handleCurrentChange,
      handleSizeChange,
      isLoading,
      listItems,
      filterData,
      refreshDataFn,
      filter,
      deleteConfig,
      formatDateTime,
      toggleStatus,
      showFacebookLoginModal: handleAutoConnect,
      connectedPageIds,
      botIdOptions,
      selectedBotId,
    };
  },
};
