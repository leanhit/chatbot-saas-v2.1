<template>
  <div class="penny-bot-config">
    <!-- Header -->
    <div class="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 mb-6 pb-4 border-b border-gray-200 dark:border-gray-700">
      <div>
        <div class="flex items-center gap-3">
          <div class="p-2.5 bg-gradient-to-br from-indigo-500 to-purple-600 rounded-xl text-white shadow-md">
            <Icon icon="mdi:robot-confused-outline" class="text-2xl" />
          </div>
          <div>
            <h1 class="text-2xl font-bold text-gray-900 dark:text-white flex items-center gap-2">
              {{ $t('penny.config.title') }}
              <span class="px-2.5 py-0.5 text-xs font-semibold rounded-full bg-emerald-100 text-emerald-800 dark:bg-emerald-900/40 dark:text-emerald-300 border border-emerald-200 dark:border-emerald-700">
                PennyBot v2.1 Smart AI
              </span>
            </h1>
            <p class="text-sm text-gray-500 dark:text-gray-400 mt-0.5">
              {{ $t('penny.config.subtitle') }}
            </p>
          </div>
        </div>
      </div>
      <div class="flex items-center space-x-3">
        <!-- Bot Selector Dropdown -->
        <div v-if="availableBots.length > 0" class="flex items-center space-x-2">
          <select
            id="bot-selector-config"
            v-model="selectedBotId"
            @change="handleBotChange(selectedBotId)"
            class="px-3.5 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-800 text-gray-900 dark:text-white text-sm font-medium focus:ring-2 focus:ring-primary focus:outline-none shadow-sm"
          >
            <option value="" disabled>{{ $t('penny.selectPennyBotPlaceholder') || '-- Chọn Penny Bot --' }}</option>
            <option
              v-for="bot in availableBots"
              :key="bot.id || bot.botId"
              :value="bot.id || bot.botId"
            >
              {{ bot.botName }} - {{ getBotTypeDisplayName(bot.botType) }}
            </option>
          </select>
        </div>
        <button
          id="btn-tour-guide-config"
          @click="startTour"
          class="inline-flex items-center px-3.5 py-2 border border-gray-300 dark:border-gray-600 text-gray-700 dark:text-gray-200 bg-white dark:bg-gray-800 rounded-lg hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors shadow-sm text-sm font-medium"
        >
          <Icon icon="mdi:help-circle-outline" class="mr-1.5 text-indigo-500 text-lg" />
          {{ $t('penny.guide') || 'Hướng dẫn' }}
        </button>
        <button
          @click="loadConfig"
          :disabled="loading || !activeBotId"
          class="inline-flex items-center px-3.5 py-2 bg-gray-100 dark:bg-gray-700 hover:bg-gray-200 dark:hover:bg-gray-600 text-gray-700 dark:text-gray-200 rounded-lg transition-colors text-sm font-medium"
        >
          <Icon icon="mdi:refresh" class="mr-1.5 text-lg" :class="{ 'animate-spin': loading }" />
          {{ $t('penny.config.refresh') || 'Làm mới' }}
        </button>
        <button
          @click="saveConfig"
          :disabled="loading || !activeBotId"
          class="inline-flex items-center px-5 py-2 bg-gradient-to-r from-indigo-600 to-purple-600 text-white rounded-lg hover:from-indigo-700 hover:to-purple-700 shadow-md transition-all disabled:opacity-50 text-sm font-medium"
        >
          <Icon v-if="loading" icon="mdi:loading" class="animate-spin mr-2 text-lg" />
          <Icon v-else icon="mdi:content-save-check-outline" class="mr-2 text-lg" />
          {{ $t('penny.config.saveChanges') }}
        </button>
      </div>
    </div>

    <div class="space-y-6">
      <!-- 1. Smart Fallback & Routing Strategy -->
      <div class="bg-white dark:bg-gray-800 p-6 rounded-xl border border-gray-200 dark:border-gray-700 shadow-sm relative overflow-hidden">
        <div class="absolute -right-10 -top-10 w-36 h-36 bg-gradient-to-br from-indigo-500/10 to-purple-500/10 rounded-full blur-2xl pointer-events-none"></div>
        <div class="flex items-center justify-between mb-4">
          <h2 class="font-semibold text-lg text-gray-900 dark:text-white flex items-center gap-2">
            <Icon icon="mdi:routes-clock" class="text-indigo-500 text-xl" />
            {{ $t('penny.config.routingStrategyTitle') }}
          </h2>
          <span class="text-xs font-mono px-2.5 py-1 bg-indigo-50 dark:bg-indigo-900/30 text-indigo-600 dark:text-indigo-400 rounded-md border border-indigo-200 dark:border-indigo-800">
            0.01s Rule Engine + LLM Fallback
          </span>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
          <!-- Hybrid Mode -->
          <label 
            class="relative p-4 rounded-xl border-2 cursor-pointer transition-all flex flex-col justify-between"
            :class="config.routingStrategy === 'hybrid' 
              ? 'border-indigo-500 bg-indigo-50/40 dark:bg-indigo-950/30 shadow-sm' 
              : 'border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800 hover:border-gray-300 dark:hover:border-gray-600'"
          >
            <input type="radio" v-model="config.routingStrategy" value="hybrid" class="sr-only" />
            <div>
              <div class="flex items-center justify-between mb-2">
                <span class="font-semibold text-gray-900 dark:text-white text-sm flex items-center gap-1.5">
                  <Icon icon="mdi:lightning-bolt" class="text-amber-500" />
                  {{ $t('penny.config.strategyHybrid') }}
                </span>
                <Icon v-if="config.routingStrategy === 'hybrid'" icon="mdi:check-circle" class="text-indigo-600 dark:text-indigo-400 text-lg" />
              </div>
              <p class="text-xs text-gray-600 dark:text-gray-300">
                {{ $t('penny.config.strategyHybridDesc') }}
              </p>
            </div>
            <div class="mt-3 pt-2 border-t border-gray-200/50 dark:border-gray-700/50 flex items-center justify-between text-[11px] text-gray-500">
              <span>Tốc độ: <strong>0.01s (Rule) / 1.2s (AI)</strong></span>
              <span class="text-emerald-600 dark:text-emerald-400 font-medium">Khuyên dùng</span>
            </div>
          </label>

          <!-- LLM Primary -->
          <label 
            class="relative p-4 rounded-xl border-2 cursor-pointer transition-all flex flex-col justify-between"
            :class="config.routingStrategy === 'llm_only' 
              ? 'border-indigo-500 bg-indigo-50/40 dark:bg-indigo-950/30 shadow-sm' 
              : 'border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800 hover:border-gray-300 dark:hover:border-gray-600'"
          >
            <input type="radio" v-model="config.routingStrategy" value="llm_only" class="sr-only" />
            <div>
              <div class="flex items-center justify-between mb-2">
                <span class="font-semibold text-gray-900 dark:text-white text-sm flex items-center gap-1.5">
                  <Icon icon="mdi:brain" class="text-purple-500" />
                  {{ $t('penny.config.strategyLlmOnly') }}
                </span>
                <Icon v-if="config.routingStrategy === 'llm_only'" icon="mdi:check-circle" class="text-indigo-600 dark:text-indigo-400 text-lg" />
              </div>
              <p class="text-xs text-gray-600 dark:text-gray-300">
                {{ $t('penny.config.strategyLlmOnlyDesc') }}
              </p>
            </div>
            <div class="mt-3 pt-2 border-t border-gray-200/50 dark:border-gray-700/50 flex items-center justify-between text-[11px] text-gray-500">
              <span>Tốc độ: <strong>1.0s - 2.5s</strong></span>
              <span class="text-purple-600 dark:text-purple-400 font-medium">Linh hoạt cao</span>
            </div>
          </label>

          <!-- Rule Only -->
          <label 
            class="relative p-4 rounded-xl border-2 cursor-pointer transition-all flex flex-col justify-between"
            :class="config.routingStrategy === 'rule_only' 
              ? 'border-indigo-500 bg-indigo-50/40 dark:bg-indigo-950/30 shadow-sm' 
              : 'border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800 hover:border-gray-300 dark:hover:border-gray-600'"
          >
            <input type="radio" v-model="config.routingStrategy" value="rule_only" class="sr-only" />
            <div>
              <div class="flex items-center justify-between mb-2">
                <span class="font-semibold text-gray-900 dark:text-white text-sm flex items-center gap-1.5">
                  <Icon icon="mdi:format-list-checks" class="text-blue-500" />
                  {{ $t('penny.config.strategyRuleOnly') }}
                </span>
                <Icon v-if="config.routingStrategy === 'rule_only'" icon="mdi:check-circle" class="text-indigo-600 dark:text-indigo-400 text-lg" />
              </div>
              <p class="text-xs text-gray-600 dark:text-gray-300">
                {{ $t('penny.config.strategyRuleOnlyDesc') }}
              </p>
            </div>
            <div class="mt-3 pt-2 border-t border-gray-200/50 dark:border-gray-700/50 flex items-center justify-between text-[11px] text-gray-500">
              <span>Tốc độ: <strong>0.01s (Cố định)</strong></span>
              <span class="text-gray-500 font-medium">Tiết kiệm API</span>
            </div>
          </label>
        </div>
      </div>

      <!-- 2. Tool Calling & Tra cứu Đơn hàng Real-time -->
      <div class="bg-white dark:bg-gray-800 p-6 rounded-xl border border-gray-200 dark:border-gray-700 shadow-sm">
        <div class="flex items-center justify-between mb-4">
          <h2 class="font-semibold text-lg text-gray-900 dark:text-white flex items-center gap-2">
            <Icon icon="mdi:package-variant-closed-check" class="text-emerald-500 text-xl" />
            {{ $t('penny.config.toolCallingTitle') }}
          </h2>
          <span class="px-2.5 py-1 text-xs font-medium rounded-full bg-emerald-100 text-emerald-800 dark:bg-emerald-900/40 dark:text-emerald-300">
            Realtime DB Integration
          </span>
        </div>

        <div class="space-y-4">
          <div class="flex items-start">
            <div class="flex items-center h-5">
              <input v-model="config.orderLookupEnabled" type="checkbox" id="order-lookup-check" class="focus:ring-indigo-500 h-4 w-4 text-indigo-600 border-gray-300 dark:border-gray-700 rounded dark:bg-gray-900" />
            </div>
            <div class="ml-3 text-sm">
              <label for="order-lookup-check" class="font-semibold text-gray-900 dark:text-white cursor-pointer">{{ $t('penny.config.enableOrderLookup') }}</label>
              <p class="text-xs text-gray-500 dark:text-gray-400 mt-0.5">{{ $t('penny.config.orderLookupHelp') }}</p>
            </div>
          </div>

          <div v-if="config.orderLookupEnabled" class="space-y-4 pt-4 border-t border-gray-100 dark:border-gray-700">
            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label class="text-sm font-medium text-gray-700 dark:text-gray-300 mb-1.5 block">
                  {{ $t('penny.config.orderLookupPattern') }}
                </label>
                <input 
                  v-model="config.orderLookupPattern" 
                  type="text" 
                  class="w-full px-3.5 py-2 border border-gray-300 dark:border-gray-700 rounded-lg dark:bg-gray-900 dark:text-white font-mono text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500" 
                  placeholder="(GH|DH|DON|ORDER)[A-Z0-9]{4,12}"
                />
                <small class="text-xs text-gray-500 dark:text-gray-400 mt-1 block">Tự động bắt các mã đơn hàng có tiền tố GH, DH, DON, ORDER...</small>
              </div>

              <!-- Interactive Order Lookup Sandbox Test -->
              <div class="bg-gray-50 dark:bg-gray-900/60 p-4 rounded-xl border border-gray-200 dark:border-gray-700">
                <label class="text-xs font-semibold text-gray-700 dark:text-gray-300 uppercase tracking-wider mb-2 block flex items-center gap-1.5">
                  <Icon icon="mdi:flask-outline" class="text-amber-500" />
                  Thử nghiệm tra cứu mã đơn hàng
                </label>
                <div class="flex gap-2">
                  <input 
                    v-model="testOrderCode" 
                    type="text" 
                    class="flex-1 px-3 py-1.5 text-sm border border-gray-300 dark:border-gray-700 rounded-md dark:bg-gray-800 dark:text-white font-mono" 
                    :placeholder="$t('penny.config.testOrderCodePlaceholder')"
                    @keyup.enter="handleTestOrderLookup"
                  />
                  <button 
                    @click="handleTestOrderLookup" 
                    :disabled="testingOrder"
                    class="px-3.5 py-1.5 bg-emerald-600 hover:bg-emerald-700 text-white rounded-md text-sm font-medium transition-colors inline-flex items-center gap-1"
                  >
                    <Icon v-if="testingOrder" icon="mdi:loading" class="animate-spin" />
                    <Icon v-else icon="mdi:magnify" />
                    {{ $t('penny.config.testOrderButton') }}
                  </button>
                </div>
                <!-- Test Result Display -->
                <div v-if="testOrderResult" class="mt-3 p-2.5 rounded bg-white dark:bg-gray-800 border border-emerald-200 dark:border-emerald-800/50 text-xs font-mono text-gray-800 dark:text-gray-200">
                  <div class="font-semibold text-emerald-600 dark:text-emerald-400 mb-1 flex items-center gap-1">
                    <Icon icon="mdi:check-circle-outline" /> Kết quả tra cứu API:
                  </div>
                  <pre class="whitespace-pre-wrap leading-relaxed">{{ testOrderResult }}</pre>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 3. Basic Info & Context Window -->
      <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
        <!-- Basic Info -->
        <div class="bg-white dark:bg-gray-800 p-6 rounded-xl border border-gray-200 dark:border-gray-700 shadow-sm space-y-4">
          <h2 class="font-semibold text-lg text-gray-900 dark:text-white mb-2 flex items-center gap-2">
            <Icon icon="mdi:information-outline" class="text-blue-500 text-xl" />
            {{ $t('penny.config.basicInfo') }}
          </h2>
          <div>
            <label class="text-sm font-medium text-gray-700 dark:text-gray-300 mb-1 block">{{ $t('penny.config.botName') }}</label>
            <input v-model="config.botName" type="text" class="w-full px-3.5 py-2 border border-gray-300 dark:border-gray-700 rounded-lg dark:bg-gray-900 dark:text-white disabled:opacity-60 disabled:bg-gray-100 dark:disabled:bg-gray-850 text-sm font-medium" disabled />
          </div>
          <div>
            <label class="text-sm font-medium text-gray-700 dark:text-gray-300 mb-1 block">{{ $t('penny.config.businessName') }}</label>
            <input v-model="config.businessName" type="text" class="w-full px-3.5 py-2 border border-gray-300 dark:border-gray-700 rounded-lg dark:bg-gray-900 dark:text-white text-sm focus:ring-2 focus:ring-indigo-500 focus:outline-none" :placeholder="$t('penny.config.businessNamePlaceholder')" />
          </div>
          <div>
            <label class="text-sm font-medium text-gray-700 dark:text-gray-300 mb-1 block">{{ $t('penny.config.businessDescription') }}</label>
            <textarea v-model="config.businessDescription" class="w-full px-3.5 py-2 border border-gray-300 dark:border-gray-700 rounded-lg dark:bg-gray-900 dark:text-white text-sm focus:ring-2 focus:ring-indigo-500 focus:outline-none" rows="3" :placeholder="$t('penny.config.businessDescriptionPlaceholder')"></textarea>
          </div>
        </div>

        <!-- Conversation History Window -->
        <div class="bg-white dark:bg-gray-800 p-6 rounded-xl border border-gray-200 dark:border-gray-700 shadow-sm space-y-4">
          <h2 class="font-semibold text-lg text-gray-900 dark:text-white mb-2 flex items-center gap-2">
            <Icon icon="mdi:history" class="text-amber-500 text-xl" />
            {{ $t('penny.config.contextWindowTitle') }}
          </h2>
          <div>
            <label class="text-sm font-medium text-gray-700 dark:text-gray-300 mb-1 block">
              {{ $t('penny.config.historyWindowSize') }}
            </label>
            <select v-model.number="config.historyWindowSize" class="w-full px-3.5 py-2 border border-gray-300 dark:border-gray-700 rounded-lg dark:bg-gray-900 dark:text-white text-sm focus:ring-2 focus:ring-indigo-500 focus:outline-none">
              <option :value="3">3 tin nhắn gần nhất (Tốc độ tối ưu)</option>
              <option :value="5">5 tin nhắn gần nhất (Cân bằng bối cảnh - Khuyên dùng)</option>
              <option :value="10">10 tin nhắn gần nhất (Ngữ cảnh sâu)</option>
            </select>
            <small class="text-xs text-gray-500 dark:text-gray-400 mt-1.5 block">
              {{ $t('penny.config.historyWindowSizeHelp') }}
            </small>
          </div>
          <div>
            <label class="text-sm font-medium text-gray-700 dark:text-gray-300 mb-1 block">{{ $t('penny.config.confidenceThreshold') }}</label>
            <div class="flex items-center gap-3">
              <input v-model.number="config.confidenceThreshold" type="range" step="0.05" min="0.1" max="0.9" class="flex-1 accent-indigo-600" />
              <span class="px-2.5 py-1 bg-indigo-50 dark:bg-indigo-900/40 text-indigo-700 dark:text-indigo-300 rounded font-mono text-sm font-bold">
                {{ (config.confidenceThreshold * 100).toFixed(0) }}%
              </span>
            </div>
            <small class="text-xs text-gray-500 dark:text-gray-400 mt-1 block">{{ $t('penny.config.confidenceThresholdHelp') }}</small>
          </div>
        </div>
      </div>

      <!-- 4. AI Provider & Model Configuration -->
      <div id="ai-model-strategy-box" class="bg-white dark:bg-gray-800 p-6 rounded-xl border border-gray-200 dark:border-gray-700 shadow-sm">
        <h2 class="font-semibold text-lg text-gray-900 dark:text-white mb-4 flex items-center gap-2">
          <Icon icon="mdi:robot" class="text-purple-500 text-xl" />
          {{ $t('penny.aiConfig.title') }}
        </h2>
        <AiBotConfig 
          :bot-id="activeBotId" 
          :initial-config="aiConfig"
          @saved="handleAiConfigSaved"
        />
      </div>

      <!-- 5. System Prompt & Personality -->
      <div id="system-prompt-box" class="bg-white dark:bg-gray-800 p-6 rounded-xl border border-gray-200 dark:border-gray-700 shadow-sm">
        <div class="flex items-center justify-between mb-4">
          <h2 class="font-semibold text-lg text-gray-900 dark:text-white flex items-center gap-2">
            <Icon icon="mdi:card-text-outline" class="text-indigo-500 text-xl" />
            {{ $t('penny.config.aiConfig') }}
          </h2>
          <span class="text-xs text-gray-500 dark:text-gray-400">
            {{ $t('penny.config.personalityNotice') || 'Định hình cá tính & chỉ thị cốt lõi cho AI' }}
          </span>
        </div>

        <!-- Quick Prompt Templates Selection -->
        <div id="prompt-templates-container" class="mb-5 p-4 bg-gray-50 dark:bg-gray-900/50 rounded-xl border border-gray-200 dark:border-gray-700">
          <label class="text-xs font-semibold uppercase tracking-wider text-gray-600 dark:text-gray-400 mb-2.5 flex items-center gap-1.5">
            <Icon icon="mdi:lightning-bolt" class="text-amber-500" />
            ⚡ {{ $t('penny.config.quickPromptTemplates') || 'Nạp Mẫu Prompt Nhanh Theo Ngành Nghề:' }}:
          </label>
          <div class="flex flex-wrap gap-2">
            <button
              v-for="tpl in promptTemplates"
              :key="tpl.id"
              type="button"
              @click="applyPromptTemplate(tpl)"
              class="inline-flex items-center px-3 py-1.5 text-xs font-medium rounded-lg border border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800 hover:bg-indigo-50 dark:hover:bg-indigo-950/40 hover:border-indigo-300 dark:hover:border-indigo-600 text-gray-700 dark:text-gray-200 transition-all shadow-sm group"
            >
              <span class="p-1 rounded-md bg-gradient-to-r text-white mr-2" :class="tpl.color">
                <Icon :icon="tpl.icon" class="text-xs" />
              </span>
              {{ tpl.name }}
            </button>
          </div>
          <div v-if="appliedTemplateNotice" class="mt-3 text-xs font-medium text-emerald-600 dark:text-emerald-400 flex items-center gap-1">
            <Icon icon="mdi:check-circle" /> {{ $t('penny.config.appliedPromptNotice') || 'Đã áp dụng mẫu prompt' }}: <strong>{{ appliedTemplateNotice }}</strong>. {{ $t('penny.config.customizableNotice') || 'Bạn có thể tự do tùy chỉnh thêm bên dưới!' }}
          </div>
        </div>

        <div>
          <label class="text-sm font-medium text-gray-700 dark:text-gray-300 mb-1.5 block">{{ $t('penny.config.systemPrompt') }}</label>
          <textarea 
            v-model="config.systemPrompt" 
            class="w-full px-4 py-3 border border-gray-300 dark:border-gray-700 rounded-xl dark:bg-gray-900 dark:text-white font-sans text-sm focus:ring-2 focus:ring-indigo-500 focus:outline-none leading-relaxed" 
            rows="6" 
            :placeholder="$t('penny.config.systemPromptPlaceholder')"
          ></textarea>
          <small class="text-xs text-gray-500 dark:text-gray-400 mt-1.5 block">{{ $t('penny.config.systemPromptHelp') }}</small>
        </div>
      </div>

      <!-- 5. RAG (Knowledge Base Vector Search) -->
      <div class="bg-white dark:bg-gray-800 p-6 rounded-xl border border-gray-200 dark:border-gray-700 shadow-sm">
        <div class="flex items-center justify-between mb-4">
          <h2 class="font-semibold text-lg text-gray-900 dark:text-white flex items-center gap-2">
            <Icon icon="mdi:database-search-outline" class="text-purple-500 text-xl" />
            {{ $t('penny.config.ragTitle') }}
          </h2>
          <router-link 
            v-if="activeBotId"
            :to="`/penny/bots/${activeBotId}/knowledge-base`" 
            class="inline-flex items-center text-xs font-medium text-indigo-600 dark:text-indigo-400 hover:underline"
          >
            <Icon icon="mdi:open-in-new" class="mr-1" /> {{ $t('penny.config.manageKnowledgeArticles') || 'Quản lý bài viết tri thức' }}
          </router-link>
        </div>

        <div class="space-y-4">
          <div class="flex items-start">
            <div class="flex items-center h-5">
              <input v-model="config.ragEnabled" type="checkbox" id="rag-enabled-check" class="focus:ring-indigo-500 h-4 w-4 text-indigo-600 border-gray-300 dark:border-gray-700 rounded dark:bg-gray-900" />
            </div>
            <div class="ml-3 text-sm">
              <label for="rag-enabled-check" class="font-semibold text-gray-900 dark:text-white cursor-pointer">{{ $t('penny.config.enableRag') }}</label>
              <p class="text-xs text-gray-500 dark:text-gray-400 mt-0.5">{{ $t('penny.config.ragHelp') }}</p>
            </div>
          </div>
          
          <div v-if="config.ragEnabled" class="grid grid-cols-1 md:grid-cols-3 gap-4 pt-4 border-t border-gray-100 dark:border-gray-700">
            <div>
              <label class="text-sm font-medium text-gray-700 dark:text-gray-300 mb-1 block">{{ $t('penny.config.topK') }}</label>
              <input v-model.number="config.ragTopK" type="number" class="w-full px-3.5 py-2 border border-gray-300 dark:border-gray-700 rounded-lg dark:bg-gray-900 dark:text-white text-sm focus:ring-2 focus:ring-indigo-500 focus:outline-none" min="1" max="10" />
              <small class="text-xs text-gray-500 dark:text-gray-400 mt-1 block">{{ $t('penny.config.topKHelp') }}</small>
            </div>
            <div>
              <label class="text-sm font-medium text-gray-700 dark:text-gray-300 mb-1 block">{{ $t('penny.config.similarityThreshold') }}</label>
              <input v-model.number="config.ragSimilarityThreshold" type="number" class="w-full px-3.5 py-2 border border-gray-300 dark:border-gray-700 rounded-lg dark:bg-gray-900 dark:text-white text-sm focus:ring-2 focus:ring-indigo-500 focus:outline-none" step="0.05" min="0.1" max="1" />
              <small class="text-xs text-gray-500 dark:text-gray-400 mt-1 block">{{ $t('penny.config.similarityThresholdHelp') }}</small>
            </div>
            <div>
              <label class="text-sm font-medium text-gray-700 dark:text-gray-300 mb-1 block">{{ $t('penny.config.maxContextTokens') }}</label>
              <input v-model.number="config.ragMaxContextTokens" type="number" class="w-full px-3.5 py-2 border border-gray-300 dark:border-gray-700 rounded-lg dark:bg-gray-900 dark:text-white text-sm focus:ring-2 focus:ring-indigo-500 focus:outline-none" min="100" max="4000" />
              <small class="text-xs text-gray-500 dark:text-gray-400 mt-1 block">{{ $t('penny.config.maxContextTokensHelp') }}</small>
            </div>
          </div>
        </div>
      </div>

      <!-- 6. AI Providers & Rate Limiting -->
      <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
        <!-- Provider Config -->
        <div class="bg-white dark:bg-gray-800 p-6 rounded-xl border border-gray-200 dark:border-gray-700 shadow-sm space-y-4">
          <h2 class="font-semibold text-lg text-gray-900 dark:text-white mb-2 flex items-center gap-2">
            <Icon icon="mdi:server-network" class="text-emerald-500 text-xl" />
            {{ $t('penny.config.aiProviderConfig') }}
          </h2>

          <!-- OpenAI GPT -->
          <div class="p-4 border border-gray-200 dark:border-gray-700 rounded-xl bg-gray-50/60 dark:bg-gray-900/40">
            <div class="flex items-center justify-between mb-3">
              <span class="font-semibold text-gray-900 dark:text-white text-sm flex items-center gap-2">
                <Icon icon="mdi:openai" class="text-emerald-600 text-lg" /> OpenAI (GPT)
              </span>
              <input v-model="config.gptEnabled" type="checkbox" class="focus:ring-indigo-500 h-4 w-4 text-indigo-600 border-gray-300 rounded dark:bg-gray-900" />
            </div>
            <div v-if="config.gptEnabled" class="space-y-3 pt-3 border-t border-gray-200 dark:border-gray-700">
              <div class="grid grid-cols-2 gap-3">
                <div>
                  <label class="text-xs font-medium text-gray-500 dark:text-gray-400 mb-1 block">{{ $t('penny.config.model') }}</label>
                  <select v-model="config.gptModel" class="w-full px-2.5 py-1.5 border border-gray-300 dark:border-gray-700 rounded-md dark:bg-gray-900 dark:text-white text-xs">
                    <option value="gpt-4o-mini">GPT-4o Mini (Nhanh & Tiết kiệm)</option>
                    <option value="gpt-4o">GPT-4o (Thông minh cao)</option>
                    <option value="gpt-3.5-turbo">GPT-3.5 Turbo</option>
                  </select>
                </div>
                <div>
                  <label class="text-xs font-medium text-gray-500 dark:text-gray-400 mb-1 block">{{ $t('penny.config.temperature') }}</label>
                  <input v-model.number="config.gptTemperature" type="number" step="0.1" min="0" max="2" class="w-full px-2.5 py-1.5 border border-gray-300 dark:border-gray-700 rounded-md dark:bg-gray-900 dark:text-white text-xs" />
                </div>
              </div>
            </div>
          </div>

          <!-- Anthropic Claude -->
          <div class="p-4 border border-gray-200 dark:border-gray-700 rounded-xl bg-gray-50/60 dark:bg-gray-900/40">
            <div class="flex items-center justify-between mb-3">
              <span class="font-semibold text-gray-900 dark:text-white text-sm flex items-center gap-2">
                <Icon icon="mdi:brain" class="text-amber-600 text-lg" /> Anthropic (Claude)
              </span>
              <input v-model="config.claudeEnabled" type="checkbox" class="focus:ring-indigo-500 h-4 w-4 text-indigo-600 border-gray-300 rounded dark:bg-gray-900" />
            </div>
            <div v-if="config.claudeEnabled" class="space-y-3 pt-3 border-t border-gray-200 dark:border-gray-700">
              <div class="grid grid-cols-2 gap-3">
                <div>
                  <label class="text-xs font-medium text-gray-500 dark:text-gray-400 mb-1 block">{{ $t('penny.config.model') }}</label>
                  <select v-model="config.claudeModel" class="w-full px-2.5 py-1.5 border border-gray-300 dark:border-gray-700 rounded-md dark:bg-gray-900 dark:text-white text-xs">
                    <option value="claude-3-haiku-20240307">Claude 3 Haiku</option>
                    <option value="claude-3-sonnet-20240229">Claude 3 Sonnet</option>
                  </select>
                </div>
                <div>
                  <label class="text-xs font-medium text-gray-500 dark:text-gray-400 mb-1 block">{{ $t('penny.config.maxTokens') }}</label>
                  <input v-model.number="config.claudeMaxTokens" type="number" min="100" max="4000" class="w-full px-2.5 py-1.5 border border-gray-300 dark:border-gray-700 rounded-md dark:bg-gray-900 dark:text-white text-xs" />
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Rate Limiting -->
        <div class="bg-white dark:bg-gray-800 p-6 rounded-xl border border-gray-200 dark:border-gray-700 shadow-sm space-y-4">
          <h2 class="font-semibold text-lg text-gray-900 dark:text-white mb-2 flex items-center gap-2">
            <Icon icon="mdi:speedometer" class="text-rose-500 text-xl" />
            {{ $t('penny.config.rateLimiting') }}
          </h2>
          <div class="flex items-start">
            <div class="flex items-center h-5">
              <input v-model="config.rateLimitEnabled" type="checkbox" id="rate-limit-check" class="focus:ring-indigo-500 h-4 w-4 text-indigo-600 border-gray-300 rounded dark:bg-gray-900" />
            </div>
            <div class="ml-3 text-sm">
              <label for="rate-limit-check" class="font-semibold text-gray-900 dark:text-white cursor-pointer">{{ $t('penny.config.enableRateLimiting') }}</label>
              <p class="text-xs text-gray-500 dark:text-gray-400 mt-0.5">{{ $t('penny.config.rateLimitingHelp') }}</p>
            </div>
          </div>

          <div v-if="config.rateLimitEnabled" class="space-y-4 pt-3 border-t border-gray-100 dark:border-gray-700">
            <div>
              <label class="text-sm font-medium text-gray-700 dark:text-gray-300 mb-1 block">{{ $t('penny.config.userMessagesPerMinute') }}</label>
              <input v-model.number="config.userMessagesPerMinute" type="number" min="1" max="1000" class="w-full px-3.5 py-2 border border-gray-300 dark:border-gray-700 rounded-lg dark:bg-gray-900 dark:text-white text-sm" />
            </div>
            <div>
              <label class="text-sm font-medium text-gray-700 dark:text-gray-300 mb-1 block">{{ $t('penny.config.tenantMessagesPerMinute') }}</label>
              <input v-model.number="config.tenantMessagesPerMinute" type="number" min="1" max="10000" class="w-full px-3.5 py-2 border border-gray-300 dark:border-gray-700 rounded-lg dark:bg-gray-900 dark:text-white text-sm" />
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { pennyApi } from '@/api/pennyApi';
import { Icon } from '@iconify/vue';
import { driver } from 'driver.js';
import 'driver.js/dist/driver.css';
import AiBotConfig from '../bots/components/AiBotConfig.vue';
import { usePennyBotStore } from '@/stores/pennyBotStore';

