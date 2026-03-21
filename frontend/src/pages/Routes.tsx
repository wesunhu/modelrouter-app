/**
 * CRUD UI for routes and route API keys.
 *
 * @version 1.0.1
 * @since 2026-03-21
 * @author wesun hu
 */

import { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { api, Route, Model } from '../api/client'
import { Button, Table, TableBody, TableCell, TableHead, TableRow, Dialog, TextField, DialogTitle, DialogContent, DialogActions, IconButton, MenuItem } from '@mui/material'
import DeleteIcon from '@mui/icons-material/Delete'
import EditIcon from '@mui/icons-material/Edit'

export default function RoutesPage() {
  const { t } = useTranslation()
  const [list, setList] = useState<Route[]>([])
  const [models, setModels] = useState<Model[]>([])
  const [open, setOpen] = useState(false)
  const [editing, setEditing] = useState<Route | null>(null)
  const [form, setForm] = useState({
    name: '', apiKey: '', primaryModelId: 0, modelType: 'text',
    timeoutSeconds: 60, strategy: 'primary-first', status: 'active',
    backupModel1Id: 0, backupModel2Id: 0,
    tokenSellingPrice: '' as number | ''
  })

  const load = () => {
    api.get<Route[]>('/api/routes').then(r => setList(r.data || []))
    api.get<Model[]>('/api/models').then(r => setModels(r.data || []))
  }

  useEffect(() => { load() }, [])

  const save = async () => {
    const backupIds = [form.backupModel1Id, form.backupModel2Id].filter(id => id > 0)
    const payload = {
      name: form.name,
      apiKey: form.apiKey || null,
      primaryModelId: form.primaryModelId || null,
      modelType: form.modelType,
      timeout: form.timeoutSeconds * 1000,
      strategy: form.strategy,
      status: form.status,
      backupModelIds: backupIds,
      tokenSellingPrice: form.tokenSellingPrice === '' ? null : Number(form.tokenSellingPrice),
    }
    if (editing) {
      await api.put(`/api/routes/${editing.id}`, payload)
    } else {
      await api.post('/api/routes', payload)
    }
    setOpen(false)
    setEditing(null)
    load()
  }

  const del = async (id: number) => {
    if (confirm(t('common.confirmDelete'))) {
      await api.delete(`/api/routes/${id}`)
      load()
    }
  }

  const edit = (r: Route) => {
    setEditing(r)
    const ids = r.backupModelIds || []
    setForm({
      name: r.name,
      apiKey: r.apiKey || '',
      primaryModelId: r.primaryModelId ?? 0,
      modelType: r.modelType || 'text',
      timeoutSeconds: r.timeout ? Math.round(r.timeout / 1000) : 60,
      strategy: r.strategy || 'primary-first',
      status: r.status || 'active',
      backupModel1Id: ids[0] ?? 0,
      backupModel2Id: ids[1] ?? 0,
      tokenSellingPrice: r.tokenSellingPrice ?? '',
    })
    setOpen(true)
  }

  return (
    <div>
      <Button variant="contained" onClick={() => { setEditing(null); setForm({ name: '', apiKey: '', primaryModelId: 0, modelType: 'text', timeoutSeconds: 60, strategy: 'primary-first', status: 'active', backupModel1Id: 0, backupModel2Id: 0, tokenSellingPrice: '' }); setOpen(true) }}>{t('routes.addRoute')}</Button>
      <Table sx={{ mt: 2 }}>
        <TableHead><TableRow><TableCell>{t('common.name')}</TableCell><TableCell>{t('routes.routeKey')}</TableCell><TableCell>{t('routes.primaryModel')}</TableCell><TableCell>{t('routes.backupModels')}</TableCell><TableCell>{t('routes.timeoutSeconds')}</TableCell><TableCell>{t('routes.strategy')}</TableCell><TableCell>{t('routes.status')}</TableCell><TableCell>{t('common.actions')}</TableCell></TableRow></TableHead>
        <TableBody>
          {list.map(r => {
            const ids = r.backupModelIds || []
            const b1 = ids[0] ? models.find(m => m.id === ids[0])?.name : null
            const b2 = ids[1] ? models.find(m => m.id === ids[1])?.name : null
            const backupStr = [b1, b2].filter(Boolean).join(' → ') || '-'
            return (
            <TableRow key={r.id}>
              <TableCell>{r.name}</TableCell>
              <TableCell>{r.apiKey ? r.apiKey.substring(0, 16) + '...' : '-'}</TableCell>
              <TableCell>{models.find(m => m.id === r.primaryModelId)?.name || r.primaryModelId || '-'}</TableCell>
              <TableCell>{backupStr}</TableCell>
              <TableCell>{r.timeout ? (r.timeout >= 1000 ? r.timeout / 1000 : r.timeout) + t('routes.timeoutUnit') : '-'}</TableCell>
              <TableCell>{r.strategy}</TableCell>
              <TableCell>{r.status}</TableCell>
              <TableCell>
                <IconButton size="small" onClick={() => edit(r)}><EditIcon /></IconButton>
                <IconButton size="small" onClick={() => del(r.id)}><DeleteIcon /></IconButton>
              </TableCell>
            </TableRow>
          )})}
        </TableBody>
      </Table>
      <Dialog open={open} onClose={() => setOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>{editing ? t('routes.editRoute') : t('routes.addRouteTitle')}</DialogTitle>
        <DialogContent>
          <TextField fullWidth label={t('common.name')} value={form.name} onChange={e => setForm({ ...form, name: e.target.value })} sx={{ mt: 1 }} inputProps={{ tabIndex: 1 }} />
          <TextField select fullWidth label={t('routes.primaryModel')} value={form.primaryModelId} onChange={e => setForm({ ...form, primaryModelId: Number(e.target.value) })} sx={{ mt: 1 }} inputProps={{ tabIndex: 2 }} helperText={t('routes.primaryModelHelper')}>
            <MenuItem value={0}>{t('common.none')}</MenuItem>
            {models.map(m => <MenuItem key={m.id} value={m.id}>{m.name}</MenuItem>)}
          </TextField>
          <TextField select fullWidth label={t('routes.backupModel1')} value={form.backupModel1Id} onChange={e => setForm({ ...form, backupModel1Id: Number(e.target.value) })} sx={{ mt: 1 }} inputProps={{ tabIndex: 3 }} helperText={t('routes.backupModel1Helper')}>
            <MenuItem value={0}>{t('common.none')}</MenuItem>
            {models.map(m => <MenuItem key={m.id} value={m.id}>{m.name}</MenuItem>)}
          </TextField>
          <TextField select fullWidth label={t('routes.backupModel2')} value={form.backupModel2Id} onChange={e => setForm({ ...form, backupModel2Id: Number(e.target.value) })} sx={{ mt: 1 }} inputProps={{ tabIndex: 4 }} helperText={t('routes.backupModel2Helper')}>
            <MenuItem value={0}>{t('common.none')}</MenuItem>
            {models.map(m => <MenuItem key={m.id} value={m.id}>{m.name}</MenuItem>)}
          </TextField>
          <TextField type="number" fullWidth label={t('routes.timeoutSeconds')} value={form.timeoutSeconds} onChange={e => setForm({ ...form, timeoutSeconds: Number(e.target.value) || 60 })} sx={{ mt: 1 }} inputProps={{ min: 5, max: 300, tabIndex: 5 }} helperText={t('routes.timeoutHelper')} />
          <TextField fullWidth label={t('routes.strategy')} value={form.strategy} onChange={e => setForm({ ...form, strategy: e.target.value })} sx={{ mt: 1 }} placeholder={t('routes.strategyPlaceholder')} inputProps={{ tabIndex: 6 }} />
          <TextField fullWidth label={t('routes.routeKey')} value={form.apiKey} onChange={e => setForm({ ...form, apiKey: e.target.value })} sx={{ mt: 1 }} placeholder={t('routes.apiKeyPlaceholder')} helperText={t('routes.routeKeyHelper')} inputProps={{ tabIndex: 7 }} />
          <TextField type="number" fullWidth label={t('routes.tokenSellingPrice')} value={form.tokenSellingPrice} onChange={e => setForm({ ...form, tokenSellingPrice: e.target.value === '' ? '' : Number(e.target.value) })} sx={{ mt: 1 }} inputProps={{ step: 0.0001, min: 0 }} placeholder="0.001" helperText={t('routes.tokenSellingPriceHelper')} />
        </DialogContent>
        <DialogActions><Button onClick={() => setOpen(false)}>{t('common.cancel')}</Button><Button variant="contained" onClick={save}>{t('common.save')}</Button></DialogActions>
      </Dialog>
    </div>
  )
}
