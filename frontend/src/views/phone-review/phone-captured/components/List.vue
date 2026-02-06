<script lang="ts" src="@/scripts/phone-review/phone-captured/components/list.ts"></script>

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
                                                style="width: 15%"
                                                class="text-nowrap">
                                                {{ t('Phone') }}
                                            </th>
                                            <th
                                                style="width: 10%"
                                                class="text-nowrap">
                                                {{ t('Created At') }}
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
                                                :title="itemData.phone">
                                                <span class="truncate-text">{{
                                                    itemData.phoneNumber
                                                }}</span>
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
