import { useState } from 'react'
import { useTranslation } from 'react-i18next'
import { IconButton, Menu, MenuItem } from '@mui/material'
import LanguageIcon from '@mui/icons-material/Language'

const langs = [
  { code: 'zh', labelKey: 'lang.zh' },
  { code: 'en', labelKey: 'lang.en' },
  { code: 'ja', labelKey: 'lang.ja' },
] as const

export default function LanguageSelector() {
  const { i18n, t } = useTranslation()
  const [anchor, setAnchor] = useState<HTMLElement | null>(null)

  const changeLang = (code: string) => {
    i18n.changeLanguage(code)
    localStorage.setItem('lang', code)
    setAnchor(null)
  }

  return (
    <>
      <IconButton color="inherit" onClick={(e) => setAnchor(e.currentTarget)} title={t('lang.' + i18n.language)}>
        <LanguageIcon />
      </IconButton>
      <Menu anchorEl={anchor} open={!!anchor} onClose={() => setAnchor(null)}>
        {langs.map(({ code, labelKey }) => (
          <MenuItem key={code} selected={i18n.language === code} onClick={() => changeLang(code)}>
            {t(labelKey)}
          </MenuItem>
        ))}
      </Menu>
    </>
  )
}
