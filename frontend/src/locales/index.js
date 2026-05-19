import { createI18n } from 'vue-i18n'
import { nextTick } from 'vue'

const savedLanguage = localStorage.getItem('language') || 'vi'

const i18n = createI18n({
  legacy: false,
  locale: savedLanguage,
  fallbackLocale: 'vi',
  messages: {}
})

const loadedLanguages = []

export function setI18nLanguage(i18n, locale) {
  if (i18n.mode === 'legacy') {
    i18n.global.locale = locale
  } else {
    i18n.global.locale.value = locale
  }
  document.querySelector('html').setAttribute('lang', locale)
}

export async function loadLocaleMessages(i18n, locale) {
  // If the language was already loaded
  if (loadedLanguages.includes(locale)) {
    setI18nLanguage(i18n, locale)
    return nextTick()
  }

  // If the language hasn't been loaded yet
  const messages = await import(/* webpackChunkName: "locale-[request]" */ `./${locale}.json`)
  i18n.global.setLocaleMessage(locale, messages.default)
  loadedLanguages.push(locale)
  setI18nLanguage(i18n, locale)
  return nextTick()
}

// Load initial language
loadLocaleMessages(i18n, savedLanguage)

export default i18n
