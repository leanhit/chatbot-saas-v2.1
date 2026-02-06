import { useI18n } from 'vue-i18n';
import { ref, reactive, watch, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { splitData } from '@/utils/search';
import { exportDataAsJson } from '@/utils/writeFile';
import { useWorkspaceStore } from '@/stores/botpressStore';
import { botApi } from '@/api/botApi';

export default {
  props: ['viewSettings'],
  setup() {
    const { t } = useI18n();
    const workspaceStore = useWorkspaceStore();

    const isLoading = ref(false);
    const tempList = ref([]); // Dữ liệu gốc
    const paginatedData = ref([]); // Dữ liệu sau khi áp dụng filter (nếu có)
    const listItems = ref([]); // Dữ liệu hiển thị thực tế

    const pagePagination = reactive({
      pageSize: 10,
      currentPage: 1,
      totalItems: 0,
    });

    /** 📦 Hàm áp dụng phân trang */
    function applyPagination(list: any[]) {
      pagePagination.totalItems = list.length;
      listItems.value = splitData(list, pagePagination);
    }

    /** 🔄 Lấy dữ liệu từ API */
    async function refreshDataFn() {
      isLoading.value = true;
      try {
        const data = await workspaceStore.fetchWorkspaces();
        tempList.value = data || [];
        paginatedData.value = tempList.value;

        //exportDataAsJson(data, 'workspaces.json');
        pagePagination.currentPage = 1;
        applyPagination(paginatedData.value);
      } catch (err) {
        console.error('❌ Error fetching workspaces:', err);
        ElMessage.error(t('Error fetching workspaces'));
      } finally {
        isLoading.value = false;
      }
    }

    /** ✅ Sự kiện đổi kích thước trang */
    const handleSizeChange = (size: number) => {
      pagePagination.pageSize = size;
      pagePagination.currentPage = 1;
      applyPagination(paginatedData.value);
    };

    /** ✅ Sự kiện đổi trang */
    const handleCurrentChange = (page: number) => {
      pagePagination.currentPage = page;
      applyPagination(paginatedData.value);
    };

    /** 🧩 Chọn workspace */
    function selectWorkspace(workspace: any) {
      workspaceStore.selectedWorkspace = workspace;
    }

    /** 🔍 Xem thông tin bot */
    async function viewBotInfo(botId: string) {
      try {
        const res = await botApi.getBotInfoFromBotpress(botId);
        console.log("🔍 View bot info:", res.data);
      } catch (err) {
        console.error(err);
        ElMessage.error(t("Failed to fetch bot info"));
      }
    }

    /** 🗂️ Archive Bot */
    async function archiveBot(botId: string) {
      try {
        await botApi.archiveBot(botId);
        ElMessage.success(t("Bot archived successfully"));
        await refreshDataFn();
      } catch (err) {
        ElMessage.error(t("Failed to archive bot"));
      }
    }

    /** 🗂️ Unarchive Bot */
    async function unarchiveBot(botId: string) {
      try {
        await botApi.unarchiveBot(botId);
        ElMessage.success(t("Bot unarchived successfully"));
        await refreshDataFn();
      } catch (err) {
        ElMessage.error(t("Failed to unarchive bot"));
      }
    }

    /** ❌ Delete Bot */
    async function deleteBot(botId: string) {
      try {
        await botApi.deleteBot(botId);
        ElMessage.success(t("Bot deleted successfully"));
        await refreshDataFn();
      } catch (err) {
        ElMessage.error(t("Failed to delete bot"));
      }
    }

    /** 👀 Tự động cập nhật khi thay đổi page/pageSize */
    watch(
      () => [pagePagination.pageSize, pagePagination.currentPage],
      () => applyPagination(paginatedData.value)
    );

    /** 🔄 Tải dữ liệu khi mount */
    onMounted(() => {
      refreshDataFn();
    });

    return {
      t,
      pagePagination,
      handleSizeChange,
      handleCurrentChange,
      refreshDataFn,
      selectWorkspace,
      isLoading,
      listItems,
      selectedWorkspace: workspaceStore.selectedWorkspace,
      roles: workspaceStore.roles,
      bots: workspaceStore.bots,
      pipeline: workspaceStore.pipeline,
      authStrategies: workspaceStore.authStrategies,
      rolloutStrategy: workspaceStore.rolloutStrategy,
      viewBotInfo,
      archiveBot,
      unarchiveBot,
      deleteBot,
    };
  },
};
