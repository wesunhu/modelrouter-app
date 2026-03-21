/**
 * i18next initialization and resource loading for UI languages.
 *
 * @version 1.0.1
 * @since 2026-03-21
 * @author wesun hu
 */

import i18n from 'i18next'
import { initReactI18next } from 'react-i18next'
import en from './locales/en.json'
import zh from './locales/zh.json'
import ja from './locales/ja.json'

const stored = localStorage.getItem('lang') as 'en' | 'zh' | 'ja' | null
const defaultLang = stored && ['en', 'zh', 'ja'].includes(stored) ? stored : 'en'

i18n.use(initReactI18next).init({
  resources: {
    en: { translation: en },
    zh: { translation: zh },
    ja: { translation: ja },
  },
  lng: defaultLang,
  fallbackLng: 'en',
  interpolation: { escapeValue: false },
  react: { useSuspense: true },
})

export default i18n
