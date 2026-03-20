import { ReactNode } from 'react'
import { Link, useLocation } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { AppBar, Toolbar, Button, Typography, Box, Alert } from '@mui/material'
import LanguageSelector from './LanguageSelector'

const nav = [
  { path: '/', key: 'nav.dashboard' },
  { path: '/providers', key: 'nav.providers' },
  { path: '/models', key: 'nav.models' },
  { path: '/routes', key: 'nav.routes' },
  { path: '/usage', key: 'nav.usage' },
  { path: '/test', key: 'nav.test' },
]

export default function Layout({ children }: { children: ReactNode }) {
  const location = useLocation()
  const { t } = useTranslation()
  return (
    <Box sx={{ minHeight: '100vh', bgcolor: '#f5f5f5' }}>
      <Alert severity="warning" sx={{ borderRadius: 0, '& .MuiAlert-message': { width: '100%' } }}>
        <strong>[警告]</strong> 实验性软件，严禁公网访问。使用风险自负。详见{' '}
        <a href="/LEGAL.md" target="_blank" rel="noopener noreferrer" style={{ color: 'inherit', textDecoration: 'underline' }}>LEGAL</a>
        {' '}(<a href="/LEGAL.en.md" target="_blank" rel="noopener noreferrer" style={{ color: 'inherit', textDecoration: 'underline' }}>EN</a>
        {' '}|{' '}
        <a href="/LEGAL.ja.md" target="_blank" rel="noopener noreferrer" style={{ color: 'inherit', textDecoration: 'underline' }}>JA</a>)
      </Alert>
      <AppBar position="static">
        <Toolbar>
          <Typography variant="h6" sx={{ flexGrow: 1 }}>
            ModelRouter
          </Typography>
          {nav.map(({ path, key }) => (
            <Button
              key={path}
              color="inherit"
              component={Link}
              to={path}
              sx={{ opacity: location.pathname === path ? 1 : 0.8 }}
            >
              {t(key)}
            </Button>
          ))}
          <LanguageSelector />
        </Toolbar>
      </AppBar>
      <Box sx={{ p: 3 }}>{children}</Box>
    </Box>
  )
}
