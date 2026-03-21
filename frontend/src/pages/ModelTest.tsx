/**
 * Interactive chat test against configured routes.
 *
 * @version 1.0.1
 * @since 2026-03-21
 * @author wesun hu
 */

import { useState, useEffect } from 'react'
import { useTranslation } from 'react-i18next'
import { api, type Route } from '../api/client'
import { Box, Paper, Typography } from '@mui/material'

/** /api/test/chat 成功或错误时的 JSON 形状（宽松） */
interface ChatTestResponse {
  choices?: { message?: { content?: string } }[]
  error?: unknown
  router_log?: Record<string, unknown>[]
}

/** Router 行为日志条目 */
interface RouterLogEntry {
  id: number
  time: string
  type: 'routes_loaded' | 'chat'
  /** routes_loaded: 路由数量; chat: router_log 数组 */
  payload: { count?: number; routeName?: string; routerLog?: Record<string, unknown>[] }
}

let logId = 0

export default function ModelTest() {
  const { t } = useTranslation()
  const [routes, setRoutes] = useState<Route[]>([])
  const [routeId, setRouteId] = useState<number>(0)
  const [prompt, setPrompt] = useState('Hello')
  const [result, setResult] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [logs, setLogs] = useState<RouterLogEntry[]>([])

  const addLog = (entry: Omit<RouterLogEntry, 'id' | 'time'>) => {
    setLogs(prev => [{
      ...entry,
      id: ++logId,
      time: new Date().toLocaleTimeString('zh-CN', { hour12: false }),
    }, ...prev])
  }

  useEffect(() => {
    const load = async () => {
      try {
        const r = await api.get<Route[]>('/api/routes')
        const data = r.data || []
        setRoutes(data)
        addLog({
          type: 'routes_loaded',
          payload: { count: data.length },
        })
      } catch (e: any) {
        addLog({
          type: 'routes_loaded',
          payload: { count: 0 },
        })
      }
    }
    load()
  }, [])

  const run = async () => {
    if (!routeId) {
      setError(t('test.selectRouteFirst'))
      return
    }
    setError('')
    setResult('')
    setLoading(true)
    const routeName = routes.find(r => r.id === routeId)?.name || ''
    // 使用 api 默认 baseURL（与 /api/routes 一致）：直连后端或 VITE_API_URL=PROXY 时走 Vite 代理，勿用 baseURL:'' 误拿 SPA 的 index.html
    try {
      const res = await api.post('/api/test/chat', {
        routeId,
        messages: [{ role: 'user', content: prompt }],
        max_tokens: 256,
      })
      const raw = res.data
      if (typeof raw === 'string' && (raw.includes('<!DOCTYPE') || raw.includes('<html'))) {
        setError(t('test.errHtmlResponse'))
        return
      }
      const data = raw as ChatTestResponse
      const routerLog = data.router_log ?? []
      addLog({
        type: 'chat',
        payload: { routeName, routerLog },
      })
      const content = data.choices?.[0]?.message?.content
      if (typeof content === 'string' && content.length > 0) {
        setResult(content)
      } else if (data.error != null) {
        setError(typeof data.error === 'string' ? data.error : JSON.stringify(data.error))
      } else {
        setError(JSON.stringify(data))
      }
    } catch (e: any) {
      addLog({
        type: 'chat',
        payload: {
          routeName,
          routerLog: [{
            event: 'error',
            message: e.message || String(e),
          }],
        },
      })
      setError(e.message || String(e))
    } finally {
      setLoading(false)
    }
  }

  const formatLogEntry = (entry: RouterLogEntry) => {
    const lines: string[] = [`[${entry.time}]`]
    if (entry.type === 'routes_loaded') {
      lines.push(t('test.logRoutesLoaded', { count: entry.payload.count ?? 0 }))
    } else if (entry.type === 'chat') {
      const { routeName, routerLog } = entry.payload
      if (routeName) {
        lines.push(t('test.logRouteStart', { name: routeName }))
      }
      if (routerLog?.length) {
        routerLog.forEach(item => {
          const ev = (item.event as string) || ''
          if (ev === 'trying') {
            lines.push(`  → ${t('test.logTrying', {
              index: item.index ?? '?',
              total: item.total ?? '?',
              model: item.model ?? '',
              provider: item.provider ?? '',
            })}`)
          } else if (ev === 'succeeded') {
            lines.push(`  ✓ ${t('test.logSucceeded', { model: item.model ?? '' })}`)
          } else if (ev === 'failed') {
            lines.push(`  ✗ ${t('test.logFailed', {
              model: item.model ?? '',
              error: item.error ?? '',
            })}`)
          } else if (ev === 'no_models') {
            lines.push(`  ! ${t('test.logNoModels')}`)
          } else if (ev === 'all_failed') {
            lines.push(`  ✗ ${t('test.logAllFailed')}: ${item.message ?? ''}`)
          } else if (ev === 'error') {
            lines.push(`  ! ${item.message ?? ''}`)
          }
        })
      }
    }
    return lines.join('\n')
  }

  return (
    <Box display="flex" gap={2} alignItems="stretch" flexWrap="wrap">
      <Box flex="0 0 400px" minWidth={280}>
        <h2>{t('test.title')}</h2>
        <p style={{ marginBottom: 16 }}>{t('test.desc')}</p>
        <Box display="flex" flexDirection="column" gap={1.5}>
          <label style={{ fontWeight: 500 }}>{t('test.selectRoute')}</label>
          <select value={routeId} onChange={e => setRouteId(Number(e.target.value))} style={{ padding: 8 }}>
            <option value={0}>{t('test.selectRoutePlaceholder')}</option>
            {routes.map(r => (
              <option key={r.id} value={r.id}>{r.name}</option>
            ))}
          </select>
          <textarea placeholder={t('test.promptPlaceholder')} value={prompt} onChange={e => setPrompt(e.target.value)} rows={3} style={{ padding: 8 }} />
          <button onClick={run} disabled={loading} style={{ padding: 12 }}>
            {loading ? t('test.sending') : t('test.send')}
          </button>
          {error && <div style={{ color: 'red' }}>{error}</div>}
          {result && <div style={{ whiteSpace: 'pre-wrap', background: '#f5f5f5', padding: 12 }}>{result}</div>}
        </Box>
      </Box>
      <Paper elevation={1} sx={{ flex: 1, minWidth: 360, minHeight: 400, maxHeight: 'calc(100vh - 120px)', display: 'flex', flexDirection: 'column', overflow: 'hidden' }}>
        <Typography variant="subtitle1" sx={{ p: 1.5, borderBottom: 1, borderColor: 'divider', fontWeight: 600, flexShrink: 0 }}>
          {t('test.logTitle')}
        </Typography>
        <Box sx={{ flex: 1, overflowY: 'scroll', overflowX: 'auto', p: 1.5, fontFamily: 'monospace', fontSize: 12, bgcolor: '#1e1e1e', color: '#d4d4d4' }}>
          {logs.length === 0 ? (
            <Typography variant="body2" color="text.secondary">{t('test.logWaiting')}</Typography>
          ) : (
            logs.map(entry => (
              <Box key={entry.id} component="pre" sx={{ mb: 2, whiteSpace: 'pre-wrap', wordBreak: 'break-all', borderBottom: '1px solid #333', pb: 2 }}>
                {formatLogEntry(entry)}
              </Box>
            ))
          )}
        </Box>
      </Paper>
    </Box>
  )
}
