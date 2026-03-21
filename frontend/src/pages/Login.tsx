/**
 * Admin login and first-time admin registration page.
 *
 * @version 1.0.1
 * @since 2026-03-21
 * @author wesun hu
 */

import { useState, useEffect } from 'react'
import { useTranslation } from 'react-i18next'
import { Box, Paper, TextField, Button, Typography, Alert } from '@mui/material'
import { useAuth } from '../contexts/AuthContext'

export default function Login() {
  const { t } = useTranslation()
  const { username: authUsername, login, init, needsInit } = useAuth()
  const [formUsername, setFormUsername] = useState('')
  const [formPassword, setFormPassword] = useState('')
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [showInitForm, setShowInitForm] = useState(!!needsInit)

  useEffect(() => {
    if (authUsername) {
      // 整页跳转确保 session cookie 生效，避免后续 API 请求 401
      window.location.replace('/')
    }
  }, [authUsername])

  useEffect(() => {
    setShowInitForm(!!needsInit)
  }, [needsInit])

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')
    setSubmitting(true)
    try {
      if (showInitForm) {
        await init(formUsername, formPassword)
      } else {
        await login(formUsername, formPassword)
      }
    } catch (err: unknown) {
      const msg = (err as { parsedMessage?: string; response?: { data?: { error?: string } } })?.parsedMessage
        ?? (err as { response?: { data?: { error?: string } } })?.response?.data?.error
        ?? 'Request failed'
      setError(String(msg))
      if (String(msg).toLowerCase().includes('initialize') || String(msg).toLowerCase().includes('init')) {
        setShowInitForm(true)
      }
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '60vh', px: 2 }}>
      <Paper sx={{ p: 4, maxWidth: 400, width: '100%' }}>
        <Typography variant="h5" sx={{ mb: 2 }}>
          {showInitForm ? t('auth.initTitle') : t('auth.loginTitle')}
        </Typography>
        {showInitForm && (
          <Alert severity="info" sx={{ mb: 2 }}>
            {t('auth.initHint')}
          </Alert>
        )}
        <form onSubmit={handleSubmit}>
          <TextField
            fullWidth
            label={t('auth.username')}
            value={formUsername}
            onChange={(e) => setFormUsername(e.target.value)}
            required
            autoComplete="username"
            sx={{ mb: 2 }}
          />
          <TextField
            fullWidth
            type="password"
            label={t('auth.password')}
            value={formPassword}
            onChange={(e) => setFormPassword(e.target.value)}
            required
            autoComplete={showInitForm ? 'new-password' : 'current-password'}
            helperText={showInitForm ? t('auth.passwordHint') : undefined}
            sx={{ mb: 2 }}
          />
          {error && (
            <Alert severity="error" sx={{ mb: 2 }}>
              {error}
            </Alert>
          )}
          <Button type="submit" variant="contained" fullWidth disabled={submitting}>
            {submitting ? t('auth.submitting') : (showInitForm ? t('auth.createAdmin') : t('auth.login'))}
          </Button>
        </form>
      </Paper>
    </Box>
  )
}
