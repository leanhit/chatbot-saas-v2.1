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
              Cấu hình mô hình AI, RAG Knowledge Base, Tool Calling & Chiến lược Smart Fallback
            </p>
          </div>
        </div>
      </div>
      <div class="flex items-center space-x-3">
        <button
          @click="loadConfig"
          :disabled="loading"
          class="inline-flex items-center px-3.5 py-2 bg-gray-100 dark:bg-gray-700 hover:bg-gray-200 dark:hover:bg-gray-600 text-gray-700 dark:text-gray-200 rounded-lg transition-colors text-sm font-medium"
        >
          <Icon icon="mdi:refresh" class="mr-1.5 text-lg" :class="{ 'animate-spin': loading }" />
          Làm mới
        </button>
        <button
          @click="saveConfig"
          :disabled="loading"
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
      <div class="bg-white dark:bg-gray-800 p-6 rounded-xl border border-gray-200 dark:border-gray-700 shadow-sm">
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
      <div class="bg-white dark:bg-gray-800 p-6 rounded-xl border border-gray-200 dark:border-gray-700 shadow-sm">
        <h2 class="font-semibold text-lg text-gray-900 dark:text-white mb-4 flex items-center gap-2">
          <Icon icon="mdi:card-text-outline" class="text-indigo-500 text-xl" />
          {{ $t('penny.config.aiConfig') }}
        </h2>
        <div>
          <label class="text-sm font-medium text-gray-700 dark:text-gray-300 mb-1.5 block">{{ $t('penny.config.systemPrompt') }}</label>
          <textarea 
            v-model="config.systemPrompt" 
            class="w-full px-4 py-3 border border-gray-300 dark:border-gray-700 rounded-xl dark:bg-gray-900 dark:text-white font-sans text-sm focus:ring-2 focus:ring-indigo-500 focus:outline-none leading-relaxed" 
            rows="5" 
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
            :to="`/penny/bots/${activeBotId}/knowledge-base`" 
            class="inline-flex items-center text-xs font-medium text-indigo-600 dark:text-indigo-400 hover:underline"
          >
            <Icon icon="mdi:open-in-new" class="mr-1" /> Quản lý bài viết tri thức
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
import AiBotConfig from '../bots/components/AiBotConfig.vue';

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
      loading: false
    };
  },
  computed: {
    activeBotId() {
      return this.botId || this.$route?.params?.botId;
    }
  },
  mounted() {
    this.loadConfig();
  },
  methods: {
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
