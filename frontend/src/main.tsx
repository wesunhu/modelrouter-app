/**
 * React root: theme, i18n, router, and error boundary mount.
 *
 * @version 1.0.1
 * @since 2026-03-21
 * @author wesun hu
 */

import './i18n'
import React, { Suspense } from 'react'
import ReactDOM from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'
import App from './App'
import { ThemeProvider, createTheme, CssBaseline } from '@mui/material'
import { ErrorBoundary } from './ErrorBoundary'

const theme = createTheme({
  palette: {
    mode: 'light',
    primary: { main: '#1976d2' },
    secondary: { main: '#9c27b0' },
  },
})

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <ErrorBoundary>
      <ThemeProvider theme={theme}>
        <CssBaseline />
        <Suspense fallback={<div style={{ padding: 24, textAlign: 'center' }}>Loading...</div>}>
          <BrowserRouter>
            <App />
          </BrowserRouter>
        </Suspense>
      </ThemeProvider>
    </ErrorBoundary>
  </React.StrictMode>
)
