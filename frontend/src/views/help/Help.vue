<template>
  <div class="p-6">
    <div class="max-w-4xl mx-auto">
      <h1 class="text-3xl font-bold text-gray-900 dark:text-white mb-8">{{ $t('help.title') }}</h1>
      
      <!-- Quick Start Section -->
      <div class="bg-white dark:bg-gray-800 rounded-lg shadow-md p-6 mb-6">
        <h2 class="text-xl font-semibold text-gray-900 dark:text-white mb-4">
          <Icon icon="mdi:rocket-launch" class="mr-2" />
          {{ $t('help.quickStart.title') }}
        </h2>
        <div class="space-y-4 text-gray-700 dark:text-gray-300">
          <div class="border-l-4 border-blue-500 pl-4">
            <h3 class="font-medium">{{ $t('help.quickStart.steps.selectTenant.title') }}</h3>
            <p class="text-sm">{{ $t('help.quickStart.steps.selectTenant.description') }}</p>
          </div>
          <div class="border-l-4 border-green-500 pl-4">
            <h3 class="font-medium">{{ $t('help.quickStart.steps.dashboard.title') }}</h3>
            <p class="text-sm">{{ $t('help.quickStart.steps.dashboard.description') }}</p>
          </div>
          <div class="border-l-4 border-purple-500 pl-4">
            <h3 class="font-medium">{{ $t('help.quickStart.steps.pennyBots.title') }}</h3>
            <p class="text-sm">{{ $t('help.quickStart.steps.pennyBots.description') }}</p>
          </div>
        </div>
      </div>

      <!-- Features Guide -->
      <div class="bg-white dark:bg-gray-800 rounded-lg shadow-md p-6 mb-6">
        <h2 class="text-xl font-semibold text-gray-900 dark:text-white mb-4">
          <Icon icon="mdi:feature-search" class="mr-2" />
          {{ $t('help.features.title') }}
        </h2>
        <div class="grid md:grid-cols-2 gap-4">
          <div class="border rounded-lg p-4">
            <h3 class="font-medium text-gray-900 dark:text-white mb-2">
              <Icon icon="mdi:office-building" class="mr-2" />
              {{ $t('help.features.tenant.title') }}
            </h3>
            <p class="text-sm text-gray-600 dark:text-gray-400">
              {{ $t('help.features.tenant.description') }}
            </p>
          </div>
          <div class="border rounded-lg p-4">
            <h3 class="font-medium text-gray-900 dark:text-white mb-2">
              <Icon icon="mdi:robot" class="mr-2" />
              {{ $t('help.features.pennyBots.title') }}
            </h3>
            <p class="text-sm text-gray-600 dark:text-gray-400">
              {{ $t('help.features.pennyBots.description') }}
            </p>
          </div>
          <div class="border rounded-lg p-4">
            <h3 class="font-medium text-gray-900 dark:text-white mb-2">
              <Icon icon="mdi:message-text" class="mr-2" />
              {{ $t('help.features.messages.title') }}
            </h3>
            <p class="text-sm text-gray-600 dark:text-gray-400">
              {{ $t('help.features.messages.description') }}
            </p>
          </div>
          <div class="border rounded-lg p-4">
            <h3 class="font-medium text-gray-900 dark:text-white mb-2">
              <Icon icon="mdi:account-multiple" class="mr-2" />
              {{ $t('help.features.customers.title') }}
            </h3>
            <p class="text-sm text-gray-600 dark:text-gray-400">
              {{ $t('help.features.customers.description') }}
            </p>
          </div>
        </div>
      </div>

      <!-- FAQ Section -->
      <div class="bg-white dark:bg-gray-800 rounded-lg shadow-md p-6 mb-6">
        <h2 class="text-xl font-semibold text-gray-900 dark:text-white mb-4">
          <Icon icon="mdi:help-circle" class="mr-2" />
          {{ $t('help.faq.title') }}
        </h2>
        <div class="space-y-4">
          <div class="border-b pb-4">
            <h3 class="font-medium text-gray-900 dark:text-white mb-2">
              {{ $t('help.faq.questions.switchTenant.question') }}
            </h3>
            <p class="text-sm text-gray-600 dark:text-gray-400">
              {{ $t('help.faq.questions.switchTenant.answer') }}
            </p>
          </div>
          <div class="border-b pb-4">
            <h3 class="font-medium text-gray-900 dark:text-white mb-2">
              {{ $t('help.faq.questions.pennyBots.question') }}
            </h3>
            <p class="text-sm text-gray-600 dark:text-gray-400">
              {{ $t('help.faq.questions.pennyBots.answer') }}
            </p>
          </div>
          <div class="border-b pb-4">
            <h3 class="font-medium text-gray-900 dark:text-white mb-2">
              {{ $t('help.faq.questions.messageHistory.question') }}
            </h3>
            <p class="text-sm text-gray-600 dark:text-gray-400">
              {{ $t('help.faq.questions.messageHistory.answer') }}
            </p>
          </div>
        </div>
      </div>

      <!-- Contact Support -->
      <div class="bg-white dark:bg-gray-800 rounded-lg shadow-md p-6">
        <h2 class="text-xl font-semibold text-gray-900 dark:text-white mb-4">
          <Icon icon="mdi:support" class="mr-2" />
          {{ $t('help.support.title') }}
        </h2>
        <div class="grid md:grid-cols-2 gap-4">
          <div>
            <h3 class="font-medium text-gray-900 dark:text-white mb-2">{{ $t('help.support.email') }}</h3>
            <p class="text-sm text-gray-600 dark:text-gray-400">{{ $t('help.support.emailAddress') }}</p>
          </div>
          <div>
            <h3 class="font-medium text-gray-900 dark:text-white mb-2">{{ $t('help.support.documentation') }}</h3>
            <a href="#" class="text-sm text-blue-600 dark:text-blue-400 hover:underline">
              {{ $t('help.support.viewFullDoc') }}
            </a>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { Icon } from "@iconify/vue";

export default {
  name: "Help",
  components: {
    Icon,
  },
};
</script>
