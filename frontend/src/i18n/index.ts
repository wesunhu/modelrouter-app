import i18n from 'i18next'
import { initReactI18next } from 'react-i18next'
import en from './locales/en.json'
import zh from './locales/zh.json'
import ja from './locales/ja.json'

const stored = localStorage.getItem('lang') as 'en' | 'zh' | 'ja' | null
const defaultLang = stored && ['en', 'zh', 'ja'].includes(stored) ? stored : 'zh'

i18n.use(initReactI18next).init({
  resources: {
    en: { translation: en },
    zh: { translation: zh },
    ja: { translation: ja },
  },
  lng: defaultLang,
  fallbackLng: 'zh',
  interpolation: { escapeValue: false },
  react: { useSuspense: true },
})

export default i18n
