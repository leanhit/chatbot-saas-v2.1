import { defineAsyncComponent, ref, nextTick } from 'vue';
import SkeletonBox from '@/components/SkeletonBox.vue';
import { useI18n } from 'vue-i18n';
import { Edit } from '@element-plus/icons-vue';

export default {
    components: {
        ListData: defineAsyncComponent({
            loader: () => import('@/views/phone-review/phone-captured/components/List.vue'),
            loadingComponent: SkeletonBox,
        }),
        EditTempUser: defineAsyncComponent({
            loader: () => import('@/views/phone-review/phone-captured/components/List.vue'),
            loadingComponent: SkeletonBox,
        }),
    },
    setup() {
        const { t } = useI18n();
        const isChangeViewLoading = ref(false);
        const currentComponent = ref('ListData');
        const viewSettings = ref({
            viewName: 'ListData',
            title: t('User List'),
            dataItem: null,
        });
        const changeView = (paramsObject: { viewName: string; data: any }) => {
            isChangeViewLoading.value = true;
            setTimeout(async () => {
                switch (paramsObject.viewName) {
                    case 'ListData':
                        viewSettings.value = {
                            viewName: 'ListData',
                            title: t("Temp User"),
                            dataItem: null,
                        };
                        break;
                    case 'EditTempUser':
                        viewSettings.value = {
                            viewName: 'EditTempUser',
                            title: t("Edit Temp User"),
                            dataItem: paramsObject.data,
                        };
                        break;
                }
                await nextTick();
                currentComponent.value = paramsObject.viewName;
                isChangeViewLoading.value = false;
            }, 100);
        };
        return {
            isChangeViewLoading,
            currentComponent,
            viewSettings,
            changeView,
        };
    },
};