<template>
  <div class="w-full">
    <!-- Tab Navigation -->
    <div class="border-b border-gray-200 dark:border-gray-700">
      <nav class="-mb-px flex space-x-8" aria-label="Tabs">
        <button
          v-for="(tab, index) in tabs"
          :key="tab.name"
          @click="activeTab = tab.name"
          :class="[
            activeTab === tab.name
              ? 'border-primary text-primary'
              : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300 dark:text-gray-400 dark:hover:text-gray-300',
            'whitespace-nowrap py-2 px-1 border-b-2 font-medium text-sm transition-colors duration-200'
          ]"
          :aria-current="activeTab === tab.name ? 'page' : undefined"
        >
          <span class="flex items-center">
            <Icon v-if="tab.icon" :icon="tab.icon" class="h-4 w-4 mr-2" />
            {{ tab.label }}
            <span v-if="tab.tooltip" class="ml-1">
              <Icon icon="mdi:information-outline" class="h-4 w-4 text-gray-400" />
            </span>
          </span>
        </button>
      </nav>
    </div>

    <!-- Tab Content -->
    <div class="mt-6">
      <slot :activeTab="activeTab"></slot>
    </div>
  </div>
</template>

<script>
import { ref } from 'vue'
import { Icon } from '@iconify/vue'

export default {
  name: 'Tabs',
  components: {
    Icon
  },
  props: {
    tabs: {
      type: Array,
      required: true,
      validator: (tabs) => {
        return tabs.every(tab => 
          typeof tab.name === 'string' && 
          typeof tab.label === 'string'
        )
      }
    },
    initialTab: {
      type: String,
      default: ''
    }
  },
  emits: ['tabChange'],
  setup(props, { emit }) {
    const activeTab = ref(props.initialTab || (props.tabs[0]?.name || ''))

    const changeTab = (tabName) => {
      activeTab.value = tabName
      emit('tabChange', tabName)
    }

    return {
      activeTab,
      changeTab
    }
  }
}
</script>
