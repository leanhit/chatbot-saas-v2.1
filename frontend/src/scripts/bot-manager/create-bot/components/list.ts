import { useI18n } from 'vue-i18n';
import { ref, reactive, watch } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { filterDataFunction, splitData, formatDateTime } from '@/utils/search';
import { botApi } from '@/api/botApi';
import { useSearchStore } from '@/stores/search';
import { useBotStore } from '@/stores/botStore';
import { useFacebookStore } from '@/stores/facebook';
import { sendAddConnections } from './autoConnectHandler';

export default {
    props: ['viewSettings'],
    emits: ['onChangeView'],
    setup(props: any, context: any) {
        const { t } = useI18n();
        const botStore = useBotStore();
        const searchStore = useSearchStore();
        const facebookStore = useFacebookStore();

        const isLoading = ref(false);
        const tempList = ref([]); // toàn bộ dữ liệu
        const filteredList = ref([]); // dữ liệu sau khi lọc
        const listItems = ref([]); // dữ liệu trang hiện tại

        const pagePagination = reactive({
            pageSize: 15,
            currentPage: 1,
            totalItems: 0,
        });

        /** 📦 Hàm chia trang */
        function applyPagination(list: any[]) {
            pagePagination.totalItems = list.length;
            listItems.value = splitData(list, pagePagination);
        }

        /** 🔄 Lấy dữ liệu từ API */
        async function refreshDataFn() {
            isLoading.value = true;
            try {
                await botStore.getAllBots({ page: 0, size: 9999 });
                tempList.value = botStore.bot.content || [];
                filteredList.value = tempList.value;

                pagePagination.currentPage = 1;
                applyPagination(filteredList.value);
            } catch (err) {
                console.error(err);
                ElMessage.error(t('Failed to load bot list'));
            } finally {
                isLoading.value = false;
            }
        }

        // Gọi khi component mount
        refreshDataFn();

        /** 🗑️ Xóa bot */
        const deleteBot = (itemData: any) => {
            ElMessageBox.confirm(
                t('Are you sure you want to delete this bot?'),
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
                        await botApi.deleteBot(itemData.botId);
                        ElMessage.success(t('Bot deleted successfully'));
                        await refreshDataFn();
                        watchSearchQuery(searchStore.query);
                    } catch (error) {
                        ElMessage.error(t('Failed to delete bot'));
                    } finally {
                        isLoading.value = false;
                    }
                })
                .catch(() => {
                    ElMessage.info(t('Delete action cancelled'));
                });
        };

        /** 🔍 Xử lý tìm kiếm */
        const watchSearchQuery = (newVal: string) => {
            pagePagination.currentPage = 1;
            if (!newVal) {
                filteredList.value = tempList.value;
            } else {
                filteredList.value = filterDataFunction(newVal, tempList.value);
            }
            applyPagination(filteredList.value);
        };

        watch(
            () => searchStore.query,
            watchSearchQuery
        );

        /** 👀 Theo dõi thay đổi page/pageSize */
        watch(
            () => [pagePagination.pageSize, pagePagination.currentPage],
            () => applyPagination(filteredList.value)
        );

        /** ⚙️ Thay đổi page size */
        const handleSizeChange = (size: number) => {
            pagePagination.pageSize = size;
            pagePagination.currentPage = 1;
            applyPagination(filteredList.value);
        };

        /** ⚙️ Thay đổi trang hiện tại */
        const handleCurrentChange = (page: number) => {
            pagePagination.currentPage = page;
            applyPagination(filteredList.value);
        };

        /** 🔑 Lấy token */
        const getToken = async () => {
            try {
                const token = await botApi.getToken();
                console.log('Token ===>', token.data);
                alert(token.data);
            } catch (err) {
                console.error(err);
                ElMessage.error('Failed to get token');
            }
        };

        /** ⚡ Facebook Auto Connect */
        const handleAutoConnect = (bot: any) => {
            if (typeof window.FB === 'undefined') {
                console.error('❌ Facebook SDK chưa load!');
                ElMessage.error('Facebook SDK chưa load! Vui lòng kiểm tra lại.');
                return;
            }

            const botpressPermissions = [
                'public_profile',
                'email',
                'pages_messaging',
                'pages_show_list',
                'pages_read_engagement',
                'pages_manage_posts',
            ];

            window.FB.login(
                (response) => {
                    if (response.authResponse) {
                        const { accessToken, userID } = response.authResponse;
                        facebookStore.setFacebookData({ accessToken, userID });
                        sendAddConnections(accessToken, bot.botId, refreshDataFn);
                    } else {
                        console.error('Login error: Đăng nhập bị hủy hoặc không cấp quyền.');
                        ElMessage.error('Đăng nhập Facebook thất bại.');
                    }
                },
                {
                    scope: botpressPermissions.join(','),
                }
            );
        };

        return {
            t,
            pagePagination,
            handleCurrentChange,
            handleSizeChange,
            isLoading,
            listItems,
            refreshDataFn,
            deleteBot,
            formatDateTime,
            getToken,
            showFacebookLoginModal: handleAutoConnect,
        };
    },
};
