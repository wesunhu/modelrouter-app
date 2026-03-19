import { ReactNode } from 'react'
import { Link, useLocation } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { AppBar, Toolbar, Button, Typography, Box } from '@mui/material'
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