export default {
  name: 'PennyBotConfig',
  components: {
    Icon,
    AiBotConfig
  },
  props: {
    botId: {
      type: String,
      required: false
    }
  },
  data() {
    return {
      selectedBotId: null,
      aiConfig: {
        providerType: 'OPENAI',
        modelName: 'gpt-4o-mini',
        temperature: 0.7,
        personaStyle: 'PROFESSIONAL',
        customInstructions: '',
        greetingMessage: '',
        fallbackMessage: ''
      },
      config: {
        botName: '',
        businessName: '',
        businessDescription: '',
        systemPrompt: '',
        confidenceThreshold: 0.6,
        routingStrategy: 'hybrid',
        orderLookupEnabled: true,
        orderLookupPattern: '(GH|DH|DON|ORDER)[A-Z0-9]{4,12}',
        historyWindowSize: 5,
        ragEnabled: true,
        ragTopK: 3,
        ragSimilarityThreshold: 0.7,
        ragMaxContextTokens: 1500,
        gptEnabled: true,
        gptModel: 'gpt-4o-mini',
        gptMaxTokens: 800,
        gptTemperature: 0.7,
        claudeEnabled: false,
        claudeModel: 'claude-3-haiku-20240307',
        claudeMaxTokens: 800,
        rateLimitEnabled: true,
        userMessagesPerMinute: 60,
        tenantMessagesPerMinute: 1000
      },
      testOrderCode: 'GH12345',
      testOrderResult: null,
      testingOrder: false,
      loading: false,
      appliedTemplateNotice: null,
      promptTemplates: [
        {
          id: 'fashion',
          name: 'Shop Thời trang & Bán lẻ',
          icon: 'mdi:tshirt-crew-outline',
          color: 'from-pink-500 to-rose-500',
          prompt: `Bạn là trợ lý tư vấn bán hàng chuyên nghiệp của cửa hàng thời trang. Nhiệm vụ của bạn là:
1. Tư vấn size, chất liệu, màu sắc và phong cách phù hợp với nhu cầu của khách hàng.
2. Thân thiện, xưng hô 'em - anh/chị', trả lời ngắn gọn, lịch sự và hào hứng.
3. Khuyến khích khách hàng chốt đơn hoặc để lại SĐT/Địa chỉ để nhân viên chốt mẫu.
4. Nếu khách hỏi mã đơn hàng hoặc giá sản phẩm, hãy tra cứu thông tin chính xác từ hệ thống.`
        },
        {
          id: 'realestate',
          name: 'Bất động sản & Dự án',
          icon: 'mdi:home-city-outline',
          color: 'from-amber-500 to-orange-500',
          prompt: `Bạn là chuyên viên tư vấn đầu tư bất động sản cao cấp. Nhiệm vụ của bạn là:
1. Lịch sự, chuyên nghiệp, tạo niềm tin cao cho khách hàng.
2. Giới thiệu tổng quan vị trí, tiện ích, pháp lý và chính sách ưu đãi của dự án.
3. Khéo léo xin số điện thoại/Zalo của khách hàng để gửi file Brochure, bảng giá và sơ đồ căn hộ chi tiết.
4. Không tự ý báo giá sai ngoài bảng giá niêm yết.`
        },
        {
          id: 'software',
          name: 'Hỗ trợ Kỹ thuật & SaaS',
          icon: 'mdi:laptop',
          color: 'from-blue-500 to-indigo-500',
          prompt: `Bạn là kỹ sư hỗ trợ kỹ thuật và CSKH cho phần mềm. Nhiệm vụ của bạn là:
1. Hướng dẫn khách hàng từng bước cách khắc phục sự cố, cài đặt hoặc cấu hình tài khoản.
2. Trả lời rõ ràng, dễ hiểu, đánh số thứ tự từng bước (1, 2, 3...).
3. Nếu vấn đề vượt quá khả năng xử lý hoặc cần kiểm tra hệ thống sâu, hãy hướng dẫn khách gửi yêu cầu Hỗ trợ (Escalation) cho nhân viên kỹ thuật.`
        },
        {
          id: 'restaurant',
          name: 'Nhà hàng & Đặt bàn',
          icon: 'mdi:silverware-fork-knife',
          color: 'from-emerald-500 to-teal-500',
          prompt: `Bạn là lễ tân thông minh của nhà hàng. Nhiệm vụ của bạn là:
1. Chào đón khách hàng nồng nhiệt, tư vấn menu món ăn, combo ưu đãi và không gian tiệc.
2. Hướng dẫn khách hàng đặt bàn: Hỏi rõ Số lượng khách, Ngày/Giờ đến, và Số điện thoại liên hệ.
3. Ghi nhận các yêu cầu đặc biệt (bàn ngoài trời, trang trí sinh nhật, ăn chay...).`
        },
        {
          id: 'spa',
          name: 'Spa, Thẩm mỹ & Y tế',
          icon: 'mdi:spa-outline',
          color: 'from-purple-500 to-violet-500',
          prompt: `Bạn là tư vấn viên chăm sóc khách hàng tại Spa & Thẩm mỹ viện. Nhiệm vụ của bạn là:
1. Nhã nhặn, thấu hiểu, tư vấn các liệu trình chăm sóc da và thư giãn phù hợp.
2. Khuyên khách hàng đặt lịch hẹn khám/tư vấn trực tiếp để được chuyên gia soi da/thám khám.
3. Thu thập tên, SĐT và khung giờ rảnh của khách để xếp lịch hẹn.`
        }
      ]
    };
  },
  computed: {
    pennyBotStore() {
      return usePennyBotStore();
    },
    availableBots() {
      return this.pennyBotStore.pennyBots || [];
    },
    effectiveBotId() {
      const clean = (val) => {
        if (!val) return null;
        const s = typeof val === 'object' ? (val.id || val.botId || '') : String(val);
        const trimmed = String(s).trim();
        if (!trimmed || trimmed === 'undefined' || trimmed === 'null') return null;
        const uuidRegex = /^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$/;
        return uuidRegex.test(trimmed) ? trimmed : null;
      };

      return clean(this.botId) || clean(this.$route?.params?.botId) || clean(this.selectedBotId) || clean(this.pennyBotStore.currentBotId);
    },
    activeBotId() {
      return this.effectiveBotId;
    }
  },
  watch: {
    availableBots: {
      immediate: true,
      handler(bots) {
        if (bots && bots.length > 0) {
          const currentTarget = this.effectiveBotId;
          if (currentTarget && bots.some(b => (b.id || b.botId) === currentTarget)) {
            this.selectedBotId = currentTarget;
          } else if (!this.selectedBotId) {
            const firstValidBot = bots.find(b => (b.id || b.botId));
            if (firstValidBot) {
              const firstId = firstValidBot.id || firstValidBot.botId;
              this.selectedBotId = firstId;
              this.pennyBotStore.setCurrentBotId(firstId);
            }
          }
        }
      }
    },
    effectiveBotId: {
      immediate: true,
      handler(newBotId) {
        if (newBotId) {
          if (this.selectedBotId !== newBotId) {
            this.selectedBotId = newBotId;
          }
          this.pennyBotStore.setCurrentBotId(newBotId);
          this.loadConfig();
        }
      }
    }
  },
  async mounted() {
    if (this.availableBots.length === 0) {
      try {
        await this.pennyBotStore.fetchPennyBots();
      } catch (err) {
        console.error('Failed to fetch Penny bots:', err);
      }
    }
    if (this.activeBotId) {
      this.loadConfig();
    }
  },
  methods: {
    getBotTypeDisplayName(botType) {
      const names = {
        'GENERAL': 'General Purpose',
        'SUPPORT': 'Customer Support',
        'BUSINESS': 'Business & Sales',
        'BOTPRESS': 'Botpress Integration'
      };
      return names[botType] || botType || 'General';
    },
    handleBotChange(newBotId) {
      this.selectedBotId = newBotId;
      if (newBotId) {
        this.pennyBotStore.setCurrentBotId(newBotId);
      }
      this.loadConfig();
    },
    async loadConfig() {
      if (!this.activeBotId) {
        console.warn('No activeBotId available');
        return;
      }
      this.loading = true;
      try {
        const response = await pennyApi.getPennyBotById(this.activeBotId);
        const bot = response.data;
        
        this.config = {
          botName: bot.botName || 'Penny Assistant',
          businessName: bot.businessName || '',
          businessDescription: bot.businessDescription || '',
          systemPrompt: bot.systemPrompt || 'Bạn là trợ lý ảo Penny thông minh, nhiệt tình của cửa hàng.',
          confidenceThreshold: bot.confidenceThreshold || 0.6,
          routingStrategy: bot.routingStrategy || 'hybrid',
          orderLookupEnabled: bot.orderLookupEnabled !== false,
          orderLookupPattern: bot.orderLookupPattern || '(GH|DH|DON|ORDER)[A-Z0-9]{4,12}',
          historyWindowSize: bot.historyWindowSize || 5,
          ragEnabled: bot.ragEnabled !== false,
          ragTopK: bot.ragTopK || 3,
          ragSimilarityThreshold: bot.ragSimilarityThreshold || 0.7,
          ragMaxContextTokens: bot.ragMaxContextTokens || 1500,
          gptEnabled: bot.gptEnabled !== false,
          gptModel: bot.gptModel || 'gpt-4o-mini',
          gptMaxTokens: bot.gptMaxTokens || 800,
          gptTemperature: bot.gptTemperature || 0.7,
          claudeEnabled: !!bot.claudeEnabled,
          claudeModel: bot.claudeModel || 'claude-3-haiku-20240307',
          claudeMaxTokens: bot.claudeMaxTokens || 800,
          rateLimitEnabled: bot.rateLimitEnabled !== false,
          userMessagesPerMinute: bot.userMessagesPerMinute || 60,
          tenantMessagesPerMinute: bot.tenantMessagesPerMinute || 1000
        };
        
        // Load AI config
        this.aiConfig = {
          providerType: bot.providerType || 'OPENAI',
          modelName: bot.modelName || 'gpt-4o-mini',
          temperature: bot.temperature || 0.7,
          personaStyle: bot.personaStyle || 'PROFESSIONAL',
          customInstructions: bot.customInstructions || '',
          greetingMessage: bot.greetingMessage || '',
          fallbackMessage: bot.fallbackMessage || ''
        };
      } catch (error) {
        console.error('Error loading config:', error);
        if (this.$toast) {
          this.$toast.error('Failed to load bot configuration');
        }
      } finally {
        this.loading = false;
      }
    },
    
    handleAiConfigSaved(config) {
      this.aiConfig = { ...this.aiConfig, ...config };
    },
    
    async saveConfig() {
      if (!this.activeBotId) {
        if (this.$toast) this.$toast.error('Bot ID not found');
        return;
      }
      this.loading = true;
      try {
        const updateData = {
          businessName: this.config.businessName,
          businessDescription: this.config.businessDescription,
          systemPrompt: this.config.systemPrompt,
          confidenceThreshold: this.config.confidenceThreshold,
          routingStrategy: this.config.routingStrategy,
          orderLookupEnabled: this.config.orderLookupEnabled,
          orderLookupPattern: this.config.orderLookupPattern,
          historyWindowSize: this.config.historyWindowSize,
          ragEnabled: this.config.ragEnabled,
          ragTopK: this.config.ragTopK,
          ragSimilarityThreshold: this.config.ragSimilarityThreshold,
          ragMaxContextTokens: this.config.ragMaxContextTokens,
          gptEnabled: this.config.gptEnabled,
          gptModel: this.config.gptModel,
          gptMaxTokens: this.config.gptMaxTokens,
          gptTemperature: this.config.gptTemperature,
          claudeEnabled: this.config.claudeEnabled,
          claudeModel: this.config.claudeModel,
          rateLimitEnabled: this.config.rateLimitEnabled
        };
        
        await pennyApi.updatePennyBot(this.activeBotId, updateData);
        if (this.$toast) {
          this.$toast.success('Configuration saved successfully');
        }
      } catch (error) {
        console.error('Error saving config:', error);
        if (this.$toast) {
          this.$toast.error('Failed to save configuration');
        }
      } finally {
        this.loading = false;
      }
    },

    async handleTestOrderLookup() {
      if (!this.testOrderCode) return;
      if (!this.activeBotId) {
        this.testOrderResult = '[Lỗi]: Không tìm thấy Bot ID';
        return;
      }
      this.testingOrder = true;
      this.testOrderResult = null;
      try {
        // Send a test chat message containing the order code
        const res = await pennyApi.chatWithPennyBot(this.activeBotId, `Kiểm tra đơn hàng ${this.testOrderCode}`, true);
        const replyText = res.data?.response || res.data?.message || JSON.stringify(res.data, null, 2);
        this.testOrderResult = replyText;
      } catch (err) {
        this.testOrderResult = `[Lỗi tra cứu]: ${err.response?.data?.message || err.message}`;
      } finally {
        this.testingOrder = false;
      }
    },

    applyPromptTemplate(tpl) {
      this.config.systemPrompt = tpl.prompt;
      this.appliedTemplateNotice = tpl.name;
      setTimeout(() => {
        this.appliedTemplateNotice = null;
      }, 4000);
    },

    startTour() {
      const tourDriver = driver({
        showProgress: true,
        animate: true,
        allowClose: true,
        overlayColor: 'rgba(0, 0, 0, 0.75)',
        nextBtnText: 'Tiếp theo',
        prevBtnText: 'Quay lại',
        doneBtnText: 'Xong',
        steps: [
          {
            element: '#btn-tour-guide-config',
            popover: {
              title: 'Cấu hình Mô hình & AI Persona ⚙️',
              description: 'Nơi thiết lập trí tuệ nhân tạo, quy tắc ứng xử, RAG Tri thức & Tool Calling cho Penny Bot.',
              side: 'bottom',
              align: 'end'
            }
          },
          {
            element: '#bot-selector-config',
            popover: {
              title: 'Chọn Penny Bot 🤖',
              description: 'Chuyển đổi linh hoạt giữa các Bot để xem và điều chỉnh cấu hình tương ứng.',
              side: 'bottom',
              align: 'start'
            }
          },
          {
            element: '#prompt-templates-container',
            popover: {
              title: 'Gợi ý Mẫu Prompt Ngành Nghề 💡',
              description: 'Click nạp nhanh mẫu câu System Prompt được thiết kế chuẩn cho Shop Thời trang, Bất động sản, SaaS, Nhà hàng, Spa.',
              side: 'bottom',
              align: 'center'
            }
          },
          {
            element: '#system-prompt-box',
            popover: {
              title: 'System Prompt (Khung Persona) ✍️',
              description: 'Nơi quy định tính cách, vai trò, phạm vi trả lời và ngôn phong giao tiếp của Bot.',
              side: 'top',
              align: 'center'
            }
          },
          {
            element: '#ai-model-strategy-box',
            popover: {
              title: 'Chiến lược Mô hình & Provider 🧠',
              description: 'Lựa chọn giữa GPT-4o, Claude 3.5 Sonnet hoặc Gemini Pro kèm nhiệt độ sáng tạo (Temperature).',
              side: 'top',
              align: 'center'
            }
          }
        ]
      });
      tourDriver.drive();
    }
  }
};
</script>

<style scoped>
.penny-bot-config {
  width: 100%;
  padding: 24px;
}

.animate-spin {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}
</style>
