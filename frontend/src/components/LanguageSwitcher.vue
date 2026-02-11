<template>
  <div class="relative">
    <button
      @click="toggleDropdown"
      class="flex items-center space-x-2 px-3 py-2 text-sm font-medium text-gray-700 dark:text-gray-200 bg-white dark:bg-gray-800 border border-gray-300 dark:border-gray-600 rounded-md hover:bg-gray-50 dark:hover:bg-gray-700 focus:outline-none focus:ring-2 focus:ring-blue-500"
    >
      <Icon icon="fa6-solid:language" class="w-4 h-4" />
      <span>{{ currentLanguageLabel }}</span>
      <Icon icon="fa6-solid:chevron-down" class="w-3 h-3" />
    </button>
    <div
      v-if="isOpen"
      v-click-outside="closeDropdown"
      class="absolute right-0 mt-2 w-48 bg-white dark:bg-gray-800 border border-gray-300 dark:border-gray-600 rounded-md shadow-lg z-50"
    >
      <div class="py-1">
        <button
          v-for="lang in languages"
          :key="lang.code"
          @click="changeLanguage(lang.code)"
          class="flex items-center w-full px-4 py-2 text-sm text-gray-700 dark:text-gray-200 hover:bg-gray-100 dark:hover:bg-gray-700"
          :class="{ 'bg-blue-50 dark:bg-blue-900 text-blue-700 dark:text-blue-200': lang.code === locale }"
        >
          <span class="mr-3">{{ lang.flag }}</span>
          {{ lang.name }}
        </button>
      </div>
    </div>
  </div>
</template>
<script>
import { Icon } from "@iconify/vue";
import { useI18n } from 'vue-i18n';
import { ref, computed } from 'vue';
export default {
  name: "LanguageSwitcher",
  components: {
    Icon,
  },
  setup() {
    const { locale, t } = useI18n();
    const isOpen = ref(false);
    const languages = [
      { code: 'vi', name: 'Tiếng Việt', flag: '🇻🇳' },
      { code: 'en', name: 'English', flag: '🇺🇸' }
    ];
    const currentLanguageLabel = computed(() => {
      const currentLang = languages.find(lang => lang.code === locale.value);
      return currentLang ? currentLang.name : 'Language';
    });
    const toggleDropdown = () => {
      isOpen.value = !isOpen.value;
    };
    const closeDropdown = () => {
      isOpen.value = false;
    };
    const changeLanguage = (langCode) => {
      locale.value = langCode;
      localStorage.setItem('language', langCode);
      isOpen.value = false;
      // Force page reload to update all translations
      window.location.reload();
    };
    return {
      languages,
      locale,
      currentLanguageLabel,
      isOpen,
      toggleDropdown,
      closeDropdown,
      changeLanguage,
      t
    };
  }
};
</script>
