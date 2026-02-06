import { useI18n } from 'vue-i18n';
import { ref, reactive, watch } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { filterDataFunction, splitData, formatDateTime } from '@/utils/search';
import { fbConnectionApi } from '@/api/fbConnectionApi';
import { useDataconnectionStore } from '@/stores/connectionStore';
import { useSearchStore } from '@/stores/search';

export default {
    props: ['viewSettings'],
    emits: ['onChangeView'],
    setup(props: any, context: any) {
        const { t } = useI18n();
        const connectionStore = useDataconnectionStore();
        const searchStore = useSearchStore();

        const isLoading = ref(false);
        const tempList = ref([]); // Dữ liệu gốc (toàn bộ)
        const paginatedData = ref([]); // Dữ liệu sau khi lọc
        const listItems = ref([]); // Dữ liệu hiển thị trên trang hiện tại

        const pagePagination = reactive({
            pageSize: 15,
            currentPage: 1,
            totalItems: 0,
        });

        /**
         * 📦 Hàm áp dụng phân trang (client-side)
         */
        function applyPagination(listToPaginate: any[]) {
            pagePagination.totalItems = listToPaginate.length;
            listItems.value = splitData(listToPaginate, pagePagination);
        }

        /**
         * 🔁 Lấy dữ liệu gốc từ store
         */
        async function refreshDataFn() {
            isLoading.value = true;
            try {
                await connectionStore.getConnectionsAll({ page: 0, size: 9999 });
                tempList.value = connectionStore.connection.content || [];

                // Khởi tạo dữ liệu mặc định (toàn bộ danh sách)
                paginatedData.value = tempList.value;
                pagePagination.currentPage = 1;
                applyPagination(paginatedData.value);
            } catch (error) {
                console.error(error);
                ElMessage.error(t('Failed to load connection list'));
            } finally {
                isLoading.value = false;
            }
        }

        // Gọi khi component được setup
        refreshDataFn();

        /**
         * 🗑️ Xóa cấu hình
         */
        const deleteConfig = (id: any) => {
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
                        ElMessage.success(t('Config deleted successfully'));
                        await refreshDataFn();
                        // Giữ lại kết quả lọc nếu đang có tìm kiếm
                        watchSearchQuery(searchStore.query);
                    } catch (error) {
                        console.error(error);
                        ElMessage.error(t('Failed to delete connection'));
                    } finally {
                        isLoading.value = false;
                    }
                })
                .catch(() => {
                    ElMessage.info(t('Delete action cancelled'));
                });
        };

        /**
         * 🔄 Cập nhật trạng thái enable/disable
         */
        const toggleStatus = async (itemData: any, newStatus: boolean) => {
            try {
                isLoading.value = true;
                const updatedData = { ...itemData, enabled: newStatus };

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

        
        /**
         * 🔄 Cập nhật trạng thái active/inactive
         */
        const toggleActive = async (itemData: any, newActive: boolean) => {
              // Thông báo tuỳ vào trạng thái active
            if (itemData.active) {
                ElMessage.success(t('Kết nối đầy đủ'));
            } else {
                ElMessage.warning(t('Fanpage đã bị gỡ khỏi App Facebook. Vào "tự động kết nối", chọn lại trang để kích hoạt lại. Nếu kết nối thật bại, hãy xóa kết nối này rồi thêm lại'));
            }
        };

        /**
         * 🔍 Theo dõi thay đổi của ô tìm kiếm
         */
        const watchSearchQuery = (newVal: string) => {
            pagePagination.currentPage = 1; // Reset về trang đầu
            if (!newVal) {
                paginatedData.value = tempList.value;
            } else {
                paginatedData.value = filterDataFunction(newVal, tempList.value);
            }
            applyPagination(paginatedData.value);
        };

        watch(
            () => searchStore.query,
            watchSearchQuery
        );

        /**
         * 👀 Theo dõi thay đổi page/pageSize
         */
        watch(
            () => [pagePagination.pageSize, pagePagination.currentPage],
            () => applyPagination(paginatedData.value)
        );

        /**
         * 📄 Xử lý thay đổi trang và kích thước
         */
        const handleSizeChange = (size: number) => {
            pagePagination.pageSize = size;
            pagePagination.currentPage = 1;
            applyPagination(paginatedData.value);
        };

        const handleCurrentChange = (page: number) => {
            pagePagination.currentPage = page;
            applyPagination(paginatedData.value);
        };

        return {
            t,
            pagePagination,
            handleCurrentChange,
            handleSizeChange,
            isLoading,
            listItems,
            refreshDataFn,
            deleteConfig,
            formatDateTime,
            toggleStatus,
            toggleActive
        };
    },
};
