<script lang="ts" src="@/scripts/takeover/components/list.ts"></script>

<template>
    <div class="flex-fill d-flex flex-column w-100 p-2" v-loading="isLoading">
        <div class="d-flex align-items-center justify-content-between mb-2">
            <div class="page-titles">
                <ol class="breadcrumb">
                    <li class="breadcrumb-item active">
                        <a href="javascript:void(0)">{{ viewSettings.title }}</a>
                    </li>
                </ol>
            </div>
            
            <div class="d-flex align-items-center">
                <span class="px-2">{{ t("Select Connection")}}</span>
                <el-select 
                    v-model="selectedConnectionId" 
                    :placeholder="t('Filter Connection')" 
                    size="default" 
                    clearable
                    @change="handleConnectionChange"
                    style="width: 200px; margin-right: 10px;"
                >
                    <el-option :label="t('All Connections')" value="" />
                    
                    <el-option
                        v-for="connection in connectionsList"
                        :key="connection.id"
                        :label="connection.botName || connection.botId"
                        :value="String(connection.id)"
                    />
                </el-select>
                <el-button size="default" type="primary" @click="refreshDataFn()">
                    <div>{{ t('Check') }}</div>
                </el-button>
            </div>
        </div>
        
        <div class="inbox-container">
            <div class="conversation-container flex-grow-1 d-flex">
                <div class="conversation-list-container border-end" style="width: 350px; min-width: 350px; display: flex; flex-direction: column;">
                    <div style="flex: 1; overflow: hidden; display: flex; flex-direction: column;">
                        <Conversations
                            ref="conversationsRef"
                            :filtered-conversations="filteredConversations"
                            :active-conversation-id="takeoverStore.activeConversationId"
                            :active-tab="activeTab"
                            :search-query="searchQuery"
                            :get-contact-name="getContactName"
                            :get-avatar="getAvatar"
                            :get-time-since="getTimeSince"
                            @select-conversation="selectConversation"
                            @delete-conversation="deleteConversation"
                            @refresh-conversations="refreshConversations"
                            @update:active-tab="handleTabChange"
                            @update:search-query="handleSearch"
                            @delete-conversations="deleteConversations"
                            
                            :connections-list="connectionsList"
                            :selected-connection-id="selectedConnectionId"
                        />
                    </div>
                    
                    <div class="p-2 border-top" v-if="totalItems > 0">
                        <el-pagination
                            v-model:current-page="currentPage"
                            :page-sizes="[10, 20, 50, 100]"
                            :page-size="pageSize"
                            layout="total, sizes, prev, pager, next"
                            :total="totalItems"
                            @current-change="handlePageChange"
                            @size-change="handleSizeChange"
                            small
                            class="mt-2"
                        />
                    </div>
                </div>

                <div class="flex-grow-1 d-flex flex-column" style="min-width: 0;">
                    <ChatBox
                        v-if="activeConversation"
                        :messages="messages"
                        :get-avatar="getAvatar"
                        :active-conversation="activeConversation"
                        :agent-avatar="getAvatar('agent')"
                        :get-contact-name="getContactName"
                        @send-message="sendMessage"
                        @delete-message="deleteMessage"
                        @delete-all-messages="deleteAllMessages"
                        @delete-conversation="deleteConversation"
                    />
                    
                    <div v-else class="d-flex align-items-center justify-content-center flex-grow-1">
                        <div class="text-muted">{{ t('Select a conversation') }}</div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<style scoped>
/* Container and Chat Panel/Sidebar Layout */
.inbox-container {
    height: calc(100vh - 120px); /* Adjust 120px based on your header height */
    max-height: calc(100vh - 120px);
    min-height: 500px; /* Minimum height to ensure usability */
    overflow: visible; /* Prevent scrolling on the main container */
    display: flex;
    flex-direction: row;
}

.card { 
    margin-bottom: 0;
    width: 100%;
    display: flex;
    flex-direction: column;
}

/* Ensure the conversations and chat box take full height */
:deep(.conversation-list) {
    height: 100%;
    overflow-y: auto;
}

:deep(.chat-container) {
    height: 100%;
    display: flex;
    flex-direction: column;
}
</style>