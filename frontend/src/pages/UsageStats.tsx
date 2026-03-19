import { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { api } from '../api/client'
import { Table, TableBody, TableCell, TableHead, TableRow, Typography } from '@mui/material'

interface RouteStat {
  routeId?: number | null
  routeName: string
  totalTokens: number
  cost: number
  sales: number
  profit: number
  requestCount: number
}

interface ByRouteResponse {
  routeStats: RouteStat[]
  totalCost: number
  totalSales: number
  totalProfit: number
}

export default function UsageStats() {
  const { t } = useTranslation()
  const [data, setData] = useState<ByRouteResponse | null>(null)

  useEffect(() => {
    api.get<ByRouteResponse>('/api/usage-logs/statistics/by-route').then(r => setData(r.data || null))
  }, [])

  const fmt = (n: number) => `$${Number(n).toFixed(4)}`

  return (
    <div>
      <Typography variant="h5" sx={{ mb: 2 }}>{t('usage.title')}</Typography>
      <Table>
        <TableHead>
          <TableRow>
            <TableCell>{t('usage.route')}</TableCell>
            <TableCell align="right">{t('usage.requestCount')}</TableCell>
            <TableCell align="right">{t('usage.totalToken')}</TableCell>
            <TableCell align="right">{t('usage.cost')}</TableCell>
            <TableCell align="right">{t('usage.sales')}</TableCell>
            <TableCell align="right">{t('usage.profit')}</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {(data?.routeStats || []).map((s, i) => (
            <TableRow key={s.routeId ?? `api-${i}`}>
              <TableCell>{s.routeName}</TableCell>
              <TableCell align="right">{s.requestCount}</TableCell>
              <TableCell align="right">{s.totalTokens}</TableCell>
              <TableCell align="right">{fmt(s.cost)}</TableCell>
              <TableCell align="right">{fmt(s.sales)}</TableCell>
              <TableCell align="right" sx={{ color: s.profit >= 0 ? 'success.main' : 'error.main' }}>{fmt(s.profit)}</TableCell>
            </TableRow>
          ))}
          {data && (
            <TableRow sx={{ fontWeight: 'bold', backgroundColor: 'action.hover' }}>
              <TableCell>{t('usage.totalRow')}</TableCell>
              <TableCell align="right">-</TableCell>
              <TableCell align="right">-</TableCell>
              <TableCell align="right">{fmt(data.totalCost)}</TableCell>
              <TableCell align="right">{fmt(data.totalSales)}</TableCell>
              <TableCell align="right" sx={{ color: data.totalProfit >= 0 ? 'success.main' : 'error.main' }}>{fmt(data.totalProfit)}</TableCell>
            </TableRow>
          )}
        </TableBody>
      </Table>
    </div>
  )
}
