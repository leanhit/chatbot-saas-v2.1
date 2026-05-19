import { createI18n } from 'vue-i18n'
import en from './en.json'
import vi from './vi.json'

const savedLanguage = localStorage.getItem('language') || 'vi'

const i18n = createI18n({
  legacy: false,
  locale: savedLanguage,
  fallbackLocale: 'vi',
  messages: {
    en,
    vi
  }
})

export default i18n
