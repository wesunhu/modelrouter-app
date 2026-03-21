/**
 * Dashboard home view.
 *
 * @version 1.0.1
 * @since 2026-03-21
 * @author wesun hu
 */

import { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { api } from '../api/client'
import { Card, CardContent, Typography, Grid, Box } from '@mui/material'

export default function Dashboard() {
  const { t } = useTranslation()
  const [stats, setStats] = useState<Record<string, unknown> | null>(null)
  const [counts, setCounts] = useState({ providers: 0, models: 0, routes: 0 })

  useEffect(() => {
    api.get('/api/usage-logs/statistics').then(r => setStats(r.data)).catch(() => setStats(null))
    Promise.all([
      api.get('/api/providers'),
      api.get('/api/models'),
      api.get('/api/routes'),
    ]).then(([p, m, r]) => {
      setCounts({
        providers: p.data?.length ?? 0,
        models: m.data?.length ?? 0,
        routes: r.data?.length ?? 0,
      })
    }).catch(console.error)
  }, [])

  return (
    <Box>
      <Typography variant="h5" sx={{ mb: 3 }}>{t('dashboard.title')}</Typography>
      <Grid container spacing={3}>
        <Grid item xs={12} sm={6} md={3}>
          <Card><CardContent><Typography color="textSecondary">{t('dashboard.providersCount')}</Typography><Typography variant="h4">{counts.providers}</Typography></CardContent></Card>
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <Card><CardContent><Typography color="textSecondary">{t('dashboard.modelsCount')}</Typography><Typography variant="h4">{counts.models}</Typography></CardContent></Card>
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <Card><CardContent><Typography color="textSecondary">{t('dashboard.routesCount')}</Typography><Typography variant="h4">{counts.routes}</Typography></CardContent></Card>
        </Grid>
        {stats && (
          <>
            <Grid item xs={12} sm={6} md={4}>
              <Card><CardContent><Typography color="textSecondary">{t('dashboard.totalRequests')}</Typography><Typography variant="h5">{String(stats.total_requests ?? 0)}</Typography></CardContent></Card>
            </Grid>
            <Grid item xs={12} sm={6} md={4}>
              <Card><CardContent><Typography color="textSecondary">{t('dashboard.totalTokens')}</Typography><Typography variant="h5">{String(stats.total_tokens ?? 0)}</Typography></CardContent></Card>
            </Grid>
            <Grid item xs={12} sm={6} md={4}>
              <Card><CardContent><Typography color="textSecondary">{t('dashboard.totalCost')}</Typography><Typography variant="h5">${Number(stats.total_cost ?? 0).toFixed(4)}</Typography></CardContent></Card>
            </Grid>
          </>
        )}
      </Grid>
    </Box>
  )
}
