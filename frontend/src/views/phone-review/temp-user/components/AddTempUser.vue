<script
    lang="ts"
    src="@/scripts/phone-review/temp-user/components/addTempUser.ts">
</script>

<template>
    <div class="flex-fill d-flex flex-column w-100 p-2" v-loading="isLoading">
        <!-- Header -->
        <div class="d-flex align-items-center justify-content-between pb-3">
            <div class="page-titles">
                <ol class="breadcrumb">
                    <li class="breadcrumb-item">
                        <a href="javascript:void(0)">{{ t('Temp User') }}</a>
                    </li>
                    <li class="breadcrumb-item active">
                        <a href="javascript:void(0)">
                            {{ viewSettings.title }}
                        </a>
                    </li>
                </ol>
            </div>

            <div class="d-flex align-items-center">
                <div class="ml-1 mr-4 w-100">
                    <el-button
                        size="default"
                        type="danger"
                        class="d-none d-md-block"
                        @click="
                            $emit('onChangeView', {
                                viewName: 'ListData',
                                data: null,
                            })
                        "
                    >
                        <div>Back</div>
                    </el-button>
                </div>
            </div>
        </div>

        <!-- Form -->
        <div class="card">
            <div class="card-body">
                <el-form
                    ref="formRef"
                    :model="itemModel"
                    :rules="rules"
                    label-width="0"
                    class="ruleForm"
                >
                    <div class="row">
                        <div class="col-12">
                            <!-- Các trường chung -->
                            <div
                                class="py-2 px-2"
                                v-for="(label, key) in commonFields"
                                :key="key"
                            >
                                <strong>
                                    {{ label }}
                                    <!-- Link + Copy for fbUserUrl -->
                                    <template v-if="key === 'fbUserUrl' && itemModel[key]">
                                        <a
                                            :href="itemModel[key]"
                                            target="_blank"
                                            class="text-primary"
                                        >
                                            ({{ t('Go to page') }})
                                        </a>
                                        <a
                                            v-if="
                                                (
                                                    key === 'name' ||
                                                    key === 'phone' ||
                                                    key === 'status'
                                                )
                                            "
                                            class="text-warning">
                                            ({{ t('Require') }})
                                        </a>0000000
                                        <el-tooltip
                                            :content="
                                                copiedKey === key
                                                    ? t('Copied!')
                                                    : t('Copy')
                                            "
                                            placement="top"
                                        >
                                            <button
                                                @click="
                                                    copyToClipboard(
                                                        itemModel[key],
                                                        key
                                                    )
                                                "
                                                type="button"
                                                class="text-primary hover:underline ml-2"
                                            >
                                                ({{ t('Copy') }})
                                            </button>
                                        </el-tooltip>
                                    </template>
                                </strong>

                                <!-- Input fields -->
                                <el-form-item :prop="key">
                                    <!-- Input for normal fields -->
                                    <el-input
                                        v-if="key !== 'status'"
                                        v-model="itemModel[key]"
                                        size="large"
                                        :placeholder="label"
                                    />

                                    <!-- Select for status -->
                                    <el-select
                                        v-else
                                        v-model="itemModel.status"
                                        size="large"
                                        placeholder="Select Status"
                                    >
                                        <el-option
                                            v-for="(statusLabel, value) in CustomerStatus"
                                            :key="value"
                                            :label="statusLabel"
                                            :value="value"
                                        />
                                    </el-select>
                                </el-form-item>
                            </div>
                        </div>
                    </div>
                </el-form>
            </div>

            <!-- Footer -->
            <div class="card-footer">
                <div class="text-center py-3">
                    <el-button
                        size="large"
                        type="primary"
                        class="mr-1 ml-1"
                        @click="onSubmit(formRef)"
                    >
                        <el-icon>
                            <Plus />
                        </el-icon>
                        <span>{{ viewSettings.title }}</span>
                    </el-button>
                </div>
            </div>
        </div>
    </div>
</template>

<style scoped lang="scss">
img {
    max-width: 80%;
    height: auto;
}

.enabled-switch-container {
    display: flex;
    align-items: center;

    .el-switch {
        .el-switch__label {
            white-space: nowrap;
        }
    }
}

.el-form-item.text-nowrap {
    .el-switch .el-switch__label {
        white-space: nowrap;
    }
}
</style>
