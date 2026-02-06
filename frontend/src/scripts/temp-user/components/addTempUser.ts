import { ref, reactive, onMounted } from 'vue';
import type { FormInstance, FormRules } from 'element-plus';
import { ElMessage } from 'element-plus';
import { tempUsersApi } from '@/api/tempUserApi';
import { useI18n } from 'vue-i18n';

export default {
    props: ['viewSettings'],
    emits: ['onChangeView'],
    setup(props: any, context: any) {
        const { t } = useI18n();
        const isLoading = ref(false);
        const viewName = ref('');
        const formRef = ref<FormInstance>();
        const copiedKey = ref(null);

        // Các trường chung cho cả hai chế độ Thêm và Sửa
        const commonFields = {
            name: 'User Name',
            phone: 'Phone Number',
            email: 'Email',
            fbUserUrl: 'User Ppage URL',
            status: 'Status',
        };

        const CustomerStatus = {
            PENDING:'PENDING',              /** Đang thu thập thông tin, chưa đủ data cần thiết. */
            COMPLETED:'COMPLETED',          /** Đã thu thập đủ thông tin, sẵn sàng để đẩy vào Odoo. */
            PUSHED_TO_ODOO:'PUSHED_TO_ODOO',/** Đã đẩy thành công vào Odoo. */
            FAILED:'FAILED'                /** Đẩy vào Odoo thất bại, cần kiểm tra lại. */  
        }

        const itemModel = ref({
            name: '',
            phone: '',
            email: '',
            fbUserUrl: '',
            status: CustomerStatus.PENDING
        });

        // Form validation rules
        const rules = reactive<FormRules>({
            name: [
                {
                    required: true,
                    message: 'Tên là bắt buộc',
                    trigger: 'blur',
                },
            ],
            phone: [
                {
                    required: true,
                    message: 'Sđt là bắt buộc',
                    trigger: 'blur',
                },
            ]
        });

        onMounted(() => {
            if (props.viewSettings?.viewName === 'EditTempUser') {
                viewName.value = props.viewSettings.viewName;

                Object.assign(itemModel.value, props.viewSettings.dataItem);
            } else {
                console.warn('Something went wrong');
            }
        });

        function onSubmit(formEl: FormInstance | undefined) {
            isLoading.value = true;

            if (!formEl) return;
            formEl.validate((valid) => {
                if (valid) {
                    const { name, phone, email, fbUserUrl, status } = itemModel.value;

                    const payload = {
                        dataJson: JSON.stringify({ name, phone, email, fbUserUrl }),
                        status,
                    };

                    console.log("Payload gửi đi:", payload);

                    const psid = props.viewSettings.dataItem.psid;
                    // Giữ lại tất cả các trường khi cập nhật
                    tempUsersApi
                        .updateData(psid, payload)
                        .then((response: any) => {
                            if (response.data) {
                                ElMessage({
                                    message: t('Successful!'),
                                    type: 'success',
                                });
                                context.emit('onChangeView', {
                                    viewName: 'ListData',
                                    data: null,
                                });
                            } else {
                                ElMessage.error(`Oops, ${response.message}`);
                            }
                            isLoading.value = false;
                        })
                        .catch((error) => {
                            console.error(error);
                            isLoading.value = false;
                        });
                } else {
                    console.log('error submit!');
                    isLoading.value = false;
                }
            });
        }

        const copyToClipboard = async (text: string, key: string) => {
            try {
                await navigator.clipboard.writeText(text);
                copiedKey.value = key;
                ElMessage.success('Đã sao chép!');

                setTimeout(() => {
                    copiedKey.value = null;
                }, 1500);
            } catch (err) {
                ElMessage.error('Không thể sao chép');
            }
        };

        return {
            t,
            isLoading,
            itemModel,
            formRef,
            rules,
            copiedKey,
            viewName, // Bổ sung viewName để sử dụng trong template
            commonFields,
            onSubmit,
            copyToClipboard,
            CustomerStatus,
        };
    },
};