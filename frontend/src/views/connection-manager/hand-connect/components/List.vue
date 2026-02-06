<script lang="ts" src="@/scripts/connection-manager/hand-connect/components/list.ts"></script>

<template>
    <div class="flex-fill d-flex flex-column w-100 p-2" v-loading="isLoading">
        <!-- header -->
        <div class="d-flex align-items-center justify-content-between">
            <div class="page-titles">
                <ol class="breadcrumb">
                    <li class="breadcrumb-item active">
                        <a href="javascript:void(0)">{{
                            viewSettings.title
                        }}</a>
                    </li>
                </ol>
            </div>
            <div class="d-flex align-items-center">
                <div class="px-2 w-100">
                    <el-button
                        class="border-0 mx-1 my-1"
                        size="default"
                        type="primary"
                        @click="
                            $emit('onChangeView', {
                                viewName: 'AddConnection',
                                data: null,
                            })
                        ">
                        <span class="ml-1">{{ t('Add Connection') }}</span>
                    </el-button>
                    <el-button
                        size="default"
                        type="primary"
                        @click="refreshDataFn()">
                        <div>{{ t('Check') }}</div>
                    </el-button>
                </div>
            </div>
        </div>

        <!-- body -->
        <div class="card">
            <div class="row">
                <div class="col-lg-12">
                    <div class="card-body pt-0">
                        <div class="table-scroll-container">
                            <div class="table-responsive rounded card-table">
                                <table
                                    class="table table-striped table-bordered w-100">
                                    <thead>
                                        <tr>
                                            <th
                                                style="width: 5%"
                                                class="text-nowrap">
                                                {{ t('Idx') }}
                                            </th>
                                            <th
                                                style="width: 10%"
                                                class="text-nowrap">
                                                {{ t('BotID') }}
                                            </th>
                                            <th
                                                style="width: 20%"
                                                class="text-nowrap">
                                                {{ t('Connection Name') }}
                                            </th>
                                            <th
                                                style="width: 15%"
                                                class="text-nowrap">
                                                {{ t('PageID') }}
                                            </th>
                                            <th
                                                style="width: 10%"
                                                class="text-nowrap">
                                                {{ t('Status') }}
                                            </th>
                                            <th
                                                style="width: 10%"
                                                class="text-nowrap">
                                                {{ t('Active') }}
                                            </th>
                                            <th
                                                style="width: 10%"
                                                class="text-nowrap">
                                                {{ t('Created At') }}
                                            </th>
                                            <th
                                                style="width: 10%"
                                                class="text-nowrap">
                                                {{ t('Action') }}
                                            </th>
                                        </tr>
                                    </thead>

                                    <tbody v-if="listItems && listItems.length > 0">
                                        <tr
                                            v-for="(
                                                itemData, itemIndex
                                            ) in listItems"
                                            :key="itemIndex">
                                            <td class="text-left">
                                                {{ itemIndex + 1 }}
                                            </td>
                                            <td
                                                class="text-left text-truncate"
                                                :title="itemData.botId">
                                                <span class="truncate-text">{{
                                                    itemData.botId
                                                }}</span>
                                            </td>
                                            <td
                                                class="text-left text-truncate"
                                                :title="itemData.botName">
                                                <span class="truncate-text">{{
                                                    itemData.botName
                                                }}</span>
                                            </td>
                                            <td
                                                class="text-left text-truncate"
                                                :title="itemData.pageId">
                                                <span class="truncate-text">{{
                                                    itemData.pageId
                                                }}</span>
                                            </td>
                                            <td
                                                class="text-left text-truncate"
                                                :title="
                                                    itemData.enabled
                                                        ? t('Enabled')
                                                        : t('Disabled')
                                                ">
                                                <el-form-item
                                                    class="text-nowrap pt-3">
                                                    <el-button
                                                        size="default"
                                                        :type="
                                                            itemData.enabled
                                                                ? 'success'
                                                                : 'danger'
                                                        "
                                                        @click="
                                                            toggleStatus(
                                                                itemData,
                                                                !itemData.enabled
                                                            )
                                                        ">
                                                        {{
                                                            itemData.enabled
                                                                ? t('Enabled')
                                                                : t('Disabled')
                                                        }}
                                                    </el-button>
                                                </el-form-item>
                                            </td>
                                            <td
                                                class="text-left text-truncate"
                                                :title="
                                                    itemData.enabled
                                                        ? t('Avtive')
                                                        : t('Inactive')
                                                ">
                                                <el-form-item
                                                    class="text-nowrap pt-3">
                                                    <el-button
                                                        size="default"
                                                        :type="
                                                            itemData.active
                                                                ? 'success'
                                                                : 'info'
                                                        "
                                                        @click="
                                                            toggleActive(
                                                                itemData,
                                                                !itemData.active
                                                            )
                                                        ">
                                                        {{
                                                            itemData.active
                                                                ? t('Active')
                                                                : t('Inactive')
                                                        }}
                                                    </el-button>
                                                </el-form-item>
                                            </td>

                                            <td
                                                class="text-left text-truncate"
                                                :title="itemData.createdAt">
                                                <span class="truncate-text">{{
                                                    formatDateTime(
                                                        itemData.createdAt
                                                    )
                                                }}</span>
                                            </td>
                                            <td class="text-left">
                                                <el-dropdown
                                                    trigger="click"
                                                    class="px-1">
                                                    <el-button type="primary">
                                                        <el-icon
                                                            :size="15"
                                                            style="
                                                                vertical-align: middle;
                                                            ">
                                                            <More />
                                                        </el-icon>
                                                    </el-button>
                                                    <template #dropdown>
                                                        <el-dropdown-menu>
                                                            <table>
                                                                <tbody>
                                                                    <tr>
                                                                        <td>
                                                                            <el-button
                                                                                class="border-0 mx-1 my-1"
                                                                                size="small"
                                                                                :disabled="
                                                                                    !itemData
                                                                                "
                                                                                @click="
                                                                                    $emit(
                                                                                        'onChangeView',
                                                                                        {
                                                                                            viewName:
                                                                                                'EditConnection',
                                                                                            data: itemData,
                                                                                        }
                                                                                    )
                                                                                ">
                                                                                <el-icon
                                                                                    :size="
                                                                                        15
                                                                                    "
                                                                                    style="
                                                                                        vertical-align: middle;
                                                                                    ">
                                                                                    <Edit />
                                                                                </el-icon>
                                                                                <span
                                                                                    class="ml-1"
                                                                                    >{{
                                                                                        t(
                                                                                            'Edit Connection'
                                                                                        )
                                                                                    }}</span
                                                                                >
                                                                            </el-button>
                                                                        </td>
                                                                    </tr>
                                                                    <tr>
                                                                        <td>
                                                                            <el-button
                                                                                class="border-0 ml-1"
                                                                                size="small"
                                                                                :disabled="
                                                                                    !itemData
                                                                                "
                                                                                @click="
                                                                                    deleteConfig(
                                                                                        itemData.id
                                                                                    )
                                                                                ">
                                                                                <el-icon
                                                                                    :size="
                                                                                        15
                                                                                    "
                                                                                    class="text-danger"
                                                                                    style="
                                                                                        vertical-align: middle;
                                                                                    ">
                                                                                    <Delete />
                                                                                </el-icon>
                                                                                <span
                                                                                    class="ml-1 mr-1"
                                                                                    >{{
                                                                                        t(
                                                                                            'Delete Config'
                                                                                        )
                                                                                    }}</span
                                                                                >
                                                                            </el-button>
                                                                        </td>
                                                                    </tr>
                                                                </tbody>
                                                            </table>
                                                        </el-dropdown-menu>
                                                    </template>
                                                </el-dropdown>
                                            </td>
                                        </tr>
                                    </tbody>
                                    <tbody v-else>
                                        <tr>
                                            <td
                                                class="text-center py-5"
                                                colspan="11">
                                                <strong>{{
                                                    t('There are no item')
                                                }}</strong>
                                            </td>
                                        </tr>
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- pagination -->
        <div class="mt-2 d-flex justify-content-end">
            <el-pagination
                v-model:current-page="pagePagination.currentPage"
                v-model:page-size="pagePagination.pageSize"
                :page-sizes="[10, 15, 25, 50, 100]"
                layout="total, sizes, prev, pager, next, jumper"
                :total="pagePagination.totalItems"
                @update:page-size="handleSizeChange"
                @update:current-page="handleCurrentChange" />
        </div>
    </div>
</template>
