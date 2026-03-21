/**
 * Application shell: navigation, disclaimer banner, outlet for pages.
 *
 * @version 1.0.1
 * @since 2026-03-21
 * @author wesun hu
 */

import { ReactNode } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { AppBar, Toolbar, Button, Typography, Box, Alert } from '@mui/material'
import LanguageSelector from './LanguageSelector'
import { useAuth } from '../contexts/AuthContext'

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
  const navigate = useNavigate()
  const { t } = useTranslation()
  const { username, logout } = useAuth()

  const handleLogout = async () => {
    await logout()
    navigate('/login')
  }

  return (
    <Box sx={{ minHeight: '100vh', bgcolor: '#f5f5f5' }}>
      <Alert severity="warning" sx={{ borderRadius: 0, '& .MuiAlert-message': { width: '100%' } }}>
        <strong>[WARN]</strong> Experimental software. Do NOT expose to public internet. Use at your own risk. See{' '}
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
          <Typography component="span" sx={{ mr: 2, opacity: 0.9 }}>
            {username}
          </Typography>
          <Button color="inherit" onClick={handleLogout}>
            {t('auth.logout')}
          </Button>
          <LanguageSelector />
        </Toolbar>
      </AppBar>
      <Box sx={{ p: 3 }}>{children}</Box>
    </Box>
  )
}
